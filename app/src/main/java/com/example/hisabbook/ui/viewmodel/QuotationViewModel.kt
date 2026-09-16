package com.example.hisabbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hisabbook.data.local.entity.BusinessProfile
import com.example.hisabbook.data.local.entity.Customer
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.Quotation
import com.example.hisabbook.data.repository.BusinessProfileRepository
import com.example.hisabbook.data.repository.CustomerRepository
import com.example.hisabbook.data.repository.InventoryRepository
import com.example.hisabbook.data.repository.InvoiceRepository
import com.example.hisabbook.data.repository.QuotationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class QuotationViewModel(
    private val quotationRepository: QuotationRepository,
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository,
    private val businessProfileRepository: BusinessProfileRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null) // null = ALL
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    val customers: StateFlow<List<Customer>> = customerRepository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val inventoryItems: StateFlow<List<InventoryItem>> = inventoryRepository.allInventoryItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val businessProfile: StateFlow<BusinessProfile?> = businessProfileRepository.businessProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allQuotations: StateFlow<List<Quotation>> = quotationRepository.allQuotations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredQuotations: StateFlow<List<Quotation>> = combine(
        allQuotations,
        customers,
        searchQuery,
        statusFilter
    ) { quotations, customerList, query, filter ->
        val customerMap = customerList.associateBy { it.id }

        quotations.filter { quotation ->
            val customerName = customerMap[quotation.customerId]?.name ?: ""
            val matchesQuery = query.isBlank() ||
                    quotation.quotationNumber.contains(query, ignoreCase = true) ||
                    customerName.contains(query, ignoreCase = true) ||
                    quotation.description.contains(query, ignoreCase = true)

            val matchesFilter = when (filter?.uppercase()) {
                null, "ALL" -> true
                "DRAFT" -> quotation.status.equals("DRAFT", ignoreCase = true) || quotation.status.equals("PENDING", ignoreCase = true)
                "SENT" -> quotation.status.equals("SENT", ignoreCase = true)
                "APPROVED" -> quotation.status.equals("APPROVED", ignoreCase = true) || quotation.status.equals("ACCEPTED", ignoreCase = true)
                "REJECTED" -> quotation.status.equals("REJECTED", ignoreCase = true) || quotation.status.equals("DECLINED", ignoreCase = true)
                "CONVERTED" -> quotation.status.equals("CONVERTED", ignoreCase = true)
                else -> quotation.status.equals(filter, ignoreCase = true)
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

    fun generateNextQuotationNumber(): String {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val count = allQuotations.value.size + 1
        return String.format(Locale.getDefault(), "Q-%d-%03d", currentYear, count)
    }

    fun getQuotationById(id: Long) = quotationRepository.getQuotationById(id)

    fun getCustomerById(customerId: Long) = customerRepository.getCustomerById(customerId)

    fun createQuotation(
        customerId: Long,
        description: String,
        quantity: Double,
        amount: Double,
        discount: Double,
        notes: String,
        status: String = "Draft",
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val qNum = generateNextQuotationNumber()
            val id = quotationRepository.createQuotation(
                quotationNumber = qNum,
                customerId = customerId,
                description = description,
                quantity = quantity,
                amount = amount,
                discount = discount,
                notes = notes
            )
            val savedStatus = when (status.uppercase()) {
                "DRAFT" -> "Draft"
                "SENT" -> "Sent"
                "APPROVED", "ACCEPTED" -> "Approved"
                "REJECTED", "DECLINED" -> "Rejected"
                else -> status
            }
            quotationRepository.updateStatus(id, savedStatus)
            withContext(Dispatchers.Main) { onSuccess(id) }
        }
    }

    fun updateQuotation(
        id: Long,
        customerId: Long,
        description: String,
        quantity: Double,
        amount: Double,
        discount: Double,
        notes: String,
        status: String = "Draft",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = quotationRepository.getQuotationById(id).firstOrNull()
            if (existing != null) {
                val updated = existing.copy(
                    customerId = customerId,
                    description = description,
                    quantity = quantity,
                    amount = amount,
                    discount = discount,
                    notes = notes,
                    status = status
                )
                quotationRepository.updateQuotation(updated)
                withContext(Dispatchers.Main) { onSuccess() }
            }
        }
    }

    fun updateQuotationStatus(quotationId: Long, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            quotationRepository.updateStatus(quotationId, newStatus)
        }
    }

    fun convertToInvoice(quotationId: Long, onSuccess: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentInvoices = invoiceRepository.allInvoices.firstOrNull() ?: emptyList()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val billNum = String.format(Locale.getDefault(), "B-%d-%03d", currentYear, currentInvoices.size + 1)

            val invoiceId = quotationRepository.convertToInvoice(quotationId, billNum)
            if (invoiceId != null) {
                withContext(Dispatchers.Main) { onSuccess(invoiceId) }
            }
        }
    }

    fun deleteQuotation(quotation: Quotation) {
        viewModelScope.launch(Dispatchers.IO) {
            quotationRepository.deleteQuotation(quotation)
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

        viewModelScope.launch(Dispatchers.IO) {
            if (trimmedPhone.isNotBlank()) {
                val existing = customerRepository.getCustomerByPhoneOneShot(trimmedPhone)
                if (existing != null) {
                    withContext(Dispatchers.Main) { onError("A customer with this mobile number already exists") }
                    return@launch
                }
            }

            val newCustomer = Customer(name = trimmedName, phone = trimmedPhone, address = trimmedAddress)
            val newId = customerRepository.insertCustomer(newCustomer)
            withContext(Dispatchers.Main) { onSuccess(newCustomer.copy(id = newId)) }
        }
    }
}
