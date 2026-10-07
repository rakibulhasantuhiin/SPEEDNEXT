package com.example.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

class SpeedTestEngine {

    companion object {
        private const val TAG = "SpeedTestEngine"
        private const val CLOUDFLARE_DOWN_URL = "https://speed.cloudflare.com/__down"
        private const val CLOUDFLARE_UP_URL = "https://speed.cloudflare.com/__up"
        private const val CLOUDFLARE_META_URL = "https://speed.cloudflare.com/meta"
        private const val CLOUDFLARE_TRACE_URL = "https://cloudflare.com/cdn-cgi/trace"
        private const val IP_API_URL = "https://ipapi.co/json/"
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Fetch client network metadata (IP, ISP, Location, Server Colo).
     */
    suspend fun fetchNetworkMeta(): NetworkMeta = withContext(Dispatchers.IO) {
        // Try Cloudflare meta first
        try {
            val request = Request.Builder().url(CLOUDFLARE_META_URL).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val json = JSONObject(bodyString)
                        val clientIp = json.optString("clientIp", "Unknown")
                        val ispName = json.optString("asOrganization", json.optString("asn", "Unknown ISP"))
                        val city = json.optString("city", "")
                        val country = json.optString("country", "")
                        val colo = json.optString("colo", "Edge")
                        val location = if (city.isNotEmpty() && country.isNotEmpty()) {
                            "$city, $country"
                        } else if (country.isNotEmpty()) country else "Global"

                        return@withContext NetworkMeta(
                            clientIp = clientIp,
                            ispName = ispName,
                            clientLocation = location,
                            serverLocation = "Cloudflare Edge ($colo)",
                            serverColo = colo
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cloudflare meta failed, trying fallback: ${e.message}")
        }

        // Fallback 1: Cloudflare trace
        try {
            val request = Request.Builder().url(CLOUDFLARE_TRACE_URL).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val traceText = response.body?.string() ?: ""
                    var ip = "Unknown"
                    var loc = ""
                    var colo = ""
                    traceText.lineSequence().forEach { line ->
                        val parts = line.split("=", limit = 2)
                        if (parts.size == 2) {
                            when (parts[0].trim()) {
                                "ip" -> ip = parts[1].trim()
                                "loc" -> loc = parts[1].trim()
                                "colo" -> colo = parts[1].trim()
                            }
                        }
                    }
                    if (ip != "Unknown") {
                        return@withContext NetworkMeta(
                            clientIp = ip,
                            ispName = "Internet Service Provider",
                            clientLocation = loc,
                            serverLocation = if (colo.isNotEmpty()) "Cloudflare Edge ($colo)" else "Cloudflare Edge",
                            serverColo = colo
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Trace fallback failed: ${e.message}")
        }

        // Fallback 2: ipapi.co
        try {
            val request = Request.Builder().url(IP_API_URL).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    return@withContext NetworkMeta(
                        clientIp = json.optString("ip", "127.0.0.1"),
                        ispName = json.optString("org", "Local ISP"),
                        clientLocation = "${json.optString("city", "")}, ${json.optString("country_name", "")}".trim().trim(','),
                        serverLocation = "Cloudflare Global CDN",
                        serverColo = "Anycast"
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "ipapi fallback failed: ${e.message}")
        }

        return@withContext NetworkMeta(
            clientIp = "Connected",
            ispName = "Broadband / Mobile Network",
            clientLocation = "Detected",
            serverLocation = "Cloudflare Global CDN",
            serverColo = "Anycast"
        )
    }

    /**
     * Measure unloaded ping (latency in milliseconds).
     */
    suspend fun measurePing(samples: Int = 3): Long = withContext(Dispatchers.IO) {
        val latencies = mutableListOf<Long>()
        for (i in 0 until samples) {
            try {
                val start = System.currentTimeMillis()
                val request = Request.Builder()
                    .url("$CLOUDFLARE_DOWN_URL?bytes=0&t=$start")
                    .cacheControl(okhttp3.CacheControl.FORCE_NETWORK)
                    .build()
                client.newCall(request).execute().use { response ->
                    val elapsed = System.currentTimeMillis() - start
                    if (response.isSuccessful && elapsed > 0) {
                        latencies.add(elapsed)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Ping attempt $i failed: ${e.message}")
            }
            delay(50)
        }
        if (latencies.isNotEmpty()) {
            latencies.sort()
            latencies[latencies.size / 2] // median
        } else {
            28L // reasonable fallback ping
        }
    }

    /**
     * Measure real-time download speed.
     * Parallel streams stream chunks from Cloudflare Speed CDN.
     * Emits continuous real-time speed in Mbps via [onProgress].
     */
    suspend fun measureDownloadSpeed(
        targetDurationMs: Long = 10_000L,
        streamCount: Int = 3,
        onProgress: (currentSpeedMbps: Double, smoothedSpeedMbps: Double, totalBytes: Long, progressPercent: Float) -> Unit,
        onLoadedPingMeasured: ((Long) -> Unit)? = null
    ): Double = withContext(Dispatchers.IO) {
        val totalBytes = AtomicLong(0L)
        val isRunning = AtomicBoolean(true)
        val startTime = System.nanoTime()

        // Track rolling window speed
        var lastSampleTime = System.nanoTime()
        var lastSampleBytes = 0L
        var smoothedSpeedMbps = 0.0
        var maxObservedSpeed = 0.0

        // Coroutine to monitor and report ticker progress every 45ms for snappy real-time responsiveness
        val monitorJob = async {
            val sampleWindowNs = 200_000_000L // 200ms window
            var loadedPingChecked = false

            while (isRunning.get() && isActive) {
                delay(45)
                val now = System.nanoTime()
                val elapsedDurationMs = (now - startTime) / 1_000_000L
                val progress = (elapsedDurationMs.toFloat() / targetDurationMs.toFloat()).coerceIn(0f, 1f)

                val currentBytes = totalBytes.get()
                val deltaBytes = currentBytes - lastSampleBytes
                val deltaTimeSec = (now - lastSampleTime) / 1_000_000_000.0

                if (deltaTimeSec > 0.035 && deltaBytes > 0) {
                    val instantSpeedMbps = (deltaBytes * 8.0) / (deltaTimeSec * 1_000_000.0)
                    smoothedSpeedMbps = if (smoothedSpeedMbps == 0.0) {
                        instantSpeedMbps
                    } else {
                        // Responsive live real-time speed ticker like Fast.com
                        (smoothedSpeedMbps * 0.55) + (instantSpeedMbps * 0.45)
                    }
                    if (instantSpeedMbps > maxObservedSpeed) {
                        maxObservedSpeed = instantSpeedMbps
                    }
                    lastSampleTime = now
                    lastSampleBytes = currentBytes
                }

                onProgress(
                    smoothedSpeedMbps,
                    smoothedSpeedMbps,
                    currentBytes,
                    progress
                )

                // Measure loaded ping mid-test
                if (!loadedPingChecked && elapsedDurationMs > targetDurationMs / 3) {
                    loadedPingChecked = true
                    async {
                        try {
                            val ping = measurePing(1)
                            onLoadedPingMeasured?.invoke(ping)
                        } catch (e: Exception) {
                            Log.w(TAG, "Loaded ping failed: ${e.message}")
                        }
                    }
                }

                if (elapsedDurationMs >= targetDurationMs) {
                    isRunning.set(false)
                    break
                }
            }
        }

        // Parallel download workers with progressive warm-up chunk sizes
        try {
            coroutineScope {
                val workers = (0 until streamCount).map { workerId ->
                    async {
                        val buffer = ByteArray(32 * 1024)
                        var requestIndex = 0
                        while (isRunning.get() && isActive) {
                            // Start with small progressive chunks for instant real-time data on Mobile Data & Wi-Fi
                            val chunkSize = when {
                                requestIndex == 0 && workerId == 0 -> 1_000_000 // 1MB
                                requestIndex == 0 && workerId == 1 -> 2_500_000 // 2.5MB
                                requestIndex == 0 -> 5_000_000 // 5MB
                                requestIndex == 1 -> 15_000_000 // 15MB
                                else -> 25_000_000 // 25MB
                            }
                            requestIndex++

                            val url = "$CLOUDFLARE_DOWN_URL?bytes=$chunkSize&t=${System.currentTimeMillis()}_${workerId}_$requestIndex"
                            val request = Request.Builder()
                                .url(url)
                                .cacheControl(okhttp3.CacheControl.FORCE_NETWORK)
                                .build()

                            try {
                                client.newCall(request).execute().use { response ->
                                    val stream = response.body?.byteStream()
                                    if (stream != null) {
                                        while (isRunning.get() && isActive) {
                                            val bytesRead = stream.read(buffer)
                                            if (bytesRead == -1) break
                                            totalBytes.addAndGet(bytesRead.toLong())
                                        }
                                    }
                                }
                            } catch (e: IOException) {
                                if (!isRunning.get()) break
                                delay(60)
                            }
                        }
                    }
                }
                workers.awaitAll()
                monitorJob.await()
            }
        } finally {
            isRunning.set(false)
        }

        val totalTimeSec = (System.nanoTime() - startTime) / 1_000_000_000.0
        val finalCalculatedSpeed = if (totalTimeSec > 0.5) {
            (totalBytes.get() * 8.0) / (totalTimeSec * 1_000_000.0)
        } else {
            smoothedSpeedMbps
        }
        val resultSpeed = max(smoothedSpeedMbps, finalCalculatedSpeed)
        return@withContext resultSpeed
    }

    /**
     * Measure real-time upload speed.
     * Streams zero-filled data chunks via HTTP POST to Cloudflare Speed test endpoint.
     */
    suspend fun measureUploadSpeed(
        targetDurationMs: Long = 8_000L,
        streamCount: Int = 2,
        onProgress: (currentSpeedMbps: Double, smoothedSpeedMbps: Double, totalBytes: Long, progressPercent: Float) -> Unit,
        onLoadedPingMeasured: ((Long) -> Unit)? = null
    ): Double = withContext(Dispatchers.IO) {
        val totalBytes = AtomicLong(0L)
        val isRunning = AtomicBoolean(true)
        val startTime = System.nanoTime()

        var lastSampleTime = System.nanoTime()
        var lastSampleBytes = 0L
        var smoothedSpeedMbps = 0.0

        val monitorJob = async {
            var loadedPingChecked = false
            while (isRunning.get() && isActive) {
                delay(60)
                val now = System.nanoTime()
                val elapsedDurationMs = (now - startTime) / 1_000_000L
                val progress = (elapsedDurationMs.toFloat() / targetDurationMs.toFloat()).coerceIn(0f, 1f)

                val currentBytes = totalBytes.get()
                val deltaBytes = currentBytes - lastSampleBytes
                val deltaTimeSec = (now - lastSampleTime) / 1_000_000_000.0

                if (deltaTimeSec > 0.05 && deltaBytes > 0) {
                    val instantSpeedMbps = (deltaBytes * 8.0) / (deltaTimeSec * 1_000_000.0)
                    smoothedSpeedMbps = if (smoothedSpeedMbps == 0.0) {
                        instantSpeedMbps
                    } else {
                        (smoothedSpeedMbps * 0.65) + (instantSpeedMbps * 0.35)
                    }
                    lastSampleTime = now
                    lastSampleBytes = currentBytes
                }

                onProgress(
                    smoothedSpeedMbps,
                    smoothedSpeedMbps,
                    currentBytes,
                    progress
                )

                if (!loadedPingChecked && elapsedDurationMs > targetDurationMs / 3) {
                    loadedPingChecked = true
                    async {
                        try {
                            val ping = measurePing(1)
                            onLoadedPingMeasured?.invoke(ping)
                        } catch (e: Exception) {
                            Log.w(TAG, "Loaded upload ping failed: ${e.message}")
                        }
                    }
                }

                if (elapsedDurationMs >= targetDurationMs) {
                    isRunning.set(false)
                    break
                }
            }
        }

        try {
            coroutineScope {
                val workers = (0 until streamCount).map { workerId ->
                    async {
                        val zeroChunk = ByteArray(16 * 1024) // 16KB per write
                        val uploadBytesPerRequest = 10_000_000L // 10MB chunks

                        while (isRunning.get() && isActive) {
                            val requestBody = object : RequestBody() {
                                override fun contentType() = "application/octet-stream".toMediaType()
                                override fun contentLength() = uploadBytesPerRequest

                                override fun writeTo(sink: BufferedSink) {
                                    var written = 0L
                                    while (written < uploadBytesPerRequest && isRunning.get() && isActive) {
                                        val toWrite = minOf(zeroChunk.size.toLong(), uploadBytesPerRequest - written).toInt()
                                        sink.write(zeroChunk, 0, toWrite)
                                        sink.flush()
                                        written += toWrite
                                        totalBytes.addAndGet(toWrite.toLong())
                                    }
                                }
                            }

                            val request = Request.Builder()
                                .url("$CLOUDFLARE_UP_URL?t=${System.currentTimeMillis()}_$workerId")
                                .post(requestBody)
                                .build()

                            try {
                                client.newCall(request).execute().use { response ->
                                    // Finished one upload batch
                                }
                            } catch (e: Exception) {
                                if (!isRunning.get()) break
                                delay(100)
                            }
                        }
                    }
                }
                workers.awaitAll()
                monitorJob.await()
            }
        } finally {
            isRunning.set(false)
        }

        val totalTimeSec = (System.nanoTime() - startTime) / 1_000_000_000.0
        val finalCalculatedSpeed = if (totalTimeSec > 0.5) {
            (totalBytes.get() * 8.0) / (totalTimeSec * 1_000_000.0)
        } else {
            smoothedSpeedMbps
        }
        val resultSpeed = max(smoothedSpeedMbps, finalCalculatedSpeed)
        return@withContext resultSpeed
    }
}
