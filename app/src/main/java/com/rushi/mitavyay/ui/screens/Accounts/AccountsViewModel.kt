package com.rushi.mitavyay.ui.screens.Accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AccountsUiState(
    val isLoading: Boolean = false,
    val accounts: List<AccountDisplayItem> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = accountRepository.getAllAccounts()
        .map { list ->
            AccountsUiState(
                isLoading = false,
                accounts = list.map { it.toDisplayItem() }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountsUiState(isLoading = true)
        )
}
