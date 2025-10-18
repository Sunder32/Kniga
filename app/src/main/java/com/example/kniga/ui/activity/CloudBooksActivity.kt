package com.example.kniga.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.kniga.data.local.AppDatabase
import com.example.kniga.data.remote.RetrofitClient
import com.example.kniga.data.remote.dto.BookDto
import com.example.kniga.data.repository.CloudSyncRepository
import kotlinx.coroutines.launch

class CloudBooksActivity : ComponentActivity() {
    
    private lateinit var cloudSyncRepository: CloudSyncRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        cloudSyncRepository = CloudSyncRepository(
            RetrofitClient.apiService,
            database.bookDao(),
            database.cloudBookDao(),
            database.userDao(),
            this
        )
        
        setContent {
            MaterialTheme {
                CloudBooksScreen()
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CloudBooksScreen() {
        var cloudBooks by remember { mutableStateOf<List<BookDto>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var downloadingBooks by remember { mutableStateOf<Set<String>>(emptySet()) }
        
        LaunchedEffect(Unit) {
            loadCloudBooks { books, error ->
                cloudBooks = books
                errorMessage = error
                isLoading = false
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("☁️ Облачные книги") },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Text("←", fontSize = 24.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "❌",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMessage ?: "Ошибка загрузки",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                isLoading = true
                                errorMessage = null
                                loadCloudBooks { books, error ->
                                    cloudBooks = books
                                    errorMessage = error
                                    isLoading = false
                                }
                            }) {
                                Text("🔄 Повторить")
                            }
                        }
                    }
                    cloudBooks.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📦",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Нет книг в облаке",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(cloudBooks) { book ->
                                CloudBookCard(
                                    book = book,
                                    isDownloading = downloadingBooks.contains(book.id),
                                    onDownload = {
                                        downloadingBooks = downloadingBooks + book.id
                                        lifecycleScope.launch {
                                            // Преобразуем BookDto обратно в CloudBook
                                            cloudSyncRepository.getAllCloudBooks().collect { cloudBooksList ->
                                                val cloudBook = cloudBooksList.find { it.cloudId == book.id }
                                                if (cloudBook != null) {
                                                    val result = cloudSyncRepository.downloadCloudBook(cloudBook)
                                                    downloadingBooks = downloadingBooks - book.id
                                                    result.onSuccess {
                                                        errorMessage = "✅ Книга скачана"
                                                    }.onFailure { e ->
                                                        errorMessage = "❌ ${e.message}"
                                                    }
                                                } else {
                                                    downloadingBooks = downloadingBooks - book.id
                                                    errorMessage = "❌ Книга не найдена"
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun CloudBookCard(
        book: BookDto,
        isDownloading: Boolean,
        onDownload: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✍️ ${book.author}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📄 ${book.format.uppercase()} • ${formatFileSize(book.fileSize)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        FilledTonalButton(
                            onClick = onDownload,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("⬇️ Скачать")
                        }
                    }
                }
            }
        }
    }
    
    private fun loadCloudBooks(callback: (List<BookDto>, String?) -> Unit) {
        lifecycleScope.launch {
            try {
                cloudSyncRepository.getAllCloudBooks().collect { cloudBooks ->
                    val bookDtos = cloudBooks.map { cloudBook ->
                        BookDto(
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
                    callback(bookDtos, null)
                }
            } catch (e: Exception) {
                callback(emptyList(), e.message)
            }
        }
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes Б"
            bytes < 1024 * 1024 -> "${bytes / 1024} КБ"
            else -> "${bytes / (1024 * 1024)} МБ"
        }
    }
}
