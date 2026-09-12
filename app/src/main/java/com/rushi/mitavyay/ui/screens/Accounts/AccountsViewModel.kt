package com.rushi.mitavyay.ui.screens.Accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val isLoading: Boolean = false,
    val accounts: List<AccountDisplayItem> = emptyList(),
    val isAddEditOpen: Boolean = false,
    val editingAccount: AccountDisplayItem? = null,
    val accountToDelete: AccountDisplayItem? = null,
    val showCannotDeleteDialog: Boolean = false,
    val accountToArchive: AccountDisplayItem? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _dialogState = MutableStateFlow(DialogState())

    private data class DialogState(
        val isAddEditOpen: Boolean = false,
        val editingAccount: AccountDisplayItem? = null,
        val accountToDelete: AccountDisplayItem? = null,
        val showCannotDeleteDialog: Boolean = false,
        val accountToArchive: AccountDisplayItem? = null,
        val errorMessage: String? = null
    )

    val uiState: StateFlow<AccountsUiState> = combine(
        accountRepository.getAllAccounts(),
        _dialogState
    ) { accounts, dialogState ->
        AccountsUiState(
            isLoading = false,
            accounts = accounts.map { it.toDisplayItem() },
            isAddEditOpen = dialogState.isAddEditOpen,
            editingAccount = dialogState.editingAccount,
            accountToDelete = dialogState.accountToDelete,
            showCannotDeleteDialog = dialogState.showCannotDeleteDialog,
            accountToArchive = dialogState.accountToArchive,
            errorMessage = dialogState.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountsUiState(isLoading = true)
    )

    fun onAddClick() {
        _dialogState.value = _dialogState.value.copy(
            isAddEditOpen = true,
            editingAccount = null
        )
    }

    fun onEditClick(account: AccountDisplayItem) {
        _dialogState.value = _dialogState.value.copy(
            isAddEditOpen = true,
            editingAccount = account
        )
    }

    fun onDeleteClick(account: AccountDisplayItem) {
        viewModelScope.launch {
            val canDelete = accountRepository.canDeleteAccount(account.id)
            if (canDelete) {
                _dialogState.value = _dialogState.value.copy(
                    accountToDelete = account,
                    showCannotDeleteDialog = false,
                    accountToArchive = null
                )
            } else {
                _dialogState.value = _dialogState.value.copy(
                    accountToDelete = null,
                    showCannotDeleteDialog = true,
                    accountToArchive = account
                )
            }
        }
    }

    fun saveAccount(name: String, type: String, initialBalancePaise: Long) {
        viewModelScope.launch {
            val editing = _dialogState.value.editingAccount
            if (editing != null) {
                accountRepository.editAccount(editing.id, name, type)
            } else {
                accountRepository.createAccount(name, type, initialBalancePaise)
            }
            dismissDialogs()
        }
    }

    fun confirmDeleteAccount() {
        val account = _dialogState.value.accountToDelete ?: return
        viewModelScope.launch {
            accountRepository.deleteAccount(account.id)
            dismissDialogs()
        }
    }

    fun confirmArchiveAccount() {
        val account = _dialogState.value.accountToArchive ?: return
        viewModelScope.launch {
            accountRepository.archiveAccount(account.id)
            dismissDialogs()
        }
    }

    fun toggleActiveStatus(account: AccountDisplayItem) {
        viewModelScope.launch {
            if (account.isActive) {
                accountRepository.archiveAccount(account.id)
            } else {
                accountRepository.unarchiveAccount(account.id)
            }
        }
    }

    fun dismissDialogs() {
        _dialogState.value = DialogState()
    }
}
