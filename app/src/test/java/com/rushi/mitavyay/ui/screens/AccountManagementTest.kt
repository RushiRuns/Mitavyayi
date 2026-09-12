package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.ui.screens.Accounts.AccountsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AccountManagementTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeAccountRepository : AccountRepository {
        val accountsMap = mutableMapOf<String, Account>()
        val transactionsMap = mutableMapOf<String, MutableList<Transaction>>()
        private val accountsFlow = MutableStateFlow<List<Account>>(emptyList())

        private fun updateFlow() {
            accountsFlow.value = accountsMap.values.toList()
        }

        override fun getAllAccounts(): Flow<List<Account>> = accountsFlow

        override fun getActiveAccounts(): Flow<List<Account>> = flowOf(
            accountsMap.values.filter { it.isActive }
        )

        override fun getAccountById(id: String): Flow<Account?> = flowOf(accountsMap[id])

        override suspend fun getAccount(id: String): Account? = accountsMap[id]

        override suspend fun addAccount(account: Account) {
            accountsMap[account.id] = account
            updateFlow()
        }

        override suspend fun createAccount(name: String, type: String, initialBalancePaise: Long): Account {
            val account = Account(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                type = type.lowercase().trim(),
                balance = initialBalancePaise,
                currency = "INR",
                createdAt = System.currentTimeMillis(),
                isActive = true
            )
            accountsMap[account.id] = account

            if (initialBalancePaise != 0L) {
                val txList = transactionsMap.getOrPut(account.id) { mutableListOf() }
                txList.add(
                    Transaction(
                        id = UUID.randomUUID().toString(),
                        accountId = account.id,
                        amount = initialBalancePaise,
                        description = "Initial Balance",
                        timestamp = account.createdAt,
                        category = "Income",
                        tags = "[]",
                        transferId = null,
                        notes = null
                    )
                )
            }

            updateFlow()
            return account
        }

        override suspend fun editAccount(id: String, name: String, type: String) {
            val existing = accountsMap[id] ?: return
            accountsMap[id] = existing.copy(name = name.trim(), type = type.lowercase().trim())
            updateFlow()
        }

        override suspend fun updateAccount(account: Account) {
            accountsMap[account.id] = account
            updateFlow()
        }

        override suspend fun canDeleteAccount(id: String): Boolean {
            val txCount = transactionsMap[id]?.size ?: 0
            return txCount == 0
        }

        override suspend fun deleteAccount(id: String): Boolean {
            return if (canDeleteAccount(id)) {
                accountsMap.remove(id)
                updateFlow()
                true
            } else {
                false
            }
        }

        override suspend fun archiveAccount(id: String) {
            val existing = accountsMap[id] ?: return
            accountsMap[id] = existing.copy(isActive = false)
            updateFlow()
        }

        override suspend fun unarchiveAccount(id: String) {
            val existing = accountsMap[id] ?: return
            accountsMap[id] = existing.copy(isActive = true)
            updateFlow()
        }

        override suspend fun updateBalance(id: String, newBalance: Long) {
            val existing = accountsMap[id] ?: return
            accountsMap[id] = existing.copy(balance = newBalance)
            updateFlow()
        }

        override fun getAccountBalance(id: String): Flow<Long?> = flowOf(accountsMap[id]?.balance)
    }

    @Test
    fun createAccount_createsAccountAndInitialTransaction() = runBlocking {
        val repo = FakeAccountRepository()
        val account = repo.createAccount("SBI Savings", "bank", 2500000L) // ₹25,000.00

        assertEquals("SBI Savings", account.name)
        assertEquals("bank", account.type)
        assertEquals(2500000L, account.balance)
        assertTrue(account.isActive)

        val txList = repo.transactionsMap[account.id]
        assertNotNull(txList)
        assertEquals(1, txList!!.size)
        assertEquals(2500000L, txList[0].amount)
        assertEquals("Initial Balance", txList[0].description)
    }

    @Test
    fun editAccount_updatesNameAndTypePreservingBalance() = runBlocking {
        val repo = FakeAccountRepository()
        val account = repo.createAccount("Old Name", "cash", 100000L)

        repo.editAccount(account.id, "New Wallet", "cash")
        val updated = repo.getAccount(account.id)

        assertNotNull(updated)
        assertEquals("New Wallet", updated!!.name)
        assertEquals("cash", updated.type)
        assertEquals(100000L, updated.balance)
    }

    @Test
    fun deleteAccount_allowedOnlyWhenZeroTransactions() = runBlocking {
        val repo = FakeAccountRepository()

        // Account with 0 transactions
        val emptyAcc = Account(
            id = "acc_empty",
            name = "Empty Account",
            type = "bank",
            balance = 0L,
            currency = "INR",
            createdAt = 1000L,
            isActive = true
        )
        repo.addAccount(emptyAcc)
        assertTrue(repo.canDeleteAccount("acc_empty"))
        val deleted = repo.deleteAccount("acc_empty")
        assertTrue(deleted)
        assertNull(repo.getAccount("acc_empty"))

        // Account with transactions
        val fundedAcc = repo.createAccount("Funded Account", "bank", 50000L)
        assertFalse(repo.canDeleteAccount(fundedAcc.id))
        val deleteBlocked = repo.deleteAccount(fundedAcc.id)
        assertFalse(deleteBlocked)
        assertNotNull(repo.getAccount(fundedAcc.id))
    }

    @Test
    fun archiveAccount_togglesActiveStatusPreservingHistory() = runBlocking {
        val repo = FakeAccountRepository()
        val account = repo.createAccount("Salary Card", "credit", 0L)
        assertTrue(account.isActive)

        repo.archiveAccount(account.id)
        val archived = repo.getAccount(account.id)
        assertNotNull(archived)
        assertFalse(archived!!.isActive)

        repo.unarchiveAccount(account.id)
        val unarchived = repo.getAccount(account.id)
        assertNotNull(unarchived)
        assertTrue(unarchived!!.isActive)
    }

    @Test
    fun viewModel_addAndEditDialogFlow() = runBlocking {
        val repo = FakeAccountRepository()
        val viewModel = AccountsViewModel(repo)

        // Test onAddClick
        viewModel.onAddClick()
        var state = viewModel.uiState.first { it.isAddEditOpen }
        assertTrue(state.isAddEditOpen)
        assertNull(state.editingAccount)

        // Save new account
        viewModel.saveAccount("Pocket Cash", "cash", 50000L) // ₹500.00
        state = viewModel.uiState.first { !it.isAddEditOpen }
        assertEquals(1, state.accounts.size)
        assertEquals("Pocket Cash", state.accounts[0].name)
        assertEquals("₹500.00", state.accounts[0].balanceFormatted)

        // Test onEditClick
        val accountItem = state.accounts[0]
        viewModel.onEditClick(accountItem)
        state = viewModel.uiState.first { it.isAddEditOpen }
        assertTrue(state.isAddEditOpen)
        assertEquals(accountItem.id, state.editingAccount?.id)

        // Save edited account
        viewModel.saveAccount("Main Cash Wallet", "cash", 0L)
        state = viewModel.uiState.first { !it.isAddEditOpen }
        assertEquals("Main Cash Wallet", state.accounts[0].name)
    }

    @Test
    fun viewModel_deletionPolicyWithTransactions() = runBlocking {
        val repo = FakeAccountRepository()
        val viewModel = AccountsViewModel(repo)

        // Create account with initial balance (has transaction)
        viewModel.saveAccount("Active Bank", "bank", 100000L)
        var state = viewModel.uiState.first { state -> state.accounts.any { it.name == "Active Bank" } }
        val accountItem = state.accounts.first { it.name == "Active Bank" }

        // Attempt delete -> must trigger cannot delete dialog and offer archiving
        viewModel.onDeleteClick(accountItem)
        state = viewModel.uiState.first { it.showCannotDeleteDialog }
        assertTrue(state.showCannotDeleteDialog)
        assertEquals(accountItem.id, state.accountToArchive?.id)
        assertNull(state.accountToDelete)

        // Confirm archive
        viewModel.confirmArchiveAccount()
        state = viewModel.uiState.first { !it.showCannotDeleteDialog }
        val archived = state.accounts.first { it.id == accountItem.id }
        assertFalse(archived.isActive)
    }
}
