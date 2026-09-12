package com.rushi.mitavyay.ui.screens.RepeatExpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.ui.components.AppTextField
import com.rushi.mitavyay.ui.components.CurrencyInput
import com.rushi.mitavyay.ui.components.PrimaryButton
import com.rushi.mitavyay.ui.components.SecondaryButton
import com.rushi.mitavyay.ui.components.SpacerMd
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.components.SpacerXs
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRepeatDialog(
    categories: List<CategoryDisplayItem>,
    onDismiss: () -> Unit,
    onSave: (description: String, amountPaise: Long, frequency: String, category: String, isActive: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var description by remember { mutableStateOf("") }
    var amountPaise by remember { mutableLongStateOf(0L) }
    var selectedFrequency by remember { mutableStateOf("MONTHLY") }
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull()?.name ?: "Bills & Utilities")
    }
    var isActive by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val frequencies = listOf(
        "DAILY" to "Daily",
        "WEEKLY" to "Weekly",
        "MONTHLY" to "Monthly",
        "YEARLY" to "Yearly"
    )

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
                    text = "New Recurring Expense",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                SpacerSm()

                Text(
                    text = "Set up an automatic recurring bill or expense. Occurrences will be generated lazily when due.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SpacerMd()

                // Description
                AppTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        errorMessage = null
                    },
                    label = "Description / Subscription Name",
                    placeholder = "e.g. Broadband, Rent, Netflix",
                    isError = errorMessage != null && description.isBlank(),
                    errorMessage = if (errorMessage != null && description.isBlank()) "Description is required" else null,
                    modifier = Modifier.fillMaxWidth()
                )

                SpacerSm()

                // Amount
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SpacerXs()
                CurrencyInput(
                    amountPaise = amountPaise,
                    onAmountChange = {
                        amountPaise = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                SpacerSm()

                // Frequency selection
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SpacerXs()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
                ) {
                    frequencies.forEach { (freqKey, label) ->
                        FilterChip(
                            selected = selectedFrequency == freqKey,
                            onClick = { selectedFrequency = freqKey },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                SpacerSm()

                // Category selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SpacerXs()
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
                ) {
                    val catList = if (categories.isNotEmpty()) categories.map { it.name } else listOf("Bills & Utilities", "Subscriptions", "Housing", "Food & Dining", "Other")
                    catList.forEach { catName ->
                        FilterChip(
                            selected = selectedCategory.equals(catName, ignoreCase = true),
                            onClick = { selectedCategory = catName },
                            label = { Text(catName, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                SpacerSm()

                // Active toggle switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Status",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isActive) "Will generate when due" else "Paused (no auto-generation)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }

                if (errorMessage != null) {
                    SpacerSm()
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                SpacerMd()

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    SecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "Save",
                        onClick = {
                            if (description.isBlank()) {
                                errorMessage = "Please enter a description"
                                return@PrimaryButton
                            }
                            if (amountPaise <= 0L) {
                                errorMessage = "Please enter an amount greater than zero"
                                return@PrimaryButton
                            }
                            if (selectedCategory.isBlank()) {
                                errorMessage = "Please select a category"
                                return@PrimaryButton
                            }
                            onSave(description, amountPaise, selectedFrequency, selectedCategory, isActive)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
