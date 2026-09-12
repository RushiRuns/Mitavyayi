package com.rushi.mitavyay.ui.screens.Transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.ui.components.AppTextField
import com.rushi.mitavyay.ui.components.CurrencyInput
import com.rushi.mitavyay.ui.components.PrimaryButton
import com.rushi.mitavyay.ui.components.SecondaryButton
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter

/**
 * Dialog enabling transfers between two distinct accounts.
 * Validates non-identical accounts, positive amount, and creates paired ledger records.
 */
@Composable
fun TransferDialog(
    accounts: List<AccountDisplayItem>,
    onDismiss: () -> Unit,
    onTransfer: (fromAccountId: String, toAccountId: String, amountPaise: Long, notes: String?) -> Unit,
    initialFromAccountId: String? = null
) {
    val activeAccounts = remember(accounts) { accounts.filter { it.isActive } }
    var fromAccountId by remember(activeAccounts, initialFromAccountId) {
        mutableStateOf(
            initialFromAccountId?.takeIf { id -> activeAccounts.any { it.id == id } }
                ?: activeAccounts.firstOrNull()?.id
                ?: ""
        )
    }
    var toAccountId by remember(activeAccounts, fromAccountId) {
        mutableStateOf(
            activeAccounts.firstOrNull { it.id != fromAccountId }?.id ?: ""
        )
    }
    var amountPaise by remember { mutableLongStateOf(0L) }
    var notes by remember { mutableStateOf("") }
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transfer Funds",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

                if (activeAccounts.size < 2) {
                    Text(
                        text = "You need at least two active accounts to perform a transfer. Please create or unarchive another account first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.md)
                    )
                    SecondaryButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                    return@Column
                }

                // Transfer Route Visual Card
                val fromAcc = activeAccounts.find { it.id == fromAccountId }
                val toAcc = activeAccounts.find { it.id == toAccountId }

                Surface(
                    shape = MaterialTheme.appShapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "From",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = fromAcc?.name ?: "Select",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (fromAcc != null) {
                                Text(
                                    text = fromAcc.balanceFormatted,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val temp = fromAccountId
                                fromAccountId = toAccountId
                                toAccountId = temp
                                errorMessage = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Swap Accounts",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "To",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = toAcc?.name ?: "Select",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (toAcc != null) {
                                Text(
                                    text = toAcc.balanceFormatted,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

                // Source Account Selection
                Text(
                    text = "Transfer From",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(activeAccounts, key = { "from_${it.id}" }) { acc ->
                        FilterChip(
                            selected = acc.id == fromAccountId,
                            onClick = {
                                fromAccountId = acc.id
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

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

                // Destination Account Selection
                Text(
                    text = "Transfer To",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(activeAccounts, key = { "to_${it.id}" }) { acc ->
                        val isSameAsSource = acc.id == fromAccountId
                        FilterChip(
                            selected = acc.id == toAccountId,
                            onClick = {
                                if (!isSameAsSource) {
                                    toAccountId = acc.id
                                    errorMessage = null
                                }
                            },
                            enabled = !isSameAsSource,
                            label = { Text(acc.name) },
                            shape = MaterialTheme.appShapes.small,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

                // Amount Field
                CurrencyInput(
                    amountPaise = amountPaise,
                    onAmountChange = {
                        amountPaise = it
                        errorMessage = null
                    },
                    label = "Transfer Amount",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

                // Notes Field
                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (Optional)",
                    placeholder = "e.g. ATM withdrawal, Card payment",
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))

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
                        text = "Transfer Funds",
                        onClick = {
                            if (fromAccountId.isBlank() || toAccountId.isBlank()) {
                                errorMessage = "Please select both accounts"
                            } else if (fromAccountId == toAccountId) {
                                errorMessage = "Source and destination accounts must be different"
                            } else if (amountPaise <= 0L) {
                                errorMessage = "Transfer amount must be greater than zero"
                            } else {
                                onTransfer(fromAccountId, toAccountId, amountPaise, notes.trim().ifBlank { null })
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
