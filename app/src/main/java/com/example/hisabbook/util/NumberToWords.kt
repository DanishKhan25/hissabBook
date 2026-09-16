package com.example.hisabbook.util

object NumberToWords {

    private val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun convert(amount: Double): String {
        if (amount <= 0.0) return "Rupees Zero Only"

        val wholePart = amount.toLong()
        val decimalPart = Math.round((amount - wholePart) * 100)

        val wholeWords = convertWholeNumber(wholePart)
        val result = StringBuilder("Rupees ").append(wholeWords)

        if (decimalPart > 0) {
            val decimalWords = convertWholeNumber(decimalPart)
            result.append(" and ").append(decimalWords).append(" Paise")
        }

        result.append(" Only")
        return result.toString()
    }

    fun convertToWordsCapitalized(amount: Double): String {
        if (amount <= 0.0) return "Zero Rupees"

        val wholePart = amount.toLong()
        val decimalPart = Math.round((amount - wholePart) * 100)

        val wholeWords = convertWholeNumber(wholePart)
        val result = StringBuilder()

        if (wholePart > 0) {
            result.append(wholeWords).append(" Rupees")
        } else {
            result.append("Zero Rupees")
        }

        if (decimalPart > 0) {
            val decimalWords = convertWholeNumber(decimalPart)
            result.append(" and ").append(decimalWords).append(" Paise")
        }

        return result.toString()
    }

    private fun convertWholeNumber(number: Long): String {
        if (number == 0L) return "Zero"

        if (number < 20) {
            return units[number.toInt()]
        }
        if (number < 100) {
            return tens[(number / 10).toInt()] + (if (number % 10 != 0L) " " + units[(number % 10).toInt()] else "")
        }
        if (number < 1000) {
            return units[(number / 100).toInt()] + " Hundred" + (if (number % 100 != 0L) " " + convertWholeNumber(number % 100) else "")
        }
        if (number < 100000) {
            return convertWholeNumber(number / 1000) + " Thousand" + (if (number % 1000 != 0L) " " + convertWholeNumber(number % 1000) else "")
        }
        if (number < 10000000) {
            return convertWholeNumber(number / 100000) + " Lakh" + (if (number % 100000 != 0L) " " + convertWholeNumber(number % 100000) else "")
        }
        return convertWholeNumber(number / 10000000) + " Crore" + (if (number % 10000000 != 0L) " " + convertWholeNumber(number % 10000000) else "")
    }
}
