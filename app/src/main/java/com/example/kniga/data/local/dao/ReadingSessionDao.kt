package com.example.kniga.data.local.dao

import androidx.room.*
import com.example.kniga.data.local.entity.ReadingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {
    
    @Query("SELECT * FROM reading_sessions WHERE bookId = :bookId ORDER BY startTime DESC")
    fun getSessionsForBook(bookId: Long): Flow<List<ReadingSession>>
    
    @Query("SELECT * FROM reading_sessions WHERE date = :date ORDER BY startTime DESC")
    fun getSessionsForDate(date: String): Flow<List<ReadingSession>>
    
    @Query("SELECT * FROM reading_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getSessionsInRange(startDate: String, endDate: String): Flow<List<ReadingSession>>
    
    @Query("SELECT SUM(durationSeconds) FROM reading_sessions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalReadingTimeInRange(startDate: String, endDate: String): Long?
    
    @Query("SELECT SUM(pagesRead) FROM reading_sessions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalPagesReadInRange(startDate: String, endDate: String): Int?
    
    @Query("SELECT AVG(wordsPerMinute) FROM reading_sessions WHERE wordsPerMinute > 0 AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageReadingSpeedInRange(startDate: String, endDate: String): Int?
    
    @Query("SELECT SUM(durationSeconds) FROM reading_sessions WHERE bookId = :bookId")
    suspend fun getTotalReadingTimeForBook(bookId: Long): Int?
    
    @Query("SELECT AVG(wordsPerMinute) FROM reading_sessions WHERE bookId = :bookId AND wordsPerMinute > 0")
    suspend fun getAverageWordsPerMinuteForBook(bookId: Long): Int?
    
    @Query("SELECT COUNT(DISTINCT date) FROM reading_sessions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getReadingDaysCountInRange(startDate: String, endDate: String): Int
    
    @Query("SELECT * FROM reading_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<ReadingSession>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReadingSession): Long
    
    @Update
    suspend fun updateSession(session: ReadingSession)
    
    @Delete
    suspend fun deleteSession(session: ReadingSession)
    
    @Query("DELETE FROM reading_sessions WHERE bookId = :bookId")
    suspend fun deleteSessionsForBook(bookId: Long)
    
    @Query("UPDATE reading_sessions SET isSynced = :isSynced WHERE id = :sessionId")
    suspend fun updateSyncStatus(sessionId: Long, isSynced: Boolean)
}
