package com.rushi.mitavyay.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class FormattersTest {

    @Test
    fun currencyFormatter_formatsPositiveAmounts() {
        assertEquals("₹0.00", CurrencyFormatter.format(0L))
        assertEquals("₹10.50", CurrencyFormatter.format(1050L))
        assertEquals("₹100.00", CurrencyFormatter.format(10000L))
        assertEquals("₹1,000.00", CurrencyFormatter.format(100000L))
        assertEquals("₹10,00,000.00", CurrencyFormatter.format(100000000L))
    }

    @Test
    fun currencyFormatter_formatsNegativeAmounts() {
        assertEquals("-₹50.00", CurrencyFormatter.format(-5000L))
        assertEquals("-₹1,250.75", CurrencyFormatter.format(-125075L))
    }

    @Test
    fun currencyFormatter_parsesStringsCorrectlyWithoutFloats() {
        assertEquals(0L, CurrencyFormatter.parseToPaise(""))
        assertEquals(1050L, CurrencyFormatter.parseToPaise("10.50"))
        assertEquals(1000L, CurrencyFormatter.parseToPaise("10"))
        assertEquals(1050L, CurrencyFormatter.parseToPaise("10.5"))
        assertEquals(5L, CurrencyFormatter.parseToPaise("0.05"))
        assertEquals(125075L, CurrencyFormatter.parseToPaise("₹1,250.75"))
        assertEquals(-2500L, CurrencyFormatter.parseToPaise("-25.00"))
    }

    @Test
    fun dateTimeFormatter_formatsTimestamps() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.SEPTEMBER, 8, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis

        val formattedDate = DateTimeFormatter.formatDate(timestamp, "yyyy-MM-dd")
        assertEquals("2026-09-08", formattedDate)

        assertEquals("", DateTimeFormatter.formatDate(0L))
    }

    @Test
    fun dateTimeFormatter_formatRelativeDate() {
        val now = System.currentTimeMillis()
        val formattedToday = DateTimeFormatter.formatRelativeDate(now)
        assert(formattedToday.startsWith("Today, "))

        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val formattedYesterday = DateTimeFormatter.formatRelativeDate(calYesterday.timeInMillis)
        assert(formattedYesterday.startsWith("Yesterday, "))

        val calPast = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 15, 10, 0, 0)
        }
        val formattedPast = DateTimeFormatter.formatRelativeDate(calPast.timeInMillis)
        assert(!formattedPast.startsWith("Today") && !formattedPast.startsWith("Yesterday"))
        assert(formattedPast.contains("15 Jan 2025"))
    }
}
