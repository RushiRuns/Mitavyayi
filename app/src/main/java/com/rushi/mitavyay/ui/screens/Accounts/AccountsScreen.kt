package com.rushi.mitavyay.ui.screens.Accounts

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
import com.rushi.mitavyay.ui.components.AccountCard
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.LoadingState

@Composable
fun AccountsScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AccountsContent(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
fun AccountsContent(
    uiState: AccountsUiState,
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
                    description = "Add bank accounts, cash wallets, or credit cards to manage your balances."
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.accounts,
                        key = { it.id }
                    ) { item ->
                        AccountCard(item = item)
                    }
                }
            }
        }
    }
}
