package com.example.hisabbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billId: Long,
    val customerId: Long,
    val amount: Double,
    val paymentMode: String = "CASH", // CASH, UPI, BANK_TRANSFER, CHEQUE, OTHER
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
