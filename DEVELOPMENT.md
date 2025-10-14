# План дальнейшей разработки приложения Kniga

## ✅ Что уже сделано

### 1. Настройка проекта
- ✅ Обновлены зависимости в `build.gradle.kts`
- ✅ Добавлены все необходимые библиотеки (Room, Retrofit, Coroutines, WorkManager, etc.)
- ✅ Настроен KSP для Room компиляции
- ✅ Включен ViewBinding и Jetpack Compose

### 2. Модели данных (Data Layer)
- ✅ `Book.kt` - основная модель книги
- ✅ `ReadingProgress.kt` - прогресс чтения
- ✅ `Bookmark.kt` - закладки
- ✅ `Highlight.kt` - выделения текста
- ✅ `User.kt` - пользователь
- ✅ `ReadingSession.kt` - сессии чтения для статистики
- ✅ `SyncQueueItem.kt` - очередь синхронизации

### 3. DAO интерфейсы
- ✅ `BookDao.kt` - операции с книгами
- ✅ `ReadingProgressDao.kt` - операции с прогрессом
- ✅ `BookmarkDao.kt` - операции с закладками
- ✅ `HighlightDao.kt` - операции с выделениями
- ✅ `UserDao.kt` - операции с пользователем
- ✅ `ReadingSessionDao.kt` - операции с сессиями
- ✅ `SyncQueueDao.kt` - операции с очередью синхронизации

### 4. База данных
- ✅ `AppDatabase.kt` - главный класс базы данных Room

### 5. Repository слой
- ✅ `BookRepository.kt` - репозиторий для работы с книгами

### 6. Утилиты
- ✅ `Constants.kt` - константы приложения
- ✅ `DateUtils.kt` - работа с датами
- ✅ `FileUtils.kt` - работа с файлами
- ✅ `Result.kt` - обработка результатов операций

### 7. Документация
- ✅ `README.md` - полная документация проекта

## 📋 Следующие шаги

### Шаг 1: Синхронизация Gradle
Сначала нужно синхронизировать проект с Gradle, чтобы все зависимости загрузились.

**В Android Studio:**
1. File → Sync Project with Gradle Files
2. Или нажмите кнопку "Sync Now" в верхней части экрана

**Или через терминал:**
```bash
./gradlew build
```

### Шаг 2: Создание оставшихся Repository классов

Создайте файлы:

#### `ReadingProgressRepository.kt`
```kotlin
package com.example.kniga.data.repository

import com.example.kniga.data.local.dao.ReadingProgressDao
import com.example.kniga.data.local.entity.ReadingProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadingProgressRepository(
    private val progressDao: ReadingProgressDao
) {
    fun getLatestProgressForBook(bookId: Long): Flow<ReadingProgress?> =
        progressDao.getLatestProgressForBook(bookId)
    
    suspend fun insertProgress(progress: ReadingProgress): Long = 
        withContext(Dispatchers.IO) {
            progressDao.insertProgress(progress)
        }
    
    suspend fun updateProgress(progress: ReadingProgress) = 
        withContext(Dispatchers.IO) {
            progressDao.updateProgress(progress)
        }
    
    // Добавьте остальные методы по аналогии
}
```

#### `BookmarkRepository.kt`, `HighlightRepository.kt`, `ReadingSessionRepository.kt`, `UserRepository.kt`
Создайте аналогичные репозитории для остальных DAO.

### Шаг 3: Создание UI слоя

#### 3.1 Создайте Splash Screen
`presentation/ui/splash/SplashActivity.kt`

```kotlin
package com.example.kniga.presentation.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.kniga.presentation.ui.auth.LoginActivity
import com.example.kniga.presentation.ui.library.LibraryActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            delay(2000) // 2 секунды
            
            // TODO: Проверить авторизацию
            val isLoggedIn = false // Заглушка
            
            val intent = if (isLoggedIn) {
                Intent(this@SplashActivity, LibraryActivity::class.java)
            } else {
                Intent(this@SplashActivity, LoginActivity::class.java)
            }
            
            startActivity(intent)
            finish()
        }
    }
}
```

#### 3.2 Создайте экран входа
`presentation/ui/auth/LoginActivity.kt` - используйте Jetpack Compose

#### 3.3 Создайте главный экран библиотеки
`presentation/ui/library/LibraryActivity.kt` - с RecyclerView или LazyColumn в Compose

### Шаг 4: ViewModels

Создайте ViewModels для каждого экрана:

#### `LibraryViewModel.kt`
```kotlin
package com.example.kniga.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kniga.data.repository.BookRepository
import com.example.kniga.data.local.entity.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {
    
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books
    
    init {
        loadBooks()
    }
    
    private fun loadBooks() {
        viewModelScope.launch {
            bookRepository.getAllBooks().collect { bookList ->
                _books.value = bookList
            }
        }
    }
    
    fun searchBooks(query: String) {
        viewModelScope.launch {
            bookRepository.searchBooks(query).collect { results ->
                _books.value = results
            }
        }
    }
}
```

