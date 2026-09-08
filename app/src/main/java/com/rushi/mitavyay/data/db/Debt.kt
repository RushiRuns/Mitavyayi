package com.rushi.mitavyay.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing money lent or borrowed.
 *
 * Invariant Rule:
 * Debts are NEVER deleted—only marked as settled by updating [settledAt].
 */
@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey
    val id: String,
    val type: String, // "lent" or "borrowed"
    val counterparty: String,
    val amount: Long, // smallest currency unit (paise)
    val createdAt: Long = System.currentTimeMillis(),
    val settledAt: Long? = null,
    val notes: String? = null
)
