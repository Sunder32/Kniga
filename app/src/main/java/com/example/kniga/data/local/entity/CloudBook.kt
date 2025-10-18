package com.example.kniga.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index


@Entity(
    tableName = "cloud_books",
    indices = [
        Index(value = ["cloudId"], unique = true),
        Index(value = ["uploaderUserId"]),
        Index(value = ["title", "author"])
    ]
)
data class CloudBook(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val cloudId: String,
    
    // Информация о книге
    val title: String,
    val author: String,
    val isbn: String? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    

    val format: String,
    
    val coverUrl: String? = null,
    
    val fileSize: Long = 0,
    
    val totalPages: Int = 0,
    

    val uploaderUserId: String,
    
    val uploaderUsername: String,
    
    val downloadCount: Int = 0,
    
    val rating: Float = 0f,

    val ratingCount: Int = 0,
    
    val uploadedAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis(),

    val localFilePath: String? = null,
    
    val fileHash: String? = null,
    
    val language: String? = null,
    
    val tags: String? = null
)
