package com.example.kniga.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Утилиты для работы с датой и временем
 */
object DateUtils {
    
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val TIME_FORMAT = "HH:mm"
    
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault())
    private val timeFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
    
    /**
     * Получить текущую дату в формате yyyy-MM-dd
     */
    fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }
    
    /**
     * Получить дату в формате yyyy-MM-dd
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * Получить дату и время в формате yyyy-MM-dd HH:mm:ss
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }
    
    /**
     * Получить время в формате HH:mm
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
    
    /**
     * Форматирование времени чтения (например: "2 ч 30 мин")
     */
    fun formatReadingTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        
        return when {
            hours > 0 && minutes > 0 -> "$hours ч $minutes мин"
            hours > 0 -> "$hours ч"
            minutes > 0 -> "$minutes мин"
            else -> "${seconds} сек"
        }
    }
    
    /**
     * Относительное время (например: "2 часа назад", "вчера", "3 дня назад")
     */
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            seconds < 60 -> "только что"
            minutes < 60 -> "$minutes мин назад"
            hours < 24 -> "$hours ч назад"
            days == 1L -> "вчера"
            days < 7 -> "$days дн назад"
            days < 30 -> "${days / 7} нед назад"
            days < 365 -> "${days / 30} мес назад"
            else -> "${days / 365} г назад"
        }
    }
    
    /**
     * Получить начало дня
     */
    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    /**
     * Получить конец дня
     */
    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    /**
     * Получить дату N дней назад
     */
    fun getDaysAgo(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return dateFormat.format(calendar.time)
    }
    
    /**
     * Разница в днях между двумя датами
     */
    fun daysBetween(start: Long, end: Long): Int {
        val diff = end - start
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }
}
