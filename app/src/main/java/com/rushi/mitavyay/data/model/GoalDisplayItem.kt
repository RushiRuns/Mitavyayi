package com.rushi.mitavyay.data.model

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Goal
import com.rushi.mitavyay.util.CurrencyFormatter
import com.rushi.mitavyay.util.DateTimeFormatter

enum class GoalStatus {
    ACHIEVED,
    ON_TRACK,
    WARNING,
    OVERDUE
}

data class GoalDisplayItem(
    val id: String,
    val name: String,
    val targetAmountFormatted: String,
    val targetAmountPaise: Long,
    val currentAmountFormatted: String,
    val currentAmountPaise: Long,
    val progress: Float,
    val progressPercentage: Int,
    val deadline: Long,
    val deadlineFormatted: String,
    val timelineText: String,
    val status: GoalStatus,
    val linkedAccountId: String? = null,
    val linkedAccountName: String? = null,
    val category: String,
    val notes: String? = null
)

fun Goal.toDisplayItem(
    linkedAccount: Account? = null,
    currentTimeMs: Long = System.currentTimeMillis()
): GoalDisplayItem {
    // 1. Progress Calculation:
    // If linked to an account: progress = account balance / target
    // If dedicated: progress = dedicated balance (currentAmount) / target
    val effectiveCurrentPaise = if (linkedAccountId != null && linkedAccount != null) {
        linkedAccount.balance.coerceAtLeast(0L)
    } else {
        currentAmount.coerceAtLeast(0L)
    }

    val safeTarget = if (targetAmount <= 0L) 1L else targetAmount
    val rawProgress = effectiveCurrentPaise.toFloat() / safeTarget.toFloat()
    val progressRatio = rawProgress.coerceIn(0f, 1f)
    val percentage = (rawProgress * 100).toInt()

    // 2. Timeline & Status Calculation:
    val diffMs = deadline - currentTimeMs
    val (timeline, status) = when {
        rawProgress >= 1.0f -> {
            "Goal Achieved!" to GoalStatus.ACHIEVED
        }
        diffMs < 0 -> {
            val daysOverdue = ((-diffMs) / (1000L * 60 * 60 * 24)).coerceAtLeast(1)
            "Overdue by $daysOverdue day${if (daysOverdue == 1L) "" else "s"}" to GoalStatus.OVERDUE
        }
        else -> {
            val daysRemaining = (diffMs / (1000L * 60 * 60 * 24)).toInt()
            val text = when {
                daysRemaining == 0 -> "Due today"
                daysRemaining == 1 -> "1 day remaining"
                daysRemaining < 14 -> "$daysRemaining days remaining"
                daysRemaining < 60 -> "${daysRemaining / 7} weeks remaining"
                else -> "${daysRemaining / 30} months remaining"
            }

            // Warning if remaining time is short relative to progress
            val isWarning = (daysRemaining <= 14 && rawProgress < 0.5f) ||
                            (daysRemaining <= 30 && rawProgress < 0.3f)
            val st = if (isWarning) GoalStatus.WARNING else GoalStatus.ON_TRACK
            text to st
        }
    }

    return GoalDisplayItem(
        id = id,
        name = name,
        targetAmountFormatted = CurrencyFormatter.format(targetAmount),
        targetAmountPaise = targetAmount,
        currentAmountFormatted = CurrencyFormatter.format(effectiveCurrentPaise),
        currentAmountPaise = effectiveCurrentPaise,
        progress = progressRatio,
        progressPercentage = percentage,
        deadline = deadline,
        deadlineFormatted = DateTimeFormatter.formatDate(deadline),
        timelineText = timeline,
        status = status,
        linkedAccountId = linkedAccountId,
        linkedAccountName = linkedAccount?.name,
        category = category,
        notes = notes
    )
}
