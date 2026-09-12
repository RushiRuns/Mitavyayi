package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.datastore.PreferencesRepository
import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.ui.MainViewModel
import com.rushi.mitavyay.ui.screens.Accounts.AccountsViewModel
import com.rushi.mitavyay.ui.screens.Analysis.AnalysisViewModel
import com.rushi.mitavyay.ui.screens.TransactionList.TransactionListViewModel
import com.rushi.mitavyay.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun transactionListViewModel_mapsTransactionsToDisplayItems() = runBlocking {
        val fakeRepo = object : TransactionRepository {
            override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(
                listOf(
                    Transaction(
                        id = "tx_1",
                        accountId = "acc_cash",
                        amount = -25000L, // -₹250.00
                        description = "Lunch",
                        timestamp = 1700000000000L,
                        category = "Food & Dining",
                        tags = "[]",
                        transferId = null,
                        notes = null
                    ),
                    Transaction(
                        id = "tx_2",
                        accountId = "acc_bank",
                        amount = 500000L, // ₹5,000.00
                        description = "Freelance",
                        timestamp = 1700000000000L,
                        category = "Income",
                        tags = "[]",
                        transferId = null,
                        notes = null
                    )
                )
            )
            override suspend fun getTransactionById(id: String): Transaction? = null
            override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> = flowOf(emptyList())
            override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> = flowOf(emptyList())
            override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> = flowOf(emptyList())
            override suspend fun addTransaction(transaction: Transaction) {}
            override suspend fun updateTransaction(transaction: Transaction) {}
            override suspend fun deleteTransaction(id: String) {}
            override fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(0L)
            override fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(0L)
            override fun getCategorySpending(start: Long, end: Long): Flow<List<com.rushi.mitavyay.data.repository.CategorySpending>> = flowOf(emptyList())
            override fun getTimeSpendingTrend(start: Long, end: Long, period: com.rushi.mitavyay.data.repository.AnalysisPeriod): Flow<List<com.rushi.mitavyay.data.repository.TimeSpendingPoint>> = flowOf(emptyList())
            override fun getAnalysisSummary(start: Long, end: Long): Flow<com.rushi.mitavyay.data.repository.AnalysisSummary> = flowOf(com.rushi.mitavyay.data.repository.AnalysisSummary())
        }

        val viewModel = TransactionListViewModel(fakeRepo)
        val state = viewModel.uiState.first { !it.isLoading }

        assertFalse(state.isLoading)
        assertEquals(2, state.transactions.size)
        assertEquals("Lunch", state.transactions[0].description)
        assertFalse(state.transactions[0].isCredit)
        assertEquals("Freelance", state.transactions[1].description)
        assertEquals(true, state.transactions[1].isCredit)
    }

    @Test
    fun accountsViewModel_mapsAccountsToDisplayItems() = runBlocking {
        val fakeRepo = object : AccountRepository {
            override fun getAllAccounts(): Flow<List<Account>> = flowOf(
                listOf(
                    Account(
                        id = "acc_1",
                        name = "HDFC Bank",
                        type = "bank",
                        balance = 15000000L, // ₹1,50,000.00
                        currency = "INR",
                        createdAt = 1700000000000L,
                        isActive = true
                    )
                )
            )
            override fun getActiveAccounts(): Flow<List<Account>> = flowOf(emptyList())
            override fun getAccountById(id: String): Flow<Account?> = flowOf(null)
            override suspend fun getAccount(id: String): Account? = null
            override fun getAccountBalance(id: String): Flow<Long?> = flowOf(0L)
            override suspend fun addAccount(account: Account) {}
            override suspend fun createAccount(name: String, type: String, initialBalancePaise: Long): Account {
                return Account(id = "acc_new", name = name, type = type, balance = initialBalancePaise, currency = "INR", createdAt = 0L, isActive = true)
            }
            override suspend fun editAccount(id: String, name: String, type: String) {}
            override suspend fun updateAccount(account: Account) {}
            override suspend fun deleteAccount(id: String): Boolean = true
            override suspend fun canDeleteAccount(id: String): Boolean = true
            override suspend fun archiveAccount(id: String) {}
            override suspend fun unarchiveAccount(id: String) {}
            override suspend fun updateBalance(id: String, newBalance: Long) {}
        }

        val viewModel = AccountsViewModel(fakeRepo)
        val state = viewModel.uiState.first { !it.isLoading }

        assertFalse(state.isLoading)
        assertEquals(1, state.accounts.size)
        assertEquals("HDFC Bank", state.accounts[0].name)
        assertEquals("bank", state.accounts[0].type)
        assertEquals("₹1,50,000.00", state.accounts[0].balanceFormatted)
    }

    @Test
    fun analysisViewModel_initializesCorrectly() = runBlocking {
        val fakeTxRepo = object : TransactionRepository {
            override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
            override suspend fun getTransactionById(id: String): Transaction? = null
            override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> = flowOf(emptyList())
            override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> = flowOf(emptyList())
            override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> = flowOf(emptyList())
            override suspend fun addTransaction(transaction: Transaction) {}
            override suspend fun updateTransaction(transaction: Transaction) {}
            override suspend fun deleteTransaction(id: String) {}
            override fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(0L)
            override fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(0L)
            override fun getCategorySpending(start: Long, end: Long): Flow<List<com.rushi.mitavyay.data.repository.CategorySpending>> = flowOf(emptyList())
            override fun getTimeSpendingTrend(start: Long, end: Long, period: com.rushi.mitavyay.data.repository.AnalysisPeriod): Flow<List<com.rushi.mitavyay.data.repository.TimeSpendingPoint>> = flowOf(emptyList())
            override fun getAnalysisSummary(start: Long, end: Long): Flow<com.rushi.mitavyay.data.repository.AnalysisSummary> = flowOf(com.rushi.mitavyay.data.repository.AnalysisSummary())
        }
        val fakeCatRepo = object : CategoryRepository {
            override fun getAllCategories(): Flow<List<Category>> = flowOf(emptyList())
            override fun getCustomCategories(): Flow<List<Category>> = flowOf(emptyList())
            override suspend fun getCategoryById(id: String): Category? = null
            override suspend fun getCategoryByName(name: String): Category? = null
            override suspend fun addCategory(category: Category) {}
            override suspend fun updateCategory(category: Category) {}
            override suspend fun deleteCategory(id: String) {}
            override suspend fun seedDefaultCategories() {}
            override suspend fun createCustomCategory(name: String, colorHex: String, icon: String) = Result.failure<Category>(NotImplementedError())
            override suspend fun updateCustomCategory(id: String, name: String, colorHex: String, icon: String) = Result.failure<Unit>(NotImplementedError())
            override suspend fun canDeleteCategory(id: String) = false
        }

        val viewModel = AnalysisViewModel(fakeTxRepo, fakeCatRepo)
        val state = viewModel.uiState.value
        assertNotNull(state)
        assertEquals(com.rushi.mitavyay.data.repository.AnalysisPeriod.MONTH, state.selectedPeriod)
    }

    @Test
    fun mainViewModel_mapsThemeAndIncrementsAppOpen() = runBlocking {
        var openCountIncremented = false
        val fakePrefs = object : PreferencesRepository {
            override val themeMode: Flow<String> = flowOf("DARK")
            override val fontScaleMultiplier: Flow<Float> = flowOf(1.2f)
            override val currencySymbol: Flow<String> = flowOf("₹")
            override val language: Flow<String> = flowOf("en")
            override val appOpenCount: Flow<Int> = flowOf(1)

            override suspend fun setThemeMode(mode: String) {}
            override suspend fun setFontScaleMultiplier(scale: Float) {}
            override suspend fun setCurrencySymbol(symbol: String) {}
            override suspend fun setLanguage(lang: String) {}
            override suspend fun incrementAppOpenCount() {
                openCountIncremented = true
            }
        }

        val viewModel = MainViewModel(fakePrefs)
        val theme = viewModel.themeMode.first { it == ThemeMode.DARK }
        val fontScale = viewModel.fontScale.first { it == 1.2f }

        assertEquals(ThemeMode.DARK, theme)
        assertEquals(1.2f, fontScale, 0.001f)
        assertEquals(true, openCountIncremented)
    }
}
