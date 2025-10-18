package com.example.kniga.presentation.ui.statistics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.kniga.data.local.AppDatabase
import com.example.kniga.data.repository.BookRepository
import com.example.kniga.data.repository.ReadingSessionRepository
import com.example.kniga.ui.theme.KnigaTheme
import kotlinx.coroutines.launch

class StatisticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val bookRepository = BookRepository(database.bookDao())
        val readingSessionRepository = ReadingSessionRepository(database.readingSessionDao())
        
        setContent {
            KnigaTheme {
                StatisticsScreen(
                    bookRepository = bookRepository,
                    readingSessionRepository = readingSessionRepository,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    bookRepository: BookRepository,
    readingSessionRepository: ReadingSessionRepository,
    onBackClick: () -> Unit
) {
    var completedBooks by remember { mutableStateOf(0) }
    var readingBooks by remember { mutableStateOf(0) }
    var totalPages by remember { mutableStateOf(0) }
    var totalReadingTime by remember { mutableStateOf(0L) }
    var readingStreak by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        // Получаем статистику из базы данных
        bookRepository.getCompletedBooks().collect { books ->
            completedBooks = books.size
        }
    }
    
    LaunchedEffect(Unit) {
        bookRepository.getReadingBooks().collect { books ->
            readingBooks = books.size
        }
    }
    
    LaunchedEffect(Unit) {
        bookRepository.getAllBooks().collect { books ->
            totalPages = books.filter { it.status == "COMPLETED" }.sumOf { it.totalPages }
        }
    }
    
    LaunchedEffect(Unit) {
        readingSessionRepository.getTotalReadingTime().collect { time ->
            totalReadingTime = time
        }
    }
    
    LaunchedEffect(Unit) {
        readingSessionRepository.getCurrentStreak().collect { streak ->
            readingStreak = streak
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика чтения", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Общая статистика - серия чтения
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎯",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Серия чтения",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$readingStreak ${if (readingStreak == 1) "день" else if (readingStreak in 2..4) "дня" else "дней"} подряд",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Статистика в карточках
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Прочитано",
                    value = "$completedBooks",
                    subtitle = when (completedBooks % 10) {
                        1 -> if (completedBooks % 100 != 11) "книга" else "книг"
                        in 2..4 -> if (completedBooks % 100 !in 12..14) "книги" else "книг"
                        else -> "книг"
                    },
                    emoji = "📚",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "В процессе",
                    value = "$readingBooks",
                    subtitle = when (readingBooks % 10) {
                        1 -> if (readingBooks % 100 != 11) "книга" else "книг"
                        in 2..4 -> if (readingBooks % 100 !in 12..14) "книги" else "книг"
                        else -> "книг"
                    },
                    emoji = "📖",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Время чтения",
                    value = "${totalReadingTime / 3600000}ч ${(totalReadingTime % 3600000) / 60000}м",
                    subtitle = "всего",
                    emoji = "⏱️",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Страниц",
                    value = "$totalPages",
                    subtitle = "прочитано",
                    emoji = "📄",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Скорость чтения
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = "Скорость чтения",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "245 слов в минуту",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // Цели
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Цель на месяц",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = "3 книги прочитано из 5",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LinearProgressIndicator(
                        progress = { 0.6f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Text(
                        text = "60% выполнено",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Достижения
            Text(
                text = "Достижения",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AchievementCard("🏆", "Первая книга")
                AchievementCard("📚", "10 книг")
                AchievementCard("🔥", "Неделя подряд")
                AchievementCard("⭐", "100 часов")
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun RowScope.AchievementCard(
    emoji: String,
    title: String
) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
