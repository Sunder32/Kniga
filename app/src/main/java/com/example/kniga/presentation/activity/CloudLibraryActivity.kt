package com.example.kniga.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.kniga.data.local.AppDatabase
import com.example.kniga.data.remote.RetrofitClient
import com.example.kniga.data.remote.dto.BookDto
import com.example.kniga.data.repository.CloudSyncRepository
import com.example.kniga.ui.theme.KnigaTheme
import com.example.kniga.presentation.viewmodel.CloudLibraryViewModel
import com.example.kniga.presentation.viewmodel.CloudLibraryViewModelFactory
import kotlinx.coroutines.launch

class CloudLibraryActivity : ComponentActivity() {
    
    private lateinit var viewModel: CloudLibraryViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(applicationContext)
        val cloudSyncRepository = CloudSyncRepository(
            RetrofitClient.apiService,
            database.bookDao(),
            database.cloudBookDao(),
            database.userDao(),
            applicationContext
        )
        
        viewModel = ViewModelProvider(
            this,
            CloudLibraryViewModelFactory(cloudSyncRepository)
        )[CloudLibraryViewModel::class.java]
        
        setContent {
            KnigaTheme {
                CloudLibraryScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudLibraryScreen(viewModel: CloudLibraryViewModel) {
    val cloudBooks by viewModel.cloudBooks.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.loadCloudBooks()
    }
    
    LaunchedEffect(downloadState) {
        when (downloadState) {
            is CloudLibraryViewModel.DownloadState.Success -> {
                Toast.makeText(context, "Книга скачана", Toast.LENGTH_SHORT).show()
                viewModel.resetDownloadState()
            }
            is CloudLibraryViewModel.DownloadState.Error -> {
                val message = (downloadState as CloudLibraryViewModel.DownloadState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.resetDownloadState()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("☁️ Облачная библиотека") },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                cloudBooks.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "📚",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Облако пустое",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Загрузите книги из библиотеки",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(cloudBooks) { bookDto ->
                            CloudBookCard(
                                bookDto = bookDto,
                                isDownloading = downloadState is CloudLibraryViewModel.DownloadState.Downloading,
                                onDownload = { viewModel.downloadBook(bookDto.id) }
                            )
                        }
                    }
                }
            }
            
            if (downloadState is CloudLibraryViewModel.DownloadState.Downloading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Скачивание...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CloudBookCard(
    bookDto: BookDto,
    isDownloading: Boolean,
    onDownload: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
            .clickable(enabled = !isDownloading) {
                scale = 0.95f
                onDownload()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "📖",
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bookDto.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    bookDto.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "${bookDto.format.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${bookDto.fileSize / 1024 / 1024} МБ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${bookDto.totalPages} стр.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FilledTonalButton(
                onClick = onDownload,
                enabled = !isDownloading,
                modifier = Modifier.size(48.dp)
            ) {
                Text("⬇️", fontSize = 20.sp)
            }
        }
    }
    
    LaunchedEffect(scale) {
        if (scale != 1f) {
            kotlinx.coroutines.delay(100)
            scale = 1f
        }
    }
}
