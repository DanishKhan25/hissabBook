package com.example.hisabbook.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberToWordsTest {

    @Test
    fun testConvertFortyEightThousand() {
        val result = NumberToWords.convert(48000.0)
        assertEquals("Rupees Forty Eight Thousand Only", result)
    }

    @Test
    fun testConvertZero() {
        val result = NumberToWords.convert(0.0)
        assertEquals("Rupees Zero Only", result)
    }

    @Test
    fun testConvertWithDecimalPaise() {
        val result = NumberToWords.convert(1250.50)
        assertEquals("Rupees One Thousand Two Hundred Fifty and Fifty Paise Only", result)
    }

    @Test
    fun testConvertLakh() {
        val result = NumberToWords.convert(150000.0)
        assertEquals("Rupees One Lakh Fifty Thousand Only", result)
    }
}
