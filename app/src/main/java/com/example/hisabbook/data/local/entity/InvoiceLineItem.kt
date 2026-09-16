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

// --- Mapping extensions ---

fun InvoiceLineItem.toEntity(invoiceId: Long): InvoiceLineItemEntity =
    InvoiceLineItemEntity(
        invoiceId = invoiceId,
        quotationId = null,
        inventoryItemId = inventoryItemId,
        description = description,
        unit = unit,
        unitRate = unitRate,
        quantity = quantity
    )

fun InvoiceLineItem.toQuotationEntity(quotationId: Long): InvoiceLineItemEntity =
    InvoiceLineItemEntity(
        invoiceId = null,
        quotationId = quotationId,
        inventoryItemId = inventoryItemId,
        description = description,
        unit = unit,
        unitRate = unitRate,
        quantity = quantity
    )

fun InvoiceLineItemEntity.toDomain(): InvoiceLineItem =
    InvoiceLineItem(
        inventoryItemId = inventoryItemId,
        description = description,
        unit = unit,
        unitRate = unitRate,
        quantity = quantity
    )
