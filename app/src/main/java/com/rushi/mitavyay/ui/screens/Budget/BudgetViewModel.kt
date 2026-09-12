package com.rushi.mitavyay.ui.screens.Budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.model.BudgetAlertLevel
import com.rushi.mitavyay.data.model.BudgetDisplayItem
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.BudgetRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.util.CurrencyFormatter
import com.rushi.mitavyay.util.DateTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetUiState(
    val isLoading: Boolean = false,
    val selectedMonthYear: String = "",
    val monthYearLabel: String = "",
    val items: List<BudgetDisplayItem> = emptyList(),
    val availableCategories: List<CategoryDisplayItem> = emptyList(),
    val totalBudgetedPaise: Long = 0L,
    val totalBudgetedFormatted: String = CurrencyFormatter.format(totalBudgetedPaise),
    val totalSpentPaise: Long = 0L,
    val totalSpentFormatted: String = CurrencyFormatter.format(totalSpentPaise),
    val totalRemainingPaise: Long = 0L,
    val totalRemainingFormatted: String = CurrencyFormatter.format(kotlin.math.abs(totalRemainingPaise)),
    val overallProgress: Float = 0f,
    val warningCount: Int = 0,
    val exceededCount: Int = 0,
    val message: String? = null,
    val isEmpty: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectedMonthYear = MutableStateFlow(DateTimeFormatter.getCurrentMonthYear())
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<BudgetUiState> = _selectedMonthYear.flatMapLatest { monthYear ->
        val (startMs, endMs) = DateTimeFormatter.getMonthStartAndEndTimestamps(monthYear)

        combine(
            budgetRepository.getBudgetsForMonth(monthYear),
            transactionRepository.getCategorySpending(startMs, endMs),
            categoryRepository.getAllCategories(),
            _message
        ) { budgets, spendings, categories, message ->
            val categorySpendingMap = spendings.associate { it.category to it.totalExpensePaise }
            val categoryMap = categories.associateBy { it.name }

            val items = budgets.map { budget ->
                val spent = categorySpendingMap[budget.category] ?: 0L
                val remaining = budget.amount - spent
                val progress = if (budget.amount > 0L) spent.toFloat() / budget.amount.toFloat() else 0f
                val percentage = (progress * 100).toInt()

                val alertLevel = when {
                    progress >= 1.0f -> BudgetAlertLevel.EXCEEDED_100
                    progress >= 0.8f -> BudgetAlertLevel.WARNING_80
                    else -> BudgetAlertLevel.SAFE
                }

                val catEntity = categoryMap[budget.category]

                BudgetDisplayItem(
                    id = budget.id,
                    category = budget.category,
                    monthYear = budget.monthYear,
                    budgetAmountPaise = budget.amount,
                    spentAmountPaise = spent,
                    remainingAmountPaise = remaining,
                    progress = progress,
                    progressPercentage = percentage,
                    alertLevel = alertLevel,
                    categoryColorHex = catEntity?.color,
                    categoryIcon = catEntity?.icon,
                    isActive = budget.isActive
                )
            }

            val totalBudgeted = items.sumOf { it.budgetAmountPaise }
            val totalSpent = items.sumOf { it.spentAmountPaise }
            val totalRemaining = totalBudgeted - totalSpent
            val overallProgress = if (totalBudgeted > 0L) totalSpent.toFloat() / totalBudgeted.toFloat() else 0f
            val warningCount = items.count { it.alertLevel == BudgetAlertLevel.WARNING_80 }
            val exceededCount = items.count { it.alertLevel == BudgetAlertLevel.EXCEEDED_100 }

            BudgetUiState(
                isLoading = false,
                selectedMonthYear = monthYear,
                monthYearLabel = DateTimeFormatter.formatMonthYear(monthYear),
                items = items,
                availableCategories = categories.map { it.toDisplayItem() },
                totalBudgetedPaise = totalBudgeted,
                totalSpentPaise = totalSpent,
                totalRemainingPaise = totalRemaining,
                overallProgress = overallProgress,
                warningCount = warningCount,
                exceededCount = exceededCount,
                message = message,
                isEmpty = items.isEmpty()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = BudgetUiState(isLoading = true)
    )

    fun selectMonth(monthYear: String) {
        _selectedMonthYear.value = monthYear
    }

    fun previousMonth() {
        _selectedMonthYear.value = DateTimeFormatter.getAdjacentMonthYear(_selectedMonthYear.value, -1)
    }

    fun nextMonth() {
        _selectedMonthYear.value = DateTimeFormatter.getAdjacentMonthYear(_selectedMonthYear.value, 1)
    }

    fun goToCurrentMonth() {
        _selectedMonthYear.value = DateTimeFormatter.getCurrentMonthYear()
    }

    fun setBudget(category: String, amountPaise: Long) {
        viewModelScope.launch {
            try {
                budgetRepository.setBudget(category, _selectedMonthYear.value, amountPaise)
                _message.value = "Budget set for $category"
            } catch (e: Exception) {
                _message.value = e.localizedMessage ?: "Failed to set budget"
            }
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(id)
                _message.value = "Budget deleted"
            } catch (e: Exception) {
                _message.value = e.localizedMessage ?: "Failed to delete budget"
            }
        }
    }

    fun copyPreviousMonthBudgets() {
        viewModelScope.launch {
            try {
                val prevMonth = DateTimeFormatter.getAdjacentMonthYear(_selectedMonthYear.value, -1)
                val copied = budgetRepository.copyBudgetsToMonth(prevMonth, _selectedMonthYear.value)
                _message.value = if (copied > 0) {
                    "Copied $copied budgets from previous month"
                } else {
                    "No previous month budgets to copy"
                }
            } catch (e: Exception) {
                _message.value = e.localizedMessage ?: "Failed to copy budgets"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
