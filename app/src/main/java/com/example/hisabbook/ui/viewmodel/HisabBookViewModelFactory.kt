package com.example.hisabbook.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hisabbook.data.local.AppDatabase
import com.example.hisabbook.data.repository.AppRepository

class HisabBookViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = AppRepository.getInstance(application)
        val database = AppDatabase.getInstance(application)

        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(
                    repository.businessProfileRepository,
                    repository.customerRepository,
                    repository.invoiceRepository,
                    repository.paymentRepository,
                    repository.inventoryRepository
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    repository.businessProfileRepository,
                    database
                ) as T
            }
            modelClass.isAssignableFrom(CustomerViewModel::class.java) -> {
                CustomerViewModel(
                    repository.customerRepository,
                    repository.invoiceRepository,
                    repository.paymentRepository,
                    repository.quotationRepository
                ) as T
            }
            modelClass.isAssignableFrom(PaymentViewModel::class.java) -> {
                PaymentViewModel(
                    repository.paymentRepository,
                    repository.customerRepository,
                    repository.invoiceRepository
                ) as T
            }
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> {
                InventoryViewModel(
                    repository.inventoryRepository
                ) as T
            }
            modelClass.isAssignableFrom(QuotationViewModel::class.java) -> {
                QuotationViewModel(
                    repository.quotationRepository,
                    repository.customerRepository,
                    repository.invoiceRepository,
                    repository.businessProfileRepository,
                    repository.inventoryRepository
                ) as T
            }
            modelClass.isAssignableFrom(InvoiceViewModel::class.java) -> {
                InvoiceViewModel(
                    repository.invoiceRepository,
                    repository.customerRepository,
                    repository.quotationRepository,
                    repository.paymentRepository,
                    repository.businessProfileRepository,
                    repository.inventoryRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
