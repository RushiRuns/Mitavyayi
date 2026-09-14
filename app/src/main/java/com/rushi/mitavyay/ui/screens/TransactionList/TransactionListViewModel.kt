package com.rushi.mitavyay.ui.screens.TransactionList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.model.DateFilter
import com.rushi.mitavyay.data.model.TransactionDisplayItem
import com.rushi.mitavyay.data.model.TransactionGroup
import com.rushi.mitavyay.data.model.matches
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.util.DateTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionListUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionDisplayItem> = emptyList(),
    val groupedTransactions: List<TransactionGroup> = emptyList(),
    val searchQuery: String = "",
    val dateFilter: DateFilter = DateFilter.AllTime,
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository? = null
) : ViewModel() {

    private val accountsFlow = accountRepository?.getAllAccounts() ?: flowOf(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _dateFilter = MutableStateFlow<DateFilter>(DateFilter.AllTime)

    val uiState: StateFlow<TransactionListUiState> = combine(
        transactionRepository.getAllTransactions(),
        accountsFlow,
        _searchQuery,
        _dateFilter
    ) { transactions, accounts, query, dateFilter ->
        val accountMap = accounts.associateBy { it.id }
        val allDisplayItems = transactions
            .filter { dateFilter.matches(it.timestamp) }
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

        val grouped = if (filtered.isEmpty()) {
            emptyList()
        } else {
            val groupMap = LinkedHashMap<Long, MutableList<TransactionDisplayItem>>()
            for (item in filtered) {
                val startOfDay = DateTimeFormatter.getStartOfDay(item.timestamp)
                groupMap.getOrPut(startOfDay) { mutableListOf() }.add(item)
            }
            groupMap.map { (startOfDay, items) ->
                TransactionGroup(
                    dateLabel = DateTimeFormatter.formatSectionDateHeader(startOfDay),
                    transactions = items
                )
            }
        }

        TransactionListUiState(
            isLoading = false,
            transactions = filtered,
            groupedTransactions = grouped,
            searchQuery = query,
            dateFilter = dateFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionListUiState(isLoading = false)
    )

    val groupedTransactions: StateFlow<List<TransactionGroup>> = uiState
        .map { it.groupedTransactions }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDateFilterChange(filter: DateFilter) {
        _dateFilter.value = filter
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
