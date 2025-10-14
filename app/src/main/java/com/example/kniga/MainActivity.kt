package com.example.kniga

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.kniga.data.preferences.UserPreferences
import com.example.kniga.presentation.ui.auth.LoginActivity
import com.example.kniga.presentation.ui.library.LibraryActivity
import com.example.kniga.ui.theme.KnigaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        userPreferences = UserPreferences(this)
        
        setContent {
            KnigaTheme {
                SplashScreen(
                    onNavigate = { isLoggedIn ->
                        val intent = if (isLoggedIn) {
                            Intent(this, LibraryActivity::class.java)
                        } else {
                            Intent(this, LoginActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onNavigate: (Boolean) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            delay(1500)
            val isLoggedIn = userPreferences.isLoggedIn.first()
            onNavigate(isLoggedIn)
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📚",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 120.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Kniga",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Читай где угодно, продолжай на любом устройстве",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator()
        }
    }
}