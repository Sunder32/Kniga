package com.example.kniga.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "reading_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"]), Index(value = ["date"])]
)
data class ReadingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val bookId: Long,
    
    // Длительность сессии в секундах
    val durationSeconds: Long,
    
    // Прочитано страниц за сессию
    val pagesRead: Int = 0,
    
    // Средняя скорость чтения (слов в минуту)
    val wordsPerMinute: Int = 0,
    
    // Дата сессии (в формате yyyy-MM-dd для группировки по дням)
    val date: String,
    
    // Время начала сессии
    val startTime: Long = System.currentTimeMillis(),
    
    // Время окончания сессии
    val endTime: Long = System.currentTimeMillis(),
    
    // Синхронизация
    val isSynced: Boolean = false
)
