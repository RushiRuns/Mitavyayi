package com.rushi.mitavyay.ui.screens.Debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.db.Debt
import com.rushi.mitavyay.data.model.DebtDisplayItem
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class DebtTab {
    ACTIVE,
    SETTLED
}

data class DebtUiState(
    val isLoading: Boolean = false,
    val activeDebts: List<DebtDisplayItem> = emptyList(),
    val settledDebts: List<DebtDisplayItem> = emptyList(),
    val totalLentActivePaise: Long = 0L,
    val totalBorrowedActivePaise: Long = 0L,
    val selectedTab: DebtTab = DebtTab.ACTIVE,
    val isAddDebtOpen: Boolean = false,
    val debtToSettle: DebtDisplayItem? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val debtRepository: DebtRepository
) : ViewModel() {

    private val _actionState = MutableStateFlow(ActionState())

    private data class ActionState(
        val selectedTab: DebtTab = DebtTab.ACTIVE,
        val isAddDebtOpen: Boolean = false,
        val debtToSettle: DebtDisplayItem? = null,
        val errorMessage: String? = null
    )

    val uiState: StateFlow<DebtUiState> = combine(
        debtRepository.getAllDebts(),
        _actionState
    ) { debts, action ->
        val active = debts.filter { it.settledAt == null }.map { it.toDisplayItem() }
        val settled = debts.filter { it.settledAt != null }.map { it.toDisplayItem() }

        val totalLent = active.filter { it.isLent }.sumOf { it.amountPaise }
        val totalBorrowed = active.filter { !it.isLent }.sumOf { it.amountPaise }

        DebtUiState(
            isLoading = false,
            activeDebts = active,
            settledDebts = settled,
            totalLentActivePaise = totalLent,
            totalBorrowedActivePaise = totalBorrowed,
            selectedTab = action.selectedTab,
            isAddDebtOpen = action.isAddDebtOpen,
            debtToSettle = action.debtToSettle,
            errorMessage = action.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DebtUiState(isLoading = true)
    )

    fun selectTab(tab: DebtTab) {
        _actionState.value = _actionState.value.copy(selectedTab = tab)
    }

    fun onAddDebtClick() {
        _actionState.value = _actionState.value.copy(isAddDebtOpen = true)
    }

    fun dismissDialogs() {
        _actionState.value = _actionState.value.copy(
            isAddDebtOpen = false,
            debtToSettle = null,
            errorMessage = null
        )
    }

    fun createDebt(
        type: String,
        counterparty: String,
        amountPaise: Long,
        notes: String?,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val trimmedType = type.trim().lowercase()
        val trimmedParty = counterparty.trim()

        if (trimmedParty.isBlank()) {
            val err = "Counterparty name cannot be empty"
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

        if (trimmedType != "lent" && trimmedType != "borrowed") {
            val err = "Type must be either 'lent' or 'borrowed'"
            _actionState.value = _actionState.value.copy(errorMessage = err)
            onError(err)
            return
        }

        viewModelScope.launch {
            try {
                val newDebt = Debt(
                    id = UUID.randomUUID().toString(),
                    type = trimmedType,
                    counterparty = trimmedParty,
                    amount = amountPaise,
                    createdAt = System.currentTimeMillis(),
                    settledAt = null,
                    notes = notes?.trim()?.ifBlank { null }
                )
                debtRepository.addDebt(newDebt)
                dismissDialogs()
                onSuccess()
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Failed to add debt record"
                _actionState.value = _actionState.value.copy(errorMessage = err)
                onError(err)
            }
        }
    }

    fun onSettleClick(debt: DebtDisplayItem) {
        _actionState.value = _actionState.value.copy(debtToSettle = debt)
    }

    fun confirmSettleDebt(onSuccess: () -> Unit = {}) {
        val debt = _actionState.value.debtToSettle ?: return
        viewModelScope.launch {
            try {
                debtRepository.markDebtAsSettled(debt.id, System.currentTimeMillis())
                dismissDialogs()
                onSuccess()
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    errorMessage = e.localizedMessage ?: "Failed to mark debt as settled"
                )
            }
        }
    }
}
