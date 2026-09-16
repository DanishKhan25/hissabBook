package com.example.hisabbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billNumber: String,
    val customerId: Long,
    val quotationId: Long? = null,
    val description: String,
    val amount: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "UNPAID", // UNPAID, PARTIALLY_PAID, PAID, CANCELLED
    val createdAt: Long = System.currentTimeMillis()
)
