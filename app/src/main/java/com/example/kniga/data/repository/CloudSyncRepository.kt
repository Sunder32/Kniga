package com.example.kniga.data.repository

import android.content.Context
import com.example.kniga.data.local.dao.BookDao
import com.example.kniga.data.local.dao.CloudBookDao
import com.example.kniga.data.local.dao.UserDao
import com.example.kniga.data.local.entity.Book
import com.example.kniga.data.local.entity.CloudBook
import com.example.kniga.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class CloudSyncRepository(
    private val apiService: ApiService,
    private val bookDao: BookDao,
    private val cloudBookDao: CloudBookDao,
    private val userDao: UserDao,
    private val context: Context
) {
    
    companion object {
        private const val USE_LOCAL_DB_MODE = true
    }
    
    fun getAllCloudBooks(): Flow<List<CloudBook>> {
        return cloudBookDao.getAllCloudBooks()
    }
    
    suspend fun syncCloudBooks(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (USE_LOCAL_DB_MODE) {
                return@withContext Result.success(Unit)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadBookToCloud(book: Book): Result<CloudBook> = withContext(Dispatchers.IO) {
        try {
            val users = userDao.getAllUsers().first()
            if (users.isEmpty()) {
                return@withContext Result.failure(Exception("Пользователь не авторизован"))
            }
            val currentUser = users.first()
            
            val file = File(book.filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Файл книги не найден"))
            }
            
            val fileHash = calculateFileHash(file)
            if (cloudBookDao.isFileHashExists(fileHash)) {
                return@withContext Result.failure(Exception("Эта книга уже есть в облачной библиотеке"))
            }
            
            kotlinx.coroutines.delay(2000)
            val cloudId = "cloud_${System.currentTimeMillis()}_${book.id}"
            
            val cloudBook = CloudBook(
                cloudId = cloudId,
                title = book.title,
                author = book.author,
                isbn = book.isbn,
                publisher = book.publisher,
                publishedDate = book.publishedDate,
                description = book.description,
                format = book.format,
                coverUrl = book.coverPath,
                fileSize = book.fileSize,
                totalPages = book.totalPages,
                uploaderUserId = currentUser.id,
                uploaderUsername = currentUser.username,
                downloadCount = 0,
                rating = 0f,
                ratingCount = 0,
                uploadedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                localFilePath = book.filePath,
                fileHash = fileHash,
                language = null,
                tags = null
            )
            
            cloudBookDao.insertCloudBook(cloudBook)
            bookDao.updateCloudId(book.id, cloudId)
            
            Result.success(cloudBook)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun downloadCloudBook(cloudBook: CloudBook): Result<Book> = withContext(Dispatchers.IO) {
        try {
            if (cloudBook.localFilePath != null) {
                val existingFile = File(cloudBook.localFilePath)
                if (existingFile.exists()) {
                    val existingBook = bookDao.getBookByCloudId(cloudBook.cloudId)
                    if (existingBook != null) {
                        return@withContext Result.success(existingBook)
                    }
                }
            }
            
            if (USE_LOCAL_DB_MODE) {
                val uploaderBook = bookDao.getBookByCloudId(cloudBook.cloudId)
                if (uploaderBook != null && File(uploaderBook.filePath).exists()) {
                    val booksDir = File(context.filesDir, "books")
                    if (!booksDir.exists()) {
                        booksDir.mkdirs()
                    }
                    
                    val sourceFile = File(uploaderBook.filePath)
                    val fileName = "${System.currentTimeMillis()}_${cloudBook.title.replace(" ", "_")}.${cloudBook.format.lowercase()}"
                    val targetFile = File(booksDir, fileName)
                    
                    kotlinx.coroutines.delay(1500)
                    sourceFile.copyTo(targetFile, overwrite = true)
                    
                    val newBook = Book(
                        title = cloudBook.title,
                        author = cloudBook.author,
                        isbn = cloudBook.isbn,
                        publisher = cloudBook.publisher,
                        publishedDate = cloudBook.publishedDate,
                        description = cloudBook.description,
                        filePath = targetFile.absolutePath,
                        format = cloudBook.format,
                        coverPath = cloudBook.coverUrl,
                        fileSize = cloudBook.fileSize,
                        totalPages = cloudBook.totalPages,
                        currentPage = 1,
                        progress = 0f,
                        status = "NOT_STARTED",
                        isFavorite = false,
                        fileHash = cloudBook.fileHash,
                        cloudId = cloudBook.cloudId,
                        isSynced = true,
                        addedAt = System.currentTimeMillis(),
                        lastReadAt = null
                    )
                    
                    val bookId = bookDao.insertBook(newBook)
                    cloudBookDao.incrementDownloadCount(cloudBook.cloudId)
                    cloudBookDao.updateLocalFilePath(cloudBook.cloudId, targetFile.absolutePath)
                    
                    val savedBook = bookDao.getBookByIdSync(bookId)
                    if (savedBook != null) {
                        return@withContext Result.success(savedBook)
                    } else {
                        return@withContext Result.failure(Exception("Ошибка сохранения книги"))
                    }
                } else {
                    return@withContext Result.failure(Exception("Файл книги недоступен для скачивания"))
                }
            }
            
            Result.failure(Exception("Функция скачивания пока недоступна"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCloudBook(cloudBook: CloudBook): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val users = userDao.getAllUsers().first()
            if (users.isEmpty()) {
                return@withContext Result.failure(Exception("Пользователь не авторизован"))
            }
            val currentUser = users.first()
            
            if (cloudBook.uploaderUserId != currentUser.id) {
                return@withContext Result.failure(Exception("Только автор может удалить книгу из облака"))
            }
            
            kotlinx.coroutines.delay(1000)
            cloudBookDao.deleteCloudBook(cloudBook)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun searchCloudBooks(query: String): Flow<List<CloudBook>> {
        return cloudBookDao.searchCloudBooks(query)
    }
    
    fun getCloudBooksByUser(userId: String): Flow<List<CloudBook>> {
        return cloudBookDao.getCloudBooksByUser(userId)
    }
    
    fun getMostPopularBooks(limit: Int = 10): Flow<List<CloudBook>> {
        return cloudBookDao.getMostPopularBooks(limit)
    }
    
    fun getRecentlyUploadedBooks(limit: Int = 20): Flow<List<CloudBook>> {
        return cloudBookDao.getRecentlyUploadedBooks(limit)
    }
    
    private fun calculateFileHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    suspend fun canDeleteCloudBook(cloudBook: CloudBook): Boolean = withContext(Dispatchers.IO) {
        val users = userDao.getAllUsers().first()
        if (users.isEmpty()) return@withContext false
        val currentUser = users.first()
        cloudBook.uploaderUserId == currentUser.id
    }
    
    suspend fun getUserCloudStats(userId: String): CloudUserStats = withContext(Dispatchers.IO) {
        CloudUserStats(
            uploadedCount = cloudBookDao.getUserUploadedCount(userId),
            totalUploadSize = cloudBookDao.getUserTotalUploadSize(userId) ?: 0L,
            downloadedCount = cloudBookDao.getDownloadedCount()
        )
    }
}

data class CloudUserStats(
    val uploadedCount: Int,
    val totalUploadSize: Long,
    val downloadedCount: Int
)
