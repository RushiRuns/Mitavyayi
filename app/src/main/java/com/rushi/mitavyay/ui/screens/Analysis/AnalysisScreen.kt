package com.rushi.mitavyay.ui.screens.Analysis

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.data.repository.AnalysisPeriod
import com.rushi.mitavyay.data.repository.AnalysisSummary
import com.rushi.mitavyay.data.repository.CategorySpending
import com.rushi.mitavyay.data.repository.TimeSpendingPoint
import com.rushi.mitavyay.ui.components.AccountBreakdownCard
import com.rushi.mitavyay.ui.components.CategoryCompositionStackedBar
import com.rushi.mitavyay.ui.components.CategoryDonutChart
import com.rushi.mitavyay.ui.components.CategorySpendingLegendList
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.components.PeriodComparisonCard
import com.rushi.mitavyay.ui.components.TrendForecastCard
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter
import java.util.Locale

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel(),
    onNavigateToInsights: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    AnalysisContent(
        uiState = uiState,
        onPeriodSelected = { viewModel.selectPeriod(it) },
        onPreviousPeriod = { viewModel.previousPeriod() },
        onNextPeriod = { viewModel.nextPeriod() },
        onAccountSelected = { viewModel.selectAccount(it) },
        onChartTypeSelected = { viewModel.setChartType(it) },
        onNavigateToInsights = onNavigateToInsights,
        modifier = modifier
    )
}

