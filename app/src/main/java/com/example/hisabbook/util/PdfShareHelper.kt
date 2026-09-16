package com.example.hisabbook.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object PdfShareHelper {

    /**
     * Cleanly formats a phone number for WhatsApp JID targeting.
     * Strips non-digit characters. Prepends country code '91' for 10-digit numbers.
     */
    fun formatPhoneNumber(phone: String?): String {
        if (phone.isNullOrBlank()) return ""
        val digits = phone.filter { it.isDigit() }
        val cleanDigits = if (digits.length == 11 && digits.startsWith("0")) {
            digits.substring(1)
        } else {
            digits
        }
        return if (cleanDigits.length == 10) {
            "91$cleanDigits"
        } else {
            cleanDigits
        }
    }

    /**
     * Builds professional WhatsApp caption for Invoices.
     */
    fun buildInvoiceCaption(
        customerName: String,
        invoiceNo: String,
        date: String,
        totalAmount: Double,
        paidAmount: Double,
        balanceDue: Double,
        shopName: String,
        shopPhone: String = ""
    ): String {
        val safeCustomerName = customerName.ifBlank { "Customer" }
        val safeShopName = shopName.ifBlank { "Business Name" }
        val formattedTotal = CurrencyFormatter.formatAmountOnly(totalAmount)
        val formattedPaid = CurrencyFormatter.formatAmountOnly(paidAmount)
        val formattedBalance = CurrencyFormatter.formatAmountOnly(balanceDue)

        val sb = StringBuilder()
        sb.append("📄 *INVOICE FROM ").append(safeShopName).append("*\n\n")
        sb.append("Hello *").append(safeCustomerName).append("*,\n\n")
        sb.append("Please find attached your invoice for your order:\n\n")
        sb.append("📋 *Invoice No:* ").append(invoiceNo).append("\n")
        sb.append("📅 *Date:* ").append(date).append("\n")
        sb.append("💰 *Total Amount:* ₹").append(formattedTotal).append("\n")
        sb.append("💳 *Paid Amount:* ₹").append(formattedPaid).append("\n")
        sb.append("🔴 *Balance Due:* ₹").append(formattedBalance).append("\n\n")
        sb.append("Thank you for your business! For any questions, please feel free to reach out.\n\n")
        sb.append("Regards,\n")
        sb.append("*").append(safeShopName).append("*")
        if (shopPhone.isNotBlank()) {
            sb.append("\n📱 ").append(shopPhone)
        }
        return sb.toString()
    }

    /**
     * Builds professional WhatsApp caption for Quotations.
     */
    fun buildQuotationCaption(
        customerName: String,
        quotationNo: String,
        date: String,
        totalAmount: Double,
        shopName: String,
        shopPhone: String = ""
    ): String {
        val safeCustomerName = customerName.ifBlank { "Customer" }
        val safeShopName = shopName.ifBlank { "Business Name" }
        val formattedTotal = CurrencyFormatter.formatAmountOnly(totalAmount)

        val sb = StringBuilder()
        sb.append("📝 *QUOTATION FROM ").append(safeShopName).append("*\n\n")
        sb.append("Hello *").append(safeCustomerName).append("*,\n\n")
        sb.append("Thank you for contacting us! Please find attached your quotation details:\n\n")
        sb.append("📋 *Quotation No:* ").append(quotationNo).append("\n")
        sb.append("📅 *Date:* ").append(date).append("\n")
        sb.append("💰 *Estimated Total:* ₹").append(formattedTotal).append("\n\n")
        sb.append("Please review the quote and let us know if you would like to proceed or need any changes.\n\n")
        sb.append("Regards,\n")
        sb.append("*").append(safeShopName).append("*")
        if (shopPhone.isNotBlank()) {
            sb.append("\n📱 ").append(shopPhone)
        }
        return sb.toString()
    }

    /**
     * Shares a PDF file directly to WhatsApp (or WhatsApp Business) with a pre-filled caption
     * and optional target customer JID.
     */
    fun shareToWhatsApp(
        context: Context,
        file: File,
        caption: String,
        customerPhone: String? = null
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val formattedPhone = formatPhoneNumber(customerPhone)

        val whatsappPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
        var targetPackage: String? = null

        val pm = context.packageManager
        for (pkg in whatsappPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                targetPackage = pkg
                break
            } catch (_: Exception) {
            }
        }

        if (targetPackage != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                if (formattedPhone.isNotEmpty()) {
                    putExtra("jid", "$formattedPhone@s.whatsapp.net")
                }
                setPackage(targetPackage)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                showWhatsAppNotInstalledToast(context)
                sharePdf(context, file, "Document")
            }
        } else {
            showWhatsAppNotInstalledToast(context)
            sharePdf(context, file, "Document")
        }
    }

    /**
     * Shares invoice or quotation details directly to WhatsApp.
     */
    fun shareToWhatsApp(
        context: Context,
        file: File,
        customerName: String,
        customerPhone: String? = null,
        docType: String, // "Invoice" or "Quotation"
        docNumber: String,
        docDate: String = "",
        totalAmount: Double,
        paidAmount: Double = 0.0,
        balanceDue: Double = 0.0,
        businessName: String,
        businessPhone: String = ""
    ) {
        val caption = if (docType.equals("Invoice", ignoreCase = true) || docType.equals("Bill", ignoreCase = true)) {
            buildInvoiceCaption(
                customerName = customerName,
                invoiceNo = docNumber,
                date = docDate,
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                balanceDue = balanceDue,
                shopName = businessName,
                shopPhone = businessPhone
            )
        } else if (docType.equals("Quotation", ignoreCase = true) || docType.equals("Quote", ignoreCase = true)) {
            buildQuotationCaption(
                customerName = customerName,
                quotationNo = docNumber,
                date = docDate,
                totalAmount = totalAmount,
                shopName = businessName,
                shopPhone = businessPhone
            )
        } else {
            val formattedAmount = CurrencyFormatter.formatAmountOnly(totalAmount)
            val safeCustomerName = customerName.ifBlank { "Customer" }
            val safeBusinessName = businessName.ifBlank { "Business Name" }
            "Hello $safeCustomerName, please find attached $docType #$docNumber for ₹$formattedAmount from $safeBusinessName."
        }

        shareToWhatsApp(context, file, caption, customerPhone)
    }

    /**
     * Standard Android PDF share sheet.
     */
    fun sharePdf(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Please find attached $title.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Document via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun showWhatsAppNotInstalledToast(context: Context) {
        Toast.makeText(
            context,
            "WhatsApp is not installed. Opening system share...",
            Toast.LENGTH_SHORT
        ).show()
    }
}
