package com.rushi.mitavyay.data.model

import com.rushi.mitavyay.data.db.RepeatExpense
import com.rushi.mitavyay.util.CurrencyFormatter
import com.rushi.mitavyay.util.DateTimeFormatter

data class RepeatExpenseDisplayItem(
    val id: String,
    val description: String,
    val amountFormatted: String,
    val amountPaise: Long = 0L,
    val frequency: String,
    val lastGeneratedFormatted: String,
    val lastGenerated: Long = 0L,
    val category: String,
    val isActive: Boolean
)

fun RepeatExpense.toDisplayItem(): RepeatExpenseDisplayItem {
    return RepeatExpenseDisplayItem(
        id = id,
        description = description,
        amountFormatted = CurrencyFormatter.format(amount),
        amountPaise = amount,
        frequency = frequency,
        lastGeneratedFormatted = if (lastGenerated > 0) DateTimeFormatter.formatDate(lastGenerated) else "Never",
        lastGenerated = lastGenerated,
        category = category,
        isActive = isActive
    )
}
