package com.rushi.mitavyay.ui.screens.QuickAddExpense

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class QuickAddExpenseUiState(
    val isLoading: Boolean = false,
    val accounts: List<AccountDisplayItem> = emptyList(),
    val categories: List<CategoryDisplayItem> = emptyList(),
    val selectedAccountId: String? = null,
    val selectedCategory: String? = null,
    val amountPaise: Long = 0L,
    val isExpense: Boolean = true,
    val description: String = "",
    val notes: String = "",
    val errorMessage: String? = null,
    val isSavedSuccessfully: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class QuickAddExpenseViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(FormState())

    private data class FormState(
        val amountPaise: Long = 0L,
        val isExpense: Boolean = true,
        val selectedAccountId: String? = null,
        val selectedCategory: String? = null,
        val description: String = "",
        val notes: String = "",
        val errorMessage: String? = null,
        val isSavedSuccessfully: Boolean = false,
        val isSaving: Boolean = false
    )

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                if (categories.isEmpty()) {
                    categoryRepository.seedDefaultCategories()
                }
            }
        }
    }

    val uiState: StateFlow<QuickAddExpenseUiState> = combine(
        accountRepository.getActiveAccounts(),
        categoryRepository.getAllCategories(),
        _formState
    ) { accounts, categories, form ->
        val accountItems = accounts.map { it.toDisplayItem() }
        val categoryItems = categories.map { it.toDisplayItem() }

        val effectiveAccountId = form.selectedAccountId?.takeIf { id -> accountItems.any { it.id == id } }
            ?: accountItems.firstOrNull()?.id

        val effectiveCategory = form.selectedCategory?.takeIf { name -> categoryItems.any { it.name == name } }
            ?: categoryItems.firstOrNull()?.name

        QuickAddExpenseUiState(
            isLoading = false,
            accounts = accountItems,
            categories = categoryItems,
            selectedAccountId = effectiveAccountId,
            selectedCategory = effectiveCategory,
            amountPaise = form.amountPaise,
            isExpense = form.isExpense,
            description = form.description,
            notes = form.notes,
            errorMessage = form.errorMessage,
            isSavedSuccessfully = form.isSavedSuccessfully,
            isSaving = form.isSaving
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = QuickAddExpenseUiState(isLoading = true)
    )

    fun onAmountChange(newAmountPaise: Long) {
        _formState.value = _formState.value.copy(
            amountPaise = newAmountPaise,
            errorMessage = if (_formState.value.errorMessage != null && newAmountPaise > 0) null else _formState.value.errorMessage
        )
    }

    fun onTypeToggle(isExpense: Boolean) {
        _formState.value = _formState.value.copy(isExpense = isExpense)
    }

    fun onAccountSelect(accountId: String) {
        _formState.value = _formState.value.copy(
            selectedAccountId = accountId,
            errorMessage = null
        )
    }

    fun onCategorySelect(categoryName: String) {
        _formState.value = _formState.value.copy(
            selectedCategory = categoryName,
            errorMessage = null
        )
    }

    fun onDescriptionChange(newDescription: String) {
        _formState.value = _formState.value.copy(description = newDescription)
    }

    fun onNotesChange(newNotes: String) {
        _formState.value = _formState.value.copy(notes = newNotes)
    }

    suspend fun submitTransaction(): Boolean {
        val current = uiState.first { !it.isLoading }
        if (current.amountPaise <= 0L) {
            _formState.value = _formState.value.copy(errorMessage = "Amount must be greater than zero")
            return false
        }
        val accountId = current.selectedAccountId
        if (accountId.isNullOrBlank()) {
            _formState.value = _formState.value.copy(errorMessage = "Please select an account")
            return false
        }
        val category = current.selectedCategory
        if (category.isNullOrBlank()) {
            _formState.value = _formState.value.copy(errorMessage = "Please select a category")
            return false
        }

        _formState.value = _formState.value.copy(isSaving = true, errorMessage = null)
        return try {
            val amount = if (current.isExpense) -current.amountPaise else current.amountPaise
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                accountId = accountId,
                amount = amount,
                description = current.description.trim().ifBlank { category },
                timestamp = System.currentTimeMillis(),
                category = category,
                tags = "[]",
                transferId = null,
                notes = current.notes.trim().ifBlank { null }
            )
            transactionRepository.addTransaction(transaction)
            _formState.value = FormState(isSavedSuccessfully = true)
            true
        } catch (e: Exception) {
            _formState.value = _formState.value.copy(
                isSaving = false,
                errorMessage = e.localizedMessage ?: "Failed to save transaction"
            )
            false
        }
    }

    fun saveTransaction(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val success = submitTransaction()
            if (success) {
                onSuccess()
            }
        }
    }

    fun resetForm() {
        _formState.value = FormState()
    }

    fun dismissError() {
        _formState.value = _formState.value.copy(errorMessage = null)
    }

    fun createAndSelectCategory(
        name: String,
        colorHex: String,
        icon: String,
        onComplete: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = categoryRepository.createCustomCategory(name, colorHex, icon)
            result.fold(
                onSuccess = { createdCategory ->
                    _formState.value = _formState.value.copy(
                        selectedCategory = createdCategory.name,
                        errorMessage = null
                    )
                    onComplete(Result.success(Unit))
                },
                onFailure = { error ->
                    onComplete(Result.failure(error))
                }
            )
        }
    }
}
