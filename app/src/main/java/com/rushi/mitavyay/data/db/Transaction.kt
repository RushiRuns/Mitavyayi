package com.rushi.mitavyay.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a financial transaction.
 *
 * Monetary Rule:
 * [amount] is stored strictly as [Long] in the smallest currency unit (paise/cents).
 * Positive = Credit (Income / Transfer In)
 * Negative = Debit (Expense / Transfer Out)
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index("accountId"),
        Index("timestamp"),
        Index("category"),
        Index("transferId")
    ]
)
data class Transaction(
    @PrimaryKey
    val id: String,
    val accountId: String,
    val amount: Long,
    val description: String,
    val timestamp: Long,
    val category: String,
    val tags: String = "[]",
    val transferId: String? = null,
    val notes: String? = null
)
