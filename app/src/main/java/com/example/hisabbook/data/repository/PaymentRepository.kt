package com.example.hisabbook.data.repository

import com.example.hisabbook.data.local.dao.PaymentDao
import com.example.hisabbook.data.local.entity.Payment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val invoiceRepository: InvoiceRepository
) {
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()

    fun getPaymentsForInvoice(billId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsForInvoice(billId)

    fun getPaymentsForCustomer(customerId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsForCustomer(customerId)

    suspend fun recordPayment(
        billId: Long,
        customerId: Long,
        amount: Double,
        paymentMode: String,
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val payment = Payment(
            billId = billId,
            customerId = customerId,
            amount = amount,
            paymentMode = paymentMode,
            notes = notes
        )
        val paymentId = paymentDao.insertPayment(payment)
        invoiceRepository.recalculateInvoiceStatus(billId)
        paymentId
    }

    suspend fun updatePayment(payment: Payment) = withContext(Dispatchers.IO) {
        paymentDao.updatePayment(payment)
        invoiceRepository.recalculateInvoiceStatus(payment.billId)
    }

    suspend fun deletePayment(payment: Payment) = withContext(Dispatchers.IO) {
        paymentDao.deletePayment(payment)
        invoiceRepository.recalculateInvoiceStatus(payment.billId)
    }
}
