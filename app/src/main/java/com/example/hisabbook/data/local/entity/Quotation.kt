package com.example.hisabbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotations")
data class Quotation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quotationNumber: String,
    val customerId: Long,
    val description: String,
    val quantity: Double = 1.0,
    val amount: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "PENDING", // PENDING, ACCEPTED, DECLINED, CONVERTED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
