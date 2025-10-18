package com.example.kniga.data.local.dao

import androidx.room.*
import com.example.kniga.data.local.entity.CloudBook
import kotlinx.coroutines.flow.Flow

@Dao
interface CloudBookDao {
    
    
    @Query("SELECT * FROM cloud_books ORDER BY uploadedAt DESC")
    fun getAllCloudBooks(): Flow<List<CloudBook>>
    
    @Query("SELECT * FROM cloud_books WHERE cloudId = :cloudId LIMIT 1")
    suspend fun getCloudBookByCloudId(cloudId: String): CloudBook?
    
    @Query("SELECT * FROM cloud_books WHERE id = :id LIMIT 1")
    suspend fun getCloudBookById(id: Long): CloudBook?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCloudBook(cloudBook: CloudBook): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCloudBooks(cloudBooks: List<CloudBook>)
    
    @Update
    suspend fun updateCloudBook(cloudBook: CloudBook)
    
    @Delete
    suspend fun deleteCloudBook(cloudBook: CloudBook)
    
    @Query("DELETE FROM cloud_books WHERE cloudId = :cloudId")
    suspend fun deleteCloudBookByCloudId(cloudId: String)
    
    @Query("DELETE FROM cloud_books")
    suspend fun deleteAllCloudBooks()
    
    
    @Query("""
        SELECT * FROM cloud_books 
        WHERE title LIKE '%' || :query || '%' 
           OR author LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY uploadedAt DESC
    """)
    fun searchCloudBooks(query: String): Flow<List<CloudBook>>
    
    @Query("SELECT * FROM cloud_books WHERE uploaderUserId = :userId ORDER BY uploadedAt DESC")
    fun getCloudBooksByUser(userId: String): Flow<List<CloudBook>>
    
    @Query("SELECT * FROM cloud_books WHERE format = :format ORDER BY uploadedAt DESC")
    fun getCloudBooksByFormat(format: String): Flow<List<CloudBook>>
    
    @Query("SELECT * FROM cloud_books WHERE localFilePath IS NOT NULL ORDER BY uploadedAt DESC")
    fun getDownloadedCloudBooks(): Flow<List<CloudBook>>
    
    @Query("SELECT * FROM cloud_books WHERE localFilePath IS NULL ORDER BY uploadedAt DESC")
    fun getNotDownloadedCloudBooks(): Flow<List<CloudBook>>
    
    
    @Query("UPDATE cloud_books SET localFilePath = :localFilePath WHERE cloudId = :cloudId")
    suspend fun updateLocalFilePath(cloudId: String, localFilePath: String?)
    
    @Query("UPDATE cloud_books SET downloadCount = downloadCount + 1 WHERE cloudId = :cloudId")
    suspend fun incrementDownloadCount(cloudId: String)
    
    @Query("UPDATE cloud_books SET rating = :rating, ratingCount = :ratingCount WHERE cloudId = :cloudId")
    suspend fun updateRating(cloudId: String, rating: Float, ratingCount: Int)
    
    @Query("UPDATE cloud_books SET coverUrl = :coverUrl WHERE cloudId = :cloudId")
    suspend fun updateCoverUrl(cloudId: String, coverUrl: String)
    
    
    @Query("SELECT COUNT(*) FROM cloud_books")
    suspend fun getCloudBooksCount(): Int
    
    @Query("SELECT COUNT(*) FROM cloud_books WHERE uploaderUserId = :userId")
    suspend fun getUserUploadedCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM cloud_books WHERE localFilePath IS NOT NULL")
    suspend fun getDownloadedCount(): Int
    
    @Query("SELECT SUM(fileSize) FROM cloud_books WHERE uploaderUserId = :userId")
    suspend fun getUserTotalUploadSize(userId: String): Long?
    
    @Query("SELECT * FROM cloud_books ORDER BY downloadCount DESC LIMIT :limit")
    fun getMostPopularBooks(limit: Int = 10): Flow<List<CloudBook>>
    
    @Query("SELECT * FROM cloud_books ORDER BY rating DESC LIMIT :limit")
    fun getTopRatedBooks(limit: Int = 10): Flow<List<CloudBook>>
    
    @Query("SELECT * FROM cloud_books ORDER BY uploadedAt DESC LIMIT :limit")
    fun getRecentlyUploadedBooks(limit: Int = 10): Flow<List<CloudBook>>
    
    // === Проверки ===
    
    @Query("SELECT EXISTS(SELECT 1 FROM cloud_books WHERE cloudId = :cloudId)")
    suspend fun isCloudBookExists(cloudId: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM cloud_books WHERE fileHash = :fileHash)")
    suspend fun isFileHashExists(fileHash: String): Boolean
}
