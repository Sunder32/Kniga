package com.example.kniga.presentation.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import com.example.kniga.ui.theme.KnigaTheme
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            KnigaTheme {
                RegisterScreen(
                    onRegisterSuccess = {
                        Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать аккаунт") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "📚",
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Присоединяйтесь к Book Reader",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    errorMessage = null
                },
                label = { Text("Email") },
                leadingIcon = { Text("✉️") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Имя пользователя
            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it
                    errorMessage = null
                },
                label = { Text("Имя пользователя") },
                leadingIcon = { Text("👤") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Пароль
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = null
                },
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
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Подтверждение пароля
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Подтвердите пароль") },
                leadingIcon = { Text("🔒") },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Text(if (confirmPasswordVisible) "🙈" else "👁️")
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None 
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Кнопка регистрации
            Button(
                onClick = {
                    scope.launch {
                        val validation = validateRegistration(email, username, password, confirmPassword)
                        if (validation == null) {
                            isLoading = true
                            // TODO: Реальная регистрация
                            kotlinx.coroutines.delay(1000)
                            isLoading = false
                            onRegisterSuccess()
                        } else {
                            errorMessage = validation
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
                    Text("Зарегистрироваться")
                }
            }
        }
    }
}

private fun validateRegistration(
    email: String, 
    username: String, 
    password: String, 
    confirmPassword: String
): String? {
    return when {
        email.isBlank() -> "Введите email"
        !email.contains("@") -> "Неверный формат email"
        username.isBlank() -> "Введите имя пользователя"
        password.length < 8 -> "Пароль должен содержать минимум 8 символов"
        password != confirmPassword -> "Пароли не совпадают"
        else -> null
    }
}
