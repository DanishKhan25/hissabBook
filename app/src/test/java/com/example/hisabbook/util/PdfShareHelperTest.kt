package com.example.hisabbook.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PdfShareHelperTest {

    @Test
    fun testPhoneNumberFormattingAndJid() {
        // 10 digits -> prepends '91'
        val raw10Digit = "7058540991"
        val formatted10 = PdfShareHelper.formatPhoneNumber(raw10Digit)
        assertEquals("917058540991", formatted10)
        assertEquals("917058540991@s.whatsapp.net", "$formatted10@s.whatsapp.net")

        // Number with +, spaces, hyphens
        val rawFormatted = "+91 70585-40991"
        val formattedWithChars = PdfShareHelper.formatPhoneNumber(rawFormatted)
        assertEquals("917058540991", formattedWithChars)

        // 11 digits starting with 0
        val rawWithZero = "07058540991"
        val formattedWithZero = PdfShareHelper.formatPhoneNumber(rawWithZero)
        assertEquals("917058540991", formattedWithZero)

        // Already has 91 prefix (12 digits)
        val raw12Digit = "917058540991"
        val formatted12 = PdfShareHelper.formatPhoneNumber(raw12Digit)
        assertEquals("917058540991", formatted12)

        // International number
        val rawUS = "+1 (555) 000-1234"
        val formattedUS = PdfShareHelper.formatPhoneNumber(rawUS)
        assertEquals("15550001234", formattedUS)

        // Null and Blank
        assertEquals("", PdfShareHelper.formatPhoneNumber(""))
        assertEquals("", PdfShareHelper.formatPhoneNumber("   "))
        assertEquals("", PdfShareHelper.formatPhoneNumber(null))
    }

    @Test
    fun testInvoiceCaptionTemplateGeneration() {
        val customerName = "John Doe"
        val invoiceNo = "INV-1001"
        val date = "24 May 2024"
        val totalAmount = 16632.00
        val paidAmount = 10000.00
        val balanceDue = 6632.00
        val shopName = "Sharma Traders"
        val shopPhone = "9876543210"

        val caption = PdfShareHelper.buildInvoiceCaption(
            customerName = customerName,
            invoiceNo = invoiceNo,
            date = date,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            balanceDue = balanceDue,
            shopName = shopName,
            shopPhone = shopPhone
        )

        val expected = """
            📄 *INVOICE FROM Sharma Traders*

            Hello *John Doe*,

            Please find attached your invoice for your order:

            📋 *Invoice No:* INV-1001
            📅 *Date:* 24 May 2024
            💰 *Total Amount:* ₹16,632.00
            💳 *Paid Amount:* ₹10,000.00
            🔴 *Balance Due:* ₹6,632.00

            Thank you for your business! For any questions, please feel free to reach out.

            Regards,
            *Sharma Traders*
            📱 9876543210
        """.trimIndent()

        assertEquals(expected, caption)
    }

    @Test
    fun testInvoiceCaptionTemplateWithoutShopPhone() {
        val caption = PdfShareHelper.buildInvoiceCaption(
            customerName = "John Doe",
            invoiceNo = "INV-1001",
            date = "24 May 2024",
            totalAmount = 1000.00,
            paidAmount = 0.00,
            balanceDue = 1000.00,
            shopName = "Sharma Traders",
            shopPhone = ""
        )

        val expected = """
            📄 *INVOICE FROM Sharma Traders*

            Hello *John Doe*,

            Please find attached your invoice for your order:

            📋 *Invoice No:* INV-1001
            📅 *Date:* 24 May 2024
            💰 *Total Amount:* ₹1,000.00
            💳 *Paid Amount:* ₹0.00
            🔴 *Balance Due:* ₹1,000.00

            Thank you for your business! For any questions, please feel free to reach out.

            Regards,
            *Sharma Traders*
        """.trimIndent()

        assertEquals(expected, caption)
    }

    @Test
    fun testQuotationCaptionTemplateGeneration() {
        val customerName = "Jane Smith"
        val quotationNo = "QTN-2005"
        val date = "24 May 2024"
        val totalAmount = 8500.50
        val shopName = "Sharma Traders"
        val shopPhone = "9876543210"

        val caption = PdfShareHelper.buildQuotationCaption(
            customerName = customerName,
            quotationNo = quotationNo,
            date = date,
            totalAmount = totalAmount,
            shopName = shopName,
            shopPhone = shopPhone
        )

        val expected = """
            📝 *QUOTATION FROM Sharma Traders*

            Hello *Jane Smith*,

            Thank you for contacting us! Please find attached your quotation details:

            📋 *Quotation No:* QTN-2005
            📅 *Date:* 24 May 2024
            💰 *Estimated Total:* ₹8,500.50

            Please review the quote and let us know if you would like to proceed or need any changes.

            Regards,
            *Sharma Traders*
            📱 9876543210
        """.trimIndent()

        assertEquals(expected, caption)
    }

    @Test
    fun testQuotationCaptionTemplateWithoutShopPhone() {
        val caption = PdfShareHelper.buildQuotationCaption(
            customerName = "Jane Smith",
            quotationNo = "QTN-2005",
            date = "24 May 2024",
            totalAmount = 8500.50,
            shopName = "Sharma Traders",
            shopPhone = ""
        )

        val expected = """
            📝 *QUOTATION FROM Sharma Traders*

            Hello *Jane Smith*,

            Thank you for contacting us! Please find attached your quotation details:

            📋 *Quotation No:* QTN-2005
            📅 *Date:* 24 May 2024
            💰 *Estimated Total:* ₹8,500.50

            Please review the quote and let us know if you would like to proceed or need any changes.

            Regards,
            *Sharma Traders*
        """.trimIndent()

        assertEquals(expected, caption)
    }
}
