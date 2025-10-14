package com.example.kniga.utils

import com.example.kniga.data.local.entity.Book
import com.example.kniga.data.local.entity.BookFormat
import com.example.kniga.data.local.entity.BookStatus

/**
 * Тестовые данные для демонстрации приложения
 */
object MockData {
    
    fun getSampleBooks(): List<Book> {
        return listOf(
            Book(
                id = 1,
                title = "Война и мир",
                author = "Лев Толстой",
                filePath = "/books/war_and_peace.epub",
                format = BookFormat.EPUB,
                totalPages = 1200,
                currentPage = 546,
                progress = 45.5f,
                status = BookStatus.READING,
                addedAt = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
                lastReadAt = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
            ),
            Book(
                id = 2,
                title = "Преступление и наказание",
                author = "Федор Достоевский",
                filePath = "/books/crime_and_punishment.epub",
                format = BookFormat.EPUB,
                totalPages = 671,
                currentPage = 671,
                progress = 100f,
                status = BookStatus.COMPLETED,
                addedAt = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000),
                lastReadAt = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000),
                completedAt = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000)
            ),
            Book(
                id = 3,
                title = "Мастер и Маргарита",
                author = "Михаил Булгаков",
                filePath = "/books/master_and_margarita.epub",
                format = BookFormat.EPUB,
                totalPages = 480,
                currentPage = 114,
                progress = 23.7f,
                status = BookStatus.READING,
                isFavorite = true,
                addedAt = System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000),
                lastReadAt = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000)
            ),
            Book(
                id = 4,
                title = "1984",
                author = "Джордж Оруэлл",
                filePath = "/books/1984.epub",
                format = BookFormat.EPUB,
                totalPages = 328,
                currentPage = 1,
                progress = 0f,
                status = BookStatus.NOT_STARTED,
                addedAt = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000)
            ),
            Book(
                id = 5,
                title = "Гарри Поттер и философский камень",
                author = "Джоан Роулинг",
                filePath = "/books/harry_potter_1.epub",
                format = BookFormat.EPUB,
                totalPages = 432,
                currentPage = 291,
                progress = 67.3f,
                status = BookStatus.READING,
                isFavorite = true,
                addedAt = System.currentTimeMillis() - (21 * 24 * 60 * 60 * 1000),
                lastReadAt = System.currentTimeMillis() - (12 * 60 * 60 * 1000)
            ),
            Book(
                id = 6,
                title = "Анна Каренина",
                author = "Лев Толстой",
                filePath = "/books/anna_karenina.epub",
                format = BookFormat.EPUB,
                totalPages = 864,
                currentPage = 108,
                progress = 12.5f,
                status = BookStatus.READING,
                addedAt = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000),
                lastReadAt = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000)
            ),
            Book(
                id = 7,
                title = "Граф Монте-Кристо",
                author = "Александр Дюма",
                filePath = "/books/monte_cristo.epub",
                format = BookFormat.EPUB,
                totalPages = 1200,
                currentPage = 1,
                progress = 0f,
                status = BookStatus.NOT_STARTED,
                addedAt = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000)
            ),
            Book(
                id = 8,
                title = "Три товарища",
                author = "Эрих Мария Ремарк",
                filePath = "/books/three_comrades.epub",
                format = BookFormat.EPUB,
                totalPages = 480,
                currentPage = 480,
                progress = 100f,
                status = BookStatus.COMPLETED,
                isFavorite = true,
                addedAt = System.currentTimeMillis() - (60 * 24 * 60 * 60 * 1000),
                lastReadAt = System.currentTimeMillis() - (45 * 24 * 60 * 60 * 1000),
                completedAt = System.currentTimeMillis() - (45 * 24 * 60 * 60 * 1000)
            )
        )
    }
    
    /**
     * Добавить тестовые данные в базу
     */
    suspend fun populateDatabase(bookRepository: com.example.kniga.data.repository.BookRepository) {
        getSampleBooks().forEach { book ->
            bookRepository.insertBook(book)
        }
    }
}
