package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_test_records")
data class SpeedTestRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val downloadSpeedMbps: Double,
    val uploadSpeedMbps: Double,
    val unloadedLatencyMs: Long,
    val loadedLatencyMs: Long,
    val clientIp: String,
    val ispName: String,
    val clientLocation: String,
    val serverLocation: String
)
