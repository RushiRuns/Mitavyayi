package com.rushi.mitavyay.ui.screens.BatchAdd

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.ui.components.AppTextField
import com.rushi.mitavyay.ui.components.CurrencyInput
import com.rushi.mitavyay.ui.components.NotesField
import com.rushi.mitavyay.ui.components.PrimaryButton
import com.rushi.mitavyay.ui.components.SecondaryButton
import com.rushi.mitavyay.ui.components.SpacerLg
import com.rushi.mitavyay.ui.components.SpacerMd
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.components.parseCategoryColor
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter

@Composable
fun BatchAddTransactionsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BatchAddViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "${uiState.rows.size} transactions saved successfully!", Toast.LENGTH_SHORT).show()
            viewModel.reset()
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.appShapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = MaterialTheme.spacing.sm,
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.lg)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Batch Add Transactions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Log multiple transactions simultaneously",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SpacerSm()

                // Summary Card
                Card(
                    shape = MaterialTheme.appShapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.rows.size} Items",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                            if (uiState.totalExpensePaise > 0L) {
                                Text(
                                    text = "Exp: ${CurrencyFormatter.format(uiState.totalExpensePaise)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if (uiState.totalIncomePaise > 0L) {
                                Text(
                                    text = "Inc: ${CurrencyFormatter.format(uiState.totalIncomePaise)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.extendedColorScheme.success
                                )
                            }
                        }
                    }
                }

                SpacerSm()

                // List of Transaction Rows
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
                ) {
                    itemsIndexed(uiState.rows, key = { _, row -> row.id }) { index, row ->
                        BatchRowCard(
                            index = index,
                            row = row,
                            canDelete = uiState.rows.size > 1,
                            accounts = uiState.accounts,
                            categories = uiState.categories,
                            onUpdate = { updated -> viewModel.updateRow(index, updated) },
                            onDelete = { viewModel.removeRow(index) }
                        )
                    }

                    item {
                        SecondaryButton(
                            text = "+ Add Another Transaction",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = { viewModel.addRow() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Error message banner
                if (uiState.errorMessage != null) {
                    SpacerSm()
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                SpacerMd()

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryButton(
                        text = "Save All (${uiState.rows.size})",
                        onClick = { viewModel.submitBatch() },
                        enabled = uiState.canSubmit && !uiState.isSaving,
                        loading = uiState.isSaving,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BatchRowCard(
    index: Int,
    row: BatchTransactionRow,
    canDelete: Boolean,
    accounts: List<AccountDisplayItem>,
    categories: List<CategoryDisplayItem>,
    onUpdate: (BatchTransactionRow) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md)
        ) {
            // Row Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction #${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete row",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            SpacerSm()

            // Type Toggle (Expense / Income)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
            ) {
                FilterChip(
                    selected = row.isExpense,
                    onClick = { onUpdate(row.copy(isExpense = true)) },
                    label = { Text("Expense") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = !row.isExpense,
                    onClick = { onUpdate(row.copy(isExpense = false)) },
                    label = { Text("Income") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.extendedColorScheme.success.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.extendedColorScheme.success
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            SpacerSm()

            // Amount Input
            CurrencyInput(
                amountPaise = row.amountPaise,
                onAmountChange = { onUpdate(row.copy(amountPaise = it)) },
                label = if (row.isExpense) "Expense Amount" else "Income Amount",
                modifier = Modifier.fillMaxWidth()
            )

            SpacerSm()

            // Description Input
            AppTextField(
                value = row.description,
                onValueChange = { onUpdate(row.copy(description = it)) },
                label = "Description",
                placeholder = "e.g. Lunch, Groceries, Taxi",
                modifier = Modifier.fillMaxWidth()
            )

            SpacerSm()

            // Account Selection Chips
            Text(
                text = "Account",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(accounts, key = { it.id }) { acc ->
                    FilterChip(
                        selected = row.accountId == acc.id,
                        onClick = { onUpdate(row.copy(accountId = acc.id)) },
                        label = { Text(acc.name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            SpacerSm()

            // Category Selection Chips
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories, key = { it.id }) { cat ->
                    FilterChip(
                        selected = row.category == cat.name,
                        onClick = { onUpdate(row.copy(category = cat.name)) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = parseCategoryColor(cat.colorHex),
                                        shape = CircleShape
                                    )
                            )
                        },
                        label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            SpacerSm()

            // Notes Field
            NotesField(
                value = row.notes,
                onValueChange = { onUpdate(row.copy(notes = it)) },
                label = "Notes / Tags",
                placeholder = "Add tags or receipt notes...",
                minLines = 2,
                maxLines = 3,
                showQuickHelpers = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
