package com.example.kniga.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kniga.data.local.entity.Book
import com.example.kniga.data.local.entity.Bookmark
import com.example.kniga.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {
    
    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()
    
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _totalPages = MutableStateFlow(100)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()
    
    private val _fontSize = MutableStateFlow(18f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()
    
    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()
    
    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            bookRepository.getBookById(bookId).collect { book ->
                book?.let {
                    _book.value = it
                    _totalPages.value = it.totalPages
                    _currentPage.value = calculateCurrentPage(it.progress)
                }
            }
        }
    }
    
    fun setPage(page: Int) {
        _currentPage.value = page
        updateProgress()
    }
    
    fun nextPage() {
        if (_currentPage.value < _totalPages.value) {
            _currentPage.value++
            updateProgress()
        }
    }
    
    fun previousPage() {
        if (_currentPage.value > 1) {
            _currentPage.value--
            updateProgress()
        }
    }
    
    fun setFontSize(size: Float) {
        _fontSize.value = size
    }
    
    fun addBookmark(note: String = "") {
        viewModelScope.launch {
            _book.value?.let { book ->
                val bookmark = Bookmark(
                    bookId = book.id,
                    pageNumber = _currentPage.value,
                    position = "",
                    textPreview = "Страница ${_currentPage.value}",
                    note = note
                )
                // TODO: Save bookmark via repository
            }
        }
    }
    
    private fun updateProgress() {
        viewModelScope.launch {
            _book.value?.let { book ->
                val progressPercent = (_currentPage.value.toFloat() / _totalPages.value.toFloat() * 100)
                bookRepository.updateProgress(book.id, progressPercent)
            }
        }
    }
    
    private fun calculateCurrentPage(progressPercent: Float): Int {
        return ((progressPercent / 100f) * _totalPages.value).toInt().coerceAtLeast(1)
    }
}
    

