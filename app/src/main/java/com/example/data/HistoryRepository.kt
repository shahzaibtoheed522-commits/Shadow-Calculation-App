package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun insertHistory(expression: String, result: String) {
        val history = HistoryEntity(expression = expression, result = result)
        historyDao.insertHistory(history)
    }

    suspend fun deleteHistory(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }
}
