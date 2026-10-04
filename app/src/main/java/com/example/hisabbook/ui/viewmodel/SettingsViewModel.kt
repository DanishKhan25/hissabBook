package com.example.hisabbook.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hisabbook.data.local.AppDatabase
import com.example.hisabbook.data.local.BackupManager
import com.example.hisabbook.data.local.DemoDataSeeder
import com.example.hisabbook.data.local.entity.BusinessProfile
import com.example.hisabbook.data.repository.BusinessProfileRepository
import com.example.hisabbook.util.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val businessProfileRepository: BusinessProfileRepository,
    private val database: AppDatabase
) : ViewModel() {

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            businessProfileRepository.ensureDefaultProfile()
        }
    }

    val businessProfile: StateFlow<BusinessProfile?> = businessProfileRepository.businessProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun saveBusinessProfile(
        businessName: String,
        subtitle: String,
        description: String,
        phone: String,
        address: String,
        gstNumber: String,
        languagePreference: String,
        businessType: String = "GENERAL",
        onSuccess: () -> Unit = {}
    ) {
        if (businessName.isBlank()) {
            _userMessage.value = "Business name cannot be empty"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val current = businessProfile.value ?: BusinessProfile()
            val updated = current.copy(
                businessName = businessName.trim(),
                subtitle = subtitle.trim(),
                description = description.trim(),
                phone = phone.trim(),
                address = address.trim(),
                gstNumber = gstNumber.trim(),
                languagePreference = languagePreference,
                businessType = businessType,
                isOnboardingCompleted = true
            )
            businessProfileRepository.updateBusinessProfile(updated)

            // Seed preset items for this business type if inventory is currently empty
            val existingItems = database.inventoryDao().getAllInventoryItemsOneShot()
            if (existingItems.isEmpty()) {
                val presets = DemoDataSeeder.getDemoInventoryItemsForCategory(businessType)
                presets.forEach { item ->
                    database.inventoryDao().insertInventoryItem(item)
                }
            }

            _userMessage.value = "Business profile updated successfully!"
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun setLanguage(context: Context, languageCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = businessProfile.value ?: BusinessProfile()
            val updated = current.copy(
                languagePreference = languageCode,
                isOnboardingCompleted = true
            )
            businessProfileRepository.updateBusinessProfile(updated)
            withContext(Dispatchers.Main) {
                LocaleHelper.applyLocale(context, languageCode)
            }
            _userMessage.value = if (languageCode == "hi") "भाषा हिन्दी पर सेट की गई" else "Language set to English"
        }
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = BackupManager.exportBackupToJson(database)
                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw IllegalStateException("Unable to open output stream for selected URI")
                outputStream.use { stream ->
                    stream.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                _userMessage.value = "Backup exported successfully!"
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Failed to export backup: ${e.localizedMessage}"
            }
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Unable to open selected file stream")
                val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                    reader.readText()
                }

                if (jsonString.isBlank()) {
                    _userMessage.value = "Selected backup file is empty"
                    return@launch
                }

                BackupManager.importBackupFromJson(database, jsonString)

                val updatedProfile = businessProfileRepository.getBusinessProfileOneShot()
                withContext(Dispatchers.Main) {
                    updatedProfile?.languagePreference?.let { lang ->
                        LocaleHelper.applyLocale(context, lang)
                    }
                }
                _userMessage.value = "Database restored successfully!"
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Failed to restore backup: ${e.localizedMessage ?: "Invalid file"}"
            }
        }
    }

    fun loadDemoData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                DemoDataSeeder.seedDemoData(database, force = true)
                _userMessage.value = "Demo data loaded successfully!"
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Failed to load demo data: ${e.localizedMessage}"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
