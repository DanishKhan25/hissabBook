package com.example.hisabbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted line item for both invoices and quotations.
 *
 * Exactly one of [invoiceId] or [quotationId] will be non-null:
 *  - Invoice items: invoiceId = invoice.id, quotationId = null
 *  - Quotation items: quotationId = quotation.id, invoiceId = null
 */
@Entity(tableName = "invoice_line_items")
data class InvoiceLineItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long? = null,
    val quotationId: Long? = null,
    val inventoryItemId: Long? = null,
    val description: String = "",
    val unit: String = "Pcs",
    val unitRate: Double = 0.0,
    val quantity: Double = 1.0
)
