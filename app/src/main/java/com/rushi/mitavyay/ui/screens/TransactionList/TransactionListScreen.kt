package com.rushi.mitavyay.ui.screens.TransactionList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.components.TransactionCard
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.rushi.mitavyay.ui.screens.BatchAdd.BatchAddTransactionsDialog

@Composable
fun TransactionListScreen(
    onTransactionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBatchAddDialog by remember { mutableStateOf(false) }

    TransactionListContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onTransactionClick = onTransactionClick,
        onBatchAddClick = { showBatchAddDialog = true },
        modifier = modifier
    )

    if (showBatchAddDialog) {
        BatchAddTransactionsDialog(
            onDismiss = { showBatchAddDialog = false }
        )
    }
}

@Composable
fun TransactionListContent(
    uiState: TransactionListUiState,
    onSearchQueryChange: (String) -> Unit = {},
    onTransactionClick: (String) -> Unit = {},
    onBatchAddClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Search Bar and Batch Add Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search description, category, or notes...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search transactions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.appShapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.weight(1f)
            )

            FilledTonalIconButton(
                onClick = onBatchAddClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Batch Add Transactions",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    LoadingState(message = "Loading transactions...")
                }
                uiState.transactions.isEmpty() -> {
                    if (uiState.searchQuery.isNotBlank()) {
                        EmptyState(
                            title = "No matching transactions",
                            description = "No transactions found matching \"${uiState.searchQuery}\". Try a different keyword or check notes.",
                            actionText = "Clear Search",
                            onAction = { onSearchQueryChange("") }
                        )
                    } else {
                        EmptyState(
                            title = "No transactions yet",
                            description = "Tap + to add your first expense or income."
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(MaterialTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                    ) {
                        items(
                            items = uiState.transactions,
                            key = { it.id }
                        ) { item ->
                            @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                            TransactionCard(
                                item = item,
                                onClick = { onTransactionClick(item.id) },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }
    }
}
