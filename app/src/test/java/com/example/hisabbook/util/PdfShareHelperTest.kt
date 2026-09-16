package com.example.hisabbook.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PdfShareHelperTest {

    @Test
    fun testCaptionFormattingForInvoice() {
        val customerName = "John Doe"
        val docType = "Invoice"
        val docNumber = "INV-1001"
        val amount = 16632.00
        val businessName = "IMAGE ALUMINIUM AND GLASS"

        val formattedAmount = CurrencyFormatter.formatAmountOnly(amount)
        val caption = "Hello $customerName, please find attached $docType #$docNumber for ₹$formattedAmount from $businessName."

        assertEquals("Hello John Doe, please find attached Invoice #INV-1001 for ₹16,632.00 from IMAGE ALUMINIUM AND GLASS.", caption)
    }

    @Test
    fun testCaptionFormattingForQuotation() {
        val customerName = "Jane Smith"
        val docType = "Quotation"
        val docNumber = "QTN-2005"
        val amount = 8500.50
        val businessName = "IMAGE ALUMINIUM AND GLASS"

        val formattedAmount = CurrencyFormatter.formatAmountOnly(amount)
        val caption = "Hello $customerName, please find attached $docType #$docNumber for ₹$formattedAmount from $businessName."

        assertEquals("Hello Jane Smith, please find attached Quotation #QTN-2005 for ₹8,500.50 from IMAGE ALUMINIUM AND GLASS.", caption)
    }
}
