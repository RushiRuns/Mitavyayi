package com.rushi.mitavyay.ui.screens.Goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.db.Goal
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.data.model.GoalDisplayItem
import com.rushi.mitavyay.data.model.GoalStatus
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class GoalFilterTab {
    ALL,
    IN_PROGRESS,
    ACHIEVED,
    OVERDUE
}

data class GoalsUiState(
    val isLoading: Boolean = false,
    val allGoals: List<GoalDisplayItem> = emptyList(),
    val displayedGoals: List<GoalDisplayItem> = emptyList(),
    val selectedTab: GoalFilterTab = GoalFilterTab.ALL,
    val totalTargetPaise: Long = 0L,
    val totalSavedPaise: Long = 0L,
    val overallProgress: Float = 0f,
    val activeCount: Int = 0,
    val achievedCount: Int = 0,
    val availableAccounts: List<AccountDisplayItem> = emptyList(),
    val availableCategories: List<CategoryDisplayItem> = emptyList(),
    val isAddDialogOpen: Boolean = false,
    val goalForSavings: GoalDisplayItem? = null,
    val goalToDelete: GoalDisplayItem? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _actionState = MutableStateFlow(ActionState())

    private data class ActionState(
        val selectedTab: GoalFilterTab = GoalFilterTab.ALL,
        val isAddDialogOpen: Boolean = false,
        val goalForSavings: GoalDisplayItem? = null,
        val goalToDelete: GoalDisplayItem? = null,
        val errorMessage: String? = null,
        val successMessage: String? = null
    )

    val uiState: StateFlow<GoalsUiState> = combine(
        goalRepository.getAllGoals(),
        accountRepository.getAllAccounts(),
        categoryRepository.getAllCategories(),
        _actionState
    ) { goals, accounts, categories, action ->
        val now = System.currentTimeMillis()
        val accountMap = accounts.associateBy { it.id }

        val goalItems = goals.map { goal ->
            val linkedAcc = goal.linkedAccountId?.let { accountMap[it] }
            goal.toDisplayItem(linkedAccount = linkedAcc, currentTimeMs = now)
        }

        val inProgress = goalItems.filter { it.status == GoalStatus.ON_TRACK || it.status == GoalStatus.WARNING }
        val achieved = goalItems.filter { it.status == GoalStatus.ACHIEVED }
        val overdue = goalItems.filter { it.status == GoalStatus.OVERDUE }

        val totalTarget = goalItems.sumOf { it.targetAmountPaise }
        val totalSaved = goalItems.sumOf { it.currentAmountPaise }
        val overallProgress = if (totalTarget > 0L) {
            (totalSaved.toFloat() / totalTarget.toFloat()).coerceIn(0f, 1f)
        } else 0f

        val displayed = when (action.selectedTab) {
            GoalFilterTab.ALL -> goalItems
            GoalFilterTab.IN_PROGRESS -> inProgress
            GoalFilterTab.ACHIEVED -> achieved
            GoalFilterTab.OVERDUE -> overdue
        }

        GoalsUiState(
            isLoading = false,
            allGoals = goalItems,
            displayedGoals = displayed,
            selectedTab = action.selectedTab,
            totalTargetPaise = totalTarget,
            totalSavedPaise = totalSaved,
            overallProgress = overallProgress,
            activeCount = inProgress.size + overdue.size,
            achievedCount = achieved.size,
            availableAccounts = accounts.filter { it.isActive }.map { it.toDisplayItem() },
            availableCategories = categories.map { it.toDisplayItem() },
            isAddDialogOpen = action.isAddDialogOpen,
            goalForSavings = action.goalForSavings,
            goalToDelete = action.goalToDelete,
            errorMessage = action.errorMessage,
            successMessage = action.successMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = GoalsUiState(isLoading = true)
    )

    fun selectTab(tab: GoalFilterTab) {
        _actionState.value = _actionState.value.copy(selectedTab = tab)
    }

    fun onAddGoalClick() {
        _actionState.value = _actionState.value.copy(isAddDialogOpen = true, errorMessage = null)
    }

    fun onAddSavingsClick(item: GoalDisplayItem) {
        _actionState.value = _actionState.value.copy(goalForSavings = item, errorMessage = null)
    }

    fun onDeleteClick(item: GoalDisplayItem) {
        _actionState.value = _actionState.value.copy(goalToDelete = item)
    }

    fun dismissDialogs() {
        _actionState.value = _actionState.value.copy(
            isAddDialogOpen = false,
            goalForSavings = null,
            goalToDelete = null,
            errorMessage = null,
            successMessage = null
        )
    }

    fun createGoal(
        name: String,
        targetAmountPaise: Long,
        deadlineMs: Long,
        linkedAccountId: String?,
        initialDepositPaise: Long = 0L,
        category: String = "General",
        notes: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val trimmedName = name.trim()
        val trimmedCat = category.trim().ifBlank { "General" }

        if (trimmedName.isBlank()) {
            val err = "Goal name cannot be blank"
            _actionState.value = _actionState.value.copy(errorMessage = err)
            onError(err)
            return
        }

        if (targetAmountPaise <= 0L) {
            val err = "Target amount must be greater than zero"
            _actionState.value = _actionState.value.copy(errorMessage = err)
            onError(err)
            return
        }

        if (deadlineMs <= 0L) {
            val err = "Please select a valid deadline"
            _actionState.value = _actionState.value.copy(errorMessage = err)
            onError(err)
            return
        }

        viewModelScope.launch {
            try {
                val newGoal = Goal(
                    id = UUID.randomUUID().toString(),
                    name = trimmedName,
                    targetAmount = targetAmountPaise,
                    deadline = deadlineMs,
                    currentAmount = if (linkedAccountId == null) initialDepositPaise.coerceAtLeast(0L) else 0L,
                    linkedAccountId = linkedAccountId?.ifBlank { null },
                    category = trimmedCat,
                    notes = notes?.trim()?.ifBlank { null }
                )
                goalRepository.addGoal(newGoal)
                dismissDialogs()
                onSuccess()
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Failed to create savings goal"
                _actionState.value = _actionState.value.copy(errorMessage = err)
                onError(err)
            }
        }
    }

    fun addSavings(goalId: String, amountPaise: Long, onSuccess: () -> Unit = {}) {
        if (amountPaise <= 0L) {
            _actionState.value = _actionState.value.copy(errorMessage = "Deposit amount must be greater than zero")
            return
        }

        viewModelScope.launch {
            try {
                goalRepository.addSavings(goalId, amountPaise)
                dismissDialogs()
                onSuccess()
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    errorMessage = e.localizedMessage ?: "Failed to add savings deposit"
                )
            }
        }
    }

    fun confirmDelete() {
        val toDelete = _actionState.value.goalToDelete ?: return
        viewModelScope.launch {
            try {
                goalRepository.deleteGoal(toDelete.id)
                dismissDialogs()
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    errorMessage = e.localizedMessage ?: "Failed to delete goal"
                )
            }
        }
    }
}
