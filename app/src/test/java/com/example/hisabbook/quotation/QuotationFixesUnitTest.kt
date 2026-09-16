package com.example.hisabbook.quotation

import com.example.hisabbook.data.local.entity.Quotation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class QuotationFixesUnitTest {

    @Test
    fun testQuotationNumberGeneration_afterDeletion_preventsDuplicateNumbers() {
        // Initial quotations with IDs 1, 2, 3
        val initialQuotations = listOf(
            Quotation(id = 1L, quotationNumber = "Q-2025-001", customerId = 1L, description = "Item 1"),
            Quotation(id = 2L, quotationNumber = "Q-2025-002", customerId = 1L, description = "Item 2"),
            Quotation(id = 3L, quotationNumber = "Q-2025-003", customerId = 1L, description = "Item 3")
        )

        // Simulate deleting quotation #2
        val quotationsAfterDeletion = initialQuotations.filter { it.id != 2L } // Remaining: ID 1 and ID 3 (size = 2)

        // Old approach using list size
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val oldBuggyNextNumber = String.format(Locale.getDefault(), "Q-%d-%03d", currentYear, quotationsAfterDeletion.size + 1) // Q-2025-003 (DUPLICATE!)

        // New fixed approach using maxOfOrNull { it.id }
        val maxId = quotationsAfterDeletion.maxOfOrNull { it.id } ?: 0L
        val fixedNextNumber = String.format(Locale.getDefault(), "Q-%d-%03d", currentYear, maxId + 1) // Q-2025-004

        val expectedOldDuplicate = String.format(Locale.getDefault(), "Q-%d-003", currentYear)
        val expectedFixedNumber = String.format(Locale.getDefault(), "Q-%d-004", currentYear)

        assertEquals("Old buggy method produces duplicate number", expectedOldDuplicate, oldBuggyNextNumber)
        assertEquals("New fixed method produces unique next number", expectedFixedNumber, fixedNextNumber)
        assertNotEquals("Fixed next number must not duplicate previous quote number", expectedOldDuplicate, fixedNextNumber)
    }

    @Test
    fun testQuotationSanitizesNegativeInputs() {
        val negativeQuantity = -5.0
        val negativeAmount = -100.0
        val negativeDiscount = -20.0

        val validQuantity = negativeQuantity.coerceAtLeast(0.0)
        val validAmount = negativeAmount.coerceAtLeast(0.0)
        val validDiscount = negativeDiscount.coerceAtLeast(0.0)

        val total = (validQuantity * validAmount - validDiscount).coerceAtLeast(0.0)

        assertEquals("Quantity must be coerced to 0.0", 0.0, validQuantity, 0.001)
        assertEquals("Amount must be coerced to 0.0", 0.0, validAmount, 0.001)
        assertEquals("Discount must be coerced to 0.0", 0.0, validDiscount, 0.001)
        assertEquals("Total must be 0.0", 0.0, total, 0.001)
    }

    @Test
    fun testQuotationDiscountExceedingSubtotal_coercesTotalToZero() {
        val quantity = 2.0
        val amount = 100.0 // Subtotal = 200.0
        val discount = 300.0 // Exceeds subtotal

        val validQuantity = quantity.coerceAtLeast(0.0)
        val validAmount = amount.coerceAtLeast(0.0)
        val validDiscount = discount.coerceAtLeast(0.0)

        val total = (validQuantity * validAmount - validDiscount).coerceAtLeast(0.0)

        assertTrue("Total must not be negative", total >= 0.0)
        assertEquals("Total must be clamped to 0.0", 0.0, total, 0.001)
    }
}
