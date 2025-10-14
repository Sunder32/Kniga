package com.example.kniga.presentation.ui.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kniga.ui.theme.KnigaTheme
import kotlinx.coroutines.launch

class ReaderActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val bookId = intent.getLongExtra("book_id", -1)
        
        setContent {
            KnigaTheme {
                ReaderScreen(
                    bookId = bookId,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: Long,
    onBackClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { com.example.kniga.data.local.AppDatabase.getDatabase(context) }
    val bookRepository = remember { com.example.kniga.data.repository.BookRepository(database.bookDao()) }
    val scope = rememberCoroutineScope()
    
    var showControls by remember { mutableStateOf(true) }
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(100) }
    var fontSize by remember { mutableStateOf(18.sp) }
    var backgroundColor by remember { mutableStateOf(Color.White) }
    var textColor by remember { mutableStateOf(Color.Black) }
    var showSettings by remember { mutableStateOf(false) }
    var bookTitle by remember { mutableStateOf("Книга") }
    var bookContent by remember { mutableStateOf<com.example.kniga.utils.BookContent?>(null) }
    var currentChapterIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(bookId) {
        isLoading = true
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            bookRepository.getBookById(bookId).collect { book ->
                book?.let {
                    bookTitle = it.title
                    totalPages = it.totalPages.coerceAtLeast(1)
                    currentPage = it.currentPage.coerceIn(1, totalPages)
                    
                    if (bookContent == null) {
                        bookContent = com.example.kniga.utils.BookParser.parseBook(
                            context = context,
                            filePath = it.filePath,
                            format = it.format
                        )
                        
                        totalPages = bookContent?.chapters?.size ?: 1
                        currentPage = it.currentPage.coerceIn(1, totalPages)
                        currentChapterIndex = (currentPage - 1).coerceIn(0, (bookContent?.chapters?.size ?: 1) - 1)
                    }
                    
                    isLoading = false
                }
            }
        }
    }
    
    LaunchedEffect(currentPage) {
        if (currentPage > 0 && !isLoading) {
            scope.launch {
                bookRepository.updatePageAndProgress(bookId, currentPage, totalPages)
            }
        }
        currentChapterIndex = (currentPage - 1).coerceIn(0, (bookContent?.chapters?.size ?: 1) - 1)
    }
    
    val currentChapter = bookContent?.chapters?.getOrNull(currentChapterIndex)
    val bookText = if (isLoading) "Загрузка книги..." else (currentChapter?.content ?: "")
    
    val scrollState = rememberScrollState()
    
    LaunchedEffect(currentPage) {
        scrollState.scrollTo(0)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable { showControls = !showControls }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (showControls) 70.dp else 16.dp,
                    bottom = if (showControls) 140.dp else 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
                .verticalScroll(scrollState)
        ) {
            Text(
                text = bookText,
                fontSize = fontSize,
                lineHeight = fontSize * 1.6f,
                textAlign = TextAlign.Justify,
                color = textColor
            )
        }
        
        // Верхняя панель
        if (showControls) {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            bookTitle,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 16.sp
                        )
                        currentChapter?.let {
                            if (it.title.isNotBlank()) {
                                Text(
                                    it.title,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Text("📑", fontSize = 20.sp)
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Text("🔖", fontSize = 20.sp)
                    }
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Text("⚙️", fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                )
            )
        }
        
        // Нижняя панель
        if (showControls) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Прогресс
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Страница $currentPage",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${(currentPage * 100 / totalPages)}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { currentPage.toFloat() / totalPages.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Навигация
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { 
                                if (currentPage > 1) currentPage--
                            },
                            enabled = currentPage > 1,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("← Назад", fontSize = 16.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "$currentPage / $totalPages",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Button(
                            onClick = { 
                                if (currentPage < totalPages) currentPage++
                            },
                            enabled = currentPage < totalPages,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Вперед →", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
        
        // Диалог настроек
        if (showSettings) {
            ReaderSettingsDialog(
                currentFontSize = fontSize.value,
                onFontSizeChange = { fontSize = it.sp },
                onThemeChange = { bg, text ->
                    backgroundColor = bg
                    textColor = text
                },
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
fun ReaderSettingsDialog(
    currentFontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    onThemeChange: (Color, Color) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Настройки читалки",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column {
                Text(
                    "Размер шрифта",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = currentFontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 12f..32f,
                    steps = 19
                )
                
                Text(
                    text = "${currentFontSize.toInt()} sp",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Цветовая тема",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onThemeChange(Color.White, Color.Black) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Светлая")
                    }
                    Button(
                        onClick = { onThemeChange(Color(0xFF1E1E1E), Color(0xFFE0E0E0)) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E1E1E),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Темная")
                    }
                    Button(
                        onClick = { onThemeChange(Color(0xFFF4ECD8), Color(0xFF5C4033)) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF4ECD8),
                            contentColor = Color(0xFF5C4033)
                        )
                    ) {
                        Text("Сепия")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Применить")
            }

        }
    )
}
