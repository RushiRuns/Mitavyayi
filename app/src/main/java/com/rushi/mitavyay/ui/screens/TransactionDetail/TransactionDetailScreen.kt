package com.rushi.mitavyay.ui.screens.TransactionDetail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.ui.components.AppAlertDialog
import com.rushi.mitavyay.ui.components.DangerButton
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.components.PrimaryButton
import com.rushi.mitavyay.ui.components.SecondaryButton
import com.rushi.mitavyay.ui.components.SpacerLg
import com.rushi.mitavyay.ui.components.SpacerMd
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter
import com.rushi.mitavyay.util.DateTimeFormatter

@Composable
fun TransactionDetailScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    when {
        uiState.isLoading -> {
            LoadingState(message = "Loading transaction details...")
        }
        uiState.transaction == null -> {
            EmptyState(
                title = "Transaction not found",
                description = "This transaction may have been deleted.",
                actionText = "Go Back",
                onAction = onNavigateBack
            )
        }
        else -> {
            val tx = uiState.transaction!!
            val display = uiState.displayItem
            val isCredit = tx.amount >= 0
            val isTransfer = uiState.displayItem?.isTransfer == true || !tx.transferId.isNullOrBlank()
            val amountColor = if (isCredit) {
                MaterialTheme.extendedColorScheme.success
            } else {
                MaterialTheme.colorScheme.error
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(MaterialTheme.spacing.lg)
            ) {
                // Main Highlight Card
                Card(
                    shape = MaterialTheme.appShapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = MaterialTheme.spacing.xs
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
                        ) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(if (isCredit) "Income" else "Expense") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isCredit) {
                                        MaterialTheme.extendedColorScheme.success.copy(alpha = 0.15f)
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                    },
                                    labelColor = if (isCredit) {
                                        MaterialTheme.extendedColorScheme.success
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                ),
                                shape = MaterialTheme.appShapes.small
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tx.category) },
                                shape = MaterialTheme.appShapes.small
                            )
                            if (isTransfer) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Transfer") },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    shape = MaterialTheme.appShapes.small
                                )
                            }
                        }

                        SpacerSm()

                        Text(
                            text = CurrencyFormatter.format(tx.amount),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = amountColor
                        )

                        SpacerSm()

                        Text(
                            text = tx.description,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                SpacerLg()

                // Metadata Details Card
                Card(
                    shape = MaterialTheme.appShapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = MaterialTheme.spacing.xs
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.cardContent)
                    ) {
                        DetailRow(
                            label = "Date & Time",
                            value = DateTimeFormatter.formatDateTime(tx.timestamp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = MaterialTheme.spacing.sm),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        DetailRow(
                            label = "Account",
                            value = uiState.accountName ?: "Unknown Account"
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = MaterialTheme.spacing.sm),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        DetailRow(
                            label = "Category",
                            value = tx.category
                        )
                    }
                }

                SpacerLg()

                if (!isTransfer) {
                    // Actions Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                    ) {
                        SecondaryButton(
                            text = "Edit",
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            },
                            onClick = { showEditDialog = true },
                            modifier = Modifier.weight(1f)
                        )

                        SecondaryButton(
                            text = "Duplicate",
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            },
                            onClick = {
                                viewModel.duplicateTransaction {
                                    Toast.makeText(context, "Transaction duplicated", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    SpacerMd()
                }

                DangerButton(
                    text = if (isTransfer) "Delete Transfer" else "Delete Transaction",
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Delete Confirmation Dialog
            if (showDeleteDialog) {
                AppAlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = if (isTransfer) "Delete Transfer" else "Delete Transaction",
                    text = if (isTransfer) {
                        "This transaction is part of a transfer. Deleting it will delete both linked transactions and restore balances for both accounts."
                    } else {
                        "Are you sure you want to delete this transaction? Your account balance will be automatically adjusted."
                    },
                    confirmText = if (isTransfer) "Delete Transfer" else "Delete",
                    isDestructive = true,
                    onConfirm = {
                        showDeleteDialog = false
                        viewModel.deleteTransaction {
                            Toast.makeText(
                                context,
                                if (isTransfer) "Transfer deleted" else "Transaction deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                            onNavigateBack()
                        }
                    }
                )
            }

            // Edit Dialog
            if (showEditDialog) {
                EditTransactionDialog(
                    transaction = tx,
                    accounts = uiState.availableAccounts,
                    categories = uiState.availableCategories,
                    onDismiss = { showEditDialog = false },
                    onSave = { amountPaise, isExpense, description, category, accountId, notes ->
                        viewModel.updateTransaction(
                            amountPaise = amountPaise,
                            isExpense = isExpense,
                            description = description,
                            category = category,
                            accountId = accountId,
                            notes = notes
                        ) {
                            showEditDialog = false
                            Toast.makeText(context, "Transaction updated", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }


        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
