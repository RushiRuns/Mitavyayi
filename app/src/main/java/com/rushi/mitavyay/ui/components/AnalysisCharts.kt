package com.rushi.mitavyay.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rushi.mitavyay.data.repository.AccountSpending
import com.rushi.mitavyay.data.repository.AnalysisPeriod
import com.rushi.mitavyay.data.repository.CategorySpending
import com.rushi.mitavyay.data.repository.PeriodComparisonData
import com.rushi.mitavyay.data.repository.SpendingTrendInsight
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter
import java.util.Locale
import kotlin.math.abs

/**
 * Animated horizontal stacked bar chart showing category spending composition.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryCompositionStackedBar(
    categorySpendings: List<CategorySpending>,
    totalExpensePaise: Long,
    modifier: Modifier = Modifier,
    barHeight: Dp = 24.dp
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(categorySpendings) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Category Composition",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Proportional share of total expenses",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

        // Stacked Bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(MaterialTheme.appShapes.full)
        ) {
            val totalWidth = size.width
            val height = size.height
            var currentX = 0f

            if (totalExpensePaise <= 0L || categorySpendings.isEmpty()) {
                drawRect(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    topLeft = Offset.Zero,
                    size = Size(totalWidth, height)
                )
            } else {
                categorySpendings.forEachIndexed { index, cat ->
                    val segmentRatio = (cat.totalExpensePaise.toFloat() / totalExpensePaise.toFloat())
                    val segmentWidth = totalWidth * segmentRatio * animProgress.value
                    val color = parseCategoryColor(cat.colorHex, index)

                    if (segmentWidth > 0f) {
                        drawRect(
                            color = color,
                            topLeft = Offset(currentX, 0f),
                            size = Size(segmentWidth, height)
                        )
                        currentX += segmentWidth
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

        // Proportional Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            modifier = Modifier.fillMaxWidth()
        ) {
            categorySpendings.take(6).forEachIndexed { index, cat ->
                val color = parseCategoryColor(cat.colorHex, index)
                Surface(
                    shape = MaterialTheme.appShapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${cat.category}: ${String.format(Locale.getDefault(), "%.1f%%", cat.percentage)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card presenting trend direction (increasing/decreasing) and run-rate burn rate forecast.
 */
@Composable
fun TrendForecastCard(
    insight: SpendingTrendInsight,
    period: AnalysisPeriod,
    periodOffset: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md)
        ) {
            Text(
                text = "Trend & Forecast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

            // Trend comparison badge & text
            if (insight.hasComparisonData) {
                val isIncreased = insight.isIncreasing
                val badgeColor = if (isIncreased) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.extendedColorScheme.success
                }
                val badgeContainer = if (isIncreased) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.extendedColorScheme.success.copy(alpha = 0.12f)
                }
                val arrow = if (isIncreased) "↑" else "↓"
                val sign = if (isIncreased) "+" else "-"

                Surface(
                    shape = MaterialTheme.appShapes.small,
                    color = badgeContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$arrow $sign${String.format(Locale.getDefault(), "%.1f%%", abs(insight.percentageChange))}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isIncreased) {
                                "Higher spending than previous period (${CurrencyFormatter.format(abs(insight.deltaExpensePaise))})"
                            } else {
                                "Lower spending than previous period (saved ${CurrencyFormatter.format(abs(insight.deltaExpensePaise))})"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "No previous period data available for comparison.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Run-rate projection for current period
            if (periodOffset == 0 && insight.dailyBurnRatePaise > 0L) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Burn Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${CurrencyFormatter.format(insight.dailyBurnRatePaise)}/day",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = when (period) {
                                AnalysisPeriod.WEEK -> "Projected Week-End"
                                AnalysisPeriod.MONTH -> "Projected Month-End"
                                AnalysisPeriod.YEAR -> "Projected Year-End"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.format(insight.projectedPeriodExpensePaise),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
                Text(
                    text = "Estimated total spending if you maintain your current daily average pace.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Card showing side-by-side period comparisons and top category movers.
 */
@Composable
fun PeriodComparisonCard(
    comparison: PeriodComparisonData,
    period: AnalysisPeriod,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md)
        ) {
            val periodTitle = when (period) {
                AnalysisPeriod.WEEK -> "Week-over-Week Comparison"
                AnalysisPeriod.MONTH -> "Month-over-Month Comparison"
                AnalysisPeriod.YEAR -> "Year-over-Year Comparison"
            }

            Text(
                text = periodTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

            // Side by Side comparison row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Period",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.format(comparison.currentTotalExpensePaise),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Previous Period",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.format(comparison.previousTotalExpensePaise),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

            // Difference Banner
            val isInc = comparison.isExpenseIncreasing
            val diffColor = if (isInc) MaterialTheme.colorScheme.error else MaterialTheme.extendedColorScheme.success
            val diffText = if (isInc) {
                "+${CurrencyFormatter.format(abs(comparison.expenseDeltaPaise))} (+${String.format(Locale.getDefault(), "%.1f%%", abs(comparison.expensePercentageChange))})"
            } else {
                "-${CurrencyFormatter.format(abs(comparison.expenseDeltaPaise))} (-${String.format(Locale.getDefault(), "%.1f%%", abs(comparison.expensePercentageChange))})"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expense Difference:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = diffText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = diffColor
                )
            }

            // Top Category Movers
            if (comparison.categoryMovers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

                Text(
                    text = "Top Category Changes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

                comparison.categoryMovers.take(4).forEach { mover ->
                    val moverInc = mover.deltaPaise > 0L
                    val moverColor = if (moverInc) MaterialTheme.colorScheme.error else MaterialTheme.extendedColorScheme.success
                    val moverSign = if (moverInc) "+" else "-"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mover.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$moverSign${CurrencyFormatter.format(abs(mover.deltaPaise))} ($moverSign${String.format(Locale.getDefault(), "%.0f%%", abs(mover.percentageChange))})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = moverColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card displaying spending breakdown across accounts.
 */
@Composable
fun AccountBreakdownCard(
    accountSpendings: List<AccountSpending>,
    totalExpensePaise: Long,
    onAccountClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md)
        ) {
            Text(
                text = "Spending by Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Breakdown across payment methods and banks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

            if (accountSpendings.isEmpty() || totalExpensePaise <= 0L) {
                Text(
                    text = "No account expenses recorded in this period.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    accountSpendings.forEach { acc ->
                        val clickableModifier = if (onAccountClick != null) {
                            Modifier.clickable { onAccountClick(acc.accountId) }
                        } else Modifier

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(clickableModifier)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = acc.accountName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = MaterialTheme.appShapes.small,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = acc.accountType.uppercase(Locale.getDefault()),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = CurrencyFormatter.format(acc.totalExpensePaise),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { (acc.percentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(MaterialTheme.appShapes.full),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${acc.transactionCount} transaction${if (acc.transactionCount == 1) "" else "s"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f%% of total", acc.percentage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
