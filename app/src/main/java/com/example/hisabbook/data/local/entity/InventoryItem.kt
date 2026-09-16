package com.example.hisabbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val unit: String = "Pcs", // Pcs, Kg, Ltr, Meter, Box, etc.
    val quantity: Double = 0.0,
    val minimumStock: Double = 0.0,
    val purchasePrice: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
