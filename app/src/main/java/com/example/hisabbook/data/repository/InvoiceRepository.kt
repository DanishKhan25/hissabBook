package com.example.hisabbook.data.repository

import com.example.hisabbook.data.local.dao.InvoiceDao
import com.example.hisabbook.data.local.dao.PaymentDao
import com.example.hisabbook.data.local.entity.Invoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val paymentDao: PaymentDao
) {
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()

    fun getInvoiceById(id: Long): Flow<Invoice?> = invoiceDao.getInvoiceById(id)

    suspend fun getInvoiceByIdOneShot(id: Long): Invoice? = withContext(Dispatchers.IO) {
        invoiceDao.getInvoiceByIdOneShot(id)
    }

    fun getInvoicesForCustomer(customerId: Long): Flow<List<Invoice>> =
        invoiceDao.getInvoicesForCustomer(customerId)

    suspend fun createInvoice(
        customerId: Long,
        billNumber: String,
        description: String,
        amount: Double,
        discount: Double,
        quotationId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val total = (amount - discount).coerceAtLeast(0.0)
        val invoice = Invoice(
            billNumber = billNumber,
            customerId = customerId,
            quotationId = quotationId,
            description = description,
            amount = amount,
            discount = discount,
            total = total,
            status = "UNPAID"
        )
        invoiceDao.insertInvoice(invoice)
    }

    suspend fun updateInvoice(invoice: Invoice) = withContext(Dispatchers.IO) {
        val total = (invoice.amount - invoice.discount).coerceAtLeast(0.0)
        val updatedInvoice = invoice.copy(total = total)
        invoiceDao.updateInvoice(updatedInvoice)
        recalculateInvoiceStatus(invoice.id)
    }

    suspend fun deleteInvoice(invoice: Invoice) = withContext(Dispatchers.IO) {
        invoiceDao.deleteInvoice(invoice)
    }

    suspend fun updateInvoiceStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        invoiceDao.updateInvoiceStatus(id, status)
    }

    /**
     * Calculates paid amount and balance due for a given invoice.
     */
    fun getInvoiceOutstandingAmount(billId: Long): Flow<Pair<Double, Double>> {
        val invoiceFlow = invoiceDao.getInvoiceById(billId)
        val paymentsFlow = paymentDao.getPaymentsForInvoice(billId)

        return combine(invoiceFlow, paymentsFlow) { invoice, payments ->
            if (invoice == null) {
                Pair(0.0, 0.0)
            } else {
                val paid = payments.sumOf { it.amount }
                val balance = (invoice.total - paid).coerceAtLeast(0.0)
                Pair(paid, balance)
            }
        }
    }

    /**
     * Recalculates and updates the status of an invoice based on recorded payments.
     */
    suspend fun recalculateInvoiceStatus(billId: Long) = withContext(Dispatchers.IO) {
        val invoice = invoiceDao.getInvoiceByIdOneShot(billId) ?: return@withContext
        if (invoice.status == "CANCELLED") return@withContext

        val totalPaid = paymentDao.getTotalPaymentsForInvoiceOneShot(billId) ?: 0.0
        val newStatus = when {
            totalPaid >= invoice.total && invoice.total > 0 -> "PAID"
            totalPaid > 0 -> "PARTIALLY_PAID"
            else -> "UNPAID"
        }

        if (invoice.status != newStatus) {
            invoiceDao.updateInvoiceStatus(billId, newStatus)
        }
    }

    /**
     * Gets total sales revenue (sum of all invoice totals).
     */
    val totalRevenue: Flow<Double> = invoiceDao.getAllInvoices().map { invoices ->
        invoices.filter { it.status != "CANCELLED" }.sumOf { it.total }
    }

    /**
     * Gets total outstanding collectibles across all invoices.
     */
    val totalOutstanding: Flow<Double> = combine(invoiceDao.getAllInvoices(), paymentDao.getAllPayments()) { invoices, payments ->
        val validInvoices = invoices.filter { it.status != "CANCELLED" }
        val totalInvoiced = validInvoices.sumOf { it.total }
        val totalPaid = payments.sumOf { it.amount }
        (totalInvoiced - totalPaid).coerceAtLeast(0.0)
    }
}
