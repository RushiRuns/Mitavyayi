package com.rushi.mitavyay.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity tracking transfers between accounts.
 * Linked to two [Transaction] records sharing [id] as their transferId.
 */
@Entity(tableName = "transfers")
data class Transfer(
    @PrimaryKey
    val id: String,
    val fromAccountId: String,
    val toAccountId: String,
    val amount: Long, // smallest currency unit (paise)
    val timestamp: Long,
    val notes: String? = null
)
