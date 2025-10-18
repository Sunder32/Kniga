package com.example.kniga.data.repository

import com.example.kniga.data.local.dao.ReadingSessionDao
import com.example.kniga.data.local.entity.ReadingSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ReadingSessionRepository(private val sessionDao: ReadingSessionDao) {
    
    suspend fun saveSession(session: ReadingSession) = withContext(Dispatchers.IO) {
        sessionDao.insertSession(session)
    }
    
    fun getAllSessions(bookId: Long): Flow<List<ReadingSession>> {
        return sessionDao.getSessionsForBook(bookId)
    }
    
    suspend fun getTotalReadingTime(bookId: Long): Int = withContext(Dispatchers.IO) {
        sessionDao.getTotalReadingTimeForBook(bookId) ?: 0
    }
    
    suspend fun getAverageWordsPerMinute(bookId: Long): Int = withContext(Dispatchers.IO) {
        sessionDao.getAverageWordsPerMinuteForBook(bookId) ?: 0
    }
    
    fun getSessionsByDateRange(startDate: String, endDate: String): Flow<List<ReadingSession>> {
        return sessionDao.getSessionsInRange(startDate, endDate)
    }
    
    suspend fun getTotalReadingTimeInRange(startDate: String, endDate: String): Long = withContext(Dispatchers.IO) {
        sessionDao.getTotalReadingTimeInRange(startDate, endDate) ?: 0L
    }
    
    suspend fun getAverageReadingSpeed(startDate: String, endDate: String): Int = withContext(Dispatchers.IO) {
        sessionDao.getAverageReadingSpeedInRange(startDate, endDate) ?: 0
    }
    
    // Новые методы для статистики
    fun getTotalReadingTime(): Flow<Long> {
        return sessionDao.getTotalReadingTime()
    }
    
    fun getCurrentStreak(): Flow<Int> {
        return sessionDao.getCurrentStreak()
    }
}
