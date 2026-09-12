package com.rushi.mitavyay.ui.screens.TransactionList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.components.TransactionCard

@Composable
fun TransactionListScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TransactionListContent(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
fun TransactionListContent(
    uiState: TransactionListUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                LoadingState(message = "Loading transactions...")
            }
            uiState.transactions.isEmpty() -> {
                EmptyState(
                    title = "No transactions yet",
                    description = "Tap + to add your first expense or income."
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.transactions,
                        key = { it.id }
                    ) { item ->
                        TransactionCard(item = item)
                    }
                }
            }
        }
    }
}
