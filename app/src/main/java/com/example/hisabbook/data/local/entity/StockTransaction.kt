package com.example.hisabbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val inventoryItemId: Long,
    val type: String, // IN, OUT, ADJUSTMENT
    val quantity: Double,
    val purchasePrice: Double = 0.0,
    val reason: String = "",
    val reference: String = "",
    val date: Long = System.currentTimeMillis()
)
