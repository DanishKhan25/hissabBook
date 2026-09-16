package com.example.hisabbook.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun formatCurrency(amount: Double, symbol: String = "₹"): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        val formatted = format.format(amount)
        return if (symbol != "₹") {
            "$symbol ${String.format(Locale.getDefault(), "%.2f", amount)}"
        } else {
            formatted
        }
    }

    fun formatAmountOnly(amount: Double): String {
        return String.format(Locale.getDefault(), "%.2f", amount)
    }
}
