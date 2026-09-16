package com.example.hisabbook.data.repository

import android.content.Context
import com.example.hisabbook.data.local.DatabaseProvider

class AppRepository private constructor(context: Context) {
    private val db = DatabaseProvider.getDatabase(context)

    val customerRepository = CustomerRepository(
        db.customerDao(),
        db.invoiceDao(),
        db.paymentDao()
    )

    val invoiceRepository = InvoiceRepository(
        db.invoiceDao(),
        db.paymentDao()
    )

    val quotationRepository = QuotationRepository(
        db.quotationDao(),
        db.invoiceDao()
    )

    val paymentRepository = PaymentRepository(
        db.paymentDao(),
        invoiceRepository
    )

    val inventoryRepository = InventoryRepository(
        db.inventoryDao(),
        db.stockTransactionDao()
    )

    val businessProfileRepository = BusinessProfileRepository(
        db.businessProfileDao()
    )

    companion object {
        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AppRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
