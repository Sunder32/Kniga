package com.example.kniga.presentation.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
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
import com.example.kniga.MainActivity
import com.example.kniga.data.preferences.UserPreferences
import com.example.kniga.ui.theme.KnigaTheme
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    
    private lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        userPreferences = UserPreferences(this)
        
        setContent {
            KnigaTheme {
                SettingsScreen(
                    onBackClick = { finish() },
                    onLogout = {
                        lifecycleScope.launch {
                            userPreferences.logout()
                            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    var syncEnabled by remember { mutableStateOf(true) }
    var wifiOnly by remember { mutableStateOf(false) }
    var autoBackup by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки", fontWeight = FontWeight.Bold) },
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
        ) {
            // Синхронизация
            SettingsSection(title = "Синхронизация") {
                SettingsSwitch(
                    title = "Облачная синхронизация",
                    description = "Синхронизировать прогресс чтения между устройствами",
                    checked = syncEnabled,
                    onCheckedChange = { syncEnabled = it }
                )
                
                SettingsSwitch(
                    title = "Только по Wi-Fi",
                    description = "Синхронизировать только при подключении к Wi-Fi",
                    checked = wifiOnly,
                    onCheckedChange = { wifiOnly = it },
                    enabled = syncEnabled
                )
                
                SettingsSwitch(
                    title = "Автоматическое резервное копирование",
                    description = "Создавать резервные копии библиотеки",
                    checked = autoBackup,
                    onCheckedChange = { autoBackup = it }
                )
            }
            
            Divider()
            
            // Внешний вид
            SettingsSection(title = "Внешний вид") {
                SettingsSwitch(
                    title = "Темная тема",
                    description = "Использовать темное оформление",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }
            
            Divider()
            
            // Уведомления
            SettingsSection(title = "Уведомления") {
                SettingsSwitch(
                    title = "Уведомления",
                    description = "Напоминания о чтении и новых книгах",
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )
            }
            
            Divider()
            
            // О приложении
            SettingsSection(title = "О приложении") {
                SettingsItem(
                    title = "Версия",
                    description = "1.0.0"
                )
                
                SettingsItem(
                    title = "Хранилище",
                    description = "125 МБ из 500 МБ использовано"
                )
            }
            
            Divider()
            
            // Аккаунт
            SettingsSection(title = "Аккаунт") {
                SettingsButton(
                    title = "Выйти из аккаунта",
                    color = MaterialTheme.colorScheme.error
                ) {
                    showLogoutDialog = true
                }
            }
        }
        
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Выход из аккаунта") },
                text = { Text("Вы уверены, что хотите выйти? Все несинхронизированные данные могут быть потеряны.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Выйти")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsButton(
    title: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = color,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
