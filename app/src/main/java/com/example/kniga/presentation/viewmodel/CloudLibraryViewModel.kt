package com.example.kniga.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kniga.data.local.entity.Book
import com.example.kniga.data.remote.dto.BookDto
import com.example.kniga.data.repository.CloudSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CloudLibraryViewModel(
    private val cloudSyncRepository: CloudSyncRepository
) : ViewModel() {
    
    private val _cloudBooks = MutableStateFlow<List<BookDto>>(emptyList())
    val cloudBooks: StateFlow<List<BookDto>> = _cloudBooks.asStateFlow()
    
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()
    
    sealed class DownloadState {
        object Idle : DownloadState()
        object Downloading : DownloadState()
        data class Success(val book: Book) : DownloadState()
        data class Error(val message: String) : DownloadState()
    }
    
    fun loadCloudBooks() {
        viewModelScope.launch {
            cloudSyncRepository.getAllCloudBooks().collect { books ->
                _cloudBooks.value = books.map { cloudBook ->
                    // Преобразуем CloudBook в BookDto для совместимости
                    com.example.kniga.data.remote.dto.BookDto(
                        id = cloudBook.cloudId,
                        title = cloudBook.title,
                        author = cloudBook.author,
                        format = cloudBook.format,
                        totalPages = cloudBook.totalPages,
                        fileSize = cloudBook.fileSize,
                        uploadedBy = cloudBook.uploaderUserId.hashCode().toLong(),
                        uploadedAt = cloudBook.uploadedAt,
                        downloadUrl = null
                    )
                }
            }
        }
    }
    
    fun downloadBook(cloudId: String) {
        viewModelScope.launch {
            _downloadState.value = DownloadState.Downloading
            
            val cloudBooks = cloudSyncRepository.getAllCloudBooks()
            cloudBooks.collect { books ->
                val cloudBook = books.find { it.cloudId == cloudId }
                if (cloudBook != null) {
                    val result = cloudSyncRepository.downloadCloudBook(cloudBook)
                    
                    _downloadState.value = if (result.isSuccess) {
                        DownloadState.Success(result.getOrNull()!!)
                    } else {
                        DownloadState.Error(result.exceptionOrNull()?.message ?: "Ошибка скачивания")
                    }
                } else {
                    _downloadState.value = DownloadState.Error("Книга не найдена")
                }
            }
        }
    }
    
    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }
}
