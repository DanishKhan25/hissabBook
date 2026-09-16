package com.example.hisabbook.data.repository

import com.example.hisabbook.data.local.dao.InvoiceDao
import com.example.hisabbook.data.local.dao.QuotationDao
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.Quotation
import kotlinx.coroutines.flow.Flow

class QuotationRepository(
    private val quotationDao: QuotationDao,
    private val invoiceDao: InvoiceDao
) {
    val allQuotations: Flow<List<Quotation>> = quotationDao.getAllQuotations()

    fun getQuotationById(id: Long): Flow<Quotation?> = quotationDao.getQuotationById(id)

    fun getQuotationsForCustomer(customerId: Long): Flow<List<Quotation>> =
        quotationDao.getQuotationsForCustomer(customerId)

    suspend fun createQuotation(
        quotationNumber: String,
        customerId: Long,
        description: String,
        quantity: Double,
        amount: Double,
        discount: Double,
        notes: String = ""
    ): Long {
        val total = (quantity * amount - discount).coerceAtLeast(0.0)
        val quotation = Quotation(
            quotationNumber = quotationNumber,
            customerId = customerId,
            description = description,
            quantity = quantity,
            amount = amount,
            discount = discount,
            total = total,
            status = "PENDING",
            notes = notes
        )
        return quotationDao.insertQuotation(quotation)
    }

    suspend fun updateQuotation(quotation: Quotation) {
        val total = (quotation.quantity * quotation.amount - quotation.discount).coerceAtLeast(0.0)
        quotationDao.updateQuotation(quotation.copy(total = total))
    }

    suspend fun deleteQuotation(quotation: Quotation) = quotationDao.deleteQuotation(quotation)

    suspend fun updateStatus(id: Long, status: String) = quotationDao.updateStatus(id, status)

    /**
     * Converts a quotation to a bill/invoice.
     * Marks the quotation as "CONVERTED" and creates a corresponding Invoice entity.
     */
    suspend fun convertToInvoice(quotationId: Long, billNumber: String): Long? {
        val quotation = quotationDao.getQuotationByIdOneShot(quotationId) ?: return null

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
        quotationDao.updateStatus(quotationId, "CONVERTED")
        return invoiceId
    }
}
