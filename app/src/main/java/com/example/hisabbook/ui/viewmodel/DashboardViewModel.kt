package com.example.hisabbook.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hisabbook.data.local.entity.BusinessProfile
import com.example.hisabbook.data.repository.BusinessProfileRepository
import com.example.hisabbook.data.repository.CustomerRepository
import com.example.hisabbook.data.repository.InventoryRepository
import com.example.hisabbook.data.repository.InvoiceRepository
import com.example.hisabbook.data.repository.PaymentRepository
import com.example.hisabbook.util.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

enum class ActivityType {
    INVOICE,
    PAYMENT
}

data class RecentActivityItem(
    val id: Long,
    val type: ActivityType,
    val title: String,
    val customerName: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val remainingBalance: Double,
    val date: Long,
    val status: String
)

class DashboardViewModel(
    private val businessProfileRepository: BusinessProfileRepository,
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            businessProfileRepository.ensureDefaultProfile()
        }
    }

    val businessProfile: StateFlow<BusinessProfile?> = businessProfileRepository.businessProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val todaySales: StateFlow<Double> = invoiceRepository.allInvoices.map { invoices ->
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis
        invoices.filter { it.status != "CANCELLED" && it.createdAt >= startOfToday }
            .sumOf { it.total }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalOutstanding: StateFlow<Double> = combine(
        invoiceRepository.allInvoices,
        paymentRepository.allPayments
    ) { invoices, payments ->
        val paymentsByInvoice = payments.groupBy { it.billId }
        invoices.filter { it.status != "CANCELLED" }.sumOf { invoice ->
            val paidForInvoice = (paymentsByInvoice[invoice.id] ?: emptyList()).sumOf { it.amount }
            (invoice.total - paidForInvoice).coerceAtLeast(0.0)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalCustomers: StateFlow<Int> = customerRepository.allCustomers.map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val lowStockCount: StateFlow<Int> = inventoryRepository.lowStockItems.map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val recentActivities: StateFlow<List<RecentActivityItem>> = combine(
        invoiceRepository.allInvoices,
        paymentRepository.allPayments,
        customerRepository.allCustomers
    ) { invoices, payments, customers ->
        val customerMap = customers.associateBy { it.id }
        val paymentsByInvoice = payments.groupBy { it.billId }

        val invoiceItems = invoices.filter { it.status != "CANCELLED" }.map { invoice ->
            val custName = customerMap[invoice.customerId]?.name ?: "Unknown Customer"
            val totalPaid = (paymentsByInvoice[invoice.id] ?: emptyList()).sumOf { it.amount }
            val remaining = (invoice.total - totalPaid).coerceAtLeast(0.0)
            val displayStatus = when {
                remaining <= 0.0 && invoice.total > 0.0 -> "PAID"
                totalPaid > 0.0 -> "PARTIALLY_PAID"
                else -> invoice.status
            }
            RecentActivityItem(
                id = invoice.id,
                type = ActivityType.INVOICE,
                title = "Bill ${invoice.billNumber}",
                customerName = custName,
                totalAmount = invoice.total,
                paidAmount = totalPaid,
                remainingBalance = remaining,
                date = invoice.createdAt,
                status = displayStatus
            )
        }

        val paymentItems = payments.map { payment ->
            val custName = customerMap[payment.customerId]?.name ?: "Unknown Customer"
            RecentActivityItem(
                id = payment.id,
                type = ActivityType.PAYMENT,
                title = "Payment (${payment.paymentMode})",
                customerName = custName,
                totalAmount = payment.amount,
                paidAmount = payment.amount,
                remainingBalance = 0.0,
                date = payment.date,
                status = "RECEIVED"
            )
        }

        (invoiceItems + paymentItems).sortedByDescending { it.date }.take(10)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleLanguage(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = businessProfile.value ?: BusinessProfile()
            val newLang = if (current.languagePreference.lowercase() == "hi") "en" else "hi"
            val updated = current.copy(languagePreference = newLang)
            businessProfileRepository.updateBusinessProfile(updated)
            withContext(Dispatchers.Main) {
                LocaleHelper.applyLocale(context, newLang)
            }
        }
    }

    fun setLanguage(context: Context, languageCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = businessProfile.value ?: BusinessProfile()
            val updated = current.copy(languagePreference = languageCode)
            businessProfileRepository.updateBusinessProfile(updated)
            withContext(Dispatchers.Main) {
                LocaleHelper.applyLocale(context, languageCode)
            }
        }
    }
}
