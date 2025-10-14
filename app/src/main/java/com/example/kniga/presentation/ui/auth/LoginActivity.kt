package com.example.kniga.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kniga.data.preferences.UserPreferences
import com.example.kniga.presentation.ui.library.LibraryActivity
import com.example.kniga.ui.theme.KnigaTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    
    private lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        userPreferences = UserPreferences(this)
        
        setContent {
            KnigaTheme {
                LoginScreen(
                    onLoginSuccess = { email, name ->
                        lifecycleScope.launch {
                            userPreferences.login(1L, email, name)
                            startActivity(Intent(this@LoginActivity, LibraryActivity::class.java))
                            finish()
                        }
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вход") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📚",
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Book Reader",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Text("✉️") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Пароль
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                leadingIcon = { Text("🔒") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "🙈" else "👁️")
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None 
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null
            )
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Забыли пароль
            TextButton(
                onClick = { /* TODO */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Забыли пароль?")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка входа
            Button(
                onClick = {
                    scope.launch {
                        if (validateInput(email, password)) {
                            isLoading = true
                            kotlinx.coroutines.delay(1000)
                            isLoading = false
                            onLoginSuccess(email, "Пользователь")
                        } else {
                            errorMessage = "Проверьте правильность введенных данных"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Войти")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Регистрация
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Нет аккаунта?")
                TextButton(onClick = onRegisterClick) {
                    Text("Зарегистрироваться")
                }
            }
        }
    }
}

private fun validateInput(email: String, password: String): Boolean {
    return email.isNotBlank() && 
           email.contains("@") && 
           password.length >= 8
}
