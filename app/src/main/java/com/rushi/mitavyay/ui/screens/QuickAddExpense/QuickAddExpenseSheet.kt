package com.rushi.mitavyay.ui.screens.QuickAddExpense

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.ui.components.AddCategoryDialog
import com.rushi.mitavyay.ui.components.AppDatePickerDialog
import com.rushi.mitavyay.ui.components.AppTextField
import com.rushi.mitavyay.ui.components.CurrencyInput
import com.rushi.mitavyay.ui.components.PrimaryButton
import com.rushi.mitavyay.ui.components.SpacerLg
import com.rushi.mitavyay.ui.components.SpacerMd
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.components.parseCategoryColor
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.DateTimeFormatter

import com.rushi.mitavyay.util.hapticError
import com.rushi.mitavyay.util.hapticLight
import com.rushi.mitavyay.util.hapticSuccess

/**
 * Material 3 ModalBottomSheet providing the fastest path to log an expense or income transaction.
 *
 * Enforces:
 * - Direct monetary entry in paise via CurrencyInput.
 * - Clear Expense/Income toggle adhering to sign convention.
 * - Fast single-tap account and category selection.
 * - Input validation before submission.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddExpenseSheet(
    onDismiss: () -> Unit,
    onBatchAddClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    hapticFeedbackEnabled: Boolean = true,
    viewModel: QuickAddExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isSavedSuccessfully) {
        if (uiState.isSavedSuccessfully) {
            context.hapticSuccess(hapticFeedbackEnabled)
            val typeStr = if (uiState.isExpense) "Expense" else "Income"
            Toast.makeText(context, "$typeStr added successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetForm()
            onDismiss()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            context.hapticError(hapticFeedbackEnabled)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = MaterialTheme.spacing.sm,
        modifier = modifier
    ) {
        QuickAddExpenseContent(
            uiState = uiState,
            onDismiss = onDismiss,
            onAmountChange = viewModel::onAmountChange,
            onTypeToggle = viewModel::onTypeToggle,
            onAccountSelect = viewModel::onAccountSelect,
            onCategorySelect = viewModel::onCategorySelect,
            onDateSelected = viewModel::onDateSelected,
            onAddCategoryClick = { showAddCategoryDialog = true },
            onDescriptionChange = viewModel::onDescriptionChange,
            onSubmit = { viewModel.saveTransaction() },
            onBatchAddClick = onBatchAddClick,
            hapticFeedbackEnabled = hapticFeedbackEnabled
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = {
                showAddCategoryDialog = false
                categoryErrorMessage = null
            },
            onSave = { name, colorHex, icon ->
                viewModel.createAndSelectCategory(name, colorHex, icon) { result ->
                    result.fold(
                        onSuccess = {
                            context.hapticSuccess(hapticFeedbackEnabled)
                            showAddCategoryDialog = false
                            categoryErrorMessage = null
                        },
                        onFailure = { error ->
                            context.hapticError(hapticFeedbackEnabled)
                            categoryErrorMessage = error.localizedMessage
                        }
                    )
                }
            },
            errorMessage = categoryErrorMessage
        )
    }
}

@Composable
fun QuickAddExpenseContent(
    uiState: QuickAddExpenseUiState,
    onDismiss: () -> Unit,
    onAmountChange: (Long) -> Unit,
    onTypeToggle: (Boolean) -> Unit,
    onAccountSelect: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onDateSelected: (Long) -> Unit = {},
    onAddCategoryClick: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBatchAddClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    hapticFeedbackEnabled: Boolean = true
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.lg)
            .padding(bottom = MaterialTheme.spacing.xxl)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.isExpense) "Quick Add Expense" else "Quick Add Income",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBatchAddClick != null) {
                    TextButton(onClick = {
                        context.hapticLight(hapticFeedbackEnabled)
                        onBatchAddClick()
                    }) {
                        Text("Batch Add")
                    }
                }
                IconButton(onClick = {
                    context.hapticLight(hapticFeedbackEnabled)
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        SpacerSm()

        // Expense / Income Segmented Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
            FilterChip(
                selected = uiState.isExpense,
                onClick = {
                    context.hapticLight(hapticFeedbackEnabled)
                    onTypeToggle(true)
                },
                label = {
                    Text(
                        text = "Expense",
                        fontWeight = if (uiState.isExpense) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.xs)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = MaterialTheme.appShapes.small,
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = !uiState.isExpense,
                onClick = {
                    context.hapticLight(hapticFeedbackEnabled)
                    onTypeToggle(false)
                },
                label = {
                    Text(
                        text = "Income",
                        fontWeight = if (!uiState.isExpense) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.xs)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.extendedColorScheme.success.copy(alpha = 0.2f),
                    selectedLabelColor = MaterialTheme.extendedColorScheme.success
                ),
                shape = MaterialTheme.appShapes.small,
                modifier = Modifier.weight(1f)
            )
        }

        SpacerMd()

        // Amount Input Field
        CurrencyInput(
            amountPaise = uiState.amountPaise,
            onAmountChange = onAmountChange,
            label = if (uiState.isExpense) "Expense Amount" else "Income Amount",
            isError = uiState.errorMessage != null && uiState.amountPaise <= 0L,
            errorMessage = if (uiState.amountPaise <= 0L) uiState.errorMessage else null,
            modifier = Modifier.fillMaxWidth()
        )

        SpacerMd()

        // Account Selector
        Text(
            text = "Account",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

        if (uiState.accounts.isEmpty()) {
            Text(
                text = "No active accounts found. Please add an account first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.accounts, key = { it.id }) { account ->
                    AccountChip(
                        account = account,
                        isSelected = uiState.selectedAccountId == account.id,
                        onClick = {
                            context.hapticLight(hapticFeedbackEnabled)
                            onAccountSelect(account.id)
                        }
                    )
                }
            }
        }

        SpacerMd()

        // Date Field Row
        var showDatePicker by remember { mutableStateOf(false) }

        Text(
            text = "Date",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

        Surface(
            onClick = {
                context.hapticLight(hapticFeedbackEnabled)
                showDatePicker = true
            },
            shape = MaterialTheme.appShapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = DateTimeFormatter.formatRelativeDate(uiState.selectedDate),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (showDatePicker) {
            AppDatePickerDialog(
                initialSelectedDateMs = uiState.selectedDate,
                onDateSelected = { timestamp ->
                    onDateSelected(timestamp)
                    showDatePicker = false
                },
                onDismissRequest = { showDatePicker = false }
            )
        }

        SpacerMd()

        // Category Selector
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
            items(uiState.categories, key = { it.id }) { category ->
                CategoryChip(
                    category = category,
                    isSelected = uiState.selectedCategory == category.name,
                    onClick = {
                        context.hapticLight(hapticFeedbackEnabled)
                        onCategorySelect(category.name)
                    }
                )
            }
            item {
                AssistChip(
                    onClick = {
                        context.hapticLight(hapticFeedbackEnabled)
                        onAddCategoryClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Custom Category",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "New",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    shape = MaterialTheme.appShapes.small,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.primary,
                        leadingIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        SpacerMd()

        // Description Input (Optional)
        AppTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = "Description (Optional)",
            placeholder = "e.g. Grocery shopping, Lunch, Salary",
            modifier = Modifier.fillMaxWidth()
        )


        // General Error message if any (other than amount error)
        if (uiState.errorMessage != null && uiState.amountPaise > 0L) {
            SpacerSm()
            Text(
                text = uiState.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        SpacerLg()

        // Submit Button
        val canSubmit = uiState.amountPaise > 0L &&
                !uiState.selectedAccountId.isNullOrBlank() &&
                !uiState.selectedCategory.isNullOrBlank() &&
                !uiState.isSaving

        PrimaryButton(
            text = if (uiState.isExpense) "Add Expense" else "Add Income",
            onClick = {
                context.hapticLight(hapticFeedbackEnabled)
                onSubmit()
            },
            enabled = canSubmit,
            loading = uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AccountChip(
    account: AccountDisplayItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = "${account.name} (${account.balanceFormatted})",
                style = MaterialTheme.typography.labelMedium
            )
        },
        shape = MaterialTheme.appShapes.small,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}

@Composable
private fun CategoryChip(
    category: CategoryDisplayItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = parseCategoryColor(category.colorHex, 0),
                        shape = CircleShape
                    )
            )
        },
        label = {
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium
            )
        },
        shape = MaterialTheme.appShapes.small,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}
