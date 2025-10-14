package com.example.kniga.data.service

import android.content.Context
import android.net.Uri
import com.example.kniga.data.local.entity.Book
import com.example.kniga.data.repository.BookRepository
import com.example.kniga.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class BookImportService(
    private val context: Context,
    private val bookRepository: BookRepository
) {
    
    suspend fun importBook(uri: Uri): Result<Book> = withContext(Dispatchers.IO) {
        try {
            // Получаем информацию о файле
            val fileName = getFileName(uri) ?: return@withContext Result.failure(
                Exception("Не удалось получить имя файла")
            )
            
            val fileExtension = fileName.substringAfterLast(".", "").lowercase()
            if (!isSupportedFormat(fileExtension)) {
                return@withContext Result.failure(
                    Exception("Неподдерживаемый формат файла. Поддерживаются: EPUB, PDF, FB2, MOBI")
                )
            }
            
            // Копируем файл в хранилище приложения
            val booksDir = FileUtils.getBooksDirectory(context)
            val destinationFile = File(booksDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(
                Exception("Не удалось прочитать файл")
            )
            
            // Извлекаем метаданные
            val metadata = extractMetadata(destinationFile, fileExtension)
            
            // Создаем запись в базе данных
            val book = Book(
                title = metadata.title,
                author = metadata.author,
                filePath = destinationFile.absolutePath,
                fileSize = destinationFile.length(),
                format = fileExtension.uppercase(),
                coverPath = null,
                description = metadata.description,
                totalPages = metadata.totalPages,
                progress = 0f,
                status = "NOT_STARTED",
                isFavorite = false,
                addedAt = System.currentTimeMillis(),
                lastReadAt = null,
                isSynced = false,
                cloudId = null
            )
            
            bookRepository.insertBook(book)
            Result.success(book)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName ?: uri.lastPathSegment
    }
    
    private fun isSupportedFormat(extension: String): Boolean {
        return extension in listOf("epub", "pdf", "fb2", "mobi")
    }
    
    private fun extractMetadata(file: File, format: String): BookMetadata {
        // TODO: Реальный парсинг метаданных из файла
        // Для EPUB можно использовать библиотеку epublib
        // Для PDF - pdfbox или iText
        // Для FB2 - XML парсер
        
        return BookMetadata(
            title = file.nameWithoutExtension,
            author = "Неизвестный автор",
            description = "Импортированная книга",
            totalPages = 100
        )
    }
    
    data class BookMetadata(
        val title: String,
        val author: String,
        val description: String,
        val totalPages: Int
    )
}
