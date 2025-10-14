package com.example.kniga.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kniga.data.local.entity.Book
import com.example.kniga.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {
    
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
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
}
