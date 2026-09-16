package com.example.hisabbook.util

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountParsingTest {

    @Test
    fun testParseCleanDouble_withCommas() {
        val input = "1,228.00"
        val parsed = input.parseCleanDouble()
        assertEquals(1228.0, parsed!!, 0.001)
    }

    @Test
    fun testParseCleanDouble_largeNumberWithCommas() {
        val input = "1,234,567.89"
        val parsed = input.parseCleanDouble()
        assertEquals(1234567.89, parsed!!, 0.001)
    }

    @Test
    fun testParseCleanDouble_withCurrencySymbolAndSpaces() {
        val input = "₹ 1,228.00"
        val parsed = input.parseCleanDouble()
        assertEquals(1228.0, parsed!!, 0.001)
    }

    @Test
    fun testParseCleanDoubleOrDefault_blankAndInvalidInputs() {
        val emptyInput = ""
        assertEquals(0.0, emptyInput.parseCleanDoubleOrDefault(0.0), 0.001)

        val invalidInput = "abc"
        assertEquals(0.0, invalidInput.parseCleanDoubleOrDefault(0.0), 0.001)

        val nullInput: String? = null
        assertEquals(0.0, nullInput.parseCleanDoubleOrDefault(0.0), 0.001)
    }

    @Test
    fun testCurrencyFormatterParseAmount() {
        val amount = CurrencyFormatter.parseAmount("1,228.00")
        assertEquals(1228.0, amount, 0.001)
    }
}
