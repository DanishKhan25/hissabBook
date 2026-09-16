package com.example.hisabbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "Business Name",
    val subtitle: String = "Subtitle",
    val description: String = "",
    val phone: String = "9999999999",
    val address: String = "",
    val languagePreference: String = "en", // "en" or "hi"
    val currencySymbol: String = "₹",
    val gstNumber: String = "",
    val isOnboardingCompleted: Boolean = false
)
