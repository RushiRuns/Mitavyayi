package com.rushi.mitavyay.data.model

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.util.CurrencyFormatter

data class AccountDisplayItem(
    val id: String,
    val name: String,
    val type: String,
    val balanceFormatted: String,
    val isNegative: Boolean,
    val isActive: Boolean
)

fun Account.toDisplayItem(): AccountDisplayItem {
    return AccountDisplayItem(
        id = id,
        name = name,
        type = type,
        balanceFormatted = CurrencyFormatter.format(balance),
        isNegative = balance < 0,
        isActive = isActive
    )
}
