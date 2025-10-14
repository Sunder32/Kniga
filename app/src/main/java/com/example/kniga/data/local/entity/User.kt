package com.example.kniga.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    val id: String,
    
    val email: String,
    val username: String,
    
    // Токены для авторизации
    val accessToken: String? = null,
    val refreshToken: String? = null,
    
    // Аватар
    val avatarUrl: String? = null,
    
    // Премиум подписка
    val isPremium: Boolean = false,
    val premiumUntil: Long? = null,
    
    // Использование хранилища
    val storageUsed: Long = 0, // в байтах
    val storageLimit: Long = 5 * 1024 * 1024 * 1024, // 5 GB по умолчанию
    
    // Настройки
    val syncEnabled: Boolean = true,
    val syncOnlyWifi: Boolean = true,
    val syncFiles: Boolean = false, // Синхронизировать ли сами файлы книг
    
    // Временные метки
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
