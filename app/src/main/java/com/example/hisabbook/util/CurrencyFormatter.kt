package com.example.hisabbook.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun formatCurrency(amount: Double, symbol: String = "₹"): String {
        return "$symbol ${formatAmountOnly(amount)}"
    }

    fun formatAmountOnly(amount: Double): String {
        return try {
            val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")) as DecimalFormat
            formatter.applyPattern("#,##,##0.00")
            formatter.format(amount)
        } catch (_: Exception) {
            String.format(Locale.US, "%.2f", amount)
        }
    }

    fun parseAmount(amountStr: String?): Double {
        if (amountStr.isNullOrBlank()) return 0.0
        val clean = amountStr.replace("₹", "").replace(",", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    fun parseAmountOrNull(amountStr: String?): Double? {
        if (amountStr.isNullOrBlank()) return null
        val clean = amountStr.replace("₹", "").replace(",", "").trim()
        return clean.toDoubleOrNull()
    }
}

fun String?.parseCleanDouble(): Double? = CurrencyFormatter.parseAmountOrNull(this)
fun String?.parseCleanDoubleOrDefault(default: Double = 0.0): Double = CurrencyFormatter.parseAmountOrNull(this) ?: default

