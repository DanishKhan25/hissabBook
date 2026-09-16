package com.example.hisabbook.data.local

import com.example.hisabbook.data.local.entity.BusinessProfile
import com.example.hisabbook.data.local.entity.Customer
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.Payment
import com.example.hisabbook.data.local.entity.Quotation
import com.example.hisabbook.data.local.entity.StockTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BackupManagerTest {

    @Test
    fun testBackupDataSerializationAndDeserializationRoundTrip() {
        val originalProfile = BusinessProfile(
            id = 1,
            businessName = "Test Aluminium & Glass Works",
            subtitle = "Fabrication Specialist",
            description = "High quality sliding windows and toughened glass",
            phone = "9876543210",
            address = "Industrial Estate, Area 51",
            languagePreference = "hi",
            currencySymbol = "₹",
            gstNumber = "27AAAAA0000A1Z5",
            isOnboardingCompleted = true
        )

        val originalCustomers = listOf(
            Customer(id = 101, name = "Rahul Sharma", phone = "9988776655", address = "Mumbai"),
            Customer(id = 102, name = "Priya Patel", phone = "8877665544", address = "Pune")
        )

        val originalInventoryItems = listOf(
            InventoryItem(id = 201, name = "Sliding Window Frame", unit = "Pcs", quantity = 25.0, minimumStock = 5.0, purchasePrice = 1200.0),
            InventoryItem(id = 202, name = "Toughened Glass 8mm", unit = "SqFt", quantity = 150.0, minimumStock = 20.0, purchasePrice = 180.0)
        )

        val originalQuotations = listOf(
            Quotation(
                id = 301,
                quotationNumber = "Q-2026-001",
                customerId = 101,
                description = "Office Partition Work",
                quantity = 2.0,
                amount = 25000.0,
                discount = 1000.0,
                total = 24000.0,
                status = "ACCEPTED",
                notes = "Advance paid"
            )
        )

        val originalInvoices = listOf(
            Invoice(
                id = 401,
                billNumber = "INV-2026-001",
                customerId = 101,
                quotationId = 301,
                description = "Office Glass Partition Installation",
                amount = 25000.0,
                discount = 1000.0,
                total = 24000.0,
                status = "PARTIALLY_PAID"
            )
        )

        val originalPayments = listOf(
            Payment(
                id = 501,
                billId = 401,
                customerId = 101,
                amount = 10000.0,
                paymentMode = "UPI",
                notes = "Token amount"
            )
        )

        val originalStockTransactions = listOf(
            StockTransaction(
                id = 601,
                inventoryItemId = 201,
                type = "OUT",
                quantity = 5.0,
                purchasePrice = 1200.0,
                reason = "Used in INV-2026-001",
                reference = "INV-2026-001"
            )
        )

        val backupData = BackupData(
            version = 1,
            exportedAt = 1700000000000L,
            businessProfile = originalProfile,
            customers = originalCustomers,
            inventoryItems = originalInventoryItems,
            quotations = originalQuotations,
            invoices = originalInvoices,
            payments = originalPayments,
            stockTransactions = originalStockTransactions
        )

        // Serialize to JSON String
        val jsonString = BackupManager.serializeBackupToJson(backupData)
        assertTrue(jsonString.isNotBlank())
        assertTrue(jsonString.contains("Test Aluminium & Glass Works"))

        // Deserialize back from JSON String
        val parsedData = BackupManager.parseBackupJson(jsonString)

        assertEquals(1, parsedData.version)
        assertEquals(1700000000000L, parsedData.exportedAt)

        // Business Profile assertion
        val profile = parsedData.businessProfile
        assertNotNull(profile)
        assertEquals(1, profile?.id)
        assertEquals("Test Aluminium & Glass Works", profile?.businessName)
        assertEquals("Fabrication Specialist", profile?.subtitle)
        assertEquals("27AAAAA0000A1Z5", profile?.gstNumber)
        assertEquals("hi", profile?.languagePreference)

        // Customers assertion
        assertEquals(2, parsedData.customers.size)
        assertEquals(101L, parsedData.customers[0].id)
        assertEquals("Rahul Sharma", parsedData.customers[0].name)
        assertEquals(102L, parsedData.customers[1].id)
        assertEquals("Priya Patel", parsedData.customers[1].name)

        // Inventory assertion
        assertEquals(2, parsedData.inventoryItems.size)
        assertEquals(201L, parsedData.inventoryItems[0].id)
        assertEquals("Sliding Window Frame", parsedData.inventoryItems[0].name)
        assertEquals(25.0, parsedData.inventoryItems[0].quantity, 0.001)

        // Quotation assertion
        assertEquals(1, parsedData.quotations.size)
        assertEquals(301L, parsedData.quotations[0].id)
        assertEquals("Q-2026-001", parsedData.quotations[0].quotationNumber)
        assertEquals(101L, parsedData.quotations[0].customerId)

        // Invoice assertion
        assertEquals(1, parsedData.invoices.size)
        assertEquals(401L, parsedData.invoices[0].id)
        assertEquals("INV-2026-001", parsedData.invoices[0].billNumber)
        assertEquals(301L, parsedData.invoices[0].quotationId)

        // Payment assertion
        assertEquals(1, parsedData.payments.size)
        assertEquals(501L, parsedData.payments[0].id)
        assertEquals(401L, parsedData.payments[0].billId)
        assertEquals(10000.0, parsedData.payments[0].amount, 0.001)

        // Stock Transaction assertion
        assertEquals(1, parsedData.stockTransactions.size)
        assertEquals(601L, parsedData.stockTransactions[0].id)
        assertEquals(201L, parsedData.stockTransactions[0].inventoryItemId)
    }

    @Test
    fun testEmptyBackupDataSerialization() {
        val emptyData = BackupData(
            version = 1,
            exportedAt = 1700000000000L,
            businessProfile = null,
            customers = emptyList(),
            inventoryItems = emptyList(),
            quotations = emptyList(),
            invoices = emptyList(),
            payments = emptyList(),
            stockTransactions = emptyList()
        )

        val jsonString = BackupManager.serializeBackupToJson(emptyData)
        val parsed = BackupManager.parseBackupJson(jsonString)

        assertNull(parsed.businessProfile)
        assertTrue(parsed.customers.isEmpty())
        assertTrue(parsed.inventoryItems.isEmpty())
        assertTrue(parsed.quotations.isEmpty())
        assertTrue(parsed.invoices.isEmpty())
        assertTrue(parsed.payments.isEmpty())
        assertTrue(parsed.stockTransactions.isEmpty())
    }

    @Test
    fun testInvalidJsonThrowsException() {
        try {
            BackupManager.parseBackupJson("not a valid json string")
            fail("Expected IllegalArgumentException for invalid JSON")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Invalid JSON format") == true)
        }
    }

    @Test
    fun testEmptyJsonContentThrowsException() {
        try {
            BackupManager.parseBackupJson("   ")
            fail("Expected IllegalArgumentException for blank JSON string")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Backup JSON content is empty") == true)
        }
    }

    @Test
    fun testJsonWithMissingOptionalFieldsUsesDefaults() {
        val json = """
            {
              "version": 1,
              "customers": [
                { "name": "Minimal Customer" }
              ]
            }
        """.trimIndent()

        val parsed = BackupManager.parseBackupJson(json)
        assertEquals(1, parsed.customers.size)
        assertEquals("Minimal Customer", parsed.customers[0].name)
        assertEquals(0L, parsed.customers[0].id)
        assertEquals("", parsed.customers[0].phone)
    }
}
