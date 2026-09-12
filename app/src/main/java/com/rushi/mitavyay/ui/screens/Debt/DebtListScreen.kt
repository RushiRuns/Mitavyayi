package com.rushi.mitavyay.ui.screens.Debt

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.rushi.mitavyay.data.model.DebtDisplayItem
import com.rushi.mitavyay.ui.components.AppAlertDialog
import com.rushi.mitavyay.ui.components.DebtCard
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.components.pressScale
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.components.SpacerXs
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter

@Composable
fun DebtListScreen(
    modifier: Modifier = Modifier,
    viewModel: DebtViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingState(message = "Loading debts and loans...")
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Summary Metrics
                DebtSummarySection(
                    totalLent = uiState.totalLentActivePaise,
                    totalBorrowed = uiState.totalBorrowedActivePaise,
                    modifier = Modifier.padding(16.dp)
                )

                // Tabs: Active vs Settled
                TabRow(
                    selectedTabIndex = uiState.selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = uiState.selectedTab == DebtTab.ACTIVE,
                        onClick = { viewModel.selectTab(DebtTab.ACTIVE) },
                        text = {
                            Text(
                                text = "Active (${uiState.activeDebts.size})",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == DebtTab.SETTLED,
                        onClick = { viewModel.selectTab(DebtTab.SETTLED) },
                        text = {
                            Text(
                                text = "Settled (${uiState.settledDebts.size})",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                }

                // Debts List
                val currentList = if (uiState.selectedTab == DebtTab.ACTIVE) {
                    uiState.activeDebts
                } else {
                    uiState.settledDebts
                }

                if (currentList.isEmpty()) {
                    if (uiState.selectedTab == DebtTab.ACTIVE) {
                        EmptyState(
                            title = "No active debts or loans",
                            description = "Keep track of money you lent to friends or borrowed from others.",
                            actionText = "Record Debt / Loan",
                            onAction = { viewModel.onAddDebtClick() }
                        )
                    } else {
                        EmptyState(
                            title = "No settled records",
                            description = "Records marked as settled will be permanently archived here."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = currentList,
                            key = { it.id }
                        ) { debt ->
                            @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                            DebtCard(
                                item = debt,
                                onMarkSettled = if (!debt.isSettled) {
                                    { viewModel.onSettleClick(debt) }
                                } else null,
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }

        // Add Debt Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.onAddDebtClick() },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = MaterialTheme.appShapes.large,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .pressScale()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Debt or Loan"
            )
        }
    }

    // Add Debt Dialog
    if (uiState.isAddDebtOpen) {
        AddDebtDialog(
            onDismiss = { viewModel.dismissDialogs() },
            onSave = { type, counterparty, amountPaise, notes ->
                viewModel.createDebt(
                    type = type,
                    counterparty = counterparty,
                    amountPaise = amountPaise,
                    notes = notes
                )
            }
        )
    }

    // Mark as Settled Confirmation Dialog
    uiState.debtToSettle?.let { debt ->
        AppAlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = "Mark Debt as Settled",
            text = "Has the balance of ${debt.amountFormatted} with '${debt.counterparty}' been fully settled?\n\nThis record will be permanently archived under the Settled tab.",
            confirmText = "Mark Settled",
            onConfirm = { viewModel.confirmSettleDebt() }
        )
    }

    // General Error Dialog
    uiState.errorMessage?.let { err ->
        AppAlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = "Notice",
            text = err,
            confirmText = "Dismiss",
            onConfirm = { viewModel.dismissDialogs() }
        )
    }
}

@Composable
private fun DebtSummarySection(
    totalLent: Long,
    totalBorrowed: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.cardContent),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lent Summary
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Total Lent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SpacerXs()
                Text(
                    text = CurrencyFormatter.format(totalLent),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.extendedColorScheme.success
                )
                Text(
                    text = "To Receive",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.extendedColorScheme.success
                )
            }

            // Borrowed Summary
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Total Borrowed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SpacerXs()
                Text(
                    text = CurrencyFormatter.format(totalBorrowed),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "To Pay",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
