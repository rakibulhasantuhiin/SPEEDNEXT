package com.example.data.network

data class NetworkMeta(
    val clientIp: String = "Detecting...",
    val ispName: String = "Detecting...",
    val clientLocation: String = "Unknown",
    val serverLocation: String = "Cloudflare Edge",
    val serverColo: String = "Auto",
    val connectionType: String = "Wi-Fi / Mobile"
)

enum class TestPhase {
    IDLE,
    CONNECTING,
    TESTING_DOWNLOAD,
    DOWNLOAD_COMPLETED,
    TESTING_UPLOAD,
    COMPLETED,
    ERROR
}

data class SpeedProgress(
    val currentSpeedMbps: Double = 0.0,
    val smoothedSpeedMbps: Double = 0.0,
    val progressPercent: Float = 0f,
    val bytesTransferred: Long = 0L,
    val phase: TestPhase = TestPhase.IDLE,
    val unloadedLatencyMs: Long = 0L,
    val loadedLatencyMs: Long = 0L,
    val networkMeta: NetworkMeta = NetworkMeta(),
    val errorMessage: String? = null
)
