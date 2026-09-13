package com.rushi.mitavyay.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Centralized formatting utilities as required by ARCHITECTURE.md.
 *
 * Monetary Rule:
 * All monetary amounts are stored as [Long] in the smallest currency unit (paise/cents).
 * Conversion to display happens exclusively here.
 */
object CurrencyFormatter {

    /**
     * Converts an amount in smallest currency unit (e.g. paise) to a formatted string.
     * Uses Indian numbering system grouping (lakhs & crores: ₹10,00,000.00).
     * Example: 1050L -> "₹10.50", -25000L -> "-₹250.00"
     */
    fun format(amountPaise: Long, currencySymbol: String = "₹"): String {
        val isNegative = amountPaise < 0
        val absPaise = kotlin.math.abs(amountPaise)
        val whole = absPaise / 100
        val fraction = absPaise % 100

        val formattedWhole = formatIndianGrouping(whole)
        val fractionStr = String.format(Locale.ROOT, "%02d", fraction)

        val result = "$currencySymbol$formattedWhole.$fractionStr"
        return if (isNegative) "-$result" else result
    }

    /**
     * Formats integer according to the Indian numbering system:
     * Groups the last 3 digits, and every 2 digits thereafter (e.g. 10,00,000).
     */
    private fun formatIndianGrouping(number: Long): String {
        val s = number.toString()
        if (s.length <= 3) return s

        val last3 = s.substring(s.length - 3)
        val remaining = s.substring(0, s.length - 3)
        val sb = StringBuilder()

        for (i in remaining.indices) {
            if (i > 0 && (remaining.length - i) % 2 == 0) {
                sb.append(',')
            }
            sb.append(remaining[i])
        }
        sb.append(',').append(last3)
        return sb.toString()
    }

    /**
     * Parses an input string (e.g., "12.50") into smallest currency unit (paise: 1250L).
     * Avoids floating-point arithmetic entirely to preserve financial precision.
     */
    fun parseToPaise(input: String): Long {
        val sanitized = input.trim().replace(",", "").replace("₹", "")
        if (sanitized.isEmpty()) return 0L

        val isNegative = sanitized.startsWith("-")
        val clean = if (isNegative) sanitized.substring(1) else sanitized

        val parts = clean.split(".")
        val wholePart = parts[0].filter { it.isDigit() }.toLongOrNull() ?: 0L
        val fractionPart = if (parts.size > 1) {
            val decimals = parts[1].filter { it.isDigit() }.take(2)
            decimals.padEnd(2, '0').toIntOrNull() ?: 0
        } else {
            0
        }

        val totalPaise = (wholePart * 100) + fractionPart
        return if (isNegative) -totalPaise else totalPaise
    }
}

object DateTimeFormatter {

    private val locale = Locale.getDefault()

    fun formatDate(timestampMs: Long, pattern: String = "dd MMM yyyy"): String {
        if (timestampMs <= 0) return ""
        val sdf = SimpleDateFormat(pattern, locale)
        return sdf.format(Date(timestampMs))
    }

    fun formatDateTime(timestampMs: Long, pattern: String = "dd MMM yyyy, hh:mm a"): String {
        if (timestampMs <= 0) return ""
        val sdf = SimpleDateFormat(pattern, locale)
        return sdf.format(Date(timestampMs))
    }

    fun formatShortDate(timestampMs: Long): String {
        return formatDate(timestampMs, "dd/MM/yyyy")
    }

    /**
     * Formats timestamp into relative date description (e.g. "Today, 13 Sep 2026", "Yesterday, 12 Sep 2026", or "11 Sep 2026").
     */
    fun formatRelativeDate(timestampMs: Long): String {
        if (timestampMs <= 0) return ""
        val calSelected = Calendar.getInstance().apply { timeInMillis = timestampMs }
        val calToday = Calendar.getInstance()
        val isToday = calSelected.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                calSelected.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)
        if (isToday) {
            return "Today, ${formatDate(timestampMs)}"
        }
        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = calSelected.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) &&
                calSelected.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)
        if (isYesterday) {
            return "Yesterday, ${formatDate(timestampMs)}"
        }
        return formatDate(timestampMs)
    }

    /**
     * Returns current month in "yyyy-MM" format (e.g. "2026-09").
     */
    fun getCurrentMonthYear(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(Date())
    }

    /**
     * Formats "yyyy-MM" (e.g. "2026-09") into localized human readable string (e.g. "September 2026").
     */
    fun formatMonthYear(monthYear: String): String {
        return try {
            val inSdf = SimpleDateFormat("yyyy-MM", Locale.US)
            val date = inSdf.parse(monthYear) ?: return monthYear
            val outSdf = SimpleDateFormat("MMMM yyyy", locale)
            outSdf.format(date)
        } catch (_: Exception) {
            monthYear
        }
    }

    /**
     * Computes start (00:00:00.000) and end (23:59:59.999) timestamps in ms for a given "yyyy-MM" string.
     */
    fun getMonthStartAndEndTimestamps(monthYear: String): Pair<Long, Long> {
        return try {
            val parts = monthYear.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis

            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, maxDay)
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis
            Pair(start, end)
        } catch (_: Exception) {
            val now = System.currentTimeMillis()
            Pair(now, now)
        }
    }

    /**
     * Offsets a "yyyy-MM" string by [offsetMonths] (e.g. -1 for previous month, +1 for next month).
     */
    fun getAdjacentMonthYear(monthYear: String, offsetMonths: Int): String {
        return try {
            val parts = monthYear.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.add(Calendar.MONTH, offsetMonths)
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            sdf.format(cal.time)
        } catch (_: Exception) {
            monthYear
        }
    }
}
