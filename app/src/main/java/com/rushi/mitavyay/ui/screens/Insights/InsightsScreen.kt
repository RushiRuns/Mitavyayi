package com.rushi.mitavyay.ui.screens.Insights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.BasicInsightsData
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.EmptyTransactionsIllustration
import com.rushi.mitavyay.ui.components.pressScale
import com.rushi.mitavyay.ui.components.shimmerBrush
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter
import com.rushi.mitavyay.util.DateTimeFormatter
import com.rushi.mitavyay.util.hapticLight
import java.util.Locale
import kotlin.math.abs

/**
 * Feature 4.9: Insights (Basic Statistics) Screen.
 *
 * Displays key financial metrics for a selected month:
 * 1. Total spent this month (with comparison to previous month)
 * 2. Average daily spend (burn rate)
 * 3. Largest transaction of the month (clickable to view details)
 * 4. Most used category (by frequency and volume)
 * 5. Monthly cash flow breakdown (income, expense, savings)
 */
@Composable
fun InsightsScreen(
    modifier: Modifier = Modifier,
    viewModel: InsightsViewModel = hiltViewModel(),
    onTransactionClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    InsightsContent(
        uiState = uiState,
        onPreviousMonth = { viewModel.previousMonth() },
        onNextMonth = { viewModel.nextMonth() },
        onResetMonth = { viewModel.resetToCurrentMonth() },
        onTransactionClick = onTransactionClick,
        modifier = modifier
    )
}

@Composable
fun InsightsContent(
    uiState: InsightsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetMonth: () -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Month Selector Bar
        MonthNavigatorBar(
            monthTitle = uiState.insights.monthYearFormatted.ifBlank {
                DateTimeFormatter.formatMonthYear(uiState.selectedMonthYear)
            },
            isCurrentMonth = uiState.isCurrentMonth,
            onPreviousMonth = {
                context.hapticLight()
                onPreviousMonth()
            },
            onNextMonth = {
                context.hapticLight()
                onNextMonth()
            },
            onResetMonth = {
                context.hapticLight()
                onResetMonth()
            }
        )

        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            InsightsSkeletonLoader()
        }

        AnimatedVisibility(
            visible = !uiState.isLoading && !uiState.insights.hasData,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyState(
                illustration = { EmptyTransactionsIllustration() },
                title = "No Spending Insights",
                description = "No transactions found for ${uiState.insights.monthYearFormatted.ifBlank { DateTimeFormatter.formatMonthYear(uiState.selectedMonthYear) }}. Add expenses to unlock basic statistics and comparisons."
            )
        }

        AnimatedVisibility(
            visible = !uiState.isLoading && uiState.insights.hasData,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = MaterialTheme.spacing.md,
                    end = MaterialTheme.spacing.md,
                    top = MaterialTheme.spacing.sm,
                    bottom = MaterialTheme.spacing.xxl
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
            ) {
                // 1. Total Spent Card (with previous month delta & % comparison)
                item {
                    TotalSpentCard(insights = uiState.insights)
                }

                // 2. Average Daily Spend (Burn Rate)
                item {
                    AverageDailySpendCard(insights = uiState.insights)
                }

                // 3. Largest Expense Transaction
                item {
                    LargestTransactionCard(
                        transaction = uiState.insights.largestTransaction,
                        onTransactionClick = onTransactionClick
                    )
                }

                // 4. Most Used Category
                item {
                    MostUsedCategoryCard(insights = uiState.insights)
                }

                // 5. Cash Flow Summary (Income, Expense, Net Savings)
                item {
                    CashFlowSummaryCard(insights = uiState.insights)
                }
            }
        }
    }
}

@Composable
private fun MonthNavigatorBar(
    monthTitle: String,
    isCurrentMonth: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetMonth: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.spacing.md,
                vertical = MaterialTheme.spacing.sm
            ),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (!isCurrentMonth) {
                FilterChip(
                    selected = true,
                    onClick = onResetMonth,
                    label = {
                        Text(
                            text = "Back to Current Month",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.xs)
                )
            }
        }
    }
}

