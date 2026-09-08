package com.rushi.mitavyay.data.model

import com.rushi.mitavyay.data.db.Debt
import com.rushi.mitavyay.util.CurrencyFormatter
import com.rushi.mitavyay.util.DateTimeFormatter

data class DebtDisplayItem(
    val id: String,
    val type: String, // "lent" or "borrowed"
    val counterparty: String,
    val amountFormatted: String,
    val createdDateFormatted: String,
    val isSettled: Boolean,
    val settledDateFormatted: String? = null,
    val notes: String? = null
)

fun Debt.toDisplayItem(): DebtDisplayItem {
    return DebtDisplayItem(
        id = id,
        type = type,
        counterparty = counterparty,
        amountFormatted = CurrencyFormatter.format(amount),
        createdDateFormatted = DateTimeFormatter.formatDate(createdAt),
        isSettled = settledAt != null,
        settledDateFormatted = settledAt?.let { DateTimeFormatter.formatDate(it) },
        notes = notes
    )
}
