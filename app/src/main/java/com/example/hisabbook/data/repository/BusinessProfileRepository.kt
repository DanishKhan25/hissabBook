package com.example.hisabbook.data.repository

import com.example.hisabbook.data.local.dao.BusinessProfileDao
import com.example.hisabbook.data.local.entity.BusinessProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BusinessProfileRepository(
    private val businessProfileDao: BusinessProfileDao
) {
    val businessProfile: Flow<BusinessProfile?> = businessProfileDao.getBusinessProfile()

    suspend fun getBusinessProfileOneShot(): BusinessProfile? = withContext(Dispatchers.IO) {
        businessProfileDao.getBusinessProfileOneShot()
    }

    suspend fun ensureDefaultProfile(): BusinessProfile = withContext(Dispatchers.IO) {
        var profile = businessProfileDao.getBusinessProfileOneShot()
        if (profile == null) {
            profile = BusinessProfile(
                businessName = "IMAGE ALU MINI U M AND GLASS",
                subtitle = "Fabrication, Sliding Windows & Toughened Glass Works",
                languagePreference = "en",
                isOnboardingCompleted = false
            )
            businessProfileDao.insertOrUpdateBusinessProfile(profile)
        }
        profile
    }

    suspend fun updateBusinessProfile(profile: BusinessProfile) = withContext(Dispatchers.IO) {
        businessProfileDao.insertOrUpdateBusinessProfile(profile.copy(id = 1))
    }
}
