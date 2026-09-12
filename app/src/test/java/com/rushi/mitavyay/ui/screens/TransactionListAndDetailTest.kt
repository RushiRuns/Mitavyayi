package com.rushi.mitavyay.ui.screens

import androidx.lifecycle.SavedStateHandle
import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.data.repository.TransferRepository
import com.rushi.mitavyay.ui.navigation.NavDestination
import com.rushi.mitavyay.ui.screens.TransactionDetail.TransactionDetailViewModel
import com.rushi.mitavyay.ui.screens.TransactionList.TransactionListViewModel
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
class TransactionListAndDetailTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeTxRepo: FakeTransactionRepository
    private lateinit var fakeAccountRepo: FakeAccountRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository

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

                if (old.accountId == transaction.accountId) {
                    val delta = transaction.amount - old.amount
                    val acc = accountRepo.accountsMap[transaction.accountId]
                    if (acc != null) {
                        accountRepo.updateBalance(acc.id, acc.balance + delta)
                    }
                } else {
                    val oldAcc = accountRepo.accountsMap[old.accountId]
                    if (oldAcc != null) {
                        accountRepo.updateBalance(oldAcc.id, oldAcc.balance - old.amount)
                    }
                    val newAcc = accountRepo.accountsMap[transaction.accountId]
                    if (newAcc != null) {
                        accountRepo.updateBalance(newAcc.id, newAcc.balance + transaction.amount)
                    }
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

        override fun getCategorySpending(start: Long, end: Long): Flow<List<com.rushi.mitavyay.data.repository.CategorySpending>> = flowOf(emptyList())
        override fun getTimeSpendingTrend(start: Long, end: Long, period: com.rushi.mitavyay.data.repository.AnalysisPeriod): Flow<List<com.rushi.mitavyay.data.repository.TimeSpendingPoint>> = flowOf(emptyList())
        override fun getAnalysisSummary(start: Long, end: Long): Flow<com.rushi.mitavyay.data.repository.AnalysisSummary> = flowOf(com.rushi.mitavyay.data.repository.AnalysisSummary())
    }

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

        override fun getAccountBalance(id: String): Flow<Long?> = flowOf(accountsMap[id]?.balance)
    }

    private class FakeCategoryRepository : CategoryRepository {
        val categories = mutableListOf<Category>()
        private val flow = MutableStateFlow<List<Category>>(emptyList())

        override fun getAllCategories(): Flow<List<Category>> = flow
        override fun getCustomCategories(): Flow<List<Category>> = flowOf(categories.filter { it.isCustom })
        override suspend fun getCategoryById(id: String): Category? = categories.find { it.id == id }
        override suspend fun getCategoryByName(name: String): Category? =
            categories.find { it.name.equals(name, ignoreCase = true) }
        override suspend fun addCategory(category: Category) {
            categories.add(category)
            flow.value = categories.toList()
        }
        override suspend fun updateCategory(category: Category) {}
        override suspend fun deleteCategory(id: String) {}
        override suspend fun seedDefaultCategories() {
            categories.addAll(
                listOf(
                    Category("cat_food", "Food & Dining", "restaurant", "#FF7043", false),
                    Category("cat_salary", "Salary & Income", "payments", "#66BB6A", false)
                )
            )
            flow.value = categories.toList()
        }
        override suspend fun createCustomCategory(name: String, colorHex: String, icon: String) =
            Result.failure<Category>(NotImplementedError())
        override suspend fun updateCustomCategory(id: String, name: String, colorHex: String, icon: String) =
            Result.failure<Unit>(NotImplementedError())
        override suspend fun canDeleteCategory(id: String) = false
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAccountRepo = FakeAccountRepository()
        fakeTxRepo = FakeTransactionRepository(fakeAccountRepo)
        fakeCategoryRepo = FakeCategoryRepository()

        runBlocking {
            fakeAccountRepo.addAccount(
                Account("acc_bank", "HDFC Bank", "bank", 500000L, "INR", 1000L, true)
            )
            fakeAccountRepo.addAccount(
                Account("acc_cash", "Wallet Cash", "cash", 100000L, "INR", 1001L, true)
            )
            fakeCategoryRepo.seedDefaultCategories()
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun transactionList_displaysSortedTransactionsWithAccountNames() = runBlocking {
        // Insert transactions out of chronological order
        fakeTxRepo.addTransaction(
            Transaction("tx_older", "acc_bank", -25000L, "Older Dinner", 1000L, "Food & Dining")
        )
        fakeTxRepo.addTransaction(
            Transaction("tx_newest", "acc_cash", -10000L, "Newest Coffee", 3000L, "Food & Dining")
        )
        fakeTxRepo.addTransaction(
            Transaction("tx_middle", "acc_bank", 500000L, "Middle Salary", 2000L, "Salary & Income")
        )

        val viewModel = TransactionListViewModel(fakeTxRepo, fakeAccountRepo)
        val state = viewModel.uiState.first { it.transactions.size == 3 }

        assertFalse(state.isLoading)
        assertEquals(3, state.transactions.size)

        // Verify sorted by newest first
        assertEquals("tx_newest", state.transactions[0].id)
        assertEquals("Newest Coffee", state.transactions[0].description)
        assertEquals("Wallet Cash", state.transactions[0].accountName)

        assertEquals("tx_middle", state.transactions[1].id)
        assertEquals("Middle Salary", state.transactions[1].description)
        assertEquals("HDFC Bank", state.transactions[1].accountName)

        assertEquals("tx_older", state.transactions[2].id)
        assertEquals("Older Dinner", state.transactions[2].description)
        assertEquals("HDFC Bank", state.transactions[2].accountName)
    }

    @Test
    fun transactionList_emptyStateWhenNoTransactions() = runBlocking {
        val viewModel = TransactionListViewModel(fakeTxRepo, fakeAccountRepo)
        val state = viewModel.uiState.first { !it.isLoading }

        assertFalse(state.isLoading)
        assertTrue(state.transactions.isEmpty())
    }

    @Test
    fun transactionList_amountFormattingDisplaysProperSigns() = runBlocking {
        fakeTxRepo.addTransaction(
            Transaction("tx_exp", "acc_bank", -150000L, "Groceries", 1000L, "Food & Dining")
        )
        fakeTxRepo.addTransaction(
            Transaction("tx_inc", "acc_bank", 2500000L, "Salary", 2000L, "Salary & Income")
        )

        val viewModel = TransactionListViewModel(fakeTxRepo, fakeAccountRepo)
        val state = viewModel.uiState.first { it.transactions.size == 2 }

        val incomeItem = state.transactions.first { it.id == "tx_inc" }
        val expenseItem = state.transactions.first { it.id == "tx_exp" }

        assertEquals("₹25,000.00", incomeItem.amountFormatted)
        assertTrue(incomeItem.isCredit)

        assertEquals("-₹1,500.00", expenseItem.amountFormatted)
        assertFalse(expenseItem.isCredit)
    }

    @Test
    fun transactionDetail_displaysFullMetadata() = runBlocking {
        val tx = Transaction(
            id = "tx_detail_test",
            accountId = "acc_bank",
            amount = -350000L, // -₹3,500.00
            description = "Flight Ticket",
            timestamp = 1700000000000L,
            category = "Transport",
            tags = "[\"Travel\"]",
            transferId = null,
            notes = "Business trip"
        )
        fakeTxRepo.addTransaction(tx)

        val savedStateHandle = SavedStateHandle(mapOf(NavDestination.TransactionDetail.ARG_TRANSACTION_ID to tx.id))
        val viewModel = TransactionDetailViewModel(savedStateHandle, fakeTxRepo, fakeAccountRepo, fakeCategoryRepo)

        val state = viewModel.uiState.first { it.transaction != null }
        assertNotNull(state.transaction)
        assertEquals("Flight Ticket", state.transaction?.description)
        assertEquals("HDFC Bank", state.accountName)
        assertEquals("-₹3,500.00", state.displayItem?.amountFormatted)
    }

    @Test
    fun transactionDetail_deleteTransactionRemovesAndRestoresBalance() = runBlocking {
        // Initial bank balance: 500,000 paise (₹5,000.00)
        val initialBalance = fakeAccountRepo.getAccount("acc_bank")!!.balance

        val tx = Transaction("tx_del", "acc_bank", -50000L, "Grocery", 1000L, "Food & Dining")
        fakeTxRepo.addTransaction(tx)

        // After expense: balance reduced to 450,000 paise
        assertEquals(450000L, fakeAccountRepo.getAccount("acc_bank")!!.balance)

        val savedStateHandle = SavedStateHandle(mapOf(NavDestination.TransactionDetail.ARG_TRANSACTION_ID to tx.id))
        val viewModel = TransactionDetailViewModel(savedStateHandle, fakeTxRepo, fakeAccountRepo, fakeCategoryRepo)

        var deletedCallbackCalled = false
        viewModel.deleteTransaction { deletedCallbackCalled = true }

        assertTrue(deletedCallbackCalled)
        assertNull(fakeTxRepo.getTransactionById("tx_del"))
        // Balance restored back to initial 500,000 paise
        assertEquals(initialBalance, fakeAccountRepo.getAccount("acc_bank")!!.balance)
    }

    @Test
    fun transactionDetail_duplicateTransactionCreatesNewRecord() = runBlocking {
        val originalTx = Transaction("tx_orig", "acc_cash", -12000L, "Coffee with friend", 1000L, "Food & Dining")
        fakeTxRepo.addTransaction(originalTx)

        val savedStateHandle = SavedStateHandle(mapOf(NavDestination.TransactionDetail.ARG_TRANSACTION_ID to originalTx.id))
        val viewModel = TransactionDetailViewModel(savedStateHandle, fakeTxRepo, fakeAccountRepo, fakeCategoryRepo)

        var newTxId: String? = null
        viewModel.duplicateTransaction { duplicatedId ->
            newTxId = duplicatedId
        }

        assertNotNull(newTxId)
        assertTrue(newTxId != originalTx.id)

        val duplicated = fakeTxRepo.getTransactionById(newTxId!!)
        assertNotNull(duplicated)
        assertEquals(originalTx.amount, duplicated?.amount)
        assertEquals(originalTx.description, duplicated?.description)
        assertEquals(originalTx.category, duplicated?.category)
        assertEquals(originalTx.accountId, duplicated?.accountId)
    }

    @Test
    fun transactionDetail_updateTransactionModifiesDetailsAndBalance() = runBlocking {
        // Add expense of ₹100 (-10,000 paise) to acc_bank (initial 500,000 -> 490,000)
        val tx = Transaction("tx_edit", "acc_bank", -10000L, "Snack", 1000L, "Food & Dining")
        fakeTxRepo.addTransaction(tx)
        assertEquals(490000L, fakeAccountRepo.getAccount("acc_bank")!!.balance)

        val savedStateHandle = SavedStateHandle(mapOf(NavDestination.TransactionDetail.ARG_TRANSACTION_ID to tx.id))
        val viewModel = TransactionDetailViewModel(savedStateHandle, fakeTxRepo, fakeAccountRepo, fakeCategoryRepo)

        var updateCallbackCalled = false
        // Update to expense of ₹150 (-15,000 paise)
        viewModel.updateTransaction(
            amountPaise = 15000L,
            isExpense = true,
            description = "Big Snack",
            category = "Food & Dining",
            accountId = "acc_bank",
            onSuccess = { updateCallbackCalled = true }
        )

        assertTrue(updateCallbackCalled)
        val updated = fakeTxRepo.getTransactionById("tx_edit")
        assertEquals(-15000L, updated?.amount)
        assertEquals("Big Snack", updated?.description)
        // Bank balance should now be 500,000 - 15,000 = 485,000
        assertEquals(485000L, fakeAccountRepo.getAccount("acc_bank")!!.balance)
    }

    @Test
    fun transactionDetail_transferRecordShowsIsTransferFlagAndDeletesAtomically() = runBlocking {
        val transferId = "transfer_test_123"
        val transferTx = Transaction(
            id = "tx_transfer_debit",
            accountId = "acc_bank",
            amount = -200000L,
            description = "Transfer to Cash",
            timestamp = 1000L,
            category = "Transfer",
            transferId = transferId
        )
        fakeTxRepo.addTransaction(transferTx)

        var deletedTransferId: String? = null
        val fakeTransferRepo = object : TransferRepository {
            override suspend fun createTransfer(fromAccountId: String, toAccountId: String, amountPaise: Long, timestamp: Long, notes: String?): String = ""
            override suspend fun deleteTransfer(transferId: String) {
                deletedTransferId = transferId
                fakeTxRepo.deleteTransaction("tx_transfer_debit")
            }
            override suspend fun getTransferTransactions(transferId: String): List<Transaction> = emptyList()
        }

        val savedStateHandle = SavedStateHandle(mapOf(NavDestination.TransactionDetail.ARG_TRANSACTION_ID to transferTx.id))
        val viewModel = TransactionDetailViewModel(
            savedStateHandle = savedStateHandle,
            transactionRepository = fakeTxRepo,
            accountRepository = fakeAccountRepo,
            categoryRepository = fakeCategoryRepo,
            transferRepository = fakeTransferRepo
        )

        val state = viewModel.uiState.first { it.transaction != null }
        assertNotNull(state.transaction)
        assertTrue(state.displayItem?.isTransfer == true)

        var deleteCallbackCalled = false
        viewModel.deleteTransaction { deleteCallbackCalled = true }

        assertTrue(deleteCallbackCalled)
        assertEquals(transferId, deletedTransferId)
        assertNull(fakeTxRepo.getTransactionById("tx_transfer_debit"))
    }

    @Test
    fun transactionList_displaysTransferBadge() = runBlocking {
        val transferTx = Transaction(
            id = "tx_transfer_1",
            accountId = "acc_bank",
            amount = -50000L,
            description = "Transfer Out",
            timestamp = 2000L,
            category = "Transfer",
            transferId = "transfer_abc"
        )
        val normalTx = Transaction(
            id = "tx_normal_1",
            accountId = "acc_bank",
            amount = -25000L,
            description = "Dinner",
            timestamp = 1000L,
            category = "Food & Dining",
            transferId = null
        )
        fakeTxRepo.addTransaction(transferTx)
        fakeTxRepo.addTransaction(normalTx)

        val viewModel = TransactionListViewModel(fakeTxRepo, fakeAccountRepo)
        val state = viewModel.uiState.first { it.transactions.size >= 2 }

        val transferItem = state.transactions.find { it.id == "tx_transfer_1" }
        val normalItem = state.transactions.find { it.id == "tx_normal_1" }

        assertNotNull(transferItem)
        assertNotNull(normalItem)
        assertTrue(transferItem!!.isTransfer)
        assertFalse(normalItem!!.isTransfer)
    }
}
