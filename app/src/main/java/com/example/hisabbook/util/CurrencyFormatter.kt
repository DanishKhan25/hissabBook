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
}
