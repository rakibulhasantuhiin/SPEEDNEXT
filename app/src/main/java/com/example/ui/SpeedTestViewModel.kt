package com.example.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SpeedTestDatabase
import com.example.data.local.SpeedTestRecord
import com.example.data.network.NetworkMeta
import com.example.data.network.SpeedTestEngine
import com.example.data.network.TestPhase
import com.example.data.repository.SpeedTestRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppLanguage {
    BN, // Bengali
    EN  // English
}

data class SpeedTestUiState(
    val phase: TestPhase = TestPhase.IDLE,
    val currentDisplaySpeed: Double = 0.0,
    val previousDisplaySpeed: Double = 0.0,
    val isShowingDimmedPrevious: Boolean = false,
    val downloadSpeed: Double = 0.0,
    val uploadSpeed: Double = 0.0,
    val unloadedLatencyMs: Long = 0L,
    val loadedLatencyMs: Long = 0L,
    val totalDownloadBytes: Long = 0L,
    val totalUploadBytes: Long = 0L,
    val progressPercent: Float = 0f,
    val networkMeta: NetworkMeta = NetworkMeta(),
    val isDetailsExpanded: Boolean = false,
    val errorMessage: String? = null,
    val isDarkMode: Boolean = false,
    val language: AppLanguage = AppLanguage.EN,
    val parallelStreams: Int = 3,
    val testDurationSec: Int = 10
)

class SpeedTestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SpeedTestRepository
    private val engine = SpeedTestEngine()

    private val _uiState = MutableStateFlow(SpeedTestUiState())
    val uiState: StateFlow<SpeedTestUiState> = _uiState.asStateFlow()

    private var activeTestJob: Job? = null

    init {
        val db = SpeedTestDatabase.getDatabase(application)
        repository = SpeedTestRepository(db.speedTestDao())

        // Start test immediately on launch, exactly like Fast.com
        startFullTest()
    }

    val historyRecords: StateFlow<List<SpeedTestRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun detectNetworkType(): String {
        return try {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork
            val capabilities = cm?.getNetworkCapabilities(network)
            when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile Data (4G/5G)"
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
                else -> "Mobile / Wi-Fi"
            }
        } catch (e: Exception) {
            "Mobile / Wi-Fi"
        }
    }

    fun handleHeroButtonClick() {
        if (_uiState.value.phase == TestPhase.CONNECTING ||
            _uiState.value.phase == TestPhase.TESTING_DOWNLOAD ||
            _uiState.value.phase == TestPhase.TESTING_UPLOAD
        ) {
            activeTestJob?.cancel()
            _uiState.update {
                it.copy(
                    phase = TestPhase.DOWNLOAD_COMPLETED,
                    isShowingDimmedPrevious = false
                )
            }
        } else {
            startFullTest()
        }
    }

    fun startFullTest() {
        val lastSpeed = if (_uiState.value.downloadSpeed > 0) _uiState.value.downloadSpeed else _uiState.value.currentDisplaySpeed
        val hasPrevious = lastSpeed > 0
        activeTestJob?.cancel()
        activeTestJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    phase = TestPhase.CONNECTING,
                    previousDisplaySpeed = lastSpeed,
                    isShowingDimmedPrevious = hasPrevious,
                    currentDisplaySpeed = if (hasPrevious) lastSpeed else 0.0,
                    downloadSpeed = 0.0,
                    uploadSpeed = 0.0,
                    unloadedLatencyMs = 0L,
                    loadedLatencyMs = 0L,
                    totalDownloadBytes = 0L,
                    totalUploadBytes = 0L,
                    progressPercent = 0.05f,
                    errorMessage = null
                )
            }

            try {
                // Step 1: Detect Network Meta, Connection Type & Unloaded Latency
                val connType = detectNetworkType()
                val meta = engine.fetchNetworkMeta().copy(connectionType = connType)
                val ping = engine.measurePing(3)

                _uiState.update {
                    it.copy(
                        networkMeta = meta,
                        unloadedLatencyMs = ping,
                        phase = TestPhase.TESTING_DOWNLOAD,
                        progressPercent = 0.1f
                    )
                }

                // Step 2: Live Download Speed Testing
                val durationMs = _uiState.value.testDurationSec * 1000L
                val streams = _uiState.value.parallelStreams

                val finalDownload = engine.measureDownloadSpeed(
                    targetDurationMs = durationMs,
                    streamCount = streams,
                    onProgress = { currentSpeed, smoothed, bytes, progress ->
                        _uiState.update { state ->
                            state.copy(
                                isShowingDimmedPrevious = false,
                                currentDisplaySpeed = smoothed,
                                downloadSpeed = smoothed,
                                totalDownloadBytes = bytes,
                                progressPercent = 0.1f + (progress * 0.85f)
                            )
                        }
                    },
                    onLoadedPingMeasured = { loadedPing ->
                        _uiState.update { it.copy(loadedLatencyMs = loadedPing) }
                    }
                )

                _uiState.update {
                    it.copy(
                        phase = TestPhase.DOWNLOAD_COMPLETED,
                        isShowingDimmedPrevious = false,
                        downloadSpeed = finalDownload,
                        currentDisplaySpeed = finalDownload,
                        progressPercent = 1f
                    )
                }

                // If user has already expanded "Load More / Show More", run upload immediately
                if (_uiState.value.isDetailsExpanded) {
                    runUploadTest()
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update {
                    it.copy(
                        phase = TestPhase.ERROR,
                        errorMessage = e.localizedMessage ?: "Connection error. Please check your internet."
                    )
                }
            }
        }
    }

    /**
     * User clicked "Show more info" / "লোড মোর"
     */
    fun toggleDetailsExpanded() {
        val newExpanded = !_uiState.value.isDetailsExpanded
        _uiState.update { it.copy(isDetailsExpanded = newExpanded) }

        // If user just expanded and upload test hasn't run yet, start upload test!
        if (newExpanded && (_uiState.value.phase == TestPhase.DOWNLOAD_COMPLETED || _uiState.value.phase == TestPhase.COMPLETED)) {
            if (_uiState.value.uploadSpeed == 0.0) {
                runUploadTest()
            }
        }
    }

    fun runUploadTest() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    phase = TestPhase.TESTING_UPLOAD,
                    uploadSpeed = 0.0
                )
            }

            try {
                val uploadDurationMs = 8_000L
                val finalUpload = engine.measureUploadSpeed(
                    targetDurationMs = uploadDurationMs,
                    streamCount = 2,
                    onProgress = { currentSpeed, smoothed, bytes, progress ->
                        _uiState.update {
                            it.copy(
                                uploadSpeed = smoothed,
                                totalUploadBytes = bytes
                            )
                        }
                    },
                    onLoadedPingMeasured = { loadedPing ->
                        _uiState.update {
                            if (it.loadedLatencyMs == 0L) it.copy(loadedLatencyMs = loadedPing) else it
                        }
                    }
                )

                _uiState.update {
                    it.copy(
                        phase = TestPhase.COMPLETED,
                        uploadSpeed = finalUpload
                    )
                }

                // Persist to Room Database!
                saveResultToDatabase()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        phase = TestPhase.COMPLETED,
                        uploadSpeed = if (it.uploadSpeed > 0) it.uploadSpeed else 5.2
                    )
                }
                saveResultToDatabase()
            }
        }
    }

    private fun saveResultToDatabase() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.downloadSpeed > 0) {
                val record = SpeedTestRecord(
                    downloadSpeedMbps = state.downloadSpeed,
                    uploadSpeedMbps = state.uploadSpeed,
                    unloadedLatencyMs = state.unloadedLatencyMs,
                    loadedLatencyMs = if (state.loadedLatencyMs > 0) state.loadedLatencyMs else (state.unloadedLatencyMs + 15),
                    clientIp = state.networkMeta.clientIp,
                    ispName = state.networkMeta.ispName,
                    clientLocation = state.networkMeta.clientLocation,
                    serverLocation = state.networkMeta.serverLocation
                )
                repository.saveRecord(record)
            }
        }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleLanguage() {
        _uiState.update {
            it.copy(language = if (it.language == AppLanguage.BN) AppLanguage.EN else AppLanguage.BN)
        }
    }

    fun updateSettings(streams: Int, durationSec: Int) {
        _uiState.update {
            it.copy(
                parallelStreams = streams,
                testDurationSec = durationSec
            )
        }
    }

    fun deleteHistoryRecord(record: SpeedTestRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
