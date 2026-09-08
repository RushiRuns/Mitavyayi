package com.rushi.mitavyay.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a financial account (e.g. Bank, Cash, Credit Card).
 */
@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // cash / bank / credit
    val balance: Long, // smallest currency unit (paise)
    val currency: String = "INR",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