### Шаг 5: EPUB парсер

Создайте `utils/epub/EpubParser.kt`:

```kotlin
package com.example.kniga.utils.epub

import java.io.File
import java.util.zip.ZipFile

data class EpubMetadata(
    val title: String,
    val author: String,
    val isbn: String? = null,
    val publisher: String? = null
)

class EpubParser {
    
    fun parseEpub(file: File): EpubMetadata {
        // TODO: Реализовать парсинг EPUB
        // 1. Открыть файл как ZIP
        // 2. Прочитать META-INF/container.xml
        // 3. Найти content.opf
        // 4. Извлечь метаданные
        
        return EpubMetadata(
            title = "Заглушка",
            author = "Неизвестный автор"
        )
    }
    
    fun extractCover(file: File): ByteArray? {
        // TODO: Извлечь обложку из EPUB
        return null
    }
}
```

### Шаг 6: API сервис (Retrofit)

Создайте `data/remote/api/ApiService.kt`:

```kotlin
package com.example.kniga.data.remote.api

import retrofit2.http.*

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val accessToken: String, val refreshToken: String)

interface ApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @POST("auth/register")
    suspend fun register(@Body request: LoginRequest): LoginResponse
    
    @GET("books")
    suspend fun getBooks(): List<Book>
    
    @POST("books")
    suspend fun createBook(@Body book: Book): Book
    
    @PUT("books/{id}")
    suspend fun updateBook(@Path("id") id: Long, @Body book: Book): Book
    
    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") id: Long)
    
    // Добавьте остальные эндпоинты
}
```

### Шаг 7: Синхронизация (WorkManager)

Создайте `utils/sync/SyncWorker.kt`:

```kotlin
package com.example.kniga.utils.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // TODO: Реализовать синхронизацию
        // 1. Проверить наличие интернета
        // 2. Получить элементы из SyncQueue
        // 3. Отправить на сервер
        // 4. Получить изменения с сервера
        // 5. Обновить локальную БД
        
        return Result.success()
    }
    
    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
                30, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "sync_work",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWork
            )
        }
    }
}
```

### Шаг 8: Обновите AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    
    <application
        android:name=".KnigaApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.Kniga">
        
        <!-- Splash Screen как главная activity -->
        <activity
            android:name=".presentation.ui.splash.SplashActivity"
            android:exported="true"
            android:theme="@style/Theme.Kniga.Splash">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- Остальные activities -->
        <activity android:name=".presentation.ui.auth.LoginActivity" />
        <activity android:name=".presentation.ui.library.LibraryActivity" />
        <activity android:name=".presentation.ui.reader.ReaderActivity" />
        
    </application>
</manifest>
```

### Шаг 9: Application класс

Создайте `KnigaApplication.kt`:

```kotlin
package com.example.kniga

import android.app.Application
import com.example.kniga.data.local.AppDatabase
import com.example.kniga.utils.sync.SyncWorker

class KnigaApplication : Application() {
    
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Запланировать периодическую синхронизацию
        SyncWorker.schedule(this)
    }
}
```

## 🎯 Приоритеты разработки

### Высокий приоритет
1. ✅ Завершить настройку проекта (сделано)
2. ✅ Создать модели данных (сделано)
3. ❌ Создать UI для входа и регистрации
4. ❌ Создать экран библиотеки
5. ❌ Реализовать импорт EPUB файлов
6. ❌ Создать простую читалку

### Средний приоритет
7. ❌ Добавить закладки
8. ❌ Реализовать базовую синхронизацию
9. ❌ Добавить настройки читалки (темы, шрифты)

### Низкий приоритет
10. ❌ Статистика чтения
11. ❌ Выделения текста
12. ❌ PDF поддержка
13. ❌ Backend сервер

## 🔧 Команды для разработки

### Запуск приложения
```bash
./gradlew installDebug
```

### Сборка APK
```bash
./gradlew assembleDebug
```

### Запуск тестов
```bash
./gradlew test
```

### Очистка проекта
```bash
./gradlew clean
```

## 📚 Полезные ресурсы

- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)

## 🐛 Известные проблемы

1. EPUB парсер не реализован - нужна библиотека или custom парсер
2. Backend API еще не существует - нужно создать Node.js сервер
3. UI экраны пока не созданы

## 📝 Заметки

- Проект использует Kotlin DSL для Gradle
- Архитектура: MVVM + Clean Architecture
- Offline-first подход - все работает локально
- База данных автоматически создастся при первом запуске
- Для парсинга EPUB можно использовать библиотеку [epublib](https://github.com/psiegman/epublib)

## ✨ Готово к работе!

Базовая структура проекта готова. Следуйте шагам выше для завершения разработки.

**Следующее действие**: Синхронизируйте Gradle и начните создавать UI экраны! 🚀
