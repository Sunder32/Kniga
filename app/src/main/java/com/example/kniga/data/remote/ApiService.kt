package com.example.kniga.data.remote

import com.example.kniga.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @GET("books")
    suspend fun getBooks(): Response<List<BookDto>>
    
    @GET("books/{id}")
    suspend fun getBook(@Path("id") bookId: String): Response<BookDto>
    
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
    suspend fun downloadBook(@Path("id") bookId: String): Response<ResponseBody>
    
    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") bookId: String): Response<Unit>
    
    @POST("books/{id}/sync")
    suspend fun syncProgress(
        @Path("id") bookId: String,
        @Body progress: ProgressDto
    ): Response<Unit>
    
    @GET("books/{id}/progress")
    suspend fun getProgress(@Path("id") bookId: String): Response<ProgressDto>
}
