package com.rushi.mitavyay.ui.screens.TransactionList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.model.TransactionDisplayItem
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TransactionListUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionDisplayItem> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository? = null
) : ViewModel() {

    private val accountsFlow = accountRepository?.getAllAccounts() ?: flowOf(emptyList())

    val uiState: StateFlow<TransactionListUiState> = combine(
        transactionRepository.getAllTransactions(),
        accountsFlow
    ) { transactions, accounts ->
        val accountMap = accounts.associateBy { it.id }
        val sortedTransactions = transactions
            .sortedByDescending { it.timestamp }
            .map { tx ->
                tx.toDisplayItem(accountName = accountMap[tx.accountId]?.name)
            }

        TransactionListUiState(
            isLoading = false,
            transactions = sortedTransactions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TransactionListUiState(isLoading = true)
    )
}
