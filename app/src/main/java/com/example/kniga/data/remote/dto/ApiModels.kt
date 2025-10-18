package com.example.kniga.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

data class AuthResponse(
    val token: String,
    val userId: Long,
    val email: String,
    val name: String
)

data class BookDto(
    val id: String,
    val title: String,
    val author: String,
    val format: String,
    val totalPages: Int,
    val fileSize: Long,
    val uploadedBy: Long,
    val uploadedAt: Long,
    val downloadUrl: String?
)

data class ProgressDto(
    val bookId: String,
    val currentPage: Int,
    val progress: Float,
    val status: String,
    val lastReadAt: Long
)
