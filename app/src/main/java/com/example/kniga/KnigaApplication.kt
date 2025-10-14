package com.example.kniga

import android.app.Application
import com.example.kniga.data.local.AppDatabase
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class KnigaApplication : Application() {
    
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        
        PDFBoxResourceLoader.init(applicationContext)
        
        instance = this
    }
    
    companion object {
        @Volatile
        private var instance: KnigaApplication? = null
        
        fun getInstance(): KnigaApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
}
