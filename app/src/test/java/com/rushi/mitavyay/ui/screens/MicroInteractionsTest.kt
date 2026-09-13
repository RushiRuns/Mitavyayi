package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.AnalysisPeriod
import com.rushi.mitavyay.data.repository.AnalysisSummary
import com.rushi.mitavyay.data.repository.CategorySpending
import com.rushi.mitavyay.data.repository.TimeSpendingPoint
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.ui.screens.TransactionList.TransactionListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class MicroInteractionsTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var fakeAccountRepository: FakeAccountRepository
    private lateinit var viewModel: TransactionListViewModel

    private class FakeAccountRepository : AccountRepository {
        val accountsMap = mutableMapOf<String, Account>()
        private val flow = MutableStateFlow<List<Account>>(emptyList())

        private fun updateFlow() {
            flow.value = accountsMap.values.toList()
        }

        override fun getAllAccounts(): Flow<List<Account>> = flow
        override fun getActiveAccounts(): Flow<List<Account>> = flowOf(accountsMap.values.filter { it.isActive })
        override fun getAccountById(id: String): Flow<Account?> = flowOf(accountsMap[id])
        override suspend fun getAccount(id: String): Account? = accountsMap[id]
        override fun getAccountBalance(id: String): Flow<Long?> = flowOf(accountsMap[id]?.balance)

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

        override suspend fun editAccount(id: String, name: String, type: String) {
            val existing = accountsMap[id] ?: return
            accountsMap[id] = existing.copy(name = name, type = type)
            updateFlow()
        }

        override suspend fun updateAccount(account: Account) {
            accountsMap[account.id] = account
            updateFlow()
        }

        override suspend fun deleteAccount(id: String): Boolean {
            accountsMap.remove(id)
            updateFlow()
            return true
        }

        override suspend fun canDeleteAccount(id: String): Boolean = true
        override suspend fun archiveAccount(id: String) {}
        override suspend fun unarchiveAccount(id: String) {}

        override suspend fun updateBalance(id: String, newBalance: Long) {
            val existing = accountsMap[id] ?: return
            accountsMap[id] = existing.copy(balance = newBalance)
            updateFlow()
        }
    }

    private class FakeTransactionRepository(
        private val accountRepo: FakeAccountRepository
    ) : TransactionRepository {
        val transactions = mutableListOf<Transaction>()
        private val flow = MutableStateFlow<List<Transaction>>(emptyList())

        private fun updateFlow() {
            flow.value = transactions.toList()
        }

        override fun getAllTransactions(): Flow<List<Transaction>> = flow

        override suspend fun getTransactionById(id: String): Transaction? =
            transactions.find { it.id == id }

        override suspend fun addTransaction(transaction: Transaction) {
            transactions.add(transaction)
            updateFlow()
            val acc = accountRepo.accountsMap[transaction.accountId]
            if (acc != null) {
                accountRepo.updateBalance(acc.id, acc.balance + transaction.amount)
            }
        }

        override suspend fun updateTransaction(transaction: Transaction) {
            val idx = transactions.indexOfFirst { it.id == transaction.id }
            if (idx >= 0) {
                val old = transactions[idx]
                transactions[idx] = transaction
                updateFlow()
                val delta = transaction.amount - old.amount
                val acc = accountRepo.accountsMap[transaction.accountId]
                if (acc != null) {
                    accountRepo.updateBalance(acc.id, acc.balance + delta)
                }
            }
        }

        override suspend fun deleteTransaction(id: String) {
            val tx = transactions.find { it.id == id }
            if (tx != null) {
                transactions.remove(tx)
                updateFlow()
                val acc = accountRepo.accountsMap[tx.accountId]
                if (acc != null) {
                    accountRepo.updateBalance(acc.id, acc.balance - tx.amount)
                }
            }
        }

        override suspend fun addTransactions(transactions: List<Transaction>) {
            this.transactions.addAll(transactions)
            updateFlow()
        }

        override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> =
            flowOf(transactions.filter { it.timestamp in start..end })

        override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> =
            flowOf(transactions.filter { it.category == category })

        override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> =
            flowOf(transactions.filter { it.accountId == accountId })

        override fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?> =
            flowOf(transactions.filter { it.timestamp in start..end && it.amount < 0 }.map { it.amount }.sum())

        override fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?> =
            flowOf(transactions.filter { it.timestamp in start..end && it.amount > 0 }.map { it.amount }.sum())

        override fun getCategorySpending(start: Long, end: Long): Flow<List<CategorySpending>> = flowOf(emptyList())
        override fun getTimeSpendingTrend(start: Long, end: Long, period: AnalysisPeriod): Flow<List<TimeSpendingPoint>> = flowOf(emptyList())
        override fun getAnalysisSummary(start: Long, end: Long): Flow<AnalysisSummary> = flowOf(AnalysisSummary())
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAccountRepository = FakeAccountRepository()
        runBlocking {
            fakeAccountRepository.addAccount(
                Account(
                    id = "acc-1",
                    name = "Main Bank",
                    type = "Bank",
                    balance = 100000L
                )
            )
        }
        fakeTransactionRepository = FakeTransactionRepository(fakeAccountRepository)
        viewModel = TransactionListViewModel(fakeTransactionRepository, fakeAccountRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun transactionList_transitionsFromLoadingToEmptyState() = runBlocking {
        val state = viewModel.uiState.first()
        assertFalse("Should not be in loading state after emission", state.isLoading)
        assertTrue("Transactions should initially be empty", state.transactions.isEmpty())
    }

    @Test
    fun swipeToDelete_deletesTransactionAndUpdatesBalance() = runBlocking {
        val txId = UUID.randomUUID().toString()
        val tx = Transaction(
            id = txId,
            accountId = "acc-1",
            amount = -25000L, // -250.00
            category = "Food",
            description = "Dinner",
            timestamp = System.currentTimeMillis()
        )
        fakeTransactionRepository.addTransaction(tx)

        val populatedState = viewModel.uiState.first { it.transactions.isNotEmpty() }
        assertEquals(1, populatedState.transactions.size)
        assertEquals("Dinner", populatedState.transactions[0].description)
        assertEquals(75000L, fakeAccountRepository.getAccount("acc-1")?.balance)

        // Trigger delete
        viewModel.deleteTransaction(txId)

        val emptyState = viewModel.uiState.first { it.transactions.isEmpty() }
        assertEquals(0, emptyState.transactions.size)
        // Balance reversed back to 100000L (+250.00)
        assertEquals(100000L, fakeAccountRepository.getAccount("acc-1")?.balance)
    }

    @Test
    fun searchFiltering_returnsMatchingOrEmptyIllustrativeState() = runBlocking {
        fakeTransactionRepository.addTransaction(
            Transaction(
                id = "1",
                accountId = "acc-1",
                amount = -5000L,
                category = "Groceries",
                description = "Weekly Vegetables",
                timestamp = System.currentTimeMillis()
            )
        )
        fakeTransactionRepository.addTransaction(
            Transaction(
                id = "2",
                accountId = "acc-1",
                amount = -15000L,
                category = "Fuel",
                description = "Petrol refill",
                timestamp = System.currentTimeMillis()
            )
        )

        val state = viewModel.uiState.first { it.transactions.size == 2 }
        assertEquals(2, state.transactions.size)

        // Query matching one item
        viewModel.onSearchQueryChange("Vegetables")
        val matchState = viewModel.uiState.first { it.transactions.size == 1 }
        assertEquals("Weekly Vegetables", matchState.transactions[0].description)

        // Query matching nothing
        viewModel.onSearchQueryChange("NonExistentKeyword")
        val noMatchState = viewModel.uiState.first { it.transactions.isEmpty() }
        assertEquals(0, noMatchState.transactions.size)
        assertEquals("NonExistentKeyword", noMatchState.searchQuery)
    }
}
