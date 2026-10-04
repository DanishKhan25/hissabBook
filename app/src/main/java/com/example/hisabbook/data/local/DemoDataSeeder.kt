package com.example.hisabbook.data.local

import com.example.hisabbook.data.local.entity.BusinessProfile
import com.example.hisabbook.data.local.entity.Customer
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.Payment
import com.example.hisabbook.data.local.entity.Quotation
import com.example.hisabbook.data.local.entity.StockTransaction
import com.example.hisabbook.data.model.BusinessTypeCategory

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DemoDataSeeder {

    fun getDemoBusinessProfile(): BusinessProfile {
        return BusinessProfile(
            id = 1,
            businessName = "HisabBook Business",
            subtitle = "Smart Ledger & Invoicing",
            description = "General Trading & Business Ledger",
            phone = "9876543210",
            address = "",
            gstNumber = "",
            languagePreference = "en",
            isOnboardingCompleted = true
        )
    }

    fun getDemoCustomers(): List<Customer> {
        return listOf(
            Customer(id = 1, name = "Sahil", phone = "7058540991", address = "Plot 42, Green Park"),
            Customer(id = 2, name = "Rahul Sharma", phone = "9876543210", address = "Whitefield, Main Road"),
            Customer(id = 3, name = "Amit Kumar", phone = "9123456789", address = "Sector 62"),
            Customer(id = 4, name = "Suresh Singh", phone = "9988776655", address = "Civil Lines")
        )
    }

    fun getDemoInventoryItemsForCategory(categoryKey: String = "GENERAL"): List<InventoryItem> {
        val category = BusinessTypeCategory.fromId(categoryKey)
        return category.presetItems.mapIndexed { index, preset ->
            InventoryItem(
                id = (index + 1).toLong(),
                name = preset.name,
                unit = preset.unit,
                quantity = 50.0,
                minimumStock = preset.minimumStock,
                purchasePrice = preset.defaultPrice
            )
        }
    }

    fun getDemoInventoryItems(): List<InventoryItem> {
        return getDemoInventoryItemsForCategory("GENERAL")
    }

    fun getDemoStockTransactions(): List<StockTransaction> {
        return getDemoInventoryItems().mapIndexed { index, item ->
            StockTransaction(
                id = (index + 1).toLong(),
                inventoryItemId = item.id,
                type = "IN",
                quantity = item.quantity,
                purchasePrice = item.purchasePrice,
                reason = "Initial Opening Stock",
                reference = "DEMO_SEED"
            )
        }
    }

    fun getDemoQuotations(): List<Quotation> {
        return listOf(
            Quotation(
                id = 1,
                quotationNumber = "Q-2026-001",
                customerId = 2, // Rahul Sharma
                description = "Sliding Windows",
                quantity = 4.0,
                amount = 12500.0,
                discount = 0.0,
                total = 50000.0,
                status = "ACCEPTED",
                notes = "4 Sliding Window frames including toughened glass"
            )
        )
    }

    fun getDemoInvoices(): List<Invoice> {
        return listOf(
            Invoice(
                id = 1,
                billNumber = "B-2026-001",
                customerId = 2, // Rahul Sharma
                quotationId = 1,
                description = "Sliding Windows (Qty 4)",
                amount = 48000.0,
                discount = 0.0,
                total = 48000.0,
                status = "PARTIALLY_PAID"
            ),
            Invoice(
                id = 2,
                billNumber = "B-2026-002",
                customerId = 1, // Sahil
                quotationId = null,
                description = "HD- ALUMINIUM & Glass Works",
                amount = 16632.55,
                discount = 0.0,
                total = 16632.55,
                status = "PARTIALLY_PAID"
            ),
            Invoice(
                id = 3,
                billNumber = "B-2026-003",
                customerId = 3, // Amit Kumar
                quotationId = null,
                description = "Toughened Glass Work",
                amount = 25000.0,
                discount = 0.0,
                total = 25000.0,
                status = "PAID"
            )
        )
    }

    fun getDemoPayments(): List<Payment> {
        return listOf(
            Payment(
                id = 1,
                billId = 1, // B-2026-001
                customerId = 2, // Rahul Sharma
                amount = 20000.0,
                paymentMode = "CASH",
                notes = "Payment against B-2026-001"
            ),
            Payment(
                id = 2,
                billId = 2, // B-2026-002
                customerId = 1, // Sahil
                amount = 6000.0,
                paymentMode = "UPI",
                notes = "Payment against B-2026-002"
            ),
            Payment(
                id = 3,
                billId = 3, // B-2026-003
                customerId = 3, // Amit Kumar
                amount = 25000.0,
                paymentMode = "BANK_TRANSFER",
                notes = "Payment against B-2026-003"
            )
        )
    }

    suspend fun clearAllData(db: AppDatabase) = withContext(Dispatchers.IO) {
        db.clearAllTables()
        db.businessProfileDao().insertOrUpdateBusinessProfile(
            BusinessProfile(
                id = 1,
                businessName = "",
                subtitle = "",
                description = "",
                phone = "",
                address = "",
                languagePreference = "en",
                currencySymbol = "₹",
                gstNumber = "",
                isOnboardingCompleted = false
            )
        )
    }

    suspend fun seedDemoData(db: AppDatabase, force: Boolean = false) = withContext(Dispatchers.IO) {
        if (!force) {
            val existingCustomers = db.customerDao().getAllCustomersOneShot()
            if (existingCustomers.isNotEmpty()) {
                return@withContext
            }
        }

        db.restoreDatabase(
            profile = getDemoBusinessProfile(),
            customers = getDemoCustomers(),
            inventoryItems = getDemoInventoryItems(),
            quotations = getDemoQuotations(),
            invoices = getDemoInvoices(),
            payments = getDemoPayments(),
            stockTransactions = getDemoStockTransactions()
        )
    }
}
