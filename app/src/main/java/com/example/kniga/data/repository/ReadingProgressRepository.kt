package com.example.kniga.data.repository

import com.example.kniga.data.local.dao.ReadingProgressDao
import com.example.kniga.data.local.entity.ReadingProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ReadingProgressRepository(private val progressDao: ReadingProgressDao) {
    
    fun getLatestProgress(bookId: Long): Flow<ReadingProgress?> {
        return progressDao.getLatestProgressForBook(bookId)
    }
    
    fun getAllProgress(bookId: Long): Flow<List<ReadingProgress>> {
        return progressDao.getAllProgressForBook(bookId)
    }
    
    suspend fun saveProgress(progress: ReadingProgress) = withContext(Dispatchers.IO) {
        progressDao.insertProgress(progress)
    }
    
    suspend fun updateProgress(progress: ReadingProgress) = withContext(Dispatchers.IO) {
        progressDao.updateProgress(progress)
    }
}
