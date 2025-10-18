package com.example.kniga.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kniga.data.local.entity.Book
import com.example.kniga.data.repository.BookRepository
import com.example.kniga.data.repository.CloudSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val bookRepository: BookRepository,
    private val cloudSyncRepository: CloudSyncRepository
) : ViewModel() {
    
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()
    
    sealed class UploadState {
        object Idle : UploadState()
        object Uploading : UploadState()
        data class Success(val cloudId: String) : UploadState()
        data class Error(val message: String) : UploadState()
    }
    
    init {
        loadBooks()
    }
    
    fun loadBooks() {
        viewModelScope.launch {
            when (_selectedFilter.value) {
                "reading" -> bookRepository.getReadingBooks()
                "completed" -> bookRepository.getCompletedBooks()
                "not_started" -> bookRepository.getNotStartedBooks()
                "favorites" -> bookRepository.getFavoriteBooks()
                else -> bookRepository.getAllBooks()
            }.collect { bookList ->
                _books.value = if (_searchQuery.value.isNotEmpty()) {
                    bookList.filter { 
                        it.title.contains(_searchQuery.value, ignoreCase = true) ||
                        it.author.contains(_searchQuery.value, ignoreCase = true)
                    }
                } else {
                    bookList
                }
            }
        }
    }
    
    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        loadBooks()
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        loadBooks()
    }
    
    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            bookRepository.deleteBookById(bookId)
        }
    }
    
    fun toggleFavorite(book: Book) {
        viewModelScope.launch {
            bookRepository.updateBook(book.copy(isFavorite = !book.isFavorite))
        }
    }
    
    fun uploadBook(book: Book) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Uploading
            
            val result = cloudSyncRepository.uploadBookToCloud(book)
            
            _uploadState.value = if (result.isSuccess) {
                // Возвращаем cloudId вместо CloudBook для совместимости
                UploadState.Success(result.getOrNull()!!.cloudId)
            } else {
                UploadState.Error(result.exceptionOrNull()?.message ?: "Неизвестная ошибка")
            }
        }
    }
    
    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}
