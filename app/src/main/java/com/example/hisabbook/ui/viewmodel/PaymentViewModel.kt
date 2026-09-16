package com.example.hisabbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.Payment
import com.example.hisabbook.data.repository.CustomerRepository
import com.example.hisabbook.data.repository.InvoiceRepository
import com.example.hisabbook.data.repository.PaymentRepository
import com.example.hisabbook.util.CurrencyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PaymentDisplayItem(
    val payment: Payment,
    val customerName: String,
    val billNumber: String?
)

class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val allPayments: StateFlow<List<Payment>> = paymentRepository.allPayments.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val paymentsWithDetails: StateFlow<List<PaymentDisplayItem>> = combine(
        allPayments,
        customerRepository.allCustomers,
        invoiceRepository.allInvoices
    ) { payments, customers, invoices ->
        val customerMap = customers.associateBy { it.id }
        val invoiceMap = invoices.associateBy { it.id }

        payments.map { payment ->
            val customer = customerMap[payment.customerId]
            val invoice = invoiceMap[payment.billId]
            PaymentDisplayItem(
                payment = payment,
                customerName = customer?.name ?: "Unknown Customer",
                billNumber = invoice?.billNumber
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun getUnpaidInvoicesForCustomer(customerId: Long): StateFlow<List<Invoice>> {
        return invoiceRepository.getInvoicesForCustomer(customerId).map { invoices ->
            invoices.filter { it.status == "UNPAID" || it.status == "PARTIALLY_PAID" }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun recordPayment(
        customerId: Long,
        billId: Long?,
        amount: Double,
        paymentMode: String,
        notes: String = "",
        onSuccess: () -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        if (customerId <= 0) {
            val err = "Please select a valid customer"
            _userMessage.value = err
            onError?.invoke(err)
            return
        }
        if (amount <= 0) {
            val err = "Payment amount must be greater than ₹0"
            _userMessage.value = err
            onError?.invoke(err)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (billId == null) {
                val unpaidInvoices = invoiceRepository.getInvoicesForCustomer(customerId)
                    .firstOrNull()
                    ?.filter { it.status == "UNPAID" || it.status == "PARTIALLY_PAID" }
                    .orEmpty()

                if (unpaidInvoices.isNotEmpty()) {
                    val err = "Please select the bill this payment should clear"
                    _userMessage.value = err
                    withContext(Dispatchers.Main) { onError?.invoke(err) }
                    return@launch
                }
            }

            val effectiveBillId = billId ?: 0L
            if (effectiveBillId > 0) {
                val invoice = invoiceRepository.getInvoiceByIdOneShot(effectiveBillId)
                if (invoice != null) {
                    val existingPayments = paymentRepository.getPaymentsForInvoice(effectiveBillId).firstOrNull() ?: emptyList()
                    val totalPaidForBill = existingPayments.sumOf { it.amount }
                    val outstanding = (invoice.total - totalPaidForBill).coerceAtLeast(0.0)

                    if (amount > outstanding && outstanding > 0) {
                        val err = "Payment amount (${CurrencyFormatter.formatCurrency(amount)}) cannot exceed bill outstanding balance (${CurrencyFormatter.formatCurrency(outstanding)})"
                        _userMessage.value = err
                        withContext(Dispatchers.Main) { onError?.invoke(err) }
                        return@launch
                    }
                }
            }

            paymentRepository.recordPayment(
                billId = effectiveBillId,
                customerId = customerId,
                amount = amount,
                paymentMode = paymentMode,
                notes = notes.trim()
            )
            _userMessage.value = "Payment of ₹${CurrencyFormatter.formatAmountOnly(amount)} recorded successfully"
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch(Dispatchers.IO) {
            paymentRepository.deletePayment(payment)
            _userMessage.value = "Payment deleted successfully"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
