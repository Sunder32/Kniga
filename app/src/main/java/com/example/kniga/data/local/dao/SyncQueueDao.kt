package com.example.kniga.data.local.dao

import androidx.room.*
import com.example.kniga.data.local.entity.SyncQueueItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    
    @Query("SELECT * FROM sync_queue WHERE attemptCount < maxAttempts ORDER BY priority DESC, createdAt ASC")
    suspend fun getPendingItems(): List<SyncQueueItem>
    
    @Query("SELECT * FROM sync_queue ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<SyncQueueItem>>
    
    @Query("SELECT * FROM sync_queue WHERE entityType = :type AND entityId = :id")
    suspend fun getItemForEntity(type: String, id: Long): SyncQueueItem?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SyncQueueItem): Long
    
    @Update
    suspend fun updateItem(item: SyncQueueItem)
    
    @Delete
    suspend fun deleteItem(item: SyncQueueItem)
    
    @Query("DELETE FROM sync_queue WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Long)
    
    @Query("DELETE FROM sync_queue WHERE attemptCount >= maxAttempts")
    suspend fun deleteFailedItems()
    
    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE attemptCount < maxAttempts")
    suspend fun getPendingItemsCount(): Int
}
