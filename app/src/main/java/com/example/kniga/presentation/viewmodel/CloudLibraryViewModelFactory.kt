package com.example.kniga.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kniga.data.repository.CloudSyncRepository

class CloudLibraryViewModelFactory(
    private val cloudSyncRepository: CloudSyncRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CloudLibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CloudLibraryViewModel(cloudSyncRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
