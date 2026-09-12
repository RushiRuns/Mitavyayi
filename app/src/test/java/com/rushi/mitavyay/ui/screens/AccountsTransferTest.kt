package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.TransferRepository
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
class AccountsTransferTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeAccountRepo: FakeAccountRepository
    private lateinit var fakeTransferRepo: FakeTransferRepository
    private lateinit var viewModel: AccountsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAccountRepo = FakeAccountRepository()
        fakeTransferRepo = FakeTransferRepository(fakeAccountRepo)

        // Populate two accounts
        runBlocking {
            fakeAccountRepo.addAccount(
                Account(id = "acc_bank", name = "Bank", type = "bank", balance = 500000L, createdAt = 1000L, isActive = true)
            )
            fakeAccountRepo.addAccount(
                Account(id = "acc_cash", name = "Cash", type = "cash", balance = 100000L, createdAt = 2000L, isActive = true)
            )
        }

        viewModel = AccountsViewModel(fakeAccountRepo, fakeTransferRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeAccountRepository : AccountRepository {
        val accountsMap = mutableMapOf<String, Account>()
        private val flow = MutableStateFlow<List<Account>>(emptyList())

        private fun updateFlow() {
            flow.value = accountsMap.values.toList()
        }

        override fun getAllAccounts(): Flow<List<Account>> = flow
        override fun getActiveAccounts(): Flow<List<Account>> =
            flowOf(accountsMap.values.filter { it.isActive })
        override fun getAccountById(id: String): Flow<Account?> = flowOf(accountsMap[id])
        override suspend fun getAccount(id: String): Account? = accountsMap[id]
        override suspend fun addAccount(account: Account) {
            accountsMap[account.id] = account
            updateFlow()
        }
        override suspend fun createAccount(name: String, type: String, initialBalancePaise: Long): Account {
            val acc = Account(UUID.randomUUID().toString(), name, type, initialBalancePaise, "INR", System.currentTimeMillis(), true)
            accountsMap[acc.id] = acc
            updateFlow()
            return acc
        }
        override suspend fun editAccount(id: String, name: String, type: String) {}
        override suspend fun updateAccount(account: Account) {
            accountsMap[account.id] = account
            updateFlow()
        }
        override suspend fun canDeleteAccount(id: String): Boolean = true
        override suspend fun deleteAccount(id: String): Boolean = true
        override suspend fun archiveAccount(id: String) {}
        override suspend fun unarchiveAccount(id: String) {}
        override suspend fun updateBalance(id: String, newBalance: Long) {
            accountsMap[id]?.let {
                accountsMap[id] = it.copy(balance = newBalance)
                updateFlow()
            }
        }
        override fun getAccountBalance(id: String): Flow<Long?> = flowOf(accountsMap[id]?.balance)
    }

    private class FakeTransferRepository(
        private val accountRepo: FakeAccountRepository
    ) : TransferRepository {
        data class TransferCall(
            val fromAccountId: String,
            val toAccountId: String,
            val amountPaise: Long,
            val notes: String?
        )

        val transferCalls = mutableListOf<TransferCall>()
        var shouldThrow = false

        override suspend fun createTransfer(
            fromAccountId: String,
            toAccountId: String,
            amountPaise: Long,
            timestamp: Long,
            notes: String?
        ): String {
            if (shouldThrow) throw IllegalStateException("Transfer failed")
            if (fromAccountId == toAccountId) throw IllegalArgumentException("Same account")
            transferCalls.add(TransferCall(fromAccountId, toAccountId, amountPaise, notes))

            val fromAcc = accountRepo.accountsMap[fromAccountId]
            val toAcc = accountRepo.accountsMap[toAccountId]
            if (fromAcc != null) accountRepo.updateBalance(fromAccountId, fromAcc.balance - amountPaise)
            if (toAcc != null) accountRepo.updateBalance(toAccountId, toAcc.balance + amountPaise)

            return UUID.randomUUID().toString()
        }

        override suspend fun deleteTransfer(transferId: String) {}
        override suspend fun getTransferTransactions(transferId: String): List<Transaction> = emptyList()
    }

    @Test
    fun onTransferClick_opensTransferDialogWithNullInitialAccount() = runBlocking {
        viewModel.onTransferClick(null)
        val state = viewModel.uiState.first { it.isTransferOpen }
        assertTrue(state.isTransferOpen)
        assertNull(state.transferInitialFromAccountId)
    }

    @Test
    fun onTransferClick_opensTransferDialogWithSpecificInitialAccount() = runBlocking {
        viewModel.onTransferClick("acc_bank")
        val state = viewModel.uiState.first { it.isTransferOpen }
        assertTrue(state.isTransferOpen)
        assertEquals("acc_bank", state.transferInitialFromAccountId)
    }

    @Test
    fun executeTransfer_successCallsRepositoryAndDismissesDialog() = runBlocking {
        viewModel.onTransferClick("acc_bank")
        assertTrue(viewModel.uiState.value.isTransferOpen)

        var successCalled = false
        viewModel.executeTransfer(
            fromAccountId = "acc_bank",
            toAccountId = "acc_cash",
            amountPaise = 200000L,
            notes = "Test transfer",
            onSuccess = { successCalled = true }
        )

        assertTrue(successCalled)
        assertEquals(1, fakeTransferRepo.transferCalls.size)
        val call = fakeTransferRepo.transferCalls[0]
        assertEquals("acc_bank", call.fromAccountId)
        assertEquals("acc_cash", call.toAccountId)
        assertEquals(200000L, call.amountPaise)
        assertEquals("Test transfer", call.notes)

        // Dialog should be dismissed
        assertFalse(viewModel.uiState.value.isTransferOpen)

        // Account balances updated
        assertEquals(300000L, fakeAccountRepo.getAccount("acc_bank")!!.balance)
        assertEquals(300000L, fakeAccountRepo.getAccount("acc_cash")!!.balance)
    }

    @Test
    fun executeTransfer_errorPassesMessageToCallback() = runBlocking {
        fakeTransferRepo.shouldThrow = true
        var errorMessage: String? = null

        viewModel.executeTransfer(
            fromAccountId = "acc_bank",
            toAccountId = "acc_cash",
            amountPaise = 100000L,
            notes = null,
            onError = { errorMessage = it }
        )

        assertNotNull(errorMessage)
        assertEquals("Transfer failed", errorMessage)
    }

    @Test
    fun dismissDialogs_clearsTransferState() = runBlocking {
        viewModel.onTransferClick("acc_bank")
        assertTrue(viewModel.uiState.value.isTransferOpen)

        viewModel.dismissDialogs()
        assertFalse(viewModel.uiState.value.isTransferOpen)
        assertNull(viewModel.uiState.value.transferInitialFromAccountId)
    }
}
