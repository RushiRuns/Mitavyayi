package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.data.repository.TransferRepository
import com.rushi.mitavyay.ui.screens.QuickAddExpense.QuickAddExpenseViewModel
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
class QuickAddExpenseTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var fakeAccountRepository: FakeAccountRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakeTransferRepository: FakeTransferRepository
    private lateinit var viewModel: QuickAddExpenseViewModel

    private class FakeTransactionRepository(
        private val accountRepository: FakeAccountRepository
    ) : TransactionRepository {
        val transactions = mutableListOf<Transaction>()
        private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())

        override fun getAllTransactions(): Flow<List<Transaction>> = transactionsFlow

        override suspend fun getTransactionById(id: String): Transaction? =
            transactions.find { it.id == id }

        override suspend fun addTransaction(transaction: Transaction) {
            transactions.add(transaction)
            transactionsFlow.value = transactions.toList()
            // Atomically update account balance
            val account = accountRepository.accountsMap[transaction.accountId]
            if (account != null) {
                accountRepository.updateBalance(account.id, account.balance + transaction.amount)
            }
        }

        override suspend fun updateTransaction(transaction: Transaction) {
            val idx = transactions.indexOfFirst { it.id == transaction.id }
            if (idx >= 0) {
                val oldTx = transactions[idx]
                transactions[idx] = transaction
                transactionsFlow.value = transactions.toList()
                val account = accountRepository.accountsMap[transaction.accountId]
                if (account != null) {
                    val delta = transaction.amount - oldTx.amount
                    accountRepository.updateBalance(account.id, account.balance + delta)
                }
            }
        }

        override suspend fun deleteTransaction(id: String) {
            val tx = transactions.find { it.id == id }
            if (tx != null) {
                transactions.remove(tx)
                transactionsFlow.value = transactions.toList()
                val account = accountRepository.accountsMap[tx.accountId]
                if (account != null) {
                    accountRepository.updateBalance(account.id, account.balance - tx.amount)
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
                name = name,
                type = type,
                balance = initialBalancePaise,
                currency = "INR",
                createdAt = System.currentTimeMillis(),
                isActive = true
            )
            accountsMap[account.id] = account
            updateFlow()
            return account
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

    private class FakeCategoryRepository : CategoryRepository {
        val categoriesMap = mutableMapOf<String, Category>()
        private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

        private fun updateFlow() {
            categoriesFlow.value = categoriesMap.values.toList()
        }

        override fun getAllCategories(): Flow<List<Category>> = categoriesFlow

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

        override suspend fun seedDefaultCategories() {
            val defaults = listOf(
                Category(id = "cat_food", name = "Food & Dining", icon = "restaurant", color = "#FF7043", isCustom = false),
                Category(id = "cat_groceries", name = "Groceries", icon = "shopping_cart", color = "#42A5F5", isCustom = false),
                Category(id = "cat_salary", name = "Salary & Income", icon = "payments", color = "#66BB6A", isCustom = false)
            )
            defaults.forEach { categoriesMap[it.id] = it }
            updateFlow()
        }

        override suspend fun createCustomCategory(
            name: String,
            colorHex: String,
            icon: String
        ): Result<Category> {
            val trimmed = name.trim()
            if (trimmed.isBlank()) {
                return Result.failure(IllegalArgumentException("Blank name"))
            }
            if (categoriesMap.values.any { it.name.equals(trimmed, ignoreCase = true) }) {
                return Result.failure(IllegalArgumentException("Duplicate category"))
            }
            val cat = Category(
                id = UUID.randomUUID().toString(),
                name = trimmed,
                color = colorHex,
                icon = icon,
                isCustom = true
            )
            categoriesMap[cat.id] = cat
            updateFlow()
            return Result.success(cat)
        }

        override suspend fun updateCustomCategory(
            id: String,
            name: String,
            colorHex: String,
            icon: String
        ): Result<Unit> {
            val existing = categoriesMap[id] ?: return Result.failure(IllegalArgumentException("Not found"))
            if (!existing.isCustom) return Result.failure(IllegalStateException("Cannot edit default"))
            val trimmed = name.trim()
            if (categoriesMap.values.any { it.name.equals(trimmed, ignoreCase = true) && it.id != id }) {
                return Result.failure(IllegalArgumentException("Duplicate category"))
            }
            categoriesMap[id] = existing.copy(name = trimmed, color = colorHex, icon = icon)
            updateFlow()
            return Result.success(Unit)
        }

        override suspend fun canDeleteCategory(id: String): Boolean =
            categoriesMap[id]?.isCustom == true
    }

    private class FakeTransferRepository(
        private val accountRepository: FakeAccountRepository,
        private val transactionRepository: FakeTransactionRepository
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
            if (fromAccountId == toAccountId) throw IllegalArgumentException("Cannot transfer to same account")
            if (amountPaise <= 0L) throw IllegalArgumentException("Amount must be greater than zero")

            val transferId = UUID.randomUUID().toString()
            transferCalls.add(TransferCall(fromAccountId, toAccountId, amountPaise, notes))

            val fromAcc = accountRepository.accountsMap[fromAccountId]
            val toAcc = accountRepository.accountsMap[toAccountId]

            val debitTx = Transaction(
                id = UUID.randomUUID().toString(),
                accountId = fromAccountId,
                amount = -amountPaise,
                description = notes ?: "Transfer to ${toAcc?.name ?: "Account"}",
                timestamp = timestamp,
                category = "Transfer",
                transferId = transferId,
                notes = notes
            )
            val creditTx = Transaction(
                id = UUID.randomUUID().toString(),
                accountId = toAccountId,
                amount = amountPaise,
                description = notes ?: "Transfer from ${fromAcc?.name ?: "Account"}",
                timestamp = timestamp,
                category = "Transfer",
                transferId = transferId,
                notes = notes
            )

            transactionRepository.addTransaction(debitTx)
            transactionRepository.addTransaction(creditTx)

            return transferId
        }

        override suspend fun deleteTransfer(transferId: String) {}
        override suspend fun getTransferTransactions(transferId: String): List<Transaction> =
            transactionRepository.transactions.filter { it.transferId == transferId }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAccountRepository = FakeAccountRepository()
        fakeTransactionRepository = FakeTransactionRepository(fakeAccountRepository)
        fakeCategoryRepository = FakeCategoryRepository()
        fakeTransferRepository = FakeTransferRepository(fakeAccountRepository, fakeTransactionRepository)

        runBlocking {
            fakeAccountRepository.addAccount(
                Account(
                    id = "acc_bank",
                    name = "Bank Account",
                    type = "bank",
                    balance = 100000L, // ₹1,000.00
                    currency = "INR",
                    createdAt = 1000L,
                    isActive = true
                )
            )
            fakeAccountRepository.addAccount(
                Account(
                    id = "acc_cash",
                    name = "Cash Wallet",
                    type = "cash",
                    balance = 50000L, // ₹500.00
                    currency = "INR",
                    createdAt = 1001L,
                    isActive = true
                )
            )
            fakeCategoryRepository.seedDefaultCategories()
        }

        viewModel = QuickAddExpenseViewModel(
            transactionRepository = fakeTransactionRepository,
            accountRepository = fakeAccountRepository,
            categoryRepository = fakeCategoryRepository,
            transferRepository = fakeTransferRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun autoSelection_selectsFirstActiveAccountAndCategoryByDefault() = runBlocking {
        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals("acc_bank", state.selectedAccountId)
        assertEquals("Food & Dining", state.selectedCategory)
        assertTrue(state.isExpense)
        assertEquals(0L, state.amountPaise)
    }

    @Test
    fun expense_savedWithNegativeAmountAndUpdatesBalance() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        viewModel.onAmountChange(25000L) // ₹250.00
        viewModel.onDescriptionChange("Dinner at restaurant")
        viewModel.onCategorySelect("Food & Dining")
        viewModel.onAccountSelect("acc_bank")

        var callbackTriggered = false
        viewModel.saveTransaction { callbackTriggered = true }

        assertTrue(callbackTriggered)
        assertEquals(1, fakeTransactionRepository.transactions.size)
        val tx = fakeTransactionRepository.transactions.first()
        assertEquals(-25000L, tx.amount) // Must be negative for expense per ADR-001
        assertEquals("Dinner at restaurant", tx.description)
        assertEquals("Food & Dining", tx.category)
        assertEquals("acc_bank", tx.accountId)

        // Verify account balance updated atomically
        val updatedAccount = fakeAccountRepository.getAccount("acc_bank")
        assertNotNull(updatedAccount)
        assertEquals(75000L, updatedAccount?.balance) // 100000 - 25000 = 75000
    }

    @Test
    fun income_savedWithPositiveAmountAndUpdatesBalance() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        viewModel.onTypeToggle(isExpense = false)
        viewModel.onAmountChange(500000L) // ₹5,000.00
        viewModel.onDescriptionChange("Freelance payment")
        viewModel.onCategorySelect("Salary & Income")
        viewModel.onAccountSelect("acc_cash")

        var callbackTriggered = false
        viewModel.saveTransaction { callbackTriggered = true }

        assertTrue(callbackTriggered)
        assertEquals(1, fakeTransactionRepository.transactions.size)
        val tx = fakeTransactionRepository.transactions.first()
        assertEquals(500000L, tx.amount) // Must be positive for income
        assertEquals("Freelance payment", tx.description)
        assertEquals("Salary & Income", tx.category)
        assertEquals("acc_cash", tx.accountId)

        // Verify account balance updated atomically
        val updatedAccount = fakeAccountRepository.getAccount("acc_cash")
        assertNotNull(updatedAccount)
        assertEquals(550000L, updatedAccount?.balance) // 50000 + 500000 = 550000
    }

    @Test
    fun validation_blocksZeroAmount() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        viewModel.onAmountChange(0L)
        var callbackTriggered = false
        viewModel.saveTransaction { callbackTriggered = true }

        assertFalse(callbackTriggered)
        assertEquals(0, fakeTransactionRepository.transactions.size)
        val state = viewModel.uiState.first { it.errorMessage != null }
        assertEquals("Amount must be greater than zero", state.errorMessage)
    }

    @Test
    fun blankDescription_defaultsToCategoryName() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        viewModel.onAmountChange(1500L) // ₹15.00
        viewModel.onCategorySelect("Groceries")
        viewModel.onDescriptionChange("   ") // Blank description

        var callbackTriggered = false
        viewModel.saveTransaction { callbackTriggered = true }

        assertTrue(callbackTriggered)
        val tx = fakeTransactionRepository.transactions.first()
        assertEquals("Groceries", tx.description)
    }

    @Test
    fun formReset_clearsAmountAndDescription() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        viewModel.onAmountChange(5000L)
        viewModel.onDescriptionChange("Test expense")
        viewModel.resetForm()

        val state = viewModel.uiState.first { it.amountPaise == 0L && it.description.isEmpty() }
        assertEquals(0L, state.amountPaise)
        assertEquals("", state.description)
        assertNull(state.errorMessage)
    }

    @Test
    fun createAndSelectCategory_addsAndImmediatelySelectsCategory() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        var resultSuccess = false

        viewModel.createAndSelectCategory("Subscriptions", "#EC407A", "star") { res ->
            resultSuccess = res.isSuccess
        }

        assertTrue(resultSuccess)
        val state = viewModel.uiState.first { it.selectedCategory == "Subscriptions" }
        assertEquals("Subscriptions", state.selectedCategory)
        assertTrue(state.categories.any { it.name == "Subscriptions" })
    }

    @Test
    fun dateSelection_updatesSelectedDate() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        val customDate = 1700000000000L
        viewModel.onDateSelected(customDate)

        val state = viewModel.uiState.first { it.selectedDate == customDate }
        assertEquals(customDate, state.selectedDate)
    }

    @Test
    fun expense_savedWithCustomSelectedDate() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        val customDate = 1700000000000L // specific past date
        viewModel.onAmountChange(12000L)
        viewModel.onDescriptionChange("Past lunch")
        viewModel.onCategorySelect("Food & Dining")
        viewModel.onAccountSelect("acc_bank")
        viewModel.onDateSelected(customDate)

        var callbackTriggered = false
        viewModel.saveTransaction { callbackTriggered = true }

        assertTrue(callbackTriggered)
        val tx = fakeTransactionRepository.transactions.first()
        assertEquals(customDate, tx.timestamp)
        assertEquals(-12000L, tx.amount)
    }

    @Test
    fun formReset_resetsSelectedDate() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        val pastDate = 1700000000000L
        viewModel.onDateSelected(pastDate)
        viewModel.resetForm()

        val state = viewModel.uiState.first { !it.isLoading }
        assertTrue(state.selectedDate > pastDate)
        assertTrue(kotlin.math.abs(System.currentTimeMillis() - state.selectedDate) < 5000L)
    }

    @Test
    fun income_defaultsToIncomeCategoryWithoutManualSelection() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        viewModel.onTypeToggle(isExpense = false)
        viewModel.onAmountChange(300000L) // ₹3,000.00
        viewModel.onAccountSelect("acc_bank")

        val state = viewModel.uiState.first { !it.isExpense }
        assertEquals("Salary & Income", state.selectedCategory)

        var callbackTriggered = false
        viewModel.saveTransaction { callbackTriggered = true }

        assertTrue(callbackTriggered)
        val tx = fakeTransactionRepository.transactions.first()
        assertEquals(300000L, tx.amount)
        assertEquals("Salary & Income", tx.category)
        assertEquals("Salary & Income", tx.description)
    }

    @Test
    fun switchingBetweenExpenseAndIncome_updatesDefaultCategory() = runBlocking {
        val initialState = viewModel.uiState.first { !it.isLoading }
        assertEquals("Food & Dining", initialState.selectedCategory)

        viewModel.onTypeToggle(isExpense = false)
        val incomeState = viewModel.uiState.first { !it.isExpense }
        assertEquals("Salary & Income", incomeState.selectedCategory)

        viewModel.onTypeToggle(isExpense = true)
        val expenseState = viewModel.uiState.first { it.isExpense }
        assertEquals("Food & Dining", expenseState.selectedCategory)
    }

    @Test
    fun transferMode_switchesUIStateAndHidesCategory() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        viewModel.onTransferSelect()

        val state = viewModel.uiState.first { it.isTransfer }
        assertTrue(state.isTransfer)
        assertEquals("Transfer", state.selectedCategory)
        assertNotNull(state.selectedAccountId)
        assertNotNull(state.selectedToAccountId)
        assertTrue(state.selectedAccountId != state.selectedToAccountId)
    }

    @Test
    fun transfer_createsTwoLinkedTransactionsAndUpdatesBalances() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        viewModel.onTransferSelect()
        viewModel.onAmountChange(50000L) // ₹500.00
        viewModel.onAccountSelect("acc_bank")
        viewModel.onToAccountSelect("acc_cash")
        viewModel.onDescriptionChange("Transfer for savings")

        var callbackTriggered = false
        viewModel.saveTransaction { callbackTriggered = true }

        assertTrue(callbackTriggered)
        assertEquals(2, fakeTransactionRepository.transactions.size)

        val debitTx = fakeTransactionRepository.transactions.find { it.accountId == "acc_bank" }
        val creditTx = fakeTransactionRepository.transactions.find { it.accountId == "acc_cash" }

        assertNotNull(debitTx)
        assertNotNull(creditTx)
        assertEquals(-50000L, debitTx?.amount)
        assertEquals(50000L, creditTx?.amount)
        assertEquals("Transfer", debitTx?.category)
        assertEquals("Transfer", creditTx?.category)
        assertNotNull(debitTx?.transferId)
        assertEquals(debitTx?.transferId, creditTx?.transferId)

        // Verify account balances updated: bank 100000 - 50000 = 50000; cash 50000 + 50000 = 100000
        val bankAcc = fakeAccountRepository.getAccount("acc_bank")
        val cashAcc = fakeAccountRepository.getAccount("acc_cash")
        assertEquals(50000L, bankAcc?.balance)
        assertEquals(100000L, cashAcc?.balance)
    }

    @Test
    fun transfer_preventsTransferToSameAccount() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        viewModel.onTransferSelect()
        viewModel.onAmountChange(20000L)
        viewModel.onAccountSelect("acc_bank")
        viewModel.onToAccountSelect("acc_bank") // force same account

        var callbackTriggered = false
        viewModel.saveTransaction { callbackTriggered = true }

        assertFalse(callbackTriggered)
        assertEquals(0, fakeTransactionRepository.transactions.size)
        val state = viewModel.uiState.first { it.errorMessage != null }
        assertEquals("From and To accounts cannot be the same", state.errorMessage)
    }

    @Test
    fun transfer_swapAccountsSwapsFromAndTo() = runBlocking {
        viewModel.uiState.first { !it.isLoading }
        viewModel.onTransferSelect()
        viewModel.onAccountSelect("acc_bank")
        viewModel.onToAccountSelect("acc_cash")

        val beforeSwap = viewModel.uiState.first { it.selectedAccountId == "acc_bank" }
        assertEquals("acc_bank", beforeSwap.selectedAccountId)
        assertEquals("acc_cash", beforeSwap.selectedToAccountId)

        viewModel.onSwapAccounts()

        val afterSwap = viewModel.uiState.first { it.selectedAccountId == "acc_cash" }
        assertEquals("acc_cash", afterSwap.selectedAccountId)
        assertEquals("acc_bank", afterSwap.selectedToAccountId)
    }
}
