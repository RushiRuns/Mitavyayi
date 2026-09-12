package com.rushi.mitavyay.data.model

import com.rushi.mitavyay.util.CurrencyFormatter

/**
 * Alert threshold level for monthly budgets.
 * - SAFE: Under 80% utilized.
 * - WARNING_80: Between 80% and 99.9% utilized (warning alert).
 * - EXCEEDED_100: 100% or more utilized (critical alert).
 */
enum class BudgetAlertLevel {
    SAFE,
    WARNING_80,
    EXCEEDED_100
}

/**
 * UI-ready display model for a category budget in a given month.
 *
 * Invariant Rule:
 * All monetary amounts are formatted via [CurrencyFormatter] and arithmetic strictly uses [Long] paise.
 */
data class BudgetDisplayItem(
    val id: String,
    val category: String,
    val monthYear: String,
    val budgetAmountPaise: Long,
    val budgetAmountFormatted: String = CurrencyFormatter.format(budgetAmountPaise),
    val spentAmountPaise: Long,
    val spentAmountFormatted: String = CurrencyFormatter.format(spentAmountPaise),
    val remainingAmountPaise: Long,
    val remainingAmountFormatted: String = CurrencyFormatter.format(kotlin.math.abs(remainingAmountPaise)),
    val isOverBudget: Boolean = remainingAmountPaise < 0L,
    val progress: Float, // 0.0f .. 1.0f (or higher if exceeded)
    val progressPercentage: Int,
    val alertLevel: BudgetAlertLevel,
    val categoryColorHex: String? = null,
    val categoryIcon: String? = null,
    val isActive: Boolean = true
)
