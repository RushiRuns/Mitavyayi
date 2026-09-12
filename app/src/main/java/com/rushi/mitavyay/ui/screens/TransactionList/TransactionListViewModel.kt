package com.rushi.mitavyay.ui.screens.TransactionList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.model.TransactionDisplayItem
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TransactionListUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionDisplayItem> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<TransactionListUiState> = transactionRepository.getAllTransactions()
        .map { list ->
            TransactionListUiState(
                isLoading = false,
                transactions = list.map { it.toDisplayItem() }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionListUiState(isLoading = true)
        )
}
