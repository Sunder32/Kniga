package com.example.kniga.presentation.ui.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kniga.data.local.AppDatabase
import com.example.kniga.data.local.entity.Book
import com.example.kniga.data.repository.BookRepository
import com.example.kniga.data.service.BookImportService
import com.example.kniga.presentation.ui.reader.ReaderActivity
import com.example.kniga.presentation.ui.settings.SettingsActivity
import com.example.kniga.presentation.ui.statistics.StatisticsActivity
import com.example.kniga.ui.theme.KnigaTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LibraryActivity : ComponentActivity() {
    
    private lateinit var bookRepository: BookRepository
    private lateinit var bookImportService: BookImportService
    
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
        
        lifecycleScope.launch {
            val booksCount = bookRepository.getBooksCount()
            if (booksCount == 0) {
                com.example.kniga.utils.MockData.populateDatabase(bookRepository)
            }
        }
        
        setContent {
            KnigaTheme {
                LibraryScreen(
                    bookRepository = bookRepository,
                    onBookClick = { book ->
                        val intent = Intent(this, ReaderActivity::class.java)
                        intent.putExtra("book_id", book.id)
                        startActivity(intent)
                    },
                    onAddBookClick = {
                        filePickerLauncher.launch("*/*")
                    },
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onStatisticsClick = {
                        startActivity(Intent(this, StatisticsActivity::class.java))
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
    onBookClick: (Book) -> Unit,
    onAddBookClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatisticsClick: () -> Unit
) {
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()
    
    // Загрузка книг
    LaunchedEffect(selectedFilter) {
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
                    icon = { Text("📊", fontSize = 24.sp) },
                    label = { Text("Статистика") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        onStatisticsClick()
                    }
                )
                NavigationBarItem(
                    icon = { Text("⚙️", fontSize = 24.sp) },
                    label = { Text("Настройки") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
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
            // Фильтры (чипсы)
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
            
            // Список книг
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
                    onBookClick = onBookClick
                )
            }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == "all",
            onClick = { onFilterSelected("all") },
            label = { Text("Все книги") }
        )
        FilterChip(
            selected = selectedFilter == "reading",
            onClick = { onFilterSelected("reading") },
            label = { Text("Читаю сейчас") }
        )
        FilterChip(
            selected = selectedFilter == "completed",
            onClick = { onFilterSelected("completed") },
            label = { Text("Прочитано") }
        )
        FilterChip(
            selected = selectedFilter == "not_started",
            onClick = { onFilterSelected("not_started") },
            label = { Text("Хочу прочитать") }
        )
    }
}

@Composable
fun BookGrid(
    books: List<Book>,
    onBookClick: (Book) -> Unit
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
                onClick = { onBookClick(book) }
            )
        }
    }
}

@Composable
fun BookCard(
    book: Book,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable(onClick = onClick)
            .shadow(4.dp, shape = MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Обложка
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📖",
                        fontSize = 64.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Название
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Автор
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Прогресс
            if (book.progress > 0) {
                Column {
                    LinearProgressIndicator(
                        progress = { book.progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${book.progress.toInt()}% прочитано",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            } else {
                Text(
                    text = "Новая книга",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
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




