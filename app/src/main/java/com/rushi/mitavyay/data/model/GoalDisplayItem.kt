package com.rushi.mitavyay.data.model

import com.rushi.mitavyay.data.db.Goal
import com.rushi.mitavyay.util.CurrencyFormatter
import com.rushi.mitavyay.util.DateTimeFormatter

data class GoalDisplayItem(
    val id: String,
    val name: String,
    val targetAmountFormatted: String,
    val currentAmountFormatted: String,
    val progress: Float,
    val deadlineFormatted: String,
    val category: String,
    val notes: String? = null
)

fun Goal.toDisplayItem(): GoalDisplayItem {
    val progressRatio = if (targetAmountAmountSafe(targetAmount) > 0) {
        (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)
    } else 0f

    return GoalDisplayItem(
        id = id,
        name = name,
        targetAmountFormatted = CurrencyFormatter.format(targetAmount),
        currentAmountFormatted = CurrencyFormatter.format(currentAmount),
        progress = progressRatio,
        deadlineFormatted = DateTimeFormatter.formatDate(deadline),
        category = category,
        notes = notes
    )
}

private fun targetAmountAmountSafe(targetAmount: Long): Long = if (targetAmount <= 0) 1L else targetAmount
