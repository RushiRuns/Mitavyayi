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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rushi.mitavyay.R
import com.rushi.mitavyay.data.model.DateFilter
import com.rushi.mitavyay.data.model.TransactionDisplayItem
import com.rushi.mitavyay.ui.components.AppAlertDialog
import com.rushi.mitavyay.ui.components.AppDateRangePickerDialog
import com.rushi.mitavyay.ui.components.EmptySearchIllustration
import com.rushi.mitavyay.ui.components.EmptyTransactionsIllustration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.rushi.mitavyay.ui.components.DateSectionHeader
import com.rushi.mitavyay.ui.components.SkeletonTransactionList
import com.rushi.mitavyay.ui.screens.BatchAdd.BatchAddTransactionsDialog
import com.rushi.mitavyay.util.DateTimeFormatter
import com.rushi.mitavyay.util.hapticError
import com.rushi.mitavyay.util.hapticLight
import com.rushi.mitavyay.util.hapticSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onTransactionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TransactionListContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onDateFilterChange = viewModel::onDateFilterChange,
        onTransactionClick = onTransactionClick,
        onDeleteTransaction = viewModel::deleteTransaction,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListContent(
    uiState: TransactionListUiState,
    onSearchQueryChange: (String) -> Unit = {},
    onDateFilterChange: (DateFilter) -> Unit = {},
    onTransactionClick: (String) -> Unit = {},
    onBatchAddClick: (() -> Unit)? = null,
    onDeleteTransaction: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var transactionPendingDelete by remember { mutableStateOf<TransactionDisplayItem?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showCustomDateRangePicker by remember { mutableStateOf(false) }
    val isFilterActive = uiState.dateFilter !is DateFilter.AllTime


    if (transactionPendingDelete != null) {
        AppAlertDialog(
            onDismissRequest = { transactionPendingDelete = null },
            title = "Delete Transaction",
            text = "Are you sure you want to delete \"${transactionPendingDelete?.description}\"? This will automatically update your account balance.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                val id = transactionPendingDelete?.id
                transactionPendingDelete = null
                if (id != null) {
                    context.hapticSuccess()
                    onDeleteTransaction(id)
                }
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Bar and Date Filter Action
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

            // Filter Folder Icon Button
            Box {
                FilledTonalIconButton(
                    onClick = {
                        context.hapticLight()
                        showFilterMenu = true
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isFilterActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_folder_open),
                        contentDescription = "Filter by date",
                        tint = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DateFilterMenu(
                    expanded = showFilterMenu,
                    selectedFilter = uiState.dateFilter,
                    onFilterSelect = { filter ->
                        context.hapticLight()
                        showFilterMenu = false
                        onDateFilterChange(filter)
                    },
                    onCustomDateClick = {
                        context.hapticLight()
                        showFilterMenu = false
                        showCustomDateRangePicker = true
                    },
                    onDismissRequest = { showFilterMenu = false }
                )
            }
        }

        // Active Date Filter Indicator Chip
        if (isFilterActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InputChip(
                    selected = true,
                    onClick = {
                        context.hapticLight()
                        showFilterMenu = true
                    },
                    label = {
                        val labelText = when (val filter = uiState.dateFilter) {
                            is DateFilter.AllTime -> "All Time"
                            is DateFilter.Today -> "Today"
                            is DateFilter.ThisWeek -> "This Week"
                            is DateFilter.ThisMonth -> "This Month"
                            is DateFilter.CustomRange -> "${DateTimeFormatter.formatShortDate(filter.startDateMs)} - ${DateTimeFormatter.formatShortDate(filter.endDateMs)}"
                        }
                        Text("Date: $labelText")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                context.hapticLight()
                                onDateFilterChange(DateFilter.AllTime)
                            },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Date Filter",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        if (showCustomDateRangePicker) {
            val initialStart = (uiState.dateFilter as? DateFilter.CustomRange)?.startDateMs ?: System.currentTimeMillis()
            val initialEnd = (uiState.dateFilter as? DateFilter.CustomRange)?.endDateMs ?: System.currentTimeMillis()
            AppDateRangePickerDialog(
                initialSelectedStartDateMs = initialStart,
                initialSelectedEndDateMs = initialEnd,
                onDateRangeSelected = { start, end ->
                    context.hapticSuccess()
                    onDateFilterChange(DateFilter.CustomRange(start, end))
                    showCustomDateRangePicker = false
                },
                onDismissRequest = { showCustomDateRangePicker = false }
            )
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading && uiState.transactions.isEmpty() -> {
                    SkeletonTransactionList(count = 6)
                }
                uiState.transactions.isEmpty() -> {
                    if (uiState.searchQuery.isNotBlank() || isFilterActive) {
                        val filterLabel = when (val filter = uiState.dateFilter) {
                            is DateFilter.AllTime -> ""
                            is DateFilter.Today -> "Today"
                            is DateFilter.ThisWeek -> "This Week"
                            is DateFilter.ThisMonth -> "This Month"
                            is DateFilter.CustomRange -> "${DateTimeFormatter.formatShortDate(filter.startDateMs)} - ${DateTimeFormatter.formatShortDate(filter.endDateMs)}"
                        }
                        val desc = when {
                            uiState.searchQuery.isNotBlank() && isFilterActive ->
                                "No transactions found matching \"${uiState.searchQuery}\" for date filter: $filterLabel."
                            isFilterActive ->
                                "No transactions found for date filter: $filterLabel."
                            else ->
                                "No transactions found matching \"${uiState.searchQuery}\". Try a different keyword."
                        }
                        EmptyState(
                            title = "No matching transactions",
                            description = desc,
                            actionText = "Clear Filters",
                            onAction = {
                                if (uiState.searchQuery.isNotBlank()) onSearchQueryChange("")
                                if (isFilterActive) onDateFilterChange(DateFilter.AllTime)
                            },
                            illustration = { EmptySearchIllustration() },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        )
                    } else {
                        EmptyState(
                            title = "No transactions yet",
                            description = "Tap + to add your first expense or income.",
                            illustration = { EmptyTransactionsIllustration() },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
                else -> {
                    @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = MaterialTheme.spacing.md,
                            end = MaterialTheme.spacing.md,
                            bottom = MaterialTheme.spacing.md
                        ),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                    ) {
                        for (group in uiState.groupedTransactions) {
                            stickyHeader(key = "header_${group.dateLabel}") {
                                DateSectionHeader(label = group.dateLabel)
                            }
                            items(
                                items = group.transactions,
                                key = { it.id }
                            ) { item ->
                                SwipeableTransactionCard(
                                    item = item,
                                    onClick = { onTransactionClick(item.id) },
                                    onSwipeDelete = {
                                        context.hapticLight()
                                        transactionPendingDelete = item
                                    },
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionCard(
    item: TransactionDisplayItem,
    onClick: () -> Unit,
    onSwipeDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onSwipeDelete()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isSwiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val backgroundColor = if (isSwiping) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                Color.Transparent
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor, shape = MaterialTheme.appShapes.medium)
                    .padding(horizontal = MaterialTheme.spacing.lg),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isSwiping) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        modifier = modifier
    ) {
        TransactionCard(
            item = item,
            onClick = onClick
        )
    }
}

@Composable
fun DateFilterMenu(
    expanded: Boolean,
    selectedFilter: DateFilter,
    onFilterSelect: (DateFilter) -> Unit,
    onCustomDateClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text("All Time") },
            trailingIcon = {
                if (selectedFilter is DateFilter.AllTime) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            onClick = { onFilterSelect(DateFilter.AllTime) }
        )
        DropdownMenuItem(
            text = { Text("Today") },
            trailingIcon = {
                if (selectedFilter is DateFilter.Today) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            onClick = { onFilterSelect(DateFilter.Today) }
        )
        DropdownMenuItem(
            text = { Text("This Week (Mon–Sun)") },
            trailingIcon = {
                if (selectedFilter is DateFilter.ThisWeek) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            onClick = { onFilterSelect(DateFilter.ThisWeek) }
        )
        DropdownMenuItem(
            text = { Text("This Month") },
            trailingIcon = {
                if (selectedFilter is DateFilter.ThisMonth) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            onClick = { onFilterSelect(DateFilter.ThisMonth) }
        )
        DropdownMenuItem(
            text = {
                Text(
                    if (selectedFilter is DateFilter.CustomRange)
                        "Custom (${DateTimeFormatter.formatShortDate(selectedFilter.startDateMs)} - ${DateTimeFormatter.formatShortDate(selectedFilter.endDateMs)})"
                    else "Custom Date..."
                )
            },
            trailingIcon = {
                if (selectedFilter is DateFilter.CustomRange) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            onClick = onCustomDateClick
        )
    }
}

