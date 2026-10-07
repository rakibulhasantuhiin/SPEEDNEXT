package com.example.data.repository

import com.example.data.local.SpeedTestDao
import com.example.data.local.SpeedTestRecord
import kotlinx.coroutines.flow.Flow

class SpeedTestRepository(private val dao: SpeedTestDao) {
    val allRecords: Flow<List<SpeedTestRecord>> = dao.getAllRecords()

    suspend fun saveRecord(record: SpeedTestRecord): Long {
        return dao.insertRecord(record)
    }

    suspend fun deleteRecord(record: SpeedTestRecord) {
        dao.deleteRecord(record)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
