package com.rushi.mitavyay.ui.screens.RepeatExpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.ui.components.AppAlertDialog
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.components.RepeatExpenseCard
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.components.SpacerXs
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter

@Composable
fun RepeatExpenseListScreen(
    modifier: Modifier = Modifier,
    viewModel: RepeatExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingState(message = "Loading recurring expenses...")
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Summary Commitment Section
                RepeatSummarySection(
                    monthlyCommitment = uiState.totalMonthlyCommitmentPaise,
                    activeCount = uiState.activeExpenses.size,
                    onTriggerCheck = { viewModel.evaluateDueOccurrences() },
                    modifier = Modifier.padding(MaterialTheme.spacing.md)
                )

                // Tabs: All / Active / Paused
                TabRow(
                    selectedTabIndex = uiState.selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = uiState.selectedTab == RepeatFilterTab.ALL,
                        onClick = { viewModel.selectTab(RepeatFilterTab.ALL) },
                        text = { Text("All (${uiState.allExpenses.size})") }
                    )
                    Tab(
                        selected = uiState.selectedTab == RepeatFilterTab.ACTIVE,
                        onClick = { viewModel.selectTab(RepeatFilterTab.ACTIVE) },
                        text = { Text("Active (${uiState.activeExpenses.size})") }
                    )
                    Tab(
                        selected = uiState.selectedTab == RepeatFilterTab.PAUSED,
                        onClick = { viewModel.selectTab(RepeatFilterTab.PAUSED) },
                        text = { Text("Paused (${uiState.pausedExpenses.size})") }
                    )
                }

                // List content
                val currentList = uiState.displayedExpenses
                if (currentList.isEmpty()) {
                    val emptyTitle = when (uiState.selectedTab) {
                        RepeatFilterTab.ALL -> "No Recurring Expenses"
                        RepeatFilterTab.ACTIVE -> "No Active Recurring Expenses"
                        RepeatFilterTab.PAUSED -> "No Paused Expenses"
                    }
                    val emptyDesc = when (uiState.selectedTab) {
                        RepeatFilterTab.ALL -> "Add automatic recurring subscriptions, bills, or allowances using the + button."
                        RepeatFilterTab.ACTIVE -> "All recurring expenses are currently paused."
                        RepeatFilterTab.PAUSED -> "You have no paused recurring expenses."
                    }
                    EmptyState(
                        title = emptyTitle,
                        description = emptyDesc,
                        actionText = if (uiState.selectedTab == RepeatFilterTab.ALL) "Add Recurring Expense" else null,
                        onAction = { viewModel.onAddClick() },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(MaterialTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                    ) {
                        items(currentList, key = { it.id }) { item ->
                            RepeatExpenseCard(
                                item = item,
                                onToggleActive = { viewModel.toggleActive(item.id) },
                                onDeleteClick = { viewModel.onDeleteClick(item) }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.onAddClick() },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(MaterialTheme.spacing.lg)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Recurring Expense"
            )
        }

        // Add Recurring Expense Dialog
        if (uiState.isAddDialogOpen) {
            AddRepeatDialog(
                categories = uiState.categories,
                onDismiss = { viewModel.dismissDialogs() },
                onSave = { desc, amount, freq, cat, isActive ->
                    viewModel.createRepeatExpense(
                        description = desc,
                        amountPaise = amount,
                        frequency = freq,
                        category = cat,
                        isActive = isActive
                    )
                }
            )
        }

        // Delete Confirmation Dialog
        if (uiState.expenseToDelete != null) {
            val toDelete = uiState.expenseToDelete!!
            AppAlertDialog(
                onDismissRequest = { viewModel.dismissDialogs() },
                title = "Delete Recurring Expense?",
                text = "Are you sure you want to delete '${toDelete.description}'? Past generated transactions will remain in your ledger.",
                onConfirm = { viewModel.confirmDelete() },
                confirmText = "Delete",
                dismissText = "Cancel",
                isDestructive = true
            )
        }
    }
}

@Composable
private fun RepeatSummarySection(
    monthlyCommitment: Long,
    activeCount: Int,
    onTriggerCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ESTIMATED MONTHLY COMMITMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                SpacerXs()
                Text(
                    text = CurrencyFormatter.format(monthlyCommitment),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                SpacerXs()
                Text(
                    text = "$activeCount active recurring item${if (activeCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onTriggerCheck
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Evaluate Due Expenses",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
