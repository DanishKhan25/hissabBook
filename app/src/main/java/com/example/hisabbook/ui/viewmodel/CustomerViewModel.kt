package com.example.hisabbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hisabbook.data.local.entity.Customer
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.Payment
import com.example.hisabbook.data.local.entity.Quotation
import com.example.hisabbook.data.repository.CustomerRepository
import com.example.hisabbook.data.repository.InvoiceRepository
import com.example.hisabbook.data.repository.PaymentRepository
import com.example.hisabbook.data.repository.QuotationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomerWithBalance(
    val customer: Customer,
    val outstandingBalance: Double = 0.0,
    val totalInvoiced: Double = 0.0,
    val totalPaid: Double = 0.0
)

data class CustomerDetailUiState(
    val customer: Customer? = null,
    val outstandingBalance: Double = 0.0,
    val invoices: List<Invoice> = emptyList(),
    val quotations: List<Quotation> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerViewModel(
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository,
    private val quotationRepository: QuotationRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId: StateFlow<Long?> = _selectedCustomerId.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val customersWithBalance: StateFlow<List<CustomerWithBalance>> = combine(
        customerRepository.allCustomers,
        invoiceRepository.allInvoices,
        paymentRepository.allPayments,
        _searchQuery
    ) { customers, invoices, payments, query ->
        val invoiceMap = invoices.filter { it.status != "CANCELLED" }.groupBy { it.customerId }
        val paymentMap = payments.groupBy { it.customerId }

        val list = customers.map { customer ->
            val custInvoices = invoiceMap[customer.id] ?: emptyList()
            val custPayments = paymentMap[customer.id] ?: emptyList()
            val totalInvoiced = custInvoices.sumOf { it.total }
            val totalPaid = custPayments.sumOf { it.amount }
            val balance = (totalInvoiced - totalPaid).coerceAtLeast(0.0)
            CustomerWithBalance(customer, balance, totalInvoiced, totalPaid)
        }

        if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.customer.name.contains(query, ignoreCase = true) ||
                it.customer.phone.contains(query, ignoreCase = true) ||
                it.customer.address.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val selectedCustomerDetail: StateFlow<CustomerDetailUiState> = _selectedCustomerId.flatMapLatest { customerId ->
        if (customerId == null) {
            flowOf(CustomerDetailUiState())
        } else {
            combine(
                customerRepository.getCustomerById(customerId),
                invoiceRepository.getInvoicesForCustomer(customerId),
                quotationRepository.getQuotationsForCustomer(customerId),
                paymentRepository.getPaymentsForCustomer(customerId)
            ) { customer, invoices, quotations, payments ->
                val activeInvoices = invoices.filter { it.status != "CANCELLED" }
                val totalInvoiced = activeInvoices.sumOf { it.total }
                val totalPaid = payments.sumOf { it.amount }
                val balance = (totalInvoiced - totalPaid).coerceAtLeast(0.0)

                CustomerDetailUiState(
                    customer = customer,
                    outstandingBalance = balance,
                    invoices = invoices,
                    quotations = quotations,
                    payments = payments,
                    isLoading = false
                )
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CustomerDetailUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCustomer(customerId: Long?) {
        _selectedCustomerId.value = customerId
    }

    fun saveCustomer(
        id: Long = 0,
        name: String,
        phone: String,
        address: String,
        onSuccess: () -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        val trimmedAddress = address.trim()

        if (trimmedName.isBlank()) {
            val err = "Customer name cannot be empty"
            _userMessage.value = err
            onError?.invoke(err)
            return
        }

        if (trimmedPhone.isNotBlank()) {
            val digits = trimmedPhone.filter { it.isDigit() }
            if (digits.length != 10) {
                val err = "Please enter a valid 10-digit mobile number"
                _userMessage.value = err
                onError?.invoke(err)
                return
            }
        }

        viewModelScope.launch {
            if (trimmedPhone.isNotBlank()) {
                val existingPhone = customerRepository.getCustomerByPhoneOneShot(trimmedPhone)
                if (existingPhone != null && existingPhone.id != id) {
                    val err = "A customer with this mobile number already exists"
                    _userMessage.value = err
                    onError?.invoke(err)
                    return@launch
                }
            }

            if (id == 0L) {
                val newCustomer = Customer(name = trimmedName, phone = trimmedPhone, address = trimmedAddress)
                customerRepository.insertCustomer(newCustomer)
                _userMessage.value = "Customer '$trimmedName' added successfully"
            } else {
                val existingCustomer = Customer(id = id, name = trimmedName, phone = trimmedPhone, address = trimmedAddress)
                customerRepository.updateCustomer(existingCustomer)
                _userMessage.value = "Customer '$trimmedName' updated successfully"
            }
            onSuccess()
        }
    }

    fun deleteCustomer(customer: Customer, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customer)
            if (_selectedCustomerId.value == customer.id) {
                _selectedCustomerId.value = null
            }
            _userMessage.value = "Customer '${customer.name}' deleted"
            onSuccess()
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
