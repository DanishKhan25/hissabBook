package com.example.hisabbook.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hisabbook.data.local.dao.BusinessProfileDao
import com.example.hisabbook.data.local.dao.CustomerDao
import com.example.hisabbook.data.local.dao.InventoryDao
import com.example.hisabbook.data.local.dao.InvoiceDao
import com.example.hisabbook.data.local.dao.InvoiceLineItemDao
import com.example.hisabbook.data.local.dao.PaymentDao
import com.example.hisabbook.data.local.dao.QuotationDao
import com.example.hisabbook.data.local.dao.StockTransactionDao
import com.example.hisabbook.data.local.entity.BusinessProfile
import com.example.hisabbook.data.local.entity.Customer
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.InvoiceLineItemEntity
import com.example.hisabbook.data.local.entity.Payment
import com.example.hisabbook.data.local.entity.Quotation
import com.example.hisabbook.data.local.entity.StockTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [
        Customer::class,
        Quotation::class,
        Invoice::class,
        Payment::class,
        InventoryItem::class,
        StockTransaction::class,
        BusinessProfile::class,
        InvoiceLineItemEntity::class
    ],
    version = 4,
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
    abstract fun invoiceLineItemDao(): InvoiceLineItemDao

    @Transaction
    open suspend fun restoreDatabase(
        profile: BusinessProfile?,
        customers: List<Customer>,
        inventoryItems: List<InventoryItem>,
        quotations: List<Quotation>,
        invoices: List<Invoice>,
        payments: List<Payment>,
        stockTransactions: List<StockTransaction>,
        invoiceLineItems: List<InvoiceLineItemEntity> = emptyList()
    ) = withContext(Dispatchers.IO) {
        clearAllTables()
        if (profile != null) {
            businessProfileDao().insertOrUpdateBusinessProfile(profile)
        }
        customers.forEach { customerDao().insertCustomer(it) }
        inventoryItems.forEach { inventoryDao().insertInventoryItem(it) }
        quotations.forEach { quotationDao().insertQuotation(it) }
        invoices.forEach { invoiceDao().insertInvoice(it) }
        payments.forEach { paymentDao().insertPayment(it) }
        stockTransactions.forEach { stockTransactionDao().insertStockTransaction(it) }
        if (invoiceLineItems.isNotEmpty()) {
            invoiceLineItemDao().insertAll(invoiceLineItems)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from version 2 to version 3:
         * Adds the invoice_line_items table to persist individual line items per invoice.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `invoice_line_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `invoiceId` INTEGER NOT NULL,
                        `inventoryItemId` INTEGER,
                        `description` TEXT NOT NULL,
                        `unit` TEXT NOT NULL,
                        `unitRate` REAL NOT NULL,
                        `quantity` REAL NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Migration from version 3 to version 4:
         * Rebuilds invoice_line_items with invoiceId nullable and adds quotationId column,
         * so the same table stores line items for both invoices and quotations.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create new table with updated schema
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `invoice_line_items_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `invoiceId` INTEGER,
                        `quotationId` INTEGER,
                        `inventoryItemId` INTEGER,
                        `description` TEXT NOT NULL,
                        `unit` TEXT NOT NULL,
                        `unitRate` REAL NOT NULL,
                        `quantity` REAL NOT NULL
                    )
                    """.trimIndent()
                )
                // Copy existing invoice line items — invoiceId stays, quotationId defaults to NULL
                db.execSQL(
                    """
                    INSERT INTO `invoice_line_items_new`
                        (id, invoiceId, quotationId, inventoryItemId, description, unit, unitRate, quantity)
                    SELECT id, invoiceId, NULL, inventoryItemId, description, unit, unitRate, quantity
                    FROM `invoice_line_items`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `invoice_line_items`")
                db.execSQL("ALTER TABLE `invoice_line_items_new` RENAME TO `invoice_line_items`")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hisab_book_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
