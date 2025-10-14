package com.example.kniga.data.local.dao

import androidx.room.*
import com.example.kniga.data.local.entity.ReadingProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {
    
    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId ORDER BY updatedAt DESC LIMIT 1")
    fun getLatestProgressForBook(bookId: Long): Flow<ReadingProgress?>
    
    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestProgressForBookSync(bookId: Long): ReadingProgress?
    
    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId ORDER BY updatedAt DESC")
    fun getAllProgressForBook(bookId: Long): Flow<List<ReadingProgress>>
    
    @Query("SELECT * FROM reading_progress WHERE isSynced = 0")
    suspend fun getUnsyncedProgress(): List<ReadingProgress>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ReadingProgress): Long
    
    @Update
    suspend fun updateProgress(progress: ReadingProgress)
    
    @Delete
    suspend fun deleteProgress(progress: ReadingProgress)
    
    @Query("DELETE FROM reading_progress WHERE bookId = :bookId")
    suspend fun deleteProgressForBook(bookId: Long)
    
    @Query("UPDATE reading_progress SET isSynced = :isSynced WHERE id = :progressId")
    suspend fun updateSyncStatus(progressId: Long, isSynced: Boolean)
}
