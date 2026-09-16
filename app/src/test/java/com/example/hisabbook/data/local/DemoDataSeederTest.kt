package com.example.hisabbook.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DemoDataSeederTest {

    @Test
    fun testDemoCustomers() {
        val customers = DemoDataSeeder.getDemoCustomers()
        assertEquals(4, customers.size)
        val sahil = customers.find { it.name == "Sahil" }
        assertNotNull(sahil)
        assertEquals("7058540991", sahil?.phone)
        assertEquals("Plot 42, Green Park", sahil?.address)
    }

    @Test
    fun testDemoInventoryItems() {
        val items = DemoDataSeeder.getDemoInventoryItems()
        assertEquals(11, items.size)
        val hdAluminium = items.find { it.name == "HD- ALUMINIUM" }
        assertNotNull(hdAluminium)
        assertEquals(500.0, hdAluminium?.quantity ?: 0.0, 0.01)
        assertEquals(535.0, hdAluminium?.purchasePrice ?: 0.0, 0.01)
    }

    @Test
    fun testDemoInvoicesAndPayments() {
        val invoices = DemoDataSeeder.getDemoInvoices()
        val payments = DemoDataSeeder.getDemoPayments()

        assertEquals(3, invoices.size)
        assertEquals(3, payments.size)

        val b001 = invoices.find { it.billNumber == "B-2026-001" }
        assertNotNull(b001)
        assertEquals(48000.0, b001?.total ?: 0.0, 0.01)

        val p001 = payments.find { it.billId == b001?.id }
        assertNotNull(p001)
        assertEquals(20000.0, p001?.amount ?: 0.0, 0.01)
    }
}
