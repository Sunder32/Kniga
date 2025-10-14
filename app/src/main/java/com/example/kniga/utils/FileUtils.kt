package com.example.kniga.utils

import android.content.Context
import java.io.File
import java.security.MessageDigest

/**
 * Утилиты для работы с файлами
 */
object FileUtils {
    
    /**
     * Получить папку для хранения книг
     */
    fun getBooksDirectory(context: Context): File {
        val dir = File(context.filesDir, Constants.BOOKS_FOLDER)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * Получить папку для хранения обложек
     */
    fun getCoversDirectory(context: Context): File {
        val dir = File(context.filesDir, Constants.COVERS_FOLDER)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * Получить расширение файла
     */
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }
    
    /**
     * Проверить, поддерживается ли формат
     */
    fun isSupportedFormat(fileName: String): Boolean {
        val extension = getFileExtension(fileName)
        return Constants.SUPPORTED_FORMATS.contains(extension)
    }
    
    /**
     * Вычислить SHA-256 хэш файла
     */
    fun calculateFileHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead = input.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Форматирование размера файла (например: "2.5 MB")
     */
    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> "%.2f GB".format(gb)
            mb >= 1 -> "%.2f MB".format(mb)
            kb >= 1 -> "%.2f KB".format(kb)
            else -> "$bytes B"
        }
    }
    
    /**
     * Проверить, достаточно ли места на устройстве
     */
    fun hasEnoughSpace(context: Context, requiredBytes: Long): Boolean {
        val availableBytes = context.filesDir.usableSpace
        return availableBytes > requiredBytes
    }
    
    /**
     * Получить общий размер всех файлов в директории
     */
    fun getDirectorySize(directory: File): Long {
        var size = 0L
        if (directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
        } else {
            size = directory.length()
        }
        return size
    }
    
    /**
     * Удалить файл безопасно
     */
    fun deleteFile(file: File): Boolean {
        return try {
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Очистить кэш приложения
     */
    fun clearCache(context: Context): Boolean {
        return try {
            val cacheDir = context.cacheDir
            deleteDirectory(cacheDir)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Удалить директорию рекурсивно
     */
    private fun deleteDirectory(directory: File): Boolean {
        if (directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                deleteDirectory(file)
            }
        }
        return directory.delete()
    }
    
    /**
     * Копировать файл
     */
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            source.inputStream().use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Создать уникальное имя файла
     */
    fun generateUniqueFileName(originalName: String): String {
        val extension = getFileExtension(originalName)
        val nameWithoutExtension = originalName.substringBeforeLast('.')
        val timestamp = System.currentTimeMillis()
        return "${nameWithoutExtension}_$timestamp.$extension"
    }
}
