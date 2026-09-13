package com.rushi.mitavyay.data.model

import com.rushi.mitavyay.util.DateTimeFormatter
import java.util.Calendar

sealed class DateFilter {
    abstract val label: String

    object AllTime : DateFilter() {
        override val label: String = "All Time"
    }

    object Today : DateFilter() {
        override val label: String = "Today"
    }

    object ThisWeek : DateFilter() {
        override val label: String = "This Week"
    }

    object ThisMonth : DateFilter() {
        override val label: String = "This Month"
    }

    data class CustomRange(
        val startDateMs: Long,
        val endDateMs: Long
    ) : DateFilter() {
        override val label: String = "Custom"
    }
}

/**
 * Evaluates whether a given timestamp matches the date filter.
 */
fun DateFilter.matches(timestamp: Long): Boolean {
    return when (this) {
        is DateFilter.AllTime -> true
        is DateFilter.Today -> {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis

            timestamp in start..end
        }
        is DateFilter.ThisWeek -> {
            val cal = Calendar.getInstance()
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis

            cal.add(Calendar.DAY_OF_MONTH, 6)
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis

            timestamp in start..end
        }
        is DateFilter.ThisMonth -> {
            val (start, end) = DateTimeFormatter.getMonthStartAndEndTimestamps(DateTimeFormatter.getCurrentMonthYear())
            timestamp in start..end
        }
        is DateFilter.CustomRange -> {
            val actualStart = kotlin.math.min(startDateMs, endDateMs)
            val actualEnd = kotlin.math.max(startDateMs, endDateMs)

            val calStart = Calendar.getInstance().apply {
                timeInMillis = actualStart
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val calEnd = Calendar.getInstance().apply {
                timeInMillis = actualEnd
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            timestamp in calStart.timeInMillis..calEnd.timeInMillis
        }
    }
}
