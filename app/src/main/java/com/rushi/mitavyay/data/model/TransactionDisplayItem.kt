package com.rushi.mitavyay.data.model

import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.util.CurrencyFormatter
import com.rushi.mitavyay.util.DateTimeFormatter

/**
 * UI display representation of a [Transaction].
 * Ensures Room Entity is never leaked directly to UI Composables.
 */
data class TransactionDisplayItem(
    val id: String,
    val description: String,
    val amountFormatted: String,
    val date: String,
    val category: String,
    val tags: List<String> = emptyList(),
    val isCredit: Boolean,
    val accountName: String? = null
) {
    val isIncome: Boolean get() = isCredit
}

fun Transaction.toDisplayItem(accountName: String? = null): TransactionDisplayItem {
    return TransactionDisplayItem(
        id = id,
        description = description,
        amountFormatted = CurrencyFormatter.format(amount),
        date = DateTimeFormatter.formatDate(timestamp),
        category = category,
        tags = if (tags.startsWith("[") && tags.endsWith("]")) {
            tags.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotEmpty() }
        } else emptyList(),
        isCredit = amount >= 0,
        accountName = accountName
    )
}
