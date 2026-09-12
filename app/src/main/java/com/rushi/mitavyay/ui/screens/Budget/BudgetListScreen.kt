package com.rushi.mitavyay.ui.screens.Budget

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.ui.components.pressScale
import com.rushi.mitavyay.data.model.BudgetDisplayItem
import com.rushi.mitavyay.ui.components.AppAlertDialog
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.DateTimeFormatter

@Composable
fun BudgetListScreen(
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showSetDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetDisplayItem?>(null) }
    var budgetToDelete by remember { mutableStateOf<BudgetDisplayItem?>(null) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingState(message = "Loading monthly budgets...")
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Month Navigation Header
                MonthNavigationHeader(
                    currentMonthLabel = uiState.monthYearLabel,
                    isCurrentMonth = uiState.selectedMonthYear == DateTimeFormatter.getCurrentMonthYear(),
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth,
                    onGoCurrent = viewModel::goToCurrentMonth
                )

                if (uiState.isEmpty) {
                    EmptyState(
                        title = "No Budgets for ${uiState.monthYearLabel}",
                        description = "Set category spending limits to receive automatic 80% warnings and over-budget alerts.",
                        actionText = "Set First Budget",
                        onAction = { showSetDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = MaterialTheme.spacing.md,
                            end = MaterialTheme.spacing.md,
                            top = MaterialTheme.spacing.xs,
                            bottom = 88.dp // Space for FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
                    ) {
                        // Alert Banner (if any warning or exceeded threshold)
                        if (uiState.warningCount > 0 || uiState.exceededCount > 0) {
                            item(key = "alert_banner") {
                                BudgetAlertBanner(
                                    warningCount = uiState.warningCount,
                                    exceededCount = uiState.exceededCount
                                )
                            }
                        }

                        // Summary KPI Card
                        item(key = "overview_card") {
                            BudgetOverviewCard(
                                totalBudgetedFormatted = uiState.totalBudgetedFormatted,
                                totalSpentFormatted = uiState.totalSpentFormatted,
                                totalRemainingFormatted = uiState.totalRemainingFormatted,
                                overallProgress = uiState.overallProgress,
                                warningCount = uiState.warningCount,
                                exceededCount = uiState.exceededCount
                            )
                        }

                        // Budget vs Actual Chart
                        item(key = "comparison_chart") {
                            BudgetVsActualChart(items = uiState.items)
                        }

                        // Section Header
                        item(key = "category_header") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Category Budgets (${uiState.items.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(onClick = viewModel::copyPreviousMonthBudgets) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text(
                                        text = "Copy Last Month",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        // Budget Cards
                        items(uiState.items, key = { it.id }) { item ->
                            @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                            BudgetCard(
                                item = item,
                                onEditClick = { editingBudget = item },
                                onDeleteClick = { budgetToDelete = item },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showSetDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(MaterialTheme.spacing.lg)
                .pressScale(),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Set Budget"
            )
        }
    }

    // Set / Edit Budget Dialog
    if (showSetDialog || editingBudget != null) {
        SetBudgetDialog(
            availableCategories = uiState.availableCategories,
            initialBudget = editingBudget,
            onDismiss = {
                showSetDialog = false
                editingBudget = null
            },
            onSave = { category, amountPaise ->
                viewModel.setBudget(category, amountPaise)
                showSetDialog = false
                editingBudget = null
            }
        )
    }

    // Delete Confirmation Dialog
    budgetToDelete?.let { budget ->
        AppAlertDialog(
            onDismissRequest = { budgetToDelete = null },
            title = "Delete Budget",
            text = "Are you sure you want to delete the budget for ${budget.category}? Actual transactions will remain untouched.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteBudget(budget.id)
                budgetToDelete = null
            }
        )
    }
}

@Composable
private fun MonthNavigationHeader(
    currentMonthLabel: String,
    isCurrentMonth: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onGoCurrent: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.xs),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentMonthLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!isCurrentMonth) {
                    TextButton(
                        onClick = onGoCurrent,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = "Back to Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun BudgetAlertBanner(
    warningCount: Int,
    exceededCount: Int
) {
    val isCritical = exceededCount > 0
    val containerColor = if (isCritical) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.extendedColorScheme.warningContainer
    }
    val contentColor = if (isCritical) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.extendedColorScheme.onWarningContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = if (isCritical) "Over Budget Alert" else "Budget Warning",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                val description = when {
                    warningCount > 0 && exceededCount > 0 ->
                        "$exceededCount category budget(s) exceeded (≥100%) and $warningCount category budget(s) approaching limit (≥80%)."
                    exceededCount > 0 ->
                        "$exceededCount category budget(s) have exceeded 100% of their allocated limit."
                    else ->
                        "$warningCount category budget(s) are near their limit (≥80% utilized)."
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
                )
            }
        }
    }
}
