package com.example.hisabbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "IMAGE ALUMINIUM AND GLASS",
    val subtitle: String = "Fabrication, Sliding Windows & Toughened Glass Works",
    val description: String = "",
    val phone: String = "8999127482",
    val address: String = "",
    val languagePreference: String = "en", // "en" or "hi"
    val currencySymbol: String = "₹",
    val gstNumber: String = "",
    val isOnboardingCompleted: Boolean = false
)
