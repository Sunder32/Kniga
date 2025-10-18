package com.example.kniga.data.network

import com.example.kniga.data.local.entity.Book
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface BookApiService {
    
    @GET("books")
    suspend fun getAllBooks(): Response<List<BookDto>>
    
    @GET("books/{id}")
    suspend fun getBook(@Path("id") id: String): Response<BookDto>
    
    @Multipart
    @POST("books/upload")
    suspend fun uploadBook(
        @Part file: MultipartBody.Part,
        @Part("title") title: String,
        @Part("author") author: String,
        @Part("format") format: String
    ): Response<BookDto>
    
    @GET("books/{id}/download")
    @Streaming
    suspend fun downloadBook(@Path("id") id: String): Response<ResponseBody>
    
    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") id: String): Response<Unit>
    
    @POST("books/{id}/sync")
    suspend fun syncBookProgress(
        @Path("id") id: String,
        @Body progress: BookProgressDto
    ): Response<Unit>
}

data class BookDto(
    val id: String,
    val title: String,
    val author: String,
    val format: String,
    val fileSize: Long,
    val totalPages: Int,
    val uploadedBy: String,
    val uploadedAt: Long,
    val downloadUrl: String
)

data class BookProgressDto(
    val currentPage: Int,
    val progress: Float,
    val status: String,
    val lastReadAt: Long
)
