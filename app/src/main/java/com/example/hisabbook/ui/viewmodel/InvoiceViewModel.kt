package com.example.hisabbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hisabbook.data.local.entity.BusinessProfile
import com.example.hisabbook.data.local.entity.Customer
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.InvoiceLineItem
import com.example.hisabbook.data.local.entity.Payment
import com.example.hisabbook.data.local.entity.Quotation
import com.example.hisabbook.data.repository.BusinessProfileRepository
import com.example.hisabbook.data.repository.CustomerRepository
import com.example.hisabbook.data.repository.InventoryRepository
import com.example.hisabbook.data.repository.InvoiceRepository
import com.example.hisabbook.data.repository.PaymentRepository
import com.example.hisabbook.data.repository.QuotationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class InvoiceViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val quotationRepository: QuotationRepository,
    private val paymentRepository: PaymentRepository,
    private val businessProfileRepository: BusinessProfileRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null) // null = ALL
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    val inventoryItems: StateFlow<List<InventoryItem>> = inventoryRepository.allInventoryItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customers: StateFlow<List<Customer>> = customerRepository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val quotations: StateFlow<List<Quotation>> = quotationRepository.allQuotations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val businessProfile: StateFlow<BusinessProfile?> = businessProfileRepository.businessProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allInvoices: StateFlow<List<Invoice>> = invoiceRepository.allInvoices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPayments: StateFlow<List<Payment>> = paymentRepository.allPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredInvoices: StateFlow<List<Invoice>> = combine(
        allInvoices,
        customers,
        searchQuery,
        statusFilter
    ) { invoices, customerList, query, filter ->
        val customerMap = customerList.associateBy { it.id }

        invoices.filter { invoice ->
            val customerName = customerMap[invoice.customerId]?.name ?: ""
            val matchesQuery = query.isBlank() ||
                    invoice.billNumber.contains(query, ignoreCase = true) ||
                    customerName.contains(query, ignoreCase = true) ||
                    invoice.description.contains(query, ignoreCase = true)

            val matchesFilter = when (filter?.uppercase()) {
                null, "ALL" -> true
                "UNPAID" -> invoice.status.equals("UNPAID", ignoreCase = true)
                "PARTIALLY_PAID", "PARTIALLY PAID" -> invoice.status.equals("PARTIALLY_PAID", ignoreCase = true) || invoice.status.equals("PARTIALLY PAID", ignoreCase = true)
                "PAID" -> invoice.status.equals("PAID", ignoreCase = true)
                else -> invoice.status.equals(filter, ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String?) {
        _statusFilter.value = filter
    }

    fun generateNextBillNumber(): String {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val count = allInvoices.value.size + 1
        return String.format(Locale.getDefault(), "B-%d-%03d", currentYear, count)
    }

    fun getInvoiceById(id: Long) = invoiceRepository.getInvoiceById(id)

    fun getCustomerById(customerId: Long) = customerRepository.getCustomerById(customerId)

    fun getPaymentsForInvoice(invoiceId: Long) = paymentRepository.getPaymentsForInvoice(invoiceId)

    fun getInvoiceOutstandingAmount(invoiceId: Long) = invoiceRepository.getInvoiceOutstandingAmount(invoiceId)

    fun createInvoice(
        customerId: Long,
        description: String,
        amount: Double,
        discount: Double,
        quotationId: Long? = null,
        lineItems: List<InvoiceLineItem> = emptyList(),
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val billNum = generateNextBillNumber()
            val id = invoiceRepository.createInvoice(
                customerId = customerId,
                billNumber = billNum,
                description = description,
                amount = amount,
                discount = discount,
                quotationId = quotationId
            )

            // Deduct stock for inventory items in line items
            for (lineItem in lineItems) {
                if (lineItem.inventoryItemId != null && lineItem.inventoryItemId > 0 && lineItem.quantity > 0) {
                    inventoryRepository.recordStockTransaction(
                        inventoryItemId = lineItem.inventoryItemId,
                        type = "STOCK_OUT",
                        quantity = lineItem.quantity,
                        reason = "Bill #$billNum",
                        reference = billNum
                    )
                }
            }

            onSuccess(id)
        }
    }

    fun addPayment(
        billId: Long,
        customerId: Long,
        amount: Double,
        paymentMode: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            paymentRepository.recordPayment(
                billId = billId,
                customerId = customerId,
                amount = amount,
                paymentMode = paymentMode,
                notes = notes
            )
        }
    }

    fun updateInvoiceStatus(invoiceId: Long, status: String) {
        viewModelScope.launch {
            invoiceRepository.updateInvoiceStatus(invoiceId, status)
        }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            invoiceRepository.deleteInvoice(invoice)
        }
    }

    fun updateInvoice(
        invoiceId: Long,
        customerId: Long,
        description: String,
        amount: Double,
        discount: Double,
        lineItems: List<InvoiceLineItem> = emptyList(),
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val existing = invoiceRepository.getInvoiceByIdOneShot(invoiceId)
            if (existing != null) {
                val total = (amount - discount).coerceAtLeast(0.0)
                val updated = existing.copy(
                    customerId = customerId,
                    description = description,
                    amount = amount,
                    discount = discount,
                    total = total
                )
                invoiceRepository.updateInvoice(updated)
                onSuccess()
            }
        }
    }

    fun saveCustomer(
        name: String,
        phone: String,
        address: String,
        onSuccess: (Customer) -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        val trimmedAddress = address.trim()

        if (trimmedName.isBlank()) {
            onError("Customer name cannot be empty")
            return
        }

        if (trimmedPhone.isNotBlank() && trimmedPhone.filter { it.isDigit() }.length != 10) {
            onError("Please enter a valid 10-digit mobile number")
            return
        }

        viewModelScope.launch {
            if (trimmedPhone.isNotBlank()) {
                val existing = customerRepository.getCustomerByPhoneOneShot(trimmedPhone)
                if (existing != null) {
                    onError("A customer with this mobile number already exists")
                    return@launch
                }
            }

            val newCustomer = Customer(name = trimmedName, phone = trimmedPhone, address = trimmedAddress)
            val newId = customerRepository.insertCustomer(newCustomer)
            onSuccess(newCustomer.copy(id = newId))
        }
    }
}
