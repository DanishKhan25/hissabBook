package com.example.hisabbook.data.repository

import com.example.hisabbook.data.local.dao.InvoiceDao
import com.example.hisabbook.data.local.dao.InvoiceLineItemDao
import com.example.hisabbook.data.local.dao.QuotationDao
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.InvoiceLineItem
import com.example.hisabbook.data.local.entity.Quotation
import com.example.hisabbook.data.local.entity.toDomain
import com.example.hisabbook.data.local.entity.toEntity
import com.example.hisabbook.data.local.entity.toQuotationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class QuotationRepository(
    private val quotationDao: QuotationDao,
    private val invoiceDao: InvoiceDao,
    private val lineItemDao: InvoiceLineItemDao
) {
    val allQuotations: Flow<List<Quotation>> = quotationDao.getAllQuotations()

    fun getQuotationById(id: Long): Flow<Quotation?> = quotationDao.getQuotationById(id)

    fun getQuotationsForCustomer(customerId: Long): Flow<List<Quotation>> =
        quotationDao.getQuotationsForCustomer(customerId)

    fun getLineItemsForQuotation(quotationId: Long): Flow<List<InvoiceLineItem>> =
        lineItemDao.getLineItemsForQuotation(quotationId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun generateNextQuotationNumber(): String = withContext(Dispatchers.IO) {
        val maxId = quotationDao.getMaxId() ?: 0L
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        String.format(Locale.getDefault(), "Q-%d-%03d", currentYear, maxId + 1)
    }

    suspend fun createQuotation(
        quotationNumber: String,
        customerId: Long,
        description: String,
        quantity: Double,
        amount: Double,
        discount: Double,
        notes: String = "",
        status: String = "Draft",
        lineItems: List<InvoiceLineItem> = emptyList()
    ): Long = withContext(Dispatchers.IO) {
        val validQuantity = quantity.coerceAtLeast(0.0)
        val validAmount = amount.coerceAtLeast(0.0)
        val validDiscount = discount.coerceAtLeast(0.0)
        val total = (validQuantity * validAmount - validDiscount).coerceAtLeast(0.0)
        val quotation = Quotation(
            quotationNumber = quotationNumber,
            customerId = customerId,
            description = description,
            quantity = validQuantity,
            amount = validAmount,
            discount = validDiscount,
            total = total,
            status = status,
            notes = notes
        )
        val id = quotationDao.insertQuotation(quotation)
        if (lineItems.isNotEmpty()) {
            lineItemDao.insertAll(lineItems.map { it.toQuotationEntity(id) })
        }
        id
    }

    suspend fun updateQuotation(
        quotation: Quotation,
        lineItems: List<InvoiceLineItem> = emptyList()
    ) = withContext(Dispatchers.IO) {
        val validQuantity = quotation.quantity.coerceAtLeast(0.0)
        val validAmount = quotation.amount.coerceAtLeast(0.0)
        val validDiscount = quotation.discount.coerceAtLeast(0.0)
        val total = (validQuantity * validAmount - validDiscount).coerceAtLeast(0.0)
        quotationDao.updateQuotation(
            quotation.copy(
                quantity = validQuantity,
                amount = validAmount,
                discount = validDiscount,
                total = total
            )
        )
        // Replace all line items
        lineItemDao.deleteByQuotationId(quotation.id)
        if (lineItems.isNotEmpty()) {
            lineItemDao.insertAll(lineItems.map { it.toQuotationEntity(quotation.id) })
        }
    }

    suspend fun deleteQuotation(quotation: Quotation) = withContext(Dispatchers.IO) {
        lineItemDao.deleteByQuotationId(quotation.id)
        quotationDao.deleteQuotation(quotation)
    }

    suspend fun updateStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        quotationDao.updateStatus(id, status)
    }

    /**
     * Converts a quotation to a bill/invoice.
     * Copies quotation line items → invoice line items so the new invoice immediately
     * shows correct per-item rates.
     * Marks the quotation as "CONVERTED".
     */
    suspend fun convertToInvoice(
        quotationId: Long,
        billNumber: String
    ): Long? = withContext(Dispatchers.IO) {
        val quotation = quotationDao.getQuotationByIdOneShot(quotationId) ?: return@withContext null

        val invoice = Invoice(
            billNumber = billNumber,
            customerId = quotation.customerId,
            quotationId = quotation.id,
            description = quotation.description,
            amount = quotation.amount * quotation.quantity,
            discount = quotation.discount,
            total = quotation.total,
            status = "UNPAID"
        )

        val invoiceId = invoiceDao.insertInvoice(invoice)

        // Copy quotation line items to the new invoice
        val quotationLineItems = lineItemDao.getLineItemsForQuotationOneShot(quotationId)
        if (quotationLineItems.isNotEmpty()) {
            lineItemDao.insertAll(
                quotationLineItems.map { it.copy(id = 0, invoiceId = invoiceId, quotationId = null) }
            )
        }

        quotationDao.updateStatus(quotationId, "CONVERTED")
        invoiceId
    }
}
