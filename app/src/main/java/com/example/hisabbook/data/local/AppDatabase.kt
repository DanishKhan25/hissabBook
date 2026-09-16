package com.example.hisabbook.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.example.hisabbook.data.local.dao.BusinessProfileDao
import com.example.hisabbook.data.local.dao.CustomerDao
import com.example.hisabbook.data.local.dao.InventoryDao
import com.example.hisabbook.data.local.dao.InvoiceDao
import com.example.hisabbook.data.local.dao.PaymentDao
import com.example.hisabbook.data.local.dao.QuotationDao
import com.example.hisabbook.data.local.dao.StockTransactionDao
import com.example.hisabbook.data.local.entity.BusinessProfile
import com.example.hisabbook.data.local.entity.Customer
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.Payment
import com.example.hisabbook.data.local.entity.Quotation
import com.example.hisabbook.data.local.entity.StockTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Customer::class,
        Quotation::class,
        Invoice::class,
        Payment::class,
        InventoryItem::class,
        StockTransaction::class,
        BusinessProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun quotationDao(): QuotationDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun paymentDao(): PaymentDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun stockTransactionDao(): StockTransactionDao
    abstract fun businessProfileDao(): BusinessProfileDao

    @Transaction
    open suspend fun restoreDatabase(
        profile: BusinessProfile?,
        customers: List<Customer>,
        quotations: List<Quotation>,
        invoices: List<Invoice>,
        payments: List<Payment>,
        inventoryItems: List<InventoryItem>,
        stockTransactions: List<StockTransaction>
    ) {
        clearAllTables()
        if (profile != null) {
            businessProfileDao().insertOrUpdateBusinessProfile(profile)
        }
        customers.forEach { customerDao().insertCustomer(it) }
        quotations.forEach { quotationDao().insertQuotation(it) }
        invoices.forEach { invoiceDao().insertInvoice(it) }
        payments.forEach { paymentDao().insertPayment(it) }
        inventoryItems.forEach { inventoryDao().insertInventoryItem(it) }
        stockTransactions.forEach { stockTransactionDao().insertStockTransaction(it) }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hisab_book_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        DemoDataSeeder.seedDemoData(instance, force = false)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                instance
            }
        }
    }
}
