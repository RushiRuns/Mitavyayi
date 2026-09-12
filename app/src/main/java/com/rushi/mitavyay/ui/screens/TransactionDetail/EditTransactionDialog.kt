package com.rushi.mitavyay.ui.screens.TransactionDetail

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rushi.mitavyay.data.db.Transaction
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
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import kotlin.math.abs

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    accounts: List<AccountDisplayItem>,
    categories: List<CategoryDisplayItem>,
    onDismiss: () -> Unit,
    onSave: (amountPaise: Long, isExpense: Boolean, description: String, category: String, accountId: String, notes: String?) -> Unit
) {
    var amountPaise by remember(transaction) {
        mutableLongStateOf(abs(transaction.amount))
    }
    var isExpense by remember(transaction) {
        mutableStateOf(transaction.amount < 0)
    }
    var description by remember(transaction) {
        mutableStateOf(transaction.description)
    }
    var selectedCategoryId by remember(transaction) {
        mutableStateOf(transaction.category)
    }
    var selectedAccountId by remember(transaction) {
        mutableStateOf(transaction.accountId)
    }
    var notes by remember(transaction) {
        mutableStateOf(transaction.notes ?: "")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.appShapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = MaterialTheme.spacing.sm,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(MaterialTheme.spacing.lg)
            ) {
                Text(
                    text = "Edit Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SpacerMd()

                // Expense / Income Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    FilterChip(
                        selected = isExpense,
                        onClick = { isExpense = true },
                        label = {
                            Text(
                                text = "Expense",
                                fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isExpense,
                        onClick = { isExpense = false },
                        label = {
                            Text(
                                text = "Income",
                                fontWeight = if (!isExpense) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.extendedColorScheme.success.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.extendedColorScheme.success
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                SpacerMd()

                // Amount
                CurrencyInput(
                    amountPaise = amountPaise,
                    onAmountChange = {
                        amountPaise = it
                        if (it > 0) errorMessage = null
                    },
                    label = if (isExpense) "Expense Amount" else "Income Amount",
                    isError = errorMessage != null && amountPaise <= 0L,
                    errorMessage = if (amountPaise <= 0L) errorMessage else null,
                    modifier = Modifier.fillMaxWidth()
                )

                SpacerMd()

                // Account Selection
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(accounts, key = { it.id }) { account ->
                        FilterChip(
                            selected = selectedAccountId == account.id,
                            onClick = { selectedAccountId = account.id },
                            label = { Text(account.name) }
                        )
                    }
                }

                SpacerMd()

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories, key = { it.id }) { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.name,
                            onClick = { selectedCategoryId = cat.name },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = parseColorSafely(cat.colorHex),
                                            shape = CircleShape
                                        )
                                )
                            },
                            label = { Text(cat.name) }
                        )
                    }
                }

                SpacerMd()

                // Description
                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    placeholder = "e.g. Grocery, Lunch",
                    modifier = Modifier.fillMaxWidth()
                )

                SpacerMd()

                // Notes
                NotesField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null && amountPaise > 0L) {
                    SpacerSm()
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                SpacerLg()

                // Buttons
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
                        text = "Save",
                        onClick = {
                            if (amountPaise <= 0L) {
                                errorMessage = "Amount must be greater than 0"
                                return@PrimaryButton
                            }
                            if (selectedAccountId.isBlank()) {
                                errorMessage = "Please select an account"
                                return@PrimaryButton
                            }
                            if (selectedCategoryId.isBlank()) {
                                errorMessage = "Please select a category"
                                return@PrimaryButton
                            }
                            onSave(amountPaise, isExpense, description, selectedCategoryId, selectedAccountId, notes.trim().ifBlank { null })
                        },
                        enabled = amountPaise > 0L && selectedAccountId.isNotBlank() && selectedCategoryId.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun parseColorSafely(hex: String, fallback: Color = Color.Gray): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}
