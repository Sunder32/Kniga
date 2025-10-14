package com.example.kniga.data.local.dao

import androidx.room.*
import com.example.kniga.data.local.entity.Bookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    
    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY pageNumber ASC")
    fun getBookmarksForBook(bookId: Long): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks WHERE id = :bookmarkId")
    suspend fun getBookmarkById(bookmarkId: Long): Bookmark?
    
    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId AND pageNumber = :pageNumber LIMIT 1")
    suspend fun getBookmarkByPage(bookId: Long, pageNumber: Int): Bookmark?
    
    @Query("SELECT * FROM bookmarks WHERE note LIKE '%' || :query || '%' OR textPreview LIKE '%' || :query || '%'")
    fun searchBookmarks(query: String): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks WHERE isSynced = 0")
    suspend fun getUnsyncedBookmarks(): List<Bookmark>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark): Long
    
    @Update
    suspend fun updateBookmark(bookmark: Bookmark)
    
    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)
    
    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Long)
    
    @Query("DELETE FROM bookmarks WHERE bookId = :bookId")
    suspend fun deleteBookmarksForBook(bookId: Long)
    
    @Query("UPDATE bookmarks SET isSynced = :isSynced WHERE id = :bookmarkId")
    suspend fun updateSyncStatus(bookmarkId: Long, isSynced: Boolean)
    
    @Query("UPDATE bookmarks SET cloudId = :cloudId, isSynced = 1 WHERE id = :bookmarkId")
    suspend fun updateCloudId(bookmarkId: Long, cloudId: String)
    
    @Query("SELECT COUNT(*) FROM bookmarks WHERE bookId = :bookId")
    suspend fun getBookmarksCountForBook(bookId: Long): Int
}
