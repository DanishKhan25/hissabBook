package com.example.hisabbook.data.local.entity

data class InvoiceLineItem(
    val inventoryItemId: Long? = null,
    val description: String = "",
    val unit: String = "Pcs",
    val unitRate: Double = 0.0,
    val quantity: Double = 1.0
) {
    val totalAmount: Double get() = unitRate * quantity
}
