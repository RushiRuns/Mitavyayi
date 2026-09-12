package com.rushi.mitavyay.ui.screens.RepeatExpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.db.RepeatExpense
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.data.model.RepeatExpenseDisplayItem
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.RepeatExpenseRepository
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

enum class RepeatFilterTab {
    ALL,
    ACTIVE,
    PAUSED
}

data class RepeatExpenseUiState(
    val isLoading: Boolean = false,
    val allExpenses: List<RepeatExpenseDisplayItem> = emptyList(),
    val activeExpenses: List<RepeatExpenseDisplayItem> = emptyList(),
    val pausedExpenses: List<RepeatExpenseDisplayItem> = emptyList(),
    val displayedExpenses: List<RepeatExpenseDisplayItem> = emptyList(),
    val selectedTab: RepeatFilterTab = RepeatFilterTab.ALL,
    val totalMonthlyCommitmentPaise: Long = 0L,
    val categories: List<CategoryDisplayItem> = emptyList(),
    val isAddDialogOpen: Boolean = false,
    val expenseToDelete: RepeatExpenseDisplayItem? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class RepeatExpenseViewModel @Inject constructor(
    private val repeatExpenseRepository: RepeatExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _actionState = MutableStateFlow(ActionState())

    private data class ActionState(
        val selectedTab: RepeatFilterTab = RepeatFilterTab.ALL,
        val isAddDialogOpen: Boolean = false,
        val expenseToDelete: RepeatExpenseDisplayItem? = null,
        val errorMessage: String? = null,
        val successMessage: String? = null
    )

    val uiState: StateFlow<RepeatExpenseUiState> = combine(
        repeatExpenseRepository.getAllRepeatExpenses(),
        categoryRepository.getAllCategories(),
        _actionState
    ) { expenses, categories, action ->
        val items = expenses.map { it.toDisplayItem() }
        val active = items.filter { it.isActive }
        val paused = items.filter { !it.isActive }

        // Compute estimated monthly commitment of active recurring expenses
        val monthlyCommitment = active.sumOf { item ->
            when (item.frequency.uppercase()) {
                "DAILY" -> item.amountPaise * 30L
                "WEEKLY" -> (item.amountPaise * 52L) / 12L
                "MONTHLY" -> item.amountPaise
                "YEARLY" -> item.amountPaise / 12L
                else -> item.amountPaise
            }
        }

        val displayed = when (action.selectedTab) {
            RepeatFilterTab.ALL -> items
            RepeatFilterTab.ACTIVE -> active
            RepeatFilterTab.PAUSED -> paused
        }

        RepeatExpenseUiState(
            isLoading = false,
            allExpenses = items,
            activeExpenses = active,
            pausedExpenses = paused,
            displayedExpenses = displayed,
            selectedTab = action.selectedTab,
            totalMonthlyCommitmentPaise = monthlyCommitment,
            categories = categories.map { it.toDisplayItem() },
            isAddDialogOpen = action.isAddDialogOpen,
            expenseToDelete = action.expenseToDelete,
            errorMessage = action.errorMessage,
            successMessage = action.successMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = RepeatExpenseUiState(isLoading = true)
    )

    fun selectTab(tab: RepeatFilterTab) {
        _actionState.value = _actionState.value.copy(selectedTab = tab)
    }

    fun onAddClick() {
        _actionState.value = _actionState.value.copy(isAddDialogOpen = true, errorMessage = null)
    }

    fun dismissDialogs() {
        _actionState.value = _actionState.value.copy(
            isAddDialogOpen = false,
            expenseToDelete = null,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onDeleteClick(item: RepeatExpenseDisplayItem) {
        _actionState.value = _actionState.value.copy(expenseToDelete = item)
    }

    fun confirmDelete() {
        val toDelete = _actionState.value.expenseToDelete ?: return
        viewModelScope.launch {
            try {
                repeatExpenseRepository.deleteRepeatExpense(toDelete.id)
                dismissDialogs()
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    errorMessage = e.localizedMessage ?: "Failed to delete recurring expense"
                )
            }
        }
    }

    fun toggleActive(id: String) {
        viewModelScope.launch {
            try {
                repeatExpenseRepository.toggleActive(id)
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    errorMessage = e.localizedMessage ?: "Failed to update status"
                )
            }
        }
    }

    fun createRepeatExpense(
        description: String,
        amountPaise: Long,
        frequency: String,
        category: String,
        isActive: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val trimmedDesc = description.trim()
        val trimmedFreq = frequency.trim().uppercase()
        val trimmedCategory = category.trim()

        if (trimmedDesc.isBlank()) {
            val err = "Description cannot be empty"
            _actionState.value = _actionState.value.copy(errorMessage = err)
            onError(err)
            return
        }

        if (amountPaise <= 0L) {
            val err = "Amount must be greater than zero"
            _actionState.value = _actionState.value.copy(errorMessage = err)
            onError(err)
            return
        }

        if (trimmedCategory.isBlank()) {
            val err = "Please select a category"
            _actionState.value = _actionState.value.copy(errorMessage = err)
            onError(err)
            return
        }

        val validFrequencies = listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY")
        if (trimmedFreq !in validFrequencies) {
            val err = "Frequency must be Daily, Weekly, Monthly, or Yearly"
            _actionState.value = _actionState.value.copy(errorMessage = err)
            onError(err)
            return
        }

        viewModelScope.launch {
            try {
                val newExpense = RepeatExpense(
                    id = UUID.randomUUID().toString(),
                    description = trimmedDesc,
                    amount = amountPaise,
                    frequency = trimmedFreq,
                    lastGenerated = System.currentTimeMillis(),
                    category = trimmedCategory,
                    isActive = isActive
                )
                repeatExpenseRepository.addRepeatExpense(newExpense)
                dismissDialogs()
                onSuccess()
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Failed to save recurring expense"
                _actionState.value = _actionState.value.copy(errorMessage = err)
                onError(err)
            }
        }
    }

    fun evaluateDueOccurrences(onCompleted: (Int) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val activeAccounts = accountRepository.getActiveAccounts().first()
                val primaryAccount = activeAccounts.firstOrNull()
                if (primaryAccount != null) {
                    val generated = repeatExpenseRepository.processAllDueOccurrences(primaryAccount.id)
                    if (generated.isNotEmpty()) {
                        _actionState.value = _actionState.value.copy(
                            successMessage = "Generated ${generated.size} due recurring transaction(s)"
                        )
                    }
                    onCompleted(generated.size)
                } else {
                    onCompleted(0)
                }
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    errorMessage = e.localizedMessage ?: "Failed to process recurring expenses"
                )
                onCompleted(0)
            }
        }
    }
}
