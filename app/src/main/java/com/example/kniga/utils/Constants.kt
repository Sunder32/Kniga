package com.example.kniga.utils

object Constants {
    
    // База данных
    const val DATABASE_NAME = "kniga_database"
    const val DATABASE_VERSION = 1
    
    // Shared Preferences / DataStore
    const val PREFS_NAME = "kniga_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    // API
    const val BASE_URL = "https://api.kniga.app/" // TODO: Заменить на реальный URL
    const val API_TIMEOUT = 30L // секунды
    const val MAX_RETRY_ATTEMPTS = 3
    
    // Синхронизация
    const val SYNC_INTERVAL_MINUTES = 30L
    const val SYNC_WORK_NAME = "sync_work"
    const val SYNC_TAG = "sync"
    
    // Файлы
    const val BOOKS_FOLDER = "books"
    const val COVERS_FOLDER = "covers"
    const val MAX_FILE_SIZE = 100 * 1024 * 1024L // 100 MB
    const val COVER_MAX_WIDTH = 600
    const val COVER_MAX_HEIGHT = 900
    const val COVER_QUALITY = 80 // JPEG quality 0-100
    
    // Форматы книг
    val SUPPORTED_FORMATS = listOf("epub", "pdf", "fb2", "mobi")
    const val MIME_TYPE_EPUB = "application/epub+zip"
    const val MIME_TYPE_PDF = "application/pdf"
    const val MIME_TYPE_FB2 = "text/xml"
    const val MIME_TYPE_MOBI = "application/x-mobipocket-ebook"
    
    // Читалка
    const val DEFAULT_FONT_SIZE = 16
    const val MIN_FONT_SIZE = 12
    const val MAX_FONT_SIZE = 28
    const val DEFAULT_LINE_HEIGHT = 1.5f
    const val DEFAULT_MARGIN_DP = 16
    
    // Автосохранение прогресса
    const val PROGRESS_SAVE_INTERVAL_SECONDS = 30L
    
    // Цвета выделения
    const val COLOR_YELLOW = 0xFFFFFF00
    const val COLOR_GREEN = 0xFF00FF00
    const val COLOR_BLUE = 0xFF0000FF
    const val COLOR_PINK = 0xFFFF1493
    const val COLOR_ORANGE = 0xFFFFA500
    
    // Статистика
    const val WORDS_PER_PAGE_ESTIMATE = 250
    const val MIN_READING_SESSION_SECONDS = 5 * 60L // 5 минут
    
    // Настройки по умолчанию
    const val DEFAULT_THEME = "light"
    const val DEFAULT_FONT_FAMILY = "serif"
    const val DEFAULT_READING_MODE = "pagination" // pagination или scroll
    
    // Хранилище
    const val DEFAULT_STORAGE_LIMIT = 5L * 1024 * 1024 * 1024 // 5 GB
    const val PREMIUM_STORAGE_LIMIT = 20L * 1024 * 1024 * 1024 // 20 GB
    
    // Уведомления
    const val NOTIFICATION_CHANNEL_ID = "kniga_notifications"
    const val NOTIFICATION_CHANNEL_NAME = "Kniga"
    const val SYNC_NOTIFICATION_ID = 1
    const val READING_NOTIFICATION_ID = 2
    
    // Intent extras
    const val EXTRA_BOOK_ID = "book_id"
    const val EXTRA_CHAPTER_ID = "chapter_id"
    const val EXTRA_PAGE_NUMBER = "page_number"
    
    // Request codes
    const val REQUEST_CODE_PICK_FILE = 1001
    const val REQUEST_CODE_PERMISSIONS = 1002
    
    // Permissions
    val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    
    // Errors
    const val ERROR_NETWORK = "Ошибка сети"
    const val ERROR_FILE_NOT_FOUND = "Файл не найден"
    const val ERROR_UNSUPPORTED_FORMAT = "Неподдерживаемый формат"
    const val ERROR_FILE_TOO_LARGE = "Файл слишком большой"
    const val ERROR_PARSING = "Ошибка чтения файла"
    const val ERROR_AUTHENTICATION = "Ошибка авторизации"
    const val ERROR_SYNC = "Ошибка синхронизации"
}
