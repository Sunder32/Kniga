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
            val fileName = getFileName(uri) ?: return@withContext Result.failure(
                Exception("Не удалось получить имя файла")
            )
            
            val fileExtension = fileName.substringAfterLast(".", "").lowercase()
            if (!isSupportedFormat(fileExtension)) {
                return@withContext Result.failure(
                    Exception("Неподдерживаемый формат файла. Поддерживаются: EPUB, PDF, FB2, MOBI")
                )
            }
            
            val booksDir = FileUtils.getBooksDirectory(context)
            val destinationFile = File(booksDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(
                Exception("Не удалось прочитать файл")
            )
            
            val metadata = extractMetadata(destinationFile, fileExtension)
            
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
        return try {
            when (format.lowercase()) {
                "pdf" -> extractPdfMetadata(file)
                "epub" -> extractEpubMetadata(file)
                "fb2" -> extractFb2Metadata(file)
                else -> BookMetadata(
                    title = file.nameWithoutExtension,
                    author = "Неизвестный автор",
                    description = "Импортированная книга",
                    totalPages = 0
                )
            }
        } catch (e: Exception) {
            BookMetadata(
                title = file.nameWithoutExtension,
                author = "Неизвестный автор",
                description = "Ошибка извлечения метаданных: ${e.message}",
                totalPages = 0
            )
        }
    }
    
    private fun extractPdfMetadata(file: File): BookMetadata {
        return try {
            val document = com.tom_roush.pdfbox.pdmodel.PDDocument.load(file)
            val pageCount = document.numberOfPages
            val info = document.documentInformation
            
            val title = info?.title?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension
            val author = info?.author?.takeIf { it.isNotBlank() } ?: "Неизвестный автор"
            val subject = info?.subject?.takeIf { it.isNotBlank() } ?: "PDF документ"
            
            document.close()
            
            BookMetadata(
                title = title,
                author = author,
                description = subject,
                totalPages = pageCount
            )
        } catch (e: Exception) {
            BookMetadata(
                title = file.nameWithoutExtension,
                author = "Неизвестный автор",
                description = "PDF файл",
                totalPages = 0
            )
        }
    }
    
    private fun extractEpubMetadata(file: File): BookMetadata {
        return BookMetadata(
            title = file.nameWithoutExtension,
            author = "Неизвестный автор",
            description = "EPUB книга",
            totalPages = 0
        )
    }
    
    private fun extractFb2Metadata(file: File): BookMetadata {
        return BookMetadata(
            title = file.nameWithoutExtension,
            author = "Неизвестный автор",
            description = "FB2 книга",
            totalPages = 0
        )
    }
    
    data class BookMetadata(
        val title: String,
        val author: String,
        val description: String,
        val totalPages: Int
    )
}
