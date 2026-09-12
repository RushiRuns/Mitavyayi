package com.rushi.mitavyay.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room Entity representing monthly category budgets.
 *
 * Invariant Rule:
 * All amounts are stored as [Long] in the smallest currency unit (paise).
 * [monthYear] is formatted as "yyyy-MM" (e.g. "2026-09").
 */
@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["monthYear"]),
        Index(value = ["category", "monthYear"], unique = true)
    ]
)
data class Budget(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val monthYear: String,
    val amount: Long, // smallest currency unit (paise)
    val isActive: Boolean = true
)