@Composable
fun AnalysisContent(
    uiState: AnalysisUiState,
    onPeriodSelected: (AnalysisPeriod) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onAccountSelected: (String?) -> Unit,
    onChartTypeSelected: (TrendChartType) -> Unit,
    onNavigateToInsights: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingState(message = "Analyzing your spending...")
        } else {
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
                // Quick Shortcut: Insights (Basic Statistics Dashboard)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToInsights),
                        shape = MaterialTheme.appShapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Monthly Insights & Key Stats",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Burn rate, largest expense & top categories",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Open Insights",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 1. Period Selector (Week / Month / Year)
                item {
                    PeriodSelectorBar(
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = onPeriodSelected
                    )
                }

                // 2. Date Range Navigator
                item {
                    PeriodNavigatorRow(
                        dateRangeLabel = uiState.dateRangeLabel,
                        periodOffset = uiState.periodOffset,
                        onPreviousPeriod = onPreviousPeriod,
                        onNextPeriod = onNextPeriod
                    )
                }

                // 3. Multi-dimensional Filter: Account Selector
                if (uiState.availableAccounts.isNotEmpty()) {
                    item {
                        AccountFilterRow(
                            accounts = uiState.availableAccounts,
                            selectedAccountId = uiState.selectedAccountId,
                            onAccountSelected = onAccountSelected
                        )
                    }
                }

                if (uiState.isEmpty) {
                    item {
                        EmptyState(
                            title = "No data for this period",
                            description = "Switch date range, clear filters, or add expenses to see charts and spending insights."
                        )
                    }
                } else {
                    // 4. High-level Summary Metrics
                    item {
                        SummaryMetricsSection(summary = uiState.summary)
                    }

                    // 5. Deeper Trend Analysis & Run-rate Forecast
                    item {
                        TrendForecastCard(
                            insight = uiState.trendInsight,
                            period = uiState.selectedPeriod,
                            periodOffset = uiState.periodOffset
                        )
                    }

                    // 6. Spending Trend (Line vs Bar Chart Toggle)
                    item {
                        SpendingTrendCard(
                            points = uiState.timeTrendPoints,
                            chartType = uiState.chartType,
                            onChartTypeSelected = onChartTypeSelected
                        )
                    }

                    // 7. Period Comparison (Year-over-Year / Month-over-Month)
                    if (uiState.periodComparison != null &&
                        (uiState.periodComparison.currentTotalExpensePaise > 0L || uiState.periodComparison.previousTotalExpensePaise > 0L)
                    ) {
                        item {
                            PeriodComparisonCard(
                                comparison = uiState.periodComparison,
                                period = uiState.selectedPeriod
                            )
                        }
                    }

                    // 8. Category Breakdown (Donut Pie Chart, Stacked Composition Bar & Legend)
                    if (uiState.categorySpendings.isNotEmpty()) {
                        item {
                            CategoryBreakdownSection(
                                categorySpendings = uiState.categorySpendings,
                                totalExpensePaise = uiState.totalExpensePaise
                            )
                        }
                    }

                    // 9. Account Breakdown (Multi-dimensional spending distribution)
                    if (uiState.selectedAccountId == null && uiState.accountSpendings.isNotEmpty()) {
                        item {
                            AccountBreakdownCard(
                                accountSpendings = uiState.accountSpendings,
                                totalExpensePaise = uiState.totalExpensePaise,
                                onAccountClick = { onAccountSelected(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodSelectorBar(
    selectedPeriod: AnalysisPeriod,
    onPeriodSelected: (AnalysisPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
    ) {
        AnalysisPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            FilterChip(
                selected = isSelected,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        text = when (period) {
                            AnalysisPeriod.WEEK -> "Week"
                            AnalysisPeriod.MONTH -> "Month"
                            AnalysisPeriod.YEAR -> "Year"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PeriodNavigatorRow(
    dateRangeLabel: String,
    periodOffset: Int,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousPeriod) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Period",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = dateRangeLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onNextPeriod,
            enabled = periodOffset < 0
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Period",
                tint = if (periodOffset < 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
    }
}

@Composable
fun AccountFilterRow(
    accounts: List<AccountDisplayItem>,
    selectedAccountId: String?,
    onAccountSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            FilterChip(
                selected = selectedAccountId == null,
                onClick = { onAccountSelected(null) },
                label = { Text("All Accounts") },
                shape = MaterialTheme.appShapes.small,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }

        items(accounts, key = { it.id }) { acc ->
            FilterChip(
                selected = selectedAccountId == acc.id,
                onClick = {
                    if (selectedAccountId == acc.id) {
                        onAccountSelected(null)
                    } else {
                        onAccountSelected(acc.id)
                    }
                },
                label = { Text(acc.name) },
                shape = MaterialTheme.appShapes.small,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun SummaryMetricsSection(
    summary: AnalysisSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
    ) {
        val isPositiveSavings = summary.netSavingsPaise >= 0L
        val savingsContainerColor = if (isPositiveSavings) {
            MaterialTheme.extendedColorScheme.success.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        }
        val savingsTextColor = if (isPositiveSavings) {
            MaterialTheme.extendedColorScheme.success
        } else {
            MaterialTheme.colorScheme.error
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.appShapes.medium,
            colors = CardDefaults.cardColors(containerColor = savingsContainerColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Savings",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.format(summary.netSavingsPaise),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = savingsTextColor
                    )
                }

                if (summary.totalIncomePaise > 0L) {
                    Surface(
                        shape = MaterialTheme.appShapes.small,
                        color = savingsTextColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "Savings: %.0f%%", summary.savingsRate * 100f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = savingsTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.appShapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.md)
                ) {
                    Text(
                        text = "Total Income",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.format(summary.totalIncomePaise),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.extendedColorScheme.success
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.appShapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.md)
                ) {
                    Text(
                        text = "Total Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.format(summary.totalExpensePaise),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SpendingTrendCard(
    points: List<TimeSpendingPoint>,
    chartType: TrendChartType,
    onChartTypeSelected: (TrendChartType) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Spending Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Expenses over the selected period",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Line / Bar Toggle
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = chartType == TrendChartType.LINE,
                        onClick = { onChartTypeSelected(TrendChartType.LINE) },
                        label = { Text("Line", style = MaterialTheme.typography.labelSmall) },
                        shape = MaterialTheme.appShapes.small
                    )
                    FilterChip(
                        selected = chartType == TrendChartType.BAR,
                        onClick = { onChartTypeSelected(TrendChartType.BAR) },
                        label = { Text("Bar", style = MaterialTheme.typography.labelSmall) },
                        shape = MaterialTheme.appShapes.small
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

            val hasExpenses = points.any { it.expensePaise > 0L }
            if (!hasExpenses || points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses recorded in this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val chartEntries = remember(points) {
                    points.mapIndexed { index, point ->
                        FloatEntry(index.toFloat(), (point.expensePaise.toFloat() / 100f))
                    }
                }
                val chartModelProducer = remember { ChartEntryModelProducer() }

                LaunchedEffect(chartEntries) {
                    chartModelProducer.setEntries(chartEntries)
                }

                ProvideChartStyle(m3ChartStyle()) {
                    val chart = if (chartType == TrendChartType.BAR) {
                        columnChart()
                    } else {
                        lineChart()
                    }

                    Chart(
                        chart = chart,
                        chartModelProducer = chartModelProducer,
                        startAxis = rememberStartAxis(
                            valueFormatter = { value, _ ->
                                if (value >= 1000f) {
                                    String.format(Locale.getDefault(), "₹%.0fk", value / 1000f)
                                } else {
                                    "₹${value.toInt()}"
                                }
                            }
                        ),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = { value, _ ->
                                val idx = value.toInt()
                                points.getOrNull(idx)?.label ?: ""
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownSection(
    categorySpendings: List<CategorySpending>,
    totalExpensePaise: Long,
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
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Breakdown and composition of expenses",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

            // 1. Horizontal Stacked Composition Bar
            CategoryCompositionStackedBar(
                categorySpendings = categorySpendings,
                totalExpensePaise = totalExpensePaise
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))

            // 2. Animated Donut Pie Chart
            CategoryDonutChart(
                categorySpendings = categorySpendings,
                totalExpensePaise = totalExpensePaise
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

            // 3. Ranked Detailed Legend
            CategorySpendingLegendList(categorySpendings = categorySpendings)
        }
    }
}
