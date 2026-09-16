package com.example.hisabbook.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object PdfShareHelper {

    /**
     * Shares a PDF file directly to WhatsApp (or WhatsApp Business).
     * If WhatsApp is not installed, shows a toast notification and falls back to standard share.
     */
    fun shareToWhatsApp(
        context: Context,
        file: File,
        customerName: String,
        docType: String, // "Invoice" or "Quotation"
        docNumber: String,
        amount: Double,
        businessName: String
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val formattedAmount = CurrencyFormatter.formatAmountOnly(amount)
        val safeCustomerName = customerName.ifBlank { "Customer" }
        val safeBusinessName = businessName.ifBlank { "Business Name" }

        val caption = "Hello $safeCustomerName, please find attached $docType #$docNumber for ₹$formattedAmount from $safeBusinessName."

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
                setPackage(targetPackage)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                showWhatsAppNotInstalledToast(context)
                sharePdf(context, file, "$docType #$docNumber")
            }
        } else {
            showWhatsAppNotInstalledToast(context)
            sharePdf(context, file, "$docType #$docNumber")
        }
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
