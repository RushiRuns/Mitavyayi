package com.rushi.mitavyay.ui.screens

import androidx.lifecycle.SavedStateHandle
import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.ui.navigation.NavDestination
import com.rushi.mitavyay.ui.screens.QuickAddExpense.QuickAddExpenseViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionNotesTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeTxRepo: FakeTransactionRepository
    private lateinit var fakeAccountRepo: FakeAccountRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository

    private class FakeAccountRepository : AccountRepository {
        val accountsMap = mutableMapOf(
            "acc-1" to Account(id = "acc-1", name = "Checking", type = "BANK", balance = 500000L, isActive = true)
        )
        private val accountsFlow = MutableStateFlow<List<Account>>(accountsMap.values.toList())

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
        override suspend fun createAccount(name: String, type: String, initialBalancePaise: Long): Account {
            val account = Account(id = UUID.randomUUID().toString(), name = name, type = type, balance = initialBalancePaise)
            accountsMap[account.id] = account
            updateFlow()
            return account
        }
        override suspend fun editAccount(id: String, name: String, type: String) {}
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
            accountsMap[id] = accountsMap[id]?.copy(balance = newBalance) ?: return
            updateFlow()
        }
        override fun getAccountBalance(id: String): Flow<Long?> = flowOf(accountsMap[id]?.balance)
    }

    private class FakeCategoryRepository : CategoryRepository {
        val categoriesMap = mutableMapOf(
            "cat-1" to Category(id = "cat-1", name = "Dining", icon = "restaurant", color = "#FF7043"),
            "cat-2" to Category(id = "cat-2", name = "Groceries", icon = "shopping_cart", color = "#42A5F5")
        )
        private val categoriesFlow = MutableStateFlow<List<Category>>(categoriesMap.values.toList())

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
        override suspend fun seedDefaultCategories() {}
        override suspend fun createCustomCategory(name: String, colorHex: String, icon: String): Result<Category> {
            val cat = Category(id = UUID.randomUUID().toString(), name = name, color = colorHex, icon = icon, isCustom = true)
            categoriesMap[cat.id] = cat
            updateFlow()
            return Result.success(cat)
        }
        override suspend fun updateCustomCategory(id: String, name: String, colorHex: String, icon: String): Result<Unit> = Result.success(Unit)
        override suspend fun canDeleteCategory(id: String): Boolean = true
    }

    private class FakeTransactionRepository : TransactionRepository {
        val transactions = mutableListOf<Transaction>()
        private val txFlow = MutableStateFlow<List<Transaction>>(emptyList())

        private fun notifyChange() {
            txFlow.value = transactions.toList()
        }

        override fun getAllTransactions(): Flow<List<Transaction>> = txFlow

        override suspend fun getTransactionById(id: String): Transaction? =
            transactions.find { it.id == id }

        override suspend fun addTransaction(transaction: Transaction) {
            transactions.add(transaction)
            notifyChange()
        }

        override suspend fun updateTransaction(transaction: Transaction) {
            val idx = transactions.indexOfFirst { it.id == transaction.id }
            if (idx >= 0) {
                transactions[idx] = transaction
                notifyChange()
            }
        }

        override suspend fun deleteTransaction(id: String) {
            transactions.removeAll { it.id == id }
            notifyChange()
        }

        override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> =
            flowOf(transactions.filter { it.timestamp in start..end })

        override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> =
            flowOf(transactions.filter { it.category == category })

        override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> =
            flowOf(transactions.filter { it.accountId == accountId })

        override fun searchTransactions(query: String): Flow<List<Transaction>> {
            val q = query.trim()
            return if (q.isBlank()) {
                txFlow
            } else {
                flowOf(transactions.filter {
                    it.description.contains(q, ignoreCase = true) ||
                    it.category.contains(q, ignoreCase = true) ||
                    (it.notes != null && it.notes.contains(q, ignoreCase = true))
                })
            }
        }

        override fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(0L)
        override fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(0L)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAccountRepo = FakeAccountRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        fakeTxRepo = FakeTransactionRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addTransaction_withNotes_persistsInRepositoryAndDisplayItem() = runBlocking {
        val tx = Transaction(
            id = "tx-1",
            accountId = "acc-1",
            amount = -15000L,
            description = "Dinner",
            timestamp = 1000L,
            category = "Dining",
            notes = "Dinner with Alice #split"
        )
        fakeTxRepo.addTransaction(tx)

        val retrieved = fakeTxRepo.getTransactionById("tx-1")
        assertNotNull(retrieved)
        assertEquals("Dinner with Alice #split", retrieved?.notes)

        val display = retrieved!!.toDisplayItem(accountName = "Checking")
        assertEquals("Dinner with Alice #split", display.notes)
    }

    @Test
    fun editNotes_viaViewModel_updatesNotesCorrectly() = runBlocking {
        val tx = Transaction(
            id = "tx-10",
            accountId = "acc-1",
            amount = -20000L,
            description = "Dinner",
            timestamp = 2000L,
            category = "Dining",
            notes = "Old note"
        )
        fakeTxRepo.addTransaction(tx)

        val savedStateHandle = SavedStateHandle(mapOf(NavDestination.TransactionDetail.ARG_TRANSACTION_ID to "tx-10"))
        val viewModel = TransactionDetailViewModel(
            savedStateHandle = savedStateHandle,
            transactionRepository = fakeTxRepo,
            accountRepository = fakeAccountRepo,
            categoryRepository = fakeCategoryRepo
        )

        var successCalled = false
        viewModel.updateNotes("Updated note: tax deductible #tax") {
            successCalled = true
        }

        assertTrue(successCalled)
        val updated = fakeTxRepo.getTransactionById("tx-10")
        assertEquals("Updated note: tax deductible #tax", updated?.notes)
    }

    @Test
    fun editNotes_clearNotes_storesNull() = runBlocking {
        val tx = Transaction(
            id = "tx-20",
            accountId = "acc-1",
            amount = -5000L,
            description = "Coffee",
            timestamp = 3000L,
            category = "Dining",
            notes = "Temporary note"
        )
        fakeTxRepo.addTransaction(tx)

        val savedStateHandle = SavedStateHandle(mapOf(NavDestination.TransactionDetail.ARG_TRANSACTION_ID to "tx-20"))
        val viewModel = TransactionDetailViewModel(
            savedStateHandle = savedStateHandle,
            transactionRepository = fakeTxRepo,
            accountRepository = fakeAccountRepo,
            categoryRepository = fakeCategoryRepo
        )

        viewModel.updateNotes("   ") {}

        val updated = fakeTxRepo.getTransactionById("tx-20")
        assertNull(updated?.notes)
    }

    @Test
    fun updateTransaction_withNewNotes_savesAllFields() = runBlocking {
        val tx = Transaction(
            id = "tx-30",
            accountId = "acc-1",
            amount = -10000L,
            description = "Lunch",
            timestamp = 4000L,
            category = "Dining",
            notes = null
        )
        fakeTxRepo.addTransaction(tx)

        val savedStateHandle = SavedStateHandle(mapOf(NavDestination.TransactionDetail.ARG_TRANSACTION_ID to "tx-30"))
        val viewModel = TransactionDetailViewModel(
            savedStateHandle = savedStateHandle,
            transactionRepository = fakeTxRepo,
            accountRepository = fakeAccountRepo,
            categoryRepository = fakeCategoryRepo
        )

        viewModel.updateTransaction(
            amountPaise = 12500L,
            isExpense = true,
            description = "Business Lunch",
            category = "Dining",
            accountId = "acc-1",
            notes = "Client discussion #reimbursable"
        ) {}

        val updated = fakeTxRepo.getTransactionById("tx-30")
        assertNotNull(updated)
        assertEquals(-12500L, updated?.amount)
        assertEquals("Business Lunch", updated?.description)
        assertEquals("Client discussion #reimbursable", updated?.notes)
    }

    @Test
    fun searchTransactions_byNotesKeyword_findsMatchingTransactions() = runBlocking {
        val tx1 = Transaction(
            id = "tx-search-1",
            accountId = "acc-1",
            amount = -15000L,
            description = "Dinner",
            timestamp = 1000L,
            category = "Dining",
            notes = "client dinner #reimbursable"
        )
        val tx2 = Transaction(
            id = "tx-search-2",
            accountId = "acc-1",
            amount = -45000L,
            description = "Grocery Market",
            timestamp = 2000L,
            category = "Groceries",
            notes = "organic vegetables"
        )
        fakeTxRepo.addTransaction(tx1)
        fakeTxRepo.addTransaction(tx2)

        val viewModel = TransactionListViewModel(
            transactionRepository = fakeTxRepo,
            accountRepository = fakeAccountRepo
        )

        // Initial state has 2 transactions
        assertEquals(2, viewModel.uiState.value.transactions.size)

        // Search by notes keyword "#reimbursable"
        viewModel.onSearchQueryChange("#reimbursable")
        val filtered = viewModel.uiState.value.transactions
        assertEquals(1, filtered.size)
        assertEquals("tx-search-1", filtered[0].id)

        // Search by notes keyword "organic"
        viewModel.onSearchQueryChange("organic")
        val filteredOrganic = viewModel.uiState.value.transactions
        assertEquals(1, filteredOrganic.size)
        assertEquals("tx-search-2", filteredOrganic[0].id)

        // Search non-existent
        viewModel.onSearchQueryChange("nonexistent_keyword")
        assertEquals(0, viewModel.uiState.value.transactions.size)

        // Clear search
        viewModel.onSearchQueryChange("")
        assertEquals(2, viewModel.uiState.value.transactions.size)
    }

    @Test
    fun quickAddExpense_withNotes_persistsNotes() = runBlocking {
        val viewModel = QuickAddExpenseViewModel(
            transactionRepository = fakeTxRepo,
            accountRepository = fakeAccountRepo,
            categoryRepository = fakeCategoryRepo
        )

        viewModel.onAmountChange(25000L) // 250.00
        viewModel.onAccountSelect("acc-1")
        viewModel.onCategorySelect("Dining")
        viewModel.onDescriptionChange("Team Lunch")
        viewModel.onNotesChange("Project celebration #reimbursable")

        val success = viewModel.submitTransaction()
        assertTrue(success)

        val all = fakeTxRepo.transactions
        assertEquals(1, all.size)
        assertEquals("Team Lunch", all[0].description)
        assertEquals("Project celebration #reimbursable", all[0].notes)
    }
}
