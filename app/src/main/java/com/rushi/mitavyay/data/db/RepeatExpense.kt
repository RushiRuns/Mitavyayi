package com.rushi.mitavyay.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing recurring expenses.
 *
 * Invariant Rule:
 * Occurrences are generated lazily one at a time on demand based on [frequency] and [lastGenerated].
 */
@Entity(tableName = "repeat_expenses")
data class RepeatExpense(
    @PrimaryKey
    val id: String,
    val description: String,
    val amount: Long, // smallest currency unit (paise)
    val frequency: String, // DAILY, WEEKLY, MONTHLY, YEARLY
    val lastGenerated: Long, // Unix ms
    val category: String,
    val isActive: Boolean = true
)
