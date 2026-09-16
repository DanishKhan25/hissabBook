package com.example.hisabbook.data.local

import com.example.hisabbook.data.local.entity.BusinessProfile
import com.example.hisabbook.data.local.entity.Customer
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.Payment
import com.example.hisabbook.data.local.entity.Quotation
import com.example.hisabbook.data.local.entity.StockTransaction
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val businessProfile: BusinessProfile? = null,
    val customers: List<Customer> = emptyList(),
    val inventoryItems: List<InventoryItem> = emptyList(),
    val quotations: List<Quotation> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val stockTransactions: List<StockTransaction> = emptyList()
)

object BackupManager {

    fun serializeBackupToJson(backupData: BackupData): String {
        val root = JSONObject()
        root.put("version", backupData.version)
        root.put("exportedAt", backupData.exportedAt)

        // Business Profile
        backupData.businessProfile?.let { profile ->
            val profObj = JSONObject().apply {
                put("id", profile.id)
                put("businessName", profile.businessName)
                put("subtitle", profile.subtitle)
                put("description", profile.description)
                put("phone", profile.phone)
                put("address", profile.address)
                put("languagePreference", profile.languagePreference)
                put("currencySymbol", profile.currencySymbol)
                put("gstNumber", profile.gstNumber)
                put("isOnboardingCompleted", profile.isOnboardingCompleted)
            }
            root.put("businessProfile", profObj)
        }

        // Customers
        val custArray = JSONArray()
        backupData.customers.forEach { c ->
            custArray.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("address", c.address)
                put("createdAt", c.createdAt)
            })
        }
        root.put("customers", custArray)

        // Inventory Items
        val itemArray = JSONArray()
        backupData.inventoryItems.forEach { item ->
            itemArray.put(JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("unit", item.unit)
                put("quantity", item.quantity)
                put("minimumStock", item.minimumStock)
                put("purchasePrice", item.purchasePrice)
                put("createdAt", item.createdAt)
            })
        }
        root.put("inventoryItems", itemArray)

        // Quotations
        val qArray = JSONArray()
        backupData.quotations.forEach { q ->
            qArray.put(JSONObject().apply {
                put("id", q.id)
                put("quotationNumber", q.quotationNumber)
                put("customerId", q.customerId)
                put("description", q.description)
                put("quantity", q.quantity)
                put("amount", q.amount)
                put("discount", q.discount)
                put("total", q.total)
                put("status", q.status)
                put("notes", q.notes)
                put("createdAt", q.createdAt)
            })
        }
        root.put("quotations", qArray)

        // Invoices
        val invArray = JSONArray()
        backupData.invoices.forEach { inv ->
            invArray.put(JSONObject().apply {
                put("id", inv.id)
                put("billNumber", inv.billNumber)
                put("customerId", inv.customerId)
                if (inv.quotationId != null) put("quotationId", inv.quotationId) else put("quotationId", JSONObject.NULL)
                put("description", inv.description)
                put("amount", inv.amount)
                put("discount", inv.discount)
                put("total", inv.total)
                put("status", inv.status)
                put("createdAt", inv.createdAt)
            })
        }
        root.put("invoices", invArray)

        // Payments
        val payArray = JSONArray()
        backupData.payments.forEach { p ->
            payArray.put(JSONObject().apply {
                put("id", p.id)
                put("billId", p.billId)
                put("customerId", p.customerId)
                put("amount", p.amount)
                put("paymentMode", p.paymentMode)
                put("date", p.date)
                put("notes", p.notes)
            })
        }
        root.put("payments", payArray)

        // Stock Transactions
        val txArray = JSONArray()
        backupData.stockTransactions.forEach { tx ->
            txArray.put(JSONObject().apply {
                put("id", tx.id)
                put("inventoryItemId", tx.inventoryItemId)
                put("type", tx.type)
                put("quantity", tx.quantity)
                put("purchasePrice", tx.purchasePrice)
                put("reason", tx.reason)
                put("reference", tx.reference)
                put("date", tx.date)
            })
        }
        root.put("stockTransactions", txArray)

        return root.toString(2)
    }

    fun parseBackupJson(jsonString: String): BackupData {
        if (jsonString.isBlank()) {
            throw IllegalArgumentException("Backup JSON content is empty")
        }

        val root = try {
            JSONObject(jsonString)
        } catch (e: JSONException) {
            throw IllegalArgumentException("Invalid JSON format: ${e.message}", e)
        }

        val version = root.optInt("version", 1)
        val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())

        val profile = if (root.has("businessProfile") && !root.isNull("businessProfile")) {
            val obj = root.getJSONObject("businessProfile")
            BusinessProfile(
                id = obj.optInt("id", 1),
                businessName = obj.optString("businessName", ""),
                subtitle = obj.optString("subtitle", ""),
                description = obj.optString("description", ""),
                phone = obj.optString("phone", ""),
                address = obj.optString("address", ""),
                languagePreference = obj.optString("languagePreference", "en"),
                currencySymbol = obj.optString("currencySymbol", "₹"),
                gstNumber = obj.optString("gstNumber", ""),
                isOnboardingCompleted = obj.optBoolean("isOnboardingCompleted", true)
            )
        } else null

        val customers = mutableListOf<Customer>()
        if (root.has("customers") && !root.isNull("customers")) {
            val arr = root.getJSONArray("customers")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                customers.add(
                    Customer(
                        id = obj.optLong("id", 0),
                        name = obj.optString("name", "Unknown Customer"),
                        phone = obj.optString("phone", ""),
                        address = obj.optString("address", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        val inventoryItems = mutableListOf<InventoryItem>()
        if (root.has("inventoryItems") && !root.isNull("inventoryItems")) {
            val arr = root.getJSONArray("inventoryItems")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                inventoryItems.add(
                    InventoryItem(
                        id = obj.optLong("id", 0),
                        name = obj.optString("name", "Unnamed Item"),
                        unit = obj.optString("unit", "Pcs"),
                        quantity = obj.optDouble("quantity", 0.0),
                        minimumStock = obj.optDouble("minimumStock", 0.0),
                        purchasePrice = obj.optDouble("purchasePrice", 0.0),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        val quotations = mutableListOf<Quotation>()
        if (root.has("quotations") && !root.isNull("quotations")) {
            val arr = root.getJSONArray("quotations")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                quotations.add(
                    Quotation(
                        id = obj.optLong("id", 0),
                        quotationNumber = obj.optString("quotationNumber", ""),
                        customerId = obj.optLong("customerId", 0),
                        description = obj.optString("description", ""),
                        quantity = obj.optDouble("quantity", 1.0),
                        amount = obj.optDouble("amount", 0.0),
                        discount = obj.optDouble("discount", 0.0),
                        total = obj.optDouble("total", 0.0),
                        status = obj.optString("status", "PENDING"),
                        notes = obj.optString("notes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        val invoices = mutableListOf<Invoice>()
        if (root.has("invoices") && !root.isNull("invoices")) {
            val arr = root.getJSONArray("invoices")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val qId = if (obj.has("quotationId") && !obj.isNull("quotationId")) obj.optLong("quotationId") else null
                invoices.add(
                    Invoice(
                        id = obj.optLong("id", 0),
                        billNumber = obj.optString("billNumber", ""),
                        customerId = obj.optLong("customerId", 0),
                        quotationId = qId,
                        description = obj.optString("description", ""),
                        amount = obj.optDouble("amount", 0.0),
                        discount = obj.optDouble("discount", 0.0),
                        total = obj.optDouble("total", 0.0),
                        status = obj.optString("status", "UNPAID"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        val payments = mutableListOf<Payment>()
        if (root.has("payments") && !root.isNull("payments")) {
            val arr = root.getJSONArray("payments")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                payments.add(
                    Payment(
                        id = obj.optLong("id", 0),
                        billId = obj.optLong("billId", 0),
                        customerId = obj.optLong("customerId", 0),
                        amount = obj.optDouble("amount", 0.0),
                        paymentMode = obj.optString("paymentMode", "CASH"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        }

        val stockTransactions = mutableListOf<StockTransaction>()
        if (root.has("stockTransactions") && !root.isNull("stockTransactions")) {
            val arr = root.getJSONArray("stockTransactions")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                stockTransactions.add(
                    StockTransaction(
                        id = obj.optLong("id", 0),
                        inventoryItemId = obj.optLong("inventoryItemId", 0),
                        type = obj.optString("type", "IN"),
                        quantity = obj.optDouble("quantity", 0.0),
                        purchasePrice = obj.optDouble("purchasePrice", 0.0),
                        reason = obj.optString("reason", ""),
                        reference = obj.optString("reference", ""),
                        date = obj.optLong("date", System.currentTimeMillis())
                    )
                )
            }
        }

        return BackupData(
            version = version,
            exportedAt = exportedAt,
            businessProfile = profile,
            customers = customers,
            inventoryItems = inventoryItems,
            quotations = quotations,
            invoices = invoices,
            payments = payments,
            stockTransactions = stockTransactions
        )
    }

    suspend fun exportBackupToJson(db: AppDatabase): String = withContext(Dispatchers.IO) {
        val profile = db.businessProfileDao().getBusinessProfileOneShot()
        val customers = db.customerDao().getAllCustomersOneShot()
        val inventoryItems = db.inventoryDao().getAllInventoryItemsOneShot()
        val quotations = db.quotationDao().getAllQuotationsOneShot()
        val invoices = db.invoiceDao().getAllInvoicesOneShot()
        val payments = db.paymentDao().getAllPaymentsOneShot()
        val stockTransactions = db.stockTransactionDao().getAllStockTransactionsOneShot()

        val backupData = BackupData(
            businessProfile = profile,
            customers = customers,
            inventoryItems = inventoryItems,
            quotations = quotations,
            invoices = invoices,
            payments = payments,
            stockTransactions = stockTransactions
        )
        serializeBackupToJson(backupData)
    }

    suspend fun importBackupFromJson(db: AppDatabase, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        val backupData = parseBackupJson(jsonString)
        db.restoreDatabase(
            profile = backupData.businessProfile,
            customers = backupData.customers,
            inventoryItems = backupData.inventoryItems,
            quotations = backupData.quotations,
            invoices = backupData.invoices,
            payments = backupData.payments,
            stockTransactions = backupData.stockTransactions
        )
        true
    }
}
