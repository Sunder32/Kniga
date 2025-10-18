package com.example.kniga.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kniga.data.local.dao.*
import com.example.kniga.data.local.entity.*

@Database(
    entities = [
        Book::class,
        ReadingProgress::class,
        Bookmark::class,
        Highlight::class,
        User::class,
        ReadingSession::class,
        SyncQueueItem::class,
        CloudBook::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun bookDao(): BookDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun highlightDao(): HighlightDao
    abstract fun userDao(): UserDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun cloudBookDao(): CloudBookDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val DATABASE_NAME = "kniga_database"
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // Для разработки, в продакшене нужны миграции
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        // Для тестирования
        fun getInMemoryDatabase(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
