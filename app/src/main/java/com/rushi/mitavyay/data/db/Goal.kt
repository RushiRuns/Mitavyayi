package com.rushi.mitavyay.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a savings or financial target.
 */
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey
    val id: String,
    val name: String,
    val targetAmount: Long, // smallest currency unit (paise)
    val deadline: Long, // Unix ms
    val currentAmount: Long = 0L,
    val linkedAccountId: String? = null,
    val category: String = "General",
    val notes: String? = null
)