@Composable
private fun TotalSpentCard(insights: BasicInsightsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.lg)
        ) {
            Text(
                text = "Total Spent",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

            Text(
                text = CurrencyFormatter.format(insights.totalSpentThisMonthPaise),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

            // Previous Month Comparison Badge
            if (insights.totalSpentPreviousMonthPaise > 0L) {
                val isUp = insights.isSpendingIncreasing
                val badgeColor = if (isUp) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.extendedColorScheme.successContainer
                }
                val textColor = if (isUp) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.extendedColorScheme.onSuccessContainer
                }
                val symbol = if (isUp) "↑" else "↓"
                val deltaText = CurrencyFormatter.format(abs(insights.spendingDeltaPaise))

                Surface(
                    shape = MaterialTheme.appShapes.small,
                    color = badgeColor,
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.xs)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.sm,
                            vertical = MaterialTheme.spacing.xs
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$symbol ${"%.1f".format(Locale.ROOT, abs(insights.spendingPercentageChange))}% vs ${insights.previousMonthYearFormatted}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "($deltaText)",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor
                        )
                    }
                }
            } else {
                Text(
                    text = "No previous month transactions for comparison",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

            Text(
                text = "${insights.totalExpenseTransactionsCount} expense transaction${if (insights.totalExpenseTransactionsCount != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AverageDailySpendCard(insights: BasicInsightsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.lg)
        ) {
            Text(
                text = "Average Daily Spend",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = CurrencyFormatter.format(insights.averageDailySpendPaise),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " / day",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

            Text(
                text = "Based on ${insights.daysElapsed} of ${insights.totalDaysInMonth} days elapsed this month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (insights.previousAverageDailySpendPaise > 0L) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
                Text(
                    text = "Last month daily average: ${CurrencyFormatter.format(insights.previousAverageDailySpendPaise)} / day",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun LargestTransactionCard(
    transaction: Transaction?,
    onTransactionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (transaction != null) {
                    Modifier
                        .pressScale(0.97f)
                        .clickable { onTransactionClick(transaction.id) }
                } else Modifier
            ),
        shape = MaterialTheme.appShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
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
                Text(
                    text = "Largest Transaction",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction != null) {
                    Text(
                        text = "View details ›",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

            if (transaction != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = transaction.description.ifBlank { "Expense" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
                        ) {
                            Surface(
                                shape = MaterialTheme.appShapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = transaction.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "• ${DateTimeFormatter.formatDate(transaction.timestamp)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = CurrencyFormatter.format(abs(transaction.amount)),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Text(
                    text = "No expense recorded for this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MostUsedCategoryCard(insights: BasicInsightsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.lg)
        ) {
            Text(
                text = "Most Used Category",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

            if (insights.mostUsedCategory != null) {
                val categoryColor = parseCategoryHexColor(
                    insights.mostUsedCategoryColorHex,
                    fallback = MaterialTheme.colorScheme.primary
                )

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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(categoryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
                        }

                        Column {
                            Text(
                                text = insights.mostUsedCategory,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val pct = if (insights.totalExpenseTransactionsCount > 0) {
                                (insights.mostUsedCategoryCount.toFloat() / insights.totalExpenseTransactionsCount.toFloat()) * 100f
                            } else 0f
                            Text(
                                text = "${insights.mostUsedCategoryCount} transactions (${"%.0f".format(Locale.ROOT, pct)}% of all expenses)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyFormatter.format(insights.mostUsedCategoryTotalPaise),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "No category data available for this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CashFlowSummaryCard(insights: BasicInsightsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.lg)
        ) {
            Text(
                text = "Monthly Cash Flow",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CashFlowStatItem(
                    label = "Income",
                    amountFormatted = CurrencyFormatter.format(insights.totalIncomeThisMonthPaise),
                    color = MaterialTheme.extendedColorScheme.success
                )
                CashFlowStatItem(
                    label = "Expenses",
                    amountFormatted = CurrencyFormatter.format(insights.totalSpentThisMonthPaise),
                    color = MaterialTheme.colorScheme.error
                )
                CashFlowStatItem(
                    label = "Net Savings",
                    amountFormatted = CurrencyFormatter.format(insights.netSavingsPaise),
                    color = if (insights.netSavingsPaise >= 0) {
                        MaterialTheme.extendedColorScheme.success
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

@Composable
private fun CashFlowStatItem(
    label: String,
    amountFormatted: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = amountFormatted,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun InsightsSkeletonLoader() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(MaterialTheme.appShapes.large)
                    .background(shimmerBrush())
            )
        }
    }
}

private fun parseCategoryHexColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}
