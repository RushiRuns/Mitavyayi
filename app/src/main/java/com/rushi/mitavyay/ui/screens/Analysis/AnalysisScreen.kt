package com.rushi.mitavyay.ui.screens.Analysis

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
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.rushi.mitavyay.data.repository.AnalysisPeriod
import com.rushi.mitavyay.data.repository.AnalysisSummary
import com.rushi.mitavyay.data.repository.TimeSpendingPoint
import com.rushi.mitavyay.ui.components.CategoryDonutChart
import com.rushi.mitavyay.ui.components.CategorySpendingLegendList
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter
import java.util.Locale

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AnalysisContent(
        uiState = uiState,
        onPeriodSelected = { viewModel.selectPeriod(it) },
        onPreviousPeriod = { viewModel.previousPeriod() },
        onNextPeriod = { viewModel.nextPeriod() },
        modifier = modifier
    )
}

@Composable
fun AnalysisContent(
    uiState: AnalysisUiState,
    onPeriodSelected: (AnalysisPeriod) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
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

                if (uiState.isEmpty) {
                    item {
                        EmptyState(
                            title = "No data for this period",
                            description = "Switch date range or add expenses to see charts and spending insights."
                        )
                    }
                } else {
                    // 3. High-level Summary Metrics
                    item {
                        SummaryMetricsSection(summary = uiState.summary)
                    }

                    // 4. Spending Trend (Vico Line Chart)
                    item {
                        SpendingTrendCard(points = uiState.timeTrendPoints)
                    }

                    // 5. Category Breakdown (Donut Pie Chart & Legend)
                    if (uiState.categorySpendings.isNotEmpty()) {
                        item {
                            CategoryBreakdownSection(
                                categorySpendings = uiState.categorySpendings,
                                totalExpensePaise = uiState.totalExpensePaise
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
                val chartModel = remember(chartEntries) {
                    entryModelOf(chartEntries)
                }

                ProvideChartStyle(m3ChartStyle()) {
                    Chart(
                        chart = lineChart(),
                        model = chartModel,
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
    categorySpendings: List<com.rushi.mitavyay.data.repository.CategorySpending>,
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
                text = "Breakdown of top expense categories",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

            CategoryDonutChart(
                categorySpendings = categorySpendings,
                totalExpensePaise = totalExpensePaise
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

            CategorySpendingLegendList(categorySpendings = categorySpendings)
        }
    }
}
