package com.rushi.mitavyay.ui.screens.Goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.ui.components.AppTextField
import com.rushi.mitavyay.ui.components.CurrencyInput
import com.rushi.mitavyay.ui.components.DatePickerField
import com.rushi.mitavyay.ui.components.PrimaryButton
import com.rushi.mitavyay.ui.components.SecondaryButton
import com.rushi.mitavyay.ui.components.SpacerLg
import com.rushi.mitavyay.ui.components.SpacerMd
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.components.SpacerXs
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing

/**
 * Dialog for creating a new savings goal towards a target and deadline.
 * Supports dual progress modes: Dedicated Fund (manual savings) or Linked Account (real account balance).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGoalDialog(
    accounts: List<AccountDisplayItem>,
    categories: List<CategoryDisplayItem>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        targetAmountPaise: Long,
        deadlineMs: Long,
        linkedAccountId: String?,
        initialDepositPaise: Long,
        category: String,
        notes: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var targetAmountPaise by remember { mutableLongStateOf(0L) }
    // Default deadline to 90 days from now
    var deadlineMs by remember {
        mutableLongStateOf(System.currentTimeMillis() + 90L * 24L * 60L * 60L * 1000L)
    }
    var trackingMode by remember { mutableStateOf("dedicated") } // "dedicated" or "linked"
    var selectedAccountId by remember(accounts) {
        mutableStateOf(accounts.firstOrNull()?.id)
    }
    var initialDepositPaise by remember { mutableLongStateOf(0L) }
    var selectedCategory by remember(categories) {
        mutableStateOf(categories.firstOrNull()?.name ?: "Savings")
    }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.appShapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = modifier
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
                    text = "New Savings Goal",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                SpacerSm()

                Text(
                    text = "Track your savings target toward a deadline. Track dedicated funds or link a real bank account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SpacerMd()

                // Goal Name
                AppTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = "Goal Name",
                    placeholder = "e.g. Emergency Fund, Vacation, Car Downpayment",
                    modifier = Modifier.fillMaxWidth()
                )

                SpacerMd()

                // Target Amount
                Text(
                    text = "Target Amount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SpacerXs()
                CurrencyInput(
                    amountPaise = targetAmountPaise,
                    onAmountChange = {
                        targetAmountPaise = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                SpacerMd()

                // Target Deadline
                Text(
                    text = "Target Deadline",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SpacerXs()
                DatePickerField(
                    selectedDateMs = deadlineMs,
                    onDateSelected = {
                        deadlineMs = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                SpacerMd()

                // Tracking Mode: Dedicated Fund vs Linked Account
                Text(
                    text = "Tracking Mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SpacerXs()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    FilterChip(
                        selected = trackingMode == "dedicated",
                        onClick = {
                            trackingMode = "dedicated"
                            errorMessage = null
                        },
                        label = { Text("Dedicated Fund") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = MaterialTheme.appShapes.small,
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = trackingMode == "linked",
                        onClick = {
                            trackingMode = "linked"
                            errorMessage = null
                        },
                        label = { Text("Linked Account") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = MaterialTheme.appShapes.small,
                        modifier = Modifier.weight(1f)
                    )
                }

                SpacerSm()

                if (trackingMode == "dedicated") {
                    Text(
                        text = "Initial Contribution (Optional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SpacerXs()
                    CurrencyInput(
                        amountPaise = initialDepositPaise,
                        onAmountChange = { initialDepositPaise = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Select Account to Track",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SpacerXs()
                    if (accounts.isEmpty()) {
                        Text(
                            text = "No active accounts found. A dedicated fund will be used instead.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.extendedColorScheme.warning
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(accounts, key = { it.id }) { acc ->
                                FilterChip(
                                    selected = selectedAccountId == acc.id,
                                    onClick = {
                                        selectedAccountId = acc.id
                                        errorMessage = null
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
                }

                SpacerMd()

                // Category Selection
                if (categories.isNotEmpty()) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SpacerXs()
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory.equals(cat.name, ignoreCase = true),
                                onClick = { selectedCategory = cat.name },
                                label = { Text(cat.name) },
                                shape = MaterialTheme.appShapes.small
                            )
                        }
                    }
                    SpacerMd()
                }

                // Notes
                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (Optional)",
                    placeholder = "e.g. Save ₹5,000 monthly from salary",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                // Error Message
                if (errorMessage != null) {
                    SpacerSm()
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                SpacerLg()

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryButton(
                        text = "Save Goal",
                        onClick = {
                            val trimmedName = name.trim()
                            when {
                                trimmedName.isBlank() -> {
                                    errorMessage = "Please enter a goal name"
                                }
                                targetAmountPaise <= 0L -> {
                                    errorMessage = "Target amount must be greater than zero"
                                }
                                deadlineMs <= 0L -> {
                                    errorMessage = "Please select a valid deadline"
                                }
                                trackingMode == "linked" && selectedAccountId == null -> {
                                    errorMessage = "Please select an account to link"
                                }
                                else -> {
                                    val linkedId = if (trackingMode == "linked") selectedAccountId else null
                                    val initialDeposit = if (trackingMode == "dedicated") initialDepositPaise else 0L
                                    onSave(
                                        trimmedName,
                                        targetAmountPaise,
                                        deadlineMs,
                                        linkedId,
                                        initialDeposit,
                                        selectedCategory,
                                        notes.trim().ifBlank { null }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
