package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.ui.screens.BatchAdd.BatchAddViewModel
import com.rushi.mitavyay.ui.screens.BatchAdd.BatchTransactionRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class BatchAddTransactionsTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var fakeAccountRepository: FakeAccountRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var viewModel: BatchAddViewModel

    private class FakeTransactionRepository(
        private val accountRepository: FakeAccountRepository
    ) : TransactionRepository {
        val transactions = mutableListOf<Transaction>()
        private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
        var addTransactionsCallCount = 0

        override fun getAllTransactions(): Flow<List<Transaction>> = transactionsFlow

        override suspend fun getTransactionById(id: String): Transaction? =
            transactions.find { it.id == id }

        override suspend fun addTransaction(transaction: Transaction) {
            transactions.add(transaction)
            transactionsFlow.value = transactions.toList()
            val account = accountRepository.accountsMap[transaction.accountId]
            if (account != null) {
                accountRepository.updateBalance(account.id, account.balance + transaction.amount)
            }
        }

        override suspend fun addTransactions(transactions: List<Transaction>) {
            addTransactionsCallCount++
            this.transactions.addAll(transactions)
            transactionsFlow.value = this.transactions.toList()

            // Group net deltas per account
            val deltas = transactions.groupBy { it.accountId }
                .mapValues { (_, txs) -> txs.sumOf { it.amount } }

            for ((accountId, delta) in deltas) {
                val acc = accountRepository.accountsMap[accountId]
                if (acc != null) {
                    accountRepository.updateBalance(accountId, acc.balance + delta)
                }
            }
        }

        override suspend fun updateTransaction(transaction: Transaction) {}
        override suspend fun deleteTransaction(id: String) {}
        override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(0L)
        override fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(0L)
    }

    private class FakeAccountRepository : AccountRepository {
        val accountsMap = mutableMapOf<String, Account>()
        private val accountsFlow = MutableStateFlow<List<Account>>(emptyList())

        private fun updateFlow() {
            accountsFlow.value = accountsMap.values.toList()
        }

        override fun getAllAccounts(): Flow<List<Account>> = accountsFlow
        override fun getActiveAccounts(): Flow<List<Account>> = flowOf(accountsMap.values.filter { it.isActive })
        override fun getAccountById(id: String): Flow<Account?> = flowOf(accountsMap[id])
        override suspend fun getAccount(id: String): Account? = accountsMap[id]
        override suspend fun addAccount(account: Account) {
            accountsMap[account.id] = account
            updateFlow()
        }
        override suspend fun createAccount(name: String, type: String, initialBalancePaise: Long): Account =
            throw UnsupportedOperationException()
        override suspend fun editAccount(id: String, name: String, type: String) {}
        override suspend fun updateAccount(account: Account) {
            accountsMap[account.id] = account
            updateFlow()
        }
        override suspend fun deleteAccount(id: String): Boolean = true
        override suspend fun canDeleteAccount(id: String): Boolean = true
        override suspend fun archiveAccount(id: String) {}
        override suspend fun unarchiveAccount(id: String) {}
        override suspend fun updateBalance(id: String, newBalance: Long) {
            val acc = accountsMap[id] ?: return
            accountsMap[id] = acc.copy(balance = newBalance)
            updateFlow()
        }
        override fun getAccountBalance(id: String): Flow<Long?> = flowOf(accountsMap[id]?.balance)
    }

    private class FakeCategoryRepository : CategoryRepository {
        val categoriesMap = mutableMapOf<String, Category>()
        private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

        private fun updateFlow() {
            categoriesFlow.value = categoriesMap.values.toList()
        }

        override fun getAllCategories(): Flow<List<Category>> = flowOf(categoriesMap.values.toList())
        override fun getCustomCategories(): Flow<List<Category>> = flowOf(categoriesMap.values.filter { it.isCustom })
        override suspend fun getCategoryById(id: String): Category? = categoriesMap[id]
        override suspend fun getCategoryByName(name: String): Category? =
            categoriesMap.values.find { it.name.equals(name, ignoreCase = true) }
        override suspend fun addCategory(category: Category) {
            categoriesMap[category.id] = category
            updateFlow()
        }
        override suspend fun updateCategory(category: Category) {
            categoriesMap[category.id] = category
            updateFlow()
        }
        override suspend fun deleteCategory(id: String) {
            categoriesMap.remove(id)
            updateFlow()
        }
        override suspend fun seedDefaultCategories() {}
        override suspend fun createCustomCategory(name: String, colorHex: String, icon: String): Result<Category> =
            throw UnsupportedOperationException()
        override suspend fun updateCustomCategory(id: String, name: String, colorHex: String, icon: String): Result<Unit> =
            throw UnsupportedOperationException()
        override suspend fun canDeleteCategory(id: String): Boolean = true
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAccountRepository = FakeAccountRepository()
        fakeTransactionRepository = FakeTransactionRepository(fakeAccountRepository)
        fakeCategoryRepository = FakeCategoryRepository()

        // Seed accounts
        fakeAccountRepository.accountsMap["acc_bank"] = Account(
            id = "acc_bank",
            name = "Bank Account",
            type = "bank",
            balance = 100000L, // ₹1,000.00
            currency = "INR",
            createdAt = 1000L,
            isActive = true
        )
        fakeAccountRepository.accountsMap["acc_cash"] = Account(
            id = "acc_cash",
            name = "Cash Wallet",
            type = "cash",
            balance = 50000L, // ₹500.00
            currency = "INR",
            createdAt = 1000L,
            isActive = true
        )

        // Seed categories
        fakeCategoryRepository.categoriesMap["cat_food"] = Category(
            id = "cat_food",
            name = "Food",
            icon = "restaurant",
            color = "#FF5722",
            isCustom = false
        )
        fakeCategoryRepository.categoriesMap["cat_transport"] = Category(
            id = "cat_transport",
            name = "Transport",
            icon = "directions_bus",
            color = "#2196F3",
            isCustom = false
        )
        fakeCategoryRepository.categoriesMap["cat_income"] = Category(
            id = "cat_income",
            name = "Income",
            icon = "payments",
            color = "#4CAF50",
            isCustom = false
        )

        viewModel = BatchAddViewModel(
            fakeTransactionRepository,
            fakeAccountRepository,
            fakeCategoryRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_hasRowsAndLoadsAccountsAndCategories() {
        val state = viewModel.uiState.value
        assertEquals(2, state.rows.size)
        assertEquals(2, state.accounts.size)
        assertEquals(3, state.categories.size)
        assertFalse(state.canSubmit)
        assertFalse(state.isSavedSuccessfully)
        assertEquals(0L, state.totalExpensePaise)
        assertEquals(0L, state.totalIncomePaise)
    }

    @Test
    fun testAddRow_increasesRowCount() {
        assertEquals(2, viewModel.uiState.value.rows.size)
        viewModel.addRow()
        assertEquals(3, viewModel.uiState.value.rows.size)
        viewModel.addRow()
        assertEquals(4, viewModel.uiState.value.rows.size)
    }

    @Test
    fun testRemoveRow_decreasesRowCount_cannotRemoveLastRow() {
        assertEquals(2, viewModel.uiState.value.rows.size)

        viewModel.removeRow(1)
        assertEquals(1, viewModel.uiState.value.rows.size)

        // Attempt removing the last row: should remain at 1
        viewModel.removeRow(0)
        assertEquals(1, viewModel.uiState.value.rows.size)
    }

    @Test
    fun testRowValidation_requiresAmountAccountCategory() {
        // Initial row has amountPaise = 0L -> invalid
        val initialRow = viewModel.uiState.value.rows[0]
        assertFalse(initialRow.isValid)
        assertFalse(viewModel.uiState.value.canSubmit)

        // Only amount without account/category -> invalid
        viewModel.updateRow(0, initialRow.copy(amountPaise = 5000L, accountId = "", category = ""))
        assertFalse(viewModel.uiState.value.rows[0].isValid)
        assertFalse(viewModel.uiState.value.canSubmit)

        // Amount + account -> invalid
        viewModel.updateRow(0, viewModel.uiState.value.rows[0].copy(accountId = "acc_cash"))
        assertFalse(viewModel.uiState.value.rows[0].isValid)

        // Amount + account + category -> valid
        viewModel.updateRow(0, viewModel.uiState.value.rows[0].copy(category = "Food"))
        assertTrue(viewModel.uiState.value.rows[0].isValid)

        // When second row is also valid, canSubmit is true
        viewModel.updateRow(1, BatchTransactionRow(
            amountPaise = 2000L,
            isExpense = true,
            description = "Tea",
            category = "Food",
            accountId = "acc_cash"
        ))
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun testSummaryTotals_calculatesExpenseAndIncomePaiseCorrectly() {
        // Row 0: Expense 1500 paise (₹15.00)
        viewModel.updateRow(0, BatchTransactionRow(
            amountPaise = 1500L,
            isExpense = true,
            description = "Snack",
            category = "Food",
            accountId = "acc_cash"
        ))

        // Row 1: Income 4500 paise (₹45.00)
        viewModel.updateRow(1, BatchTransactionRow(
            amountPaise = 4500L,
            isExpense = false,
            description = "Refund",
            category = "Income",
            accountId = "acc_bank"
        ))

        // Row 2: Expense 2000 paise (₹20.00)
        viewModel.addRow()
        viewModel.updateRow(2, BatchTransactionRow(
            amountPaise = 2000L,
            isExpense = true,
            description = "Bus",
            category = "Transport",
            accountId = "acc_cash"
        ))

        val state = viewModel.uiState.value
        assertEquals(3500L, state.totalExpensePaise) // 1500 + 2000
        assertEquals(4500L, state.totalIncomePaise) // 4500
        assertTrue(state.canSubmit)
    }

    // Feature requirement: "Test: Add 5 transactions at once"
    @Test
    fun testAdd5TransactionsAtOnce() = runBlocking {
        // Prepare 5 rows
        // 1. Expense: ₹100.00 (10000 paise) "Groceries", Food, acc_bank, note: "Weekly market"
        viewModel.updateRow(0, BatchTransactionRow(
            amountPaise = 10000L,
            isExpense = true,
            description = "Groceries",
            category = "Food",
            accountId = "acc_bank",
            notes = "Weekly market"
        ))

        // 2. Expense: ₹50.00 (5000 paise) "Fuel", Transport, acc_bank, note: null
        viewModel.updateRow(1, BatchTransactionRow(
            amountPaise = 5000L,
            isExpense = true,
            description = "Fuel",
            category = "Transport",
            accountId = "acc_bank",
            notes = ""
        ))

        // 3. Income: ₹500.00 (50000 paise) "Freelance bonus", Income, acc_bank, note: "Project Alpha"
        viewModel.addRow()
        viewModel.updateRow(2, BatchTransactionRow(
            amountPaise = 50000L,
            isExpense = false,
            description = "Freelance bonus",
            category = "Income",
            accountId = "acc_bank",
            notes = "Project Alpha"
        ))

        // 4. Expense: ₹20.00 (2000 paise) "Coffee", Food, acc_cash, note: "Starbucks"
        viewModel.addRow()
        viewModel.updateRow(3, BatchTransactionRow(
            amountPaise = 2000L,
            isExpense = true,
            description = "Coffee",
            category = "Food",
            accountId = "acc_cash",
            notes = "Starbucks"
        ))

        // 5. Expense: ₹10.00 (1000 paise) "Bus fare", Transport, acc_cash, note: null
        viewModel.addRow()
        viewModel.updateRow(4, BatchTransactionRow(
            amountPaise = 1000L,
            isExpense = true,
            description = "Bus fare",
            category = "Transport",
            accountId = "acc_cash",
            notes = ""
        ))

        val stateBeforeSubmit = viewModel.uiState.value
        assertEquals(5, stateBeforeSubmit.rows.size)
        assertTrue(stateBeforeSubmit.canSubmit)
        assertEquals(18000L, stateBeforeSubmit.totalExpensePaise) // 10000 + 5000 + 2000 + 1000
        assertEquals(50000L, stateBeforeSubmit.totalIncomePaise) // 50000

        viewModel.submitBatch()

        val stateAfterSubmit = viewModel.uiState.value
        assertTrue(stateAfterSubmit.isSavedSuccessfully)
        assertFalse(stateAfterSubmit.isSaving)
        assertNull(stateAfterSubmit.errorMessage)
    }

    // Feature requirement: "Test: All saved"
    @Test
    fun testAllSaved() = runBlocking {
        // Verify repository contains all 5 transactions committed via batch add
        viewModel.updateRow(0, BatchTransactionRow(
            amountPaise = 10000L,
            isExpense = true,
            description = "Groceries",
            category = "Food",
            accountId = "acc_bank",
            notes = "Weekly market"
        ))
        viewModel.updateRow(1, BatchTransactionRow(
            amountPaise = 5000L,
            isExpense = true,
            description = "Fuel",
            category = "Transport",
            accountId = "acc_bank"
        ))
        viewModel.addRow()
        viewModel.updateRow(2, BatchTransactionRow(
            amountPaise = 50000L,
            isExpense = false,
            description = "Freelance bonus",
            category = "Income",
            accountId = "acc_bank",
            notes = "Project Alpha"
        ))
        viewModel.addRow()
        viewModel.updateRow(3, BatchTransactionRow(
            amountPaise = 2000L,
            isExpense = true,
            description = "Coffee",
            category = "Food",
            accountId = "acc_cash",
            notes = "Starbucks"
        ))
        viewModel.addRow()
        viewModel.updateRow(4, BatchTransactionRow(
            amountPaise = 1000L,
            isExpense = true,
            description = "Bus fare",
            category = "Transport",
            accountId = "acc_cash"
        ))

        viewModel.submitBatch()

        // 1. Verify batch add method was invoked once
        assertEquals(1, fakeTransactionRepository.addTransactionsCallCount)

        // 2. Verify all 5 transactions are saved in repository
        val savedList = fakeTransactionRepository.transactions
        assertEquals(5, savedList.size)

        // 3. Verify sign convention: Expense is negative paise, Income is positive paise
        assertEquals(-10000L, savedList[0].amount)
        assertEquals("Groceries", savedList[0].description)
        assertEquals("Food", savedList[0].category)
        assertEquals("acc_bank", savedList[0].accountId)
        assertEquals("Weekly market", savedList[0].notes)

        assertEquals(-5000L, savedList[1].amount)
        assertEquals("Fuel", savedList[1].description)
        assertNull(savedList[1].notes)

        assertEquals(50000L, savedList[2].amount)
        assertEquals("Freelance bonus", savedList[2].description)
        assertEquals("Project Alpha", savedList[2].notes)

        assertEquals(-2000L, savedList[3].amount)
        assertEquals("Coffee", savedList[3].description)
        assertEquals("acc_cash", savedList[3].accountId)
        assertEquals("Starbucks", savedList[3].notes)

        assertEquals(-1000L, savedList[4].amount)
        assertEquals("Bus fare", savedList[4].description)
        assertEquals("acc_cash", savedList[4].accountId)

        // 4. Verify account balances are atomically updated:
        // acc_bank starting balance: 100000L
        // Net bank delta: -10000 - 5000 + 50000 = +35000
        // Expected acc_bank balance: 100000 + 35000 = 135000L
        val bankAccount = fakeAccountRepository.getAccount("acc_bank")
        assertEquals(135000L, bankAccount?.balance)

        // acc_cash starting balance: 50000L
        // Net cash delta: -2000 - 1000 = -3000
        // Expected acc_cash balance: 50000 - 3000 = 47000L
        val cashAccount = fakeAccountRepository.getAccount("acc_cash")
        assertEquals(47000L, cashAccount?.balance)
    }

    @Test
    fun testSubmitBatch_failsIfAnyRowIsInvalid() = runBlocking {
        // Valid row 0
        viewModel.updateRow(0, BatchTransactionRow(
            amountPaise = 5000L,
            isExpense = true,
            description = "Groceries",
            category = "Food",
            accountId = "acc_bank"
        ))
        // Incomplete row 1 (amount = 0L)
        viewModel.updateRow(1, BatchTransactionRow(
            amountPaise = 0L,
            isExpense = true,
            description = "",
            category = "Food",
            accountId = "acc_bank"
        ))

        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.submitBatch()

        assertFalse(viewModel.uiState.value.isSavedSuccessfully)
        assertEquals(0, fakeTransactionRepository.transactions.size)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testReset_clearsRowsBackToInitial() {
        viewModel.addRow()
        viewModel.addRow()
        assertEquals(4, viewModel.uiState.value.rows.size)

        viewModel.reset()

        val state = viewModel.uiState.value
        assertEquals(2, state.rows.size)
        assertFalse(state.isSavedSuccessfully)
        assertNull(state.errorMessage)
    }

    @Test
    fun testSetDefaultAccount_appliesToEmptyRowsAndFutureRows() {
        viewModel.setDefaultAccount("acc_cash")
        assertEquals("acc_cash", viewModel.uiState.value.defaultAccountId)

        // Adding a new row inherits default account
        viewModel.addRow()
        val lastIndex = viewModel.uiState.value.rows.lastIndex
        assertEquals("acc_cash", viewModel.uiState.value.rows[lastIndex].accountId)
    }
}
