package com.example.kniga.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Очередь синхронизации - хранит изменения для отправки на сервер
 */
@Entity(tableName = "sync_queue")
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Тип сущности (BOOK, PROGRESS, BOOKMARK, HIGHLIGHT, etc.)
    val entityType: String,
    
    // ID сущности в локальной базе
    val entityId: Long,
    
    // Операция (CREATE, UPDATE, DELETE)
    val operation: String,
    
    // JSON данных для синхронизации
    val data: String,
    
    // Количество попыток синхронизации
    val attemptCount: Int = 0,
    
    // Максимальное количество попыток
    val maxAttempts: Int = 3,
    
    // Последняя ошибка
    val lastError: String? = null,
    
    // Приоритет (больше = важнее)
    val priority: Int = 0,
    
    // Временные метки
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long? = null
)

// Типы сущностей
object EntityType {
    const val BOOK = "BOOK"
    const val PROGRESS = "PROGRESS"
    const val BOOKMARK = "BOOKMARK"
    const val HIGHLIGHT = "HIGHLIGHT"
    const val SESSION = "SESSION"
}

// Операции синхронизации
object SyncOperation {
    const val CREATE = "CREATE"
    const val UPDATE = "UPDATE"
    const val DELETE = "DELETE"
}
