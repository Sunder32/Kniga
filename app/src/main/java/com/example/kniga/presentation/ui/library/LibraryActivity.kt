package com.example.kniga.presentation.ui.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kniga.data.local.AppDatabase
import com.example.kniga.data.local.entity.Book
import com.example.kniga.data.repository.BookRepository
import com.example.kniga.data.repository.CloudSyncRepository
import com.example.kniga.data.service.BookImportService
import com.example.kniga.data.remote.RetrofitClient
import com.example.kniga.data.remote.dto.BookDto
import com.example.kniga.presentation.ui.reader.ReaderActivity
import com.example.kniga.presentation.ui.settings.SettingsActivity
import com.example.kniga.presentation.ui.statistics.StatisticsActivity
import com.example.kniga.presentation.activity.CloudLibraryActivity
import com.example.kniga.ui.theme.KnigaTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class LibraryActivity : ComponentActivity() {
    
    private lateinit var bookRepository: BookRepository
    private lateinit var bookImportService: BookImportService
    private lateinit var cloudSyncRepository: CloudSyncRepository
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { importBook(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        bookRepository = BookRepository(database.bookDao())
        bookImportService = BookImportService(this, bookRepository)
        cloudSyncRepository = CloudSyncRepository(
            RetrofitClient.apiService,
            database.bookDao(),
            database.cloudBookDao(),
            database.userDao(),
            this
        )
        
        lifecycleScope.launch {
            // Создаем тестового пользователя при первом запуске
            com.example.kniga.utils.MockData.createMockUser(database.userDao())
        }
        
        setContent {
            KnigaTheme {
                LibraryScreen(
                    bookRepository = bookRepository,
                    cloudSyncRepository = cloudSyncRepository,
                    onBookClick = { book ->
                        val intent = Intent(this, ReaderActivity::class.java)
                        intent.putExtra("book_id", book.id)
                        startActivity(intent)
                    },
                    onBookDelete = { book ->
                        lifecycleScope.launch {
                            bookRepository.deleteBook(book)
                            Toast.makeText(
                                this@LibraryActivity,
                                "Книга \"${book.title}\" удалена",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onAddBookClick = {
                        filePickerLauncher.launch("*/*")
                    },
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onStatisticsClick = {
                        startActivity(Intent(this, StatisticsActivity::class.java))
                    },
                    onCloudBooksClick = {
                        startActivity(Intent(this, CloudLibraryActivity::class.java))
                    }
                )
            }
        }
    }
    
    private fun importBook(uri: Uri) {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@LibraryActivity, "Импорт книги...", Toast.LENGTH_SHORT).show()
                
                val result = bookImportService.importBook(uri)
                
                result.onSuccess { book ->
                    Toast.makeText(
                        this@LibraryActivity,
                        "Книга \"${book.title}\" добавлена",
                        Toast.LENGTH_LONG
                    ).show()
                }.onFailure { error ->
                    Toast.makeText(
                        this@LibraryActivity,
                        "Ошибка импорта: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LibraryActivity,
                    "Ошибка: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    bookRepository: BookRepository,
    cloudSyncRepository: CloudSyncRepository,
    onBookClick: (Book) -> Unit,
    onBookDelete: (Book) -> Unit,
    onAddBookClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onCloudBooksClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var cloudBooks by remember { mutableStateOf<List<com.example.kniga.data.local.entity.CloudBook>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var uploadingBooks by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var syncingBooks by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var downloadingBooks by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(selectedFilter, selectedTab) {
        if (selectedTab == 0) {
            val flow = when (selectedFilter) {
                "reading" -> bookRepository.getReadingBooks()
                "completed" -> bookRepository.getCompletedBooks()
                "not_started" -> bookRepository.getNotStartedBooks()
                "favorites" -> bookRepository.getFavoriteBooks()
                else -> bookRepository.getAllBooks()
            }
            
            flow.collectLatest { bookList ->
                books = bookList
            }
        }
    }
    
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            // Подписываемся на изменения облачных книг
            cloudSyncRepository.getAllCloudBooks().collectLatest { cloudBooksList ->
                cloudBooks = cloudBooksList
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (showSearch) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Поиск книг...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    } else {
                        Text("Моя библиотека", fontWeight = FontWeight.Bold) 
                    }
                },
                actions = {
                    if (!showSearch) {
                        IconButton(onClick = { showSearch = true }) {
                            Text("🔍", fontSize = 24.sp)
                        }
                        Box {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Text("⋮", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("☁️ Облачные книги") },
                                    onClick = {
                                        showMenu = false
                                        onCloudBooksClick()
                                    },
                                    leadingIcon = { Text("☁️") }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Избранное") },
                                    onClick = {
                                        showMenu = false
                                        selectedFilter = "favorites"
                                    },
                                    leadingIcon = { Text("⭐") }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Статистика") },
                                    onClick = {
                                        showMenu = false
                                        onStatisticsClick()
                                    },
                                    leadingIcon = { Text("📊") }
                                )
                                DropdownMenuItem(
                                    text = { Text("Настройки") },
                                    onClick = {
                                        showMenu = false
                                        onSettingsClick()
                                    },
                                    leadingIcon = { Text("⚙️") }
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = { 
                            showSearch = false
                            searchQuery = ""
                        }) {
                            Text("✕", fontSize = 24.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                NavigationBarItem(
                    icon = { Text("📚", fontSize = 24.sp) },
                    label = { Text("Библиотека") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Text("☁️", fontSize = 24.sp) },
                    label = { Text("Облако") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Text("📊", fontSize = 24.sp) },
                    label = { Text("Статистика") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onStatisticsClick()
                    }
                )
                NavigationBarItem(
                    icon = { Text("⚙️", fontSize = 24.sp) },
                    label = { Text("Настройки") },
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        onSettingsClick()
                    }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddBookClick,
                icon = { Text("➕", fontSize = 20.sp) },
                text = { Text("Добавить книгу") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    FilterChips(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it }
                    )
                    
                    if (books.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyLibraryView()
                        }
                    } else {
                        BookGrid(
                            books = books,
                            uploadingBooks = uploadingBooks,
                            syncingBooks = syncingBooks,
                            onBookClick = onBookClick,
                            onBookLongClick = { book ->
                                bookToDelete = book
                                showDeleteDialog = true
                            },
                            onFavoriteClick = { book ->
                                scope.launch {
                                    bookRepository.updateBook(book.copy(isFavorite = !book.isFavorite))
                                    Toast.makeText(
                                        context,
                                        if (!book.isFavorite) "❤️ Добавлено в избранное" else "💔 Удалено из избранного",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onUploadClick = { book ->
                                if (!uploadingBooks.contains(book.id)) {
                                    uploadingBooks = uploadingBooks + book.id
                                    Toast.makeText(context, "📤 Загрузка книги \"${book.title}\" в облако...", Toast.LENGTH_SHORT).show()
                                    scope.launch {
                                        val result = cloudSyncRepository.uploadBookToCloud(book)
                                        uploadingBooks = uploadingBooks - book.id
                                        
                                        result.onSuccess { cloudBook ->
                                            Toast.makeText(
                                                context, 
                                                "✅ Книга \"${book.title}\" загружена в облако!\nТеперь её видят все пользователи",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }.onFailure { error ->
                                            Toast.makeText(
                                                context,
                                                "❌ ${error.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            },
                            onSyncClick = { book ->
                                // Синхронизация прогресса отключена в новой системе облака
                                // Облако теперь только для обмена книгами между пользователями
                                Toast.makeText(
                                    context,
                                    "ℹ️ Синхронизация прогресса временно недоступна",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
                1 -> {
                    CloudBooksTab(
                        cloudBooks = cloudBooks,
                        downloadingBooks = downloadingBooks,
                        cloudSyncRepository = cloudSyncRepository,
                        context = context,
                        onDownloadClick = { cloudBook ->
                            downloadingBooks = downloadingBooks + cloudBook.cloudId
                            scope.launch {
                                Toast.makeText(context, "📥 Скачивание \"${cloudBook.title}\"...", Toast.LENGTH_SHORT).show()
                                val result = cloudSyncRepository.downloadCloudBook(cloudBook)
                                downloadingBooks = downloadingBooks - cloudBook.cloudId
                                
                                result.onSuccess {
                                    Toast.makeText(context, "✅ Книга \"${cloudBook.title}\" скачана", Toast.LENGTH_SHORT).show()
                                }.onFailure { error ->
                                    Toast.makeText(context, "❌ ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onDeleteClick = { cloudBook ->
                            scope.launch {
                                Toast.makeText(context, "🗑️ Удаление \"${cloudBook.title}\"...", Toast.LENGTH_SHORT).show()
                                val result = cloudSyncRepository.deleteCloudBook(cloudBook)
                                
                                result.onSuccess {
                                    Toast.makeText(context, "✅ Книга удалена из облака", Toast.LENGTH_SHORT).show()
                                }.onFailure { error ->
                                    Toast.makeText(context, "❌ ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
            }
        }
        
        if (showDeleteDialog && bookToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Удалить книгу?") },
                text = { Text("Вы уверены, что хотите удалить \"${bookToDelete!!.title}\"?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onBookDelete(bookToDelete!!)
                            showDeleteDialog = false
                            bookToDelete = null
                        }
                    ) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun FilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf(
            "all" to "📚 Все",
            "reading" to "📖 Читаю",
            "completed" to "✅ Готово"
        )
        
        filters.forEach { (id, label) ->
            FilterChip(
                selected = selectedFilter == id,
                onClick = { onFilterSelected(id) },
                label = { 
                    Text(
                        label,
                        fontWeight = if (selectedFilter == id) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (selectedFilter == id) null else FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun BookGrid(
    books: List<Book>,
    uploadingBooks: Set<Long>,
    syncingBooks: Set<Long>,
    onBookClick: (Book) -> Unit,
    onBookLongClick: (Book) -> Unit,
    onUploadClick: (Book) -> Unit,
    onSyncClick: (Book) -> Unit,
    onFavoriteClick: (Book) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(books) { book ->
            BookCard(
                book = book,
                isUploading = uploadingBooks.contains(book.id),
                isSyncing = syncingBooks.contains(book.id),
                onClick = { onBookClick(book) },
                onLongClick = { onBookLongClick(book) },
                onUploadClick = { onUploadClick(book) },
                onSyncClick = { onSyncClick(book) },
                onFavoriteClick = { onFavoriteClick(book) }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun BookCard(
    book: Book,
    isUploading: Boolean,
    isSyncing: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onUploadClick: () -> Unit,
    onSyncClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                onClick = {
                    // Обычное нажатие - открыть книгу
                    onClick()
                },
                onLongClick = {
                    // Долгое нажатие - показать меню
                    isPressed = true
                    showMenu = true
                }
            )
            .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📖",
                            fontSize = 72.sp
                        )
                    }
                    
                    // Кнопка избранного
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(32.dp)
                            .align(Alignment.TopEnd)
                            .clickable { onFavoriteClick() },
                        shape = RoundedCornerShape(8.dp),
                        color = if (book.isFavorite) 
                            Color(0xFFFF6B6B).copy(alpha = 0.9f) 
                        else 
                            Color.White.copy(alpha = 0.7f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (book.isFavorite) "❤️" else "🤍",
                                fontSize = 18.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (book.progress > 0) {
                    Column {
                        LinearProgressIndicator(
                            progress = { book.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${book.progress.toInt()}% прочитано",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Новая книга",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Показываем индикатор загрузки/синхронизации
            if (isUploading || isSyncing) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { 
                    showMenu = false
                    isPressed = false
                }
            ) {
                DropdownMenuItem(
                    text = { Text("📖 Читать") },
                    onClick = {
                        showMenu = false
                        isPressed = false
                        onClick()
                    },
                    leadingIcon = { Text("📖", fontSize = 20.sp) }
                )
                
                Divider()
                
                if (book.cloudId == null) {
                    DropdownMenuItem(
                        text = { 
                            if (isUploading) {
                                Text("⬆️ Загрузка...")
                            } else {
                                Text("⬆️ Загрузить в облако")
                            }
                        },
                        onClick = {
                            if (!isUploading) {
                                showMenu = false
                                isPressed = false
                                onUploadClick()
                            }
                        },
                        leadingIcon = { Text("⬆️", fontSize = 20.sp) },
                        enabled = !isUploading
                    )
                } else {
                    DropdownMenuItem(
                        text = { 
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("☁️ В облаке")
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        },
                        onClick = {
                            if (!isSyncing) {
                                showMenu = false
                                isPressed = false
                                onSyncClick()
                            }
                        },
                        leadingIcon = { Text("🔄", fontSize = 20.sp) },
                        enabled = !isSyncing
                    )
                }
                
                Divider()
                
                DropdownMenuItem(
                    text = { Text("🗑️ Удалить", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMenu = false
                        isPressed = false
                        onLongClick()
                    },
                    leadingIcon = { Text("🗑️", fontSize = 20.sp) }
                )
            }
        }
    }
}

@Composable
fun EmptyLibraryView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = "📚",
            fontSize = 72.sp
        )
        Text(
            text = "Нет книг",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Добавьте первую книгу, нажав на кнопку \"+\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CloudBooksTab(
    cloudBooks: List<com.example.kniga.data.local.entity.CloudBook>,
    downloadingBooks: Set<String>,
    cloudSyncRepository: CloudSyncRepository,
    context: android.content.Context,
    onDownloadClick: (com.example.kniga.data.local.entity.CloudBook) -> Unit,
    onDeleteClick: (com.example.kniga.data.local.entity.CloudBook) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Информационный баннер
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("☁️", fontSize = 32.sp)
                Column {
                    Text(
                        "Облачная библиотека",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Общая база книг, видимая всем пользователям. Удалить может только автор загрузки.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        if (cloudBooks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text("☁️", fontSize = 72.sp)
                    Text(
                        "Облачная библиотека пуста",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Загрузите книги из своей библиотеки, чтобы другие пользователи могли их увидеть",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cloudBooks) { cloudBook ->
                    var canDelete by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(cloudBook.cloudId) {
                        canDelete = cloudSyncRepository.canDeleteCloudBook(cloudBook)
                    }
                    
                    CloudBookCard(
                        cloudBook = cloudBook,
                        isDownloading = downloadingBooks.contains(cloudBook.cloudId),
                        canDelete = canDelete,
                        onDownload = { onDownloadClick(cloudBook) },
                        onDelete = { onDeleteClick(cloudBook) }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CloudBookCard(
    cloudBook: com.example.kniga.data.local.entity.CloudBook,
    isDownloading: Boolean,
    canDelete: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
    
    val isDownloaded = cloudBook.localFilePath != null && File(cloudBook.localFilePath).exists()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                onClick = {
                    // Короткое нажатие - показать меню или скачать
                    if (!isDownloading) {
                        showMenu = true
                    }
                },
                onLongClick = {
                    // Длинное нажатие - открыть меню
                    showMenu = true
                }
            )
            .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Обложка
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📖", fontSize = 64.sp)
                    }
                    
                    // Бейджик статуса
                    if (isDownloaded) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "✓ Скачано",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Название
                Text(
                    text = cloudBook.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Автор
                Text(
                    text = cloudBook.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Информация о загрузившем
                Text(
                    text = "👤 ${cloudBook.uploaderUsername}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Статистика
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⬇️ ${cloudBook.downloadCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = cloudBook.format.uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Кнопка действия
                if (!isDownloaded) {
                    FilledTonalButton(
                        onClick = {
                            isPressed = true
                            onDownload()
                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(100)
                                isPressed = false
                            }
                        },
                        enabled = !isDownloading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Скачивание...")
                        } else {
                            Text("⬇️ Скачать", fontSize = 16.sp)
                        }
                    }
                }
            }
            
            // Dropdown меню
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                if (!isDownloaded) {
                    DropdownMenuItem(
                        text = { Text("⬇️ Скачать") },
                        onClick = {
                            showMenu = false
                            onDownload()
                        },
                        enabled = !isDownloading
                    )
                }
                
                if (canDelete) {
                    if (!isDownloaded) {
                        Divider()
                    }
                    DropdownMenuItem(
                        text = { Text("🗑️ Удалить из облака") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}




