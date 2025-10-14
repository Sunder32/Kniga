package com.example.kniga.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = longPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val FONT_SIZE = stringPreferencesKey("font_size")
        private val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
    }
    
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }
    
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }
    
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }
    
    suspend fun login(userId: Long, email: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = userId
            preferences[USER_EMAIL] = email
            preferences[USER_NAME] = name
        }
    }
    
    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }
    
    val darkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: false
    }
}
