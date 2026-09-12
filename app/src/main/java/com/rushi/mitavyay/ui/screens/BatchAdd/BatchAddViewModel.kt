package com.rushi.mitavyay.ui.screens.BatchAdd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class BatchTransactionRow(
    val id: String = UUID.randomUUID().toString(),
    val amountPaise: Long = 0L,
    val isExpense: Boolean = true,
    val description: String = "",
    val category: String = "",
    val accountId: String = "",
    val notes: String = ""
) {
    val isValid: Boolean get() = amountPaise > 0L && accountId.isNotBlank() && category.isNotBlank()
}

data class BatchAddUiState(
    val isLoading: Boolean = true,
    val accounts: List<AccountDisplayItem> = emptyList(),
    val categories: List<CategoryDisplayItem> = emptyList(),
    val defaultAccountId: String? = null,
    val rows: List<BatchTransactionRow> = emptyList(),
    val totalExpensePaise: Long = 0L,
    val totalIncomePaise: Long = 0L,
    val canSubmit: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
) {
    val isSavedSuccessfully: Boolean get() = isSuccess
}

@HiltViewModel
class BatchAddViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _rows = MutableStateFlow<List<BatchTransactionRow>>(emptyList())
    private val _defaultAccountId = MutableStateFlow<String?>(null)
    private val _isSaving = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isSuccess = MutableStateFlow(false)

    val uiState: StateFlow<BatchAddUiState> = combine(
        accountRepository.getActiveAccounts(),
        categoryRepository.getAllCategories(),
        _rows,
        _defaultAccountId,
        _isSaving,
        _errorMessage,
        _isSuccess
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val accounts = (args[0] as List<com.rushi.mitavyay.data.db.Account>).map { it.toDisplayItem() }
        @Suppress("UNCHECKED_CAST")
        val categories = (args[1] as List<com.rushi.mitavyay.data.db.Category>).map { it.toDisplayItem() }
        @Suppress("UNCHECKED_CAST")
        val rows = args[2] as List<BatchTransactionRow>
        val defaultAccountId = args[3] as String?
        val isSaving = args[4] as Boolean
        val errorMessage = args[5] as String?
        val isSuccess = args[6] as Boolean

        val effectiveDefaultAccount = defaultAccountId ?: accounts.firstOrNull()?.id

        // Auto-initialize rows if empty
        val effectiveRows = if (rows.isEmpty() && accounts.isNotEmpty() && categories.isNotEmpty()) {
            val defAcc = effectiveDefaultAccount ?: ""
            val defCat = categories.firstOrNull()?.name ?: ""
            listOf(
                BatchTransactionRow(accountId = defAcc, category = defCat, isExpense = true),
                BatchTransactionRow(accountId = defAcc, category = defCat, isExpense = true)
            )
        } else {
            rows
        }

        val totalExpense = effectiveRows.filter { it.isExpense && it.amountPaise > 0L }.sumOf { it.amountPaise }
        val totalIncome = effectiveRows.filter { !it.isExpense && it.amountPaise > 0L }.sumOf { it.amountPaise }
        val canSubmit = effectiveRows.isNotEmpty() && effectiveRows.all { it.isValid } && !isSaving

        BatchAddUiState(
            isLoading = accounts.isEmpty() && categories.isEmpty(),
            accounts = accounts,
            categories = categories,
            defaultAccountId = effectiveDefaultAccount,
            rows = effectiveRows,
            totalExpensePaise = totalExpense,
            totalIncomePaise = totalIncome,
            canSubmit = canSubmit,
            isSaving = isSaving,
            errorMessage = errorMessage,
            isSuccess = isSuccess
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = BatchAddUiState(isLoading = true)
    )

    fun addRow() {
        val currentRows = uiState.value.rows
        val defAcc = uiState.value.defaultAccountId ?: uiState.value.accounts.firstOrNull()?.id ?: ""
        val defCat = uiState.value.categories.firstOrNull()?.name ?: ""
        _rows.value = currentRows + BatchTransactionRow(
            accountId = defAcc,
            category = defCat,
            isExpense = true
        )
        _errorMessage.value = null
    }

    fun removeRow(index: Int) {
        val currentRows = uiState.value.rows
        if (currentRows.size > 1 && index in currentRows.indices) {
            val updated = currentRows.toMutableList()
            updated.removeAt(index)
            _rows.value = updated
            _errorMessage.value = null
        }
    }

    fun updateRow(index: Int, updatedRow: BatchTransactionRow) {
        val currentRows = uiState.value.rows.toMutableList()
        if (index in currentRows.indices) {
            currentRows[index] = updatedRow
            _rows.value = currentRows
            _errorMessage.value = null
        }
    }

    fun setDefaultAccount(accountId: String) {
        _defaultAccountId.value = accountId
        val currentRows = uiState.value.rows
        // Update any row whose account is blank
        _rows.value = currentRows.map { row ->
            if (row.accountId.isBlank()) row.copy(accountId = accountId) else row
        }
    }

    fun submitBatch(onSuccess: () -> Unit = {}) {
        val currentRows = uiState.value.rows
        if (currentRows.isEmpty()) {
            _errorMessage.value = "No transactions to add"
            return
        }

        val invalidIndex = currentRows.indexOfFirst { !it.isValid }
        if (invalidIndex >= 0) {
            val rowNum = invalidIndex + 1
            _errorMessage.value = "Transaction #$rowNum has incomplete details (check amount, category, or account)"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            try {
                val transactions = currentRows.map { row ->
                    val finalAmount = if (row.isExpense) -row.amountPaise else row.amountPaise
                    Transaction(
                        id = row.id,
                        accountId = row.accountId,
                        amount = finalAmount,
                        description = row.description.trim().ifBlank { row.category },
                        timestamp = System.currentTimeMillis(),
                        category = row.category,
                        tags = "[]",
                        transferId = null,
                        notes = row.notes.trim().ifBlank { null }
                    )
                }
                transactionRepository.addTransactions(transactions)
                _isSuccess.value = true
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                _errorMessage.value = e.localizedMessage ?: "Failed to save transactions"
            }
        }
    }

    fun reset() {
        val defAcc = uiState.value.defaultAccountId ?: uiState.value.accounts.firstOrNull()?.id ?: ""
        val defCat = uiState.value.categories.firstOrNull()?.name ?: ""
        _rows.value = listOf(
            BatchTransactionRow(accountId = defAcc, category = defCat, isExpense = true),
            BatchTransactionRow(accountId = defAcc, category = defCat, isExpense = true)
        )
        _isSaving.value = false
        _errorMessage.value = null
        _isSuccess.value = false
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
