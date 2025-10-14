package com.example.kniga.data.local.dao

import androidx.room.*
import com.example.kniga.data.local.entity.Highlight
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {
    
    @Query("SELECT * FROM highlights WHERE bookId = :bookId ORDER BY pageNumber ASC")
    fun getHighlightsForBook(bookId: Long): Flow<List<Highlight>>
    
    @Query("SELECT * FROM highlights WHERE bookId = :bookId AND pageNumber = :pageNumber")
    fun getHighlightsForPage(bookId: Long, pageNumber: Int): Flow<List<Highlight>>
    
    @Query("SELECT * FROM highlights WHERE id = :highlightId")
    suspend fun getHighlightById(highlightId: Long): Highlight?
    
    @Query("SELECT * FROM highlights WHERE bookId = :bookId AND color = :color")
    fun getHighlightsByColor(bookId: Long, color: String): Flow<List<Highlight>>
    
    @Query("SELECT * FROM highlights WHERE selectedText LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%'")
    fun searchHighlights(query: String): Flow<List<Highlight>>
    
    @Query("SELECT * FROM highlights WHERE isSynced = 0")
    suspend fun getUnsyncedHighlights(): List<Highlight>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: Highlight): Long
    
    @Update
    suspend fun updateHighlight(highlight: Highlight)
    
    @Delete
    suspend fun deleteHighlight(highlight: Highlight)
    
    @Query("DELETE FROM highlights WHERE id = :highlightId")
    suspend fun deleteHighlightById(highlightId: Long)
    
    @Query("DELETE FROM highlights WHERE bookId = :bookId")
    suspend fun deleteHighlightsForBook(bookId: Long)
    
    @Query("UPDATE highlights SET isSynced = :isSynced WHERE id = :highlightId")
    suspend fun updateSyncStatus(highlightId: Long, isSynced: Boolean)
    
    @Query("UPDATE highlights SET cloudId = :cloudId, isSynced = 1 WHERE id = :highlightId")
    suspend fun updateCloudId(highlightId: Long, cloudId: String)
    
    @Query("SELECT COUNT(*) FROM highlights WHERE bookId = :bookId")
    suspend fun getHighlightsCountForBook(bookId: Long): Int
}
