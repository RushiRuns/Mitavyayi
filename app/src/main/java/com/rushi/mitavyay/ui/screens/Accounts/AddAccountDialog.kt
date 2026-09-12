package com.rushi.mitavyay.ui.screens.Accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.window.Dialog
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.ui.components.AppTextField
import com.rushi.mitavyay.ui.components.CurrencyInput
import com.rushi.mitavyay.ui.components.PrimaryButton
import com.rushi.mitavyay.ui.components.SecondaryButton
import com.rushi.mitavyay.ui.components.SpacerMd
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing

private val ACCOUNT_TYPES = listOf("bank", "cash", "credit")

/**
 * Dialog for creating or editing an Account.
 * Uses theme tokens and enforces validation.
 */
@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, initialBalancePaise: Long) -> Unit,
    existingAccount: AccountDisplayItem? = null
) {
    val isEditMode = existingAccount != null

    var name by remember(existingAccount) {
        mutableStateOf(existingAccount?.name ?: "")
    }
    var selectedType by remember(existingAccount) {
        mutableStateOf(existingAccount?.type?.lowercase() ?: "bank")
    }
    var initialBalancePaise by remember(existingAccount) {
        mutableLongStateOf(0L)
    }
    var nameError by remember { mutableStateOf<String?>(null) }

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
                    .padding(MaterialTheme.spacing.lg)
            ) {
                Text(
                    text = if (isEditMode) "Edit Account" else "Add New Account",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SpacerMd()

                // Account Name
                AppTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = "Account Name",
                    placeholder = "e.g. HDFC Salary, Wallet, Credit Card",
                    isError = nameError != null,
                    errorMessage = nameError,
                    modifier = Modifier.fillMaxWidth()
                )

                SpacerSm()

                // Account Type Selector
                Text(
                    text = "Account Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SpacerSm()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    ACCOUNT_TYPES.forEach { type ->
                        val isSelected = selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = type },
                            label = {
                                Text(
                                    text = type.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                if (!isEditMode) {
                    SpacerSm()

                    // Initial Balance Input (only for new accounts)
                    CurrencyInput(
                        amountPaise = initialBalancePaise,
                        onAmountChange = { initialBalancePaise = it },
                        label = "Initial Balance",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                SpacerMd()

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss
                    )
                    SpacerSm()
                    PrimaryButton(
                        text = if (isEditMode) "Save Changes" else "Create Account",
                        onClick = {
                            if (name.isBlank()) {
                                nameError = "Account name is required"
                            } else {
                                onSave(name, selectedType, initialBalancePaise)
                            }
                        }
                    )
                }
            }
        }
    }
}
