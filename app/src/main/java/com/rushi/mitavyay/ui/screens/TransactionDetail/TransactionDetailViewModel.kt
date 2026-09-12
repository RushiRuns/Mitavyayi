package com.rushi.mitavyay.ui.screens.TransactionDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.data.model.TransactionDisplayItem
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.ui.navigation.NavDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val transaction: Transaction? = null,
    val displayItem: TransactionDisplayItem? = null,
    val accountName: String? = null,
    val availableAccounts: List<AccountDisplayItem> = emptyList(),
    val availableCategories: List<CategoryDisplayItem> = emptyList(),
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val transactionId: String = savedStateHandle[NavDestination.TransactionDetail.ARG_TRANSACTION_ID] ?: ""

    private val _actionState = MutableStateFlow(ActionState())

    private data class ActionState(
        val isDeleted: Boolean = false,
        val errorMessage: String? = null
    )

    val uiState: StateFlow<TransactionDetailUiState> = combine(
        transactionRepository.getAllTransactions(),
        accountRepository.getAllAccounts(),
        categoryRepository.getAllCategories(),
        _actionState
    ) { transactions, accounts, categories, action ->
        val tx = transactions.find { it.id == transactionId }
        val account = accounts.find { it.id == tx?.accountId }
        val accountItems = accounts.filter { it.isActive }.map { it.toDisplayItem() }
        val categoryItems = categories.map { it.toDisplayItem() }

        TransactionDetailUiState(
            isLoading = false,
            transaction = tx,
            displayItem = tx?.toDisplayItem(accountName = account?.name),
            accountName = account?.name,
            availableAccounts = accountItems,
            availableCategories = categoryItems,
            isDeleted = action.isDeleted,
            errorMessage = action.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TransactionDetailUiState(isLoading = true)
    )

    fun deleteTransaction(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transactionId)
                _actionState.value = _actionState.value.copy(isDeleted = true)
                onSuccess()
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    errorMessage = e.localizedMessage ?: "Failed to delete transaction"
                )
            }
        }
    }

    fun duplicateTransaction(onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            val currentTx = uiState.value.transaction ?: return@launch
            try {
                val newId = UUID.randomUUID().toString()
                val duplicated = currentTx.copy(
                    id = newId,
                    timestamp = System.currentTimeMillis()
                )
                transactionRepository.addTransaction(duplicated)
                onSuccess(newId)
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    errorMessage = e.localizedMessage ?: "Failed to duplicate transaction"
                )
            }
        }
    }

    fun updateTransaction(
        amountPaise: Long,
        isExpense: Boolean,
        description: String,
        category: String,
        accountId: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentTx = uiState.value.transaction ?: return@launch
            try {
                val finalAmount = if (isExpense) -abs(amountPaise) else abs(amountPaise)
                val updated = currentTx.copy(
                    amount = finalAmount,
                    description = description.trim().ifBlank { category },
                    category = category,
                    accountId = accountId
                )
                transactionRepository.updateTransaction(updated)
                onSuccess()
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    errorMessage = e.localizedMessage ?: "Failed to update transaction"
                )
            }
        }
    }

    fun dismissError() {
        _actionState.value = _actionState.value.copy(errorMessage = null)
    }
}
