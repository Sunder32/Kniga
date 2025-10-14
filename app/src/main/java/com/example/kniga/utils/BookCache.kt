package com.example.kniga.utils

import java.util.concurrent.ConcurrentHashMap

object BookCache {
    private val cache = ConcurrentHashMap<String, BookContent>()
    
    fun put(key: String, content: BookContent) {
        cache[key] = content
    }
    
    fun get(key: String): BookContent? {
        return cache[key]
    }
    
    fun clear() {
        cache.clear()
    }
    
    fun remove(key: String) {
        cache.remove(key)
    }
    
    fun getCacheKey(filePath: String, format: String): String {
        return "$filePath|$format"
    }
}
