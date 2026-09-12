package com.rushi.mitavyay.ui.screens.TransactionList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.model.TransactionDisplayItem
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val transactions: List<TransactionDisplayItem> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository? = null
) : ViewModel() {

    private val accountsFlow = accountRepository?.getAllAccounts() ?: flowOf(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<TransactionListUiState> = combine(
        transactionRepository.getAllTransactions(),
        accountsFlow,
        _searchQuery,
        _isRefreshing
    ) { transactions, accounts, query, isRefreshing ->
        val accountMap = accounts.associateBy { it.id }
        val allDisplayItems = transactions
            .sortedByDescending { it.timestamp }
            .map { tx ->
                tx.toDisplayItem(accountName = accountMap[tx.accountId]?.name)
            }

        val filtered = if (query.isBlank()) {
            allDisplayItems
        } else {
            val q = query.trim()
            allDisplayItems.filter { item ->
                item.description.contains(q, ignoreCase = true) ||
                item.category.contains(q, ignoreCase = true) ||
                (item.notes != null && item.notes.contains(q, ignoreCase = true)) ||
                (item.accountName != null && item.accountName.contains(q, ignoreCase = true))
            }
        }

        TransactionListUiState(
            isLoading = false,
            isRefreshing = isRefreshing,
            transactions = filtered,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TransactionListUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(400)
            _isRefreshing.value = false
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transactionId)
            } catch (_: Exception) {
                // Ignore or log error
            }
        }
    }
}
