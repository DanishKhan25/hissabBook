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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingsViewModel(
    private val businessProfileRepository: BusinessProfileRepository,
    private val database: AppDatabase
) : ViewModel() {

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
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
        onSuccess: () -> Unit = {}
    ) {
        if (businessName.isBlank()) {
            _userMessage.value = "Business name cannot be empty"
            return
        }

        viewModelScope.launch {
            val current = businessProfile.value ?: BusinessProfile()
            val updated = current.copy(
                businessName = businessName.trim(),
                subtitle = subtitle.trim(),
                description = description.trim(),
                phone = phone.trim(),
                address = address.trim(),
                gstNumber = gstNumber.trim(),
                languagePreference = languagePreference,
                isOnboardingCompleted = true
            )
            businessProfileRepository.updateBusinessProfile(updated)
            _userMessage.value = "Business profile updated successfully!"
            onSuccess()
        }
    }

    fun setLanguage(context: Context, languageCode: String) {
        viewModelScope.launch {
            val current = businessProfile.value ?: BusinessProfile()
            val updated = current.copy(
                languagePreference = languageCode,
                isOnboardingCompleted = true
            )
            businessProfileRepository.updateBusinessProfile(updated)
            LocaleHelper.applyLocale(context, languageCode)
            _userMessage.value = if (languageCode == "hi") "भाषा हिन्दी पर सेट की गई" else "Language set to English"
        }
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonString = BackupManager.exportBackupToJson(database)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                _userMessage.value = "Backup exported successfully!"
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Failed to export backup: ${e.localizedMessage}"
            }
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val stringBuilder = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            stringBuilder.append(line)
                            line = reader.readLine()
                        }
                    }
                }
                val jsonString = stringBuilder.toString()
                if (jsonString.isBlank()) {
                    _userMessage.value = "Selected backup file is empty"
                    return@launch
                }

                val success = BackupManager.importBackupFromJson(database, jsonString)
                if (success) {
                    val updatedProfile = businessProfileRepository.getBusinessProfileOneShot()
                    updatedProfile?.languagePreference?.let { lang ->
                        LocaleHelper.applyLocale(context, lang)
                    }
                    _userMessage.value = "Database restored successfully!"
                } else {
                    _userMessage.value = "Failed to restore backup: Invalid JSON file"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Error importing backup: ${e.localizedMessage}"
            }
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
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
