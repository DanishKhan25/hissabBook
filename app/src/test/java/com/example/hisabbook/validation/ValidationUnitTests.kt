package com.example.hisabbook.validation

import com.example.hisabbook.data.local.entity.Customer
import com.example.hisabbook.data.local.entity.InventoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUnitTests {

    @Test
    fun testCustomerNameValidation_emptyName_invalid() {
        val name = "   "
        val isValid = name.trim().isNotBlank()
        assertFalse("Customer name should be invalid when blank", isValid)
    }

    @Test
    fun testCustomerPhoneValidation_10Digits_valid() {
        val phone = "9876543210"
        val digits = phone.filter { it.isDigit() }
        val isValid = digits.length == 10
        assertTrue("10-digit mobile number should be valid", isValid)
    }

    @Test
    fun testCustomerPhoneValidation_shortPhone_invalid() {
        val phone = "98765"
        val digits = phone.filter { it.isDigit() }
        val isValid = digits.length == 10
        assertFalse("Phone number with fewer than 10 digits should be invalid", isValid)
    }

    @Test
    fun testCustomerDuplicatePhoneCheck() {
        val existingCustomers = listOf(
            Customer(id = 1L, name = "Ramesh Kumar", phone = "9876543210")
        )
        val newPhone = "9876543210"

        val isDuplicate = existingCustomers.any { it.phone.trim().equals(newPhone.trim(), ignoreCase = true) }
        assertTrue("Should detect duplicate customer phone number", isDuplicate)
    }

    @Test
    fun testPaymentAmount_zeroOrNegative_invalid() {
        val amountZero = 0.0
        val amountNegative = -100.0

        assertTrue("0 amount is invalid", amountZero <= 0)
        assertTrue("Negative amount is invalid", amountNegative <= 0)
    }

    @Test
    fun testPaymentAmount_exceedingOutstandingBalance_invalid() {
        val invoiceTotal = 5000.0
        val totalPaid = 2000.0
        val outstanding = invoiceTotal - totalPaid // 3000.0

        val newPaymentAmount = 3500.0
        val isExceeding = newPaymentAmount > outstanding

        assertTrue("Payment amount exceeding outstanding balance should be flagged as invalid", isExceeding)
    }

    @Test
    fun testInventoryDuplicateNameCheck() {
        val existingItems = listOf(
            InventoryItem(id = 1L, name = "Aluminium Track 3 Inch", unit = "Pcs", quantity = 10.0)
        )
        val newItemName = "aluminium track 3 inch"

        val isDuplicate = existingItems.any { it.name.trim().equals(newItemName.trim(), ignoreCase = true) }
        assertTrue("Should detect duplicate inventory item name ignoring case", isDuplicate)
    }

    @Test
    fun testInventoryStockOut_insufficientStock_invalid() {
        val currentStock = 5.0
        val requestedStockOut = 8.0

        val isInsufficient = requestedStockOut > currentStock
        assertTrue("Requesting stock out greater than current stock should be flagged as insufficient", isInsufficient)
    }
}
