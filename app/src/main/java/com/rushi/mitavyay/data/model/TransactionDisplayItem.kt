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
    val accountName: String? = null,
    val transferId: String? = null,
    val notes: String? = null,
    val timestamp: Long = 0L,
    val isNeed: Boolean = true
) {
    val isIncome: Boolean get() = isCredit
    val isTransfer: Boolean get() = !transferId.isNullOrBlank()
}

/**
 * Group of transactions for a specific date section header.
 */
data class TransactionGroup(
    val dateLabel: String,
    val transactions: List<TransactionDisplayItem>
)

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
        accountName = accountName,
        transferId = transferId,
        notes = notes?.trim()?.ifBlank { null },
        timestamp = timestamp,
        isNeed = isNeed
    )
}
