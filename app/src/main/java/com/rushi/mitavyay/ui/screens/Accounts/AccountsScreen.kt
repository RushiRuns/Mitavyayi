package com.rushi.mitavyay.ui.screens.Accounts

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.R
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.ui.components.AppAlertDialog
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.components.SpacerXs
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing

/**
 * Screen displaying accounts list with balance and management actions.
 */
@Composable
fun AccountsScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AccountsContent(
        uiState = uiState,
        onAddClick = { viewModel.onAddClick() },
        onEditClick = { viewModel.onEditClick(it) },
        onDeleteClick = { viewModel.onDeleteClick(it) },
        onToggleActive = { viewModel.toggleActiveStatus(it) },
        modifier = modifier
    )

    if (uiState.isAddEditOpen) {
        AddAccountDialog(
            onDismiss = { viewModel.dismissDialogs() },
            onSave = { name, type, initialBalance ->
                viewModel.saveAccount(name, type, initialBalance)
            },
            existingAccount = uiState.editingAccount
        )
    }

    uiState.accountToDelete?.let { account ->
        AppAlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = "Delete Account",
            text = "Are you sure you want to permanently delete '${account.name}'? This account has no transactions.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = { viewModel.confirmDeleteAccount() }
        )
    }

    if (uiState.showCannotDeleteDialog && uiState.accountToArchive != null) {
        val account = uiState.accountToArchive!!
        AppAlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = "Cannot Delete Account",
            text = "Account '${account.name}' has recorded transactions. Deleting it would compromise your ledger history. Would you like to archive it instead?",
            confirmText = "Archive Account",
            onConfirm = { viewModel.confirmArchiveAccount() }
        )
    }
}

@Composable
fun AccountsContent(
    uiState: AccountsUiState,
    onAddClick: () -> Unit,
    onEditClick: (AccountDisplayItem) -> Unit,
    onDeleteClick: (AccountDisplayItem) -> Unit,
    onToggleActive: (AccountDisplayItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                LoadingState(message = "Loading accounts...")
            }
            uiState.accounts.isEmpty() -> {
                EmptyState(
                    title = "No accounts configured",
                    description = "Add bank accounts, cash wallets, or credit cards to manage your balances.",
                    actionText = "Add Account",
                    onAction = onAddClick
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.accounts,
                        key = { it.id }
                    ) { item ->
                        AccountItemCard(
                            account = item,
                            onEdit = { onEditClick(item) },
                            onDelete = { onDeleteClick(item) },
                            onToggleActive = { onToggleActive(item) }
                        )
                    }
                }
            }
        }

        // Add Account Floating Action Button on Accounts tab
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = MaterialTheme.appShapes.large,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Account"
            )
        }
    }
}

@Composable
fun AccountItemCard(
    account: AccountDisplayItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MaterialTheme.spacing.xs
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.cardContent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SpacerXs()
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = account.type.uppercase(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        shape = MaterialTheme.appShapes.small,
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Account",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Account",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            SpacerSm()

            Text(
                text = stringResource(R.string.account_balance, account.balanceFormatted),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (!account.isNegative) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )

            SpacerXs()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (account.isActive) "Active" else "Archived (Inactive)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (account.isActive) MaterialTheme.extendedColorScheme.success else MaterialTheme.colorScheme.outline
                )

                SuggestionChip(
                    onClick = onToggleActive,
                    label = {
                        Text(
                            text = if (account.isActive) "Archive" else "Restore",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    shape = MaterialTheme.appShapes.small
                )
            }
        }
    }
}
