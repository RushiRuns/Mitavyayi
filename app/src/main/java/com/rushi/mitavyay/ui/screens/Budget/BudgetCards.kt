package com.rushi.mitavyay.ui.screens.Budget

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rushi.mitavyay.data.model.BudgetAlertLevel
import com.rushi.mitavyay.data.model.BudgetDisplayItem
import com.rushi.mitavyay.ui.components.getCategoryIcon
import com.rushi.mitavyay.ui.components.parseCategoryColor
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing

/**
 * Interactive card displaying a category's budget status, progress bar, and alert indicators.
 */
@Composable
fun BudgetCard(
    item: BudgetDisplayItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = item.progress.coerceIn(0f, 1f),
        label = "budget_progress"
    )

    val (progressColor, alertBadgeColor, alertTextColor, alertLabel) = when (item.alertLevel) {
        BudgetAlertLevel.SAFE -> Quadruple(
            MaterialTheme.extendedColorScheme.success,
            MaterialTheme.extendedColorScheme.successContainer,
            MaterialTheme.extendedColorScheme.onSuccessContainer,
            "${item.progressPercentage}% Used"
        )
        BudgetAlertLevel.WARNING_80 -> Quadruple(
            MaterialTheme.extendedColorScheme.warning,
            MaterialTheme.extendedColorScheme.warningContainer,
            MaterialTheme.extendedColorScheme.onWarningContainer,
            "⚠️ 80% Warning (${item.progressPercentage}%)"
        )
        BudgetAlertLevel.EXCEEDED_100 -> Quadruple(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "🚨 Exceeded (${item.progressPercentage}%)"
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md)
        ) {
            // Header Row: Category, Icon, Alert Badge, Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    modifier = Modifier.weight(1f)
                ) {
                    val categoryColor = parseCategoryColor(item.categoryColorHex)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(item.categoryIcon ?: ""),
                            contentDescription = item.category,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Alert Badge Pill
                        Surface(
                            shape = MaterialTheme.appShapes.small,
                            color = alertBadgeColor,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = alertLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = alertTextColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Budget",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

            // Amounts Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.spentAmountFormatted} of ${item.budgetAmountFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val statusText = if (item.isOverBudget) {
                    "Over by ${item.remainingAmountFormatted}"
                } else {
                    "${item.remainingAmountFormatted} remaining"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (item.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Top summary overview card displaying aggregate budget statistics.
 */
@Composable
fun BudgetOverviewCard(
    totalBudgetedFormatted: String,
    totalSpentFormatted: String,
    totalRemainingFormatted: String,
    overallProgress: Float,
    warningCount: Int,
    exceededCount: Int,
    modifier: Modifier = Modifier
) {
    val isOver = overallProgress >= 1.0f
    val progressColor = when {
        isOver -> MaterialTheme.colorScheme.error
        overallProgress >= 0.8f -> MaterialTheme.extendedColorScheme.warning
        else -> MaterialTheme.extendedColorScheme.success
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Monthly Budget",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalBudgetedFormatted,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (warningCount > 0 || exceededCount > 0) {
                    Surface(
                        shape = MaterialTheme.appShapes.small,
                        color = if (exceededCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.extendedColorScheme.warningContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (exceededCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.extendedColorScheme.onWarningContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            val alertMsg = when {
                                exceededCount > 0 -> "$exceededCount Exceeded"
                                else -> "$warningCount Near Limit"
                            }
                            Text(
                                text = alertMsg,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (exceededCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.extendedColorScheme.onWarningContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

            // Overall Progress
            LinearProgressIndicator(
                progress = { overallProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalSpentFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isOver) "Over Budget" else "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalRemainingFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.extendedColorScheme.success
                    )
                }
            }
        }
    }
}

/**
 * Visual breakdown comparison displaying Budget vs Actual spending across categories.
 */
@Composable
fun BudgetVsActualChart(
    items: List<BudgetDisplayItem>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md)
        ) {
            Text(
                text = "Budget vs. Actual Spending",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

            items.take(6).forEach { item ->
                val progressColor = when (item.alertLevel) {
                    BudgetAlertLevel.SAFE -> MaterialTheme.extendedColorScheme.success
                    BudgetAlertLevel.WARNING_80 -> MaterialTheme.extendedColorScheme.warning
                    BudgetAlertLevel.EXCEEDED_100 -> MaterialTheme.colorScheme.error
                }

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${item.spentAmountFormatted} / ${item.budgetAmountFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    val animProgress by animateFloatAsState(
                        targetValue = item.progress.coerceIn(0f, 1f),
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                        label = "actual_vs_budget_progress"
                    )

                    LinearProgressIndicator(
                        progress = { animProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
