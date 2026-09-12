package com.rushi.mitavyay.ui.screens.Debt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.rushi.mitavyay.ui.components.AppTextField
import com.rushi.mitavyay.ui.components.CurrencyInput
import com.rushi.mitavyay.ui.components.PrimaryButton
import com.rushi.mitavyay.ui.components.SecondaryButton
import com.rushi.mitavyay.ui.components.SpacerLg
import com.rushi.mitavyay.ui.components.SpacerMd
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.components.SpacerXs
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing

@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onSave: (type: String, counterparty: String, amountPaise: Long, notes: String?) -> Unit
) {
    var type by remember { mutableStateOf("lent") } // "lent" or "borrowed"
    var counterparty by remember { mutableStateOf("") }
    var amountPaise by remember { mutableLongStateOf(0L) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.appShapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
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
                    text = "Record Debt / Loan",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                SpacerSm()

                Text(
                    text = "Track money lent to or borrowed from others. Per ADR-007, entries are permanent and cannot be deleted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SpacerMd()

                // Type Selection: Lent vs Borrowed
                Text(
                    text = "Transaction Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SpacerXs()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    FilterChip(
                        selected = type == "lent",
                        onClick = { type = "lent" },
                        label = {
                            Text("I Lent (To Receive)")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.extendedColorScheme.success.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.extendedColorScheme.success
                        ),
                        shape = MaterialTheme.appShapes.small,
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = type == "borrowed",
                        onClick = { type = "borrowed" },
                        label = {
                            Text("I Borrowed (To Pay)")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            selectedLabelColor = MaterialTheme.colorScheme.error
                        ),
                        shape = MaterialTheme.appShapes.small,
                        modifier = Modifier.weight(1f)
                    )
                }

                SpacerMd()

                // Counterparty Input
                AppTextField(
                    value = counterparty,
                    onValueChange = {
                        counterparty = it
                        errorMessage = null
                    },
                    label = "Person or Entity Name",
                    placeholder = "e.g. Rahul Sharma, Landlord, Office Fund",
                    modifier = Modifier.fillMaxWidth()
                )

                SpacerMd()

                // Amount Input
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
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

                SpacerMd()

                // Notes Input
                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (Optional)",
                    placeholder = "e.g. Trip expenses, Expected return by Friday",
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Error Feedback
                if (errorMessage != null) {
                    SpacerSm()
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                SpacerLg()

                // Buttons
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
                        text = "Save Record",
                        onClick = {
                            if (counterparty.trim().isBlank()) {
                                errorMessage = "Please enter a person or entity name"
                                return@PrimaryButton
                            }
                            if (amountPaise <= 0L) {
                                errorMessage = "Amount must be greater than zero"
                                return@PrimaryButton
                            }
                            onSave(type, counterparty.trim(), amountPaise, notes.trim().ifBlank { null })
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
