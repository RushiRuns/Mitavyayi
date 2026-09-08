package com.rushi.mitavyay.data.model

import com.rushi.mitavyay.data.db.RepeatExpense
import com.rushi.mitavyay.util.CurrencyFormatter
import com.rushi.mitavyay.util.DateTimeFormatter

data class RepeatExpenseDisplayItem(
    val id: String,
    val description: String,
    val amountFormatted: String,
    val frequency: String,
    val lastGeneratedFormatted: String,
    val category: String,
    val isActive: Boolean
)

fun RepeatExpense.toDisplayItem(): RepeatExpenseDisplayItem {
    return RepeatExpenseDisplayItem(
        id = id,
        description = description,
        amountFormatted = CurrencyFormatter.format(amount),
        frequency = frequency,
        lastGeneratedFormatted = if (lastGenerated > 0) DateTimeFormatter.formatDate(lastGenerated) else "Never",
        category = category,
        isActive = isActive
    )
}
