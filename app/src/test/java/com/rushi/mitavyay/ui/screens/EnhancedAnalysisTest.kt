package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.CategoryDao
import com.rushi.mitavyay.data.db.CategorySpendingRaw
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.AnalysisPeriod
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.TransactionRepositoryImpl
import com.rushi.mitavyay.ui.screens.Analysis.AnalysisViewModel
import com.rushi.mitavyay.ui.screens.Analysis.TrendChartType
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
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class EnhancedAnalysisTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeTransactionDao(
        val transactions: MutableList<Transaction> = mutableListOf()
    ) : TransactionDao {
        override suspend fun insert(transaction: Transaction) { transactions.add(transaction) }
        override suspend fun insertAll(transactions: List<Transaction>) { this.transactions.addAll(transactions) }
        override suspend fun update(transaction: Transaction) {}
        override suspend fun delete(transaction: Transaction) {}
        override suspend fun deleteById(id: String) {}
        override suspend fun getById(id: String): Transaction? = transactions.find { it.id == id }
        override fun getAll(): Flow<List<Transaction>> = flowOf(transactions)
        override fun getByAccount(accountId: String): Flow<List<Transaction>> =
            flowOf(transactions.filter { it.accountId == accountId })

        override fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>> =
            flowOf(transactions.filter { it.timestamp in start..end }.sortedByDescending { it.timestamp })

        override fun getByCategory(category: String): Flow<List<Transaction>> =
            flowOf(transactions.filter { it.category == category })

        override fun search(query: String): Flow<List<Transaction>> =
            flowOf(transactions.filter {
                it.description.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                (it.notes != null && it.notes.contains(query, ignoreCase = true))
            })

        override suspend fun getByTransferId(transferId: String): List<Transaction> = emptyList()
        override suspend fun deleteByTransferId(transferId: String) {}
        override fun getBalanceForAccount(accountId: String): Flow<Long?> = flowOf(0L)

        override fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?> =
            flowOf(transactions.filter { it.timestamp in start..end && it.amount < 0 }.sumOf { it.amount })

        override fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?> =
            flowOf(transactions.filter { it.timestamp in start..end && it.amount > 0 }.sumOf { it.amount })

        override fun getTransactionCount(): Flow<Int> = flowOf(transactions.size)
        override suspend fun getTransactionCountForAccount(accountId: String): Int = 0

        override fun getCategoryExpensesByDateRange(start: Long, end: Long): Flow<List<CategorySpendingRaw>> =
            flowOf(
                transactions.filter { it.timestamp in start..end && it.amount < 0 }
                    .groupBy { it.category }
                    .map { (cat, txs) ->
                        CategorySpendingRaw(
                            category = cat,
                            totalExpensePaise = txs.sumOf { kotlin.math.abs(it.amount) },
                            transactionCount = txs.size
                        )
                    }
                    .sortedByDescending { it.totalExpensePaise }
            )
    }

    private class FakeAccountRepository : AccountRepository {
        val accounts = mutableListOf<Account>()
        val flow = MutableStateFlow<List<Account>>(emptyList())

        fun notifyChange() { flow.value = accounts.toList() }

        override fun getAllAccounts(): Flow<List<Account>> = flow
        override fun getActiveAccounts(): Flow<List<Account>> = flow
        override fun getAccountById(id: String): Flow<Account?> = flowOf(accounts.find { it.id == id })
        override suspend fun getAccount(id: String): Account? = accounts.find { it.id == id }
        override suspend fun addAccount(account: Account) {
            accounts.add(account)
            notifyChange()
        }
        override suspend fun createAccount(name: String, type: String, initialBalancePaise: Long): Account {
            val acc = Account(UUID.randomUUID().toString(), name, type, initialBalancePaise, "INR", System.currentTimeMillis(), true)
            accounts.add(acc)
            notifyChange()
            return acc
        }
        override suspend fun editAccount(id: String, name: String, type: String) {}
        override suspend fun updateAccount(account: Account) {}
        override suspend fun canDeleteAccount(id: String): Boolean = true
        override suspend fun deleteAccount(id: String): Boolean = true
        override suspend fun archiveAccount(id: String) {}
        override suspend fun unarchiveAccount(id: String) {}
        override suspend fun updateBalance(id: String, newBalance: Long) {}
        override fun getAccountBalance(id: String): Flow<Long?> = flowOf(0L)
    }

    private class FakeCategoryDao(
        val categories: List<Category> = listOf(
            Category(id = "c1", name = "Food & Dining", color = "#FF7043"),
            Category(id = "c2", name = "Groceries", color = "#42A5F5"),
            Category(id = "c3", name = "Entertainment", color = "#AB47BC")
        )
    ) : CategoryDao {
        override suspend fun insert(category: Category) {}
        override suspend fun insertAll(categories: List<Category>) {}
        override suspend fun update(category: Category) {}
        override suspend fun delete(category: Category) {}
        override suspend fun deleteById(id: String) {}
        override suspend fun getById(id: String): Category? = categories.find { it.id == id }
        override fun getAll(): Flow<List<Category>> = flowOf(categories)
        override suspend fun getByName(name: String): Category? = categories.find { it.name.equals(name, ignoreCase = true) }
        override fun getCustomCategories(): Flow<List<Category>> = flowOf(emptyList())
    }

    private class FakeCategoryRepository : CategoryRepository {
        override fun getAllCategories(): Flow<List<Category>> = flowOf(emptyList())
        override fun getCustomCategories(): Flow<List<Category>> = flowOf(emptyList())
        override suspend fun getCategoryById(id: String): Category? = null
        override suspend fun getCategoryByName(name: String): Category? = null
        override suspend fun addCategory(category: Category) {}
        override suspend fun updateCategory(category: Category) {}
        override suspend fun deleteCategory(id: String) {}
        override suspend fun seedDefaultCategories() {}
        override suspend fun createCustomCategory(name: String, colorHex: String, icon: String): Result<Category> =
            Result.failure(NotImplementedError())
        override suspend fun updateCustomCategory(id: String, name: String, colorHex: String, icon: String): Result<Unit> =
            Result.failure(NotImplementedError())
        override suspend fun canDeleteCategory(id: String): Boolean = true
    }

    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var categoryDao: FakeCategoryDao
    private lateinit var transactionRepository: TransactionRepositoryImpl
    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var viewModel: AnalysisViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transactionDao = FakeTestTransactionDao()
        categoryDao = FakeCategoryDao()
        transactionRepository = TransactionRepositoryImpl(transactionDao, categoryDao = categoryDao)
        accountRepository = FakeAccountRepository()
        categoryRepository = FakeCategoryRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun FakeTestTransactionDao(): FakeTransactionDao = FakeTransactionDao()

    @Test
    fun accountFilter_scopesAnalysisToSelectedAccount() = runBlocking {
        val now = System.currentTimeMillis()

        // 2 accounts: Bank and Cash
        val bank = Account("acc_bank", "HDFC Bank", "bank", 500000L, "INR", now, true)
        val cash = Account("acc_cash", "Wallet Cash", "cash", 100000L, "INR", now, true)
        accountRepository.addAccount(bank)
        accountRepository.addAccount(cash)

        // Transactions:
        // Bank: ₹600 Food, ₹400 Bills
        // Cash: ₹300 Groceries
        transactionDao.insert(Transaction("t1", "acc_bank", -60000L, "Dinner", now, "Food & Dining"))
        transactionDao.insert(Transaction("t2", "acc_bank", -40000L, "Electricity", now, "Bills & Utilities"))
        transactionDao.insert(Transaction("t3", "acc_cash", -30000L, "Supermarket", now, "Groceries"))

        viewModel = AnalysisViewModel(transactionRepository, categoryRepository, accountRepository)

        // 1. When All Accounts selected (default)
        val stateAll = viewModel.uiState.value
        assertNull(stateAll.selectedAccountId)
        assertEquals(130000L, stateAll.totalExpensePaise)
        assertEquals(3, stateAll.categorySpendings.size)

        // 2. Select Bank account
        viewModel.selectAccount("acc_bank")
        val stateBank = viewModel.uiState.value
        assertEquals("acc_bank", stateBank.selectedAccountId)
        assertEquals(100000L, stateBank.totalExpensePaise)
        assertEquals(2, stateBank.categorySpendings.size)
        assertTrue(stateBank.categorySpendings.none { it.category == "Groceries" })

        // 3. Select Cash account
        viewModel.selectAccount("acc_cash")
        val stateCash = viewModel.uiState.value
        assertEquals("acc_cash", stateCash.selectedAccountId)
        assertEquals(30000L, stateCash.totalExpensePaise)
        assertEquals(1, stateCash.categorySpendings.size)
        assertEquals("Groceries", stateCash.categorySpendings[0].category)

        // 4. Clear filter back to All Accounts
        viewModel.selectAccount(null)
        val stateReset = viewModel.uiState.value
        assertNull(stateReset.selectedAccountId)
        assertEquals(130000L, stateReset.totalExpensePaise)
    }

    @Test
    fun trendAnalysis_computesIncreasingAndDecreasingSpending() = runBlocking {
        // Build reference calendar for "This Month" and "Last Month"
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 15)
        }
        val thisMonthTime = calendar.timeInMillis
        calendar.add(Calendar.MONTH, -1)
        val lastMonthTime = calendar.timeInMillis

        // Last month spending: ₹1,000.00 (100,000 paise)
        transactionDao.insert(Transaction("t_old", "acc_1", -100000L, "Old Expense", lastMonthTime, "Food & Dining"))
        // This month spending: ₹1,500.00 (150,000 paise) -> 50% increase
        transactionDao.insert(Transaction("t_new", "acc_1", -150000L, "New Expense", thisMonthTime, "Food & Dining"))

        viewModel = AnalysisViewModel(transactionRepository, categoryRepository, accountRepository)

        val state = viewModel.uiState.value
        assertEquals(150000L, state.totalExpensePaise)

        val insight = state.trendInsight
        assertTrue(insight.hasComparisonData)
        assertTrue(insight.isIncreasing)
        assertEquals(150000L, insight.currentExpensePaise)
        assertEquals(100000L, insight.previousExpensePaise)
        assertEquals(50000L, insight.deltaExpensePaise)
        assertEquals(50.0, insight.percentageChange.toDouble(), 0.01)
    }

    @Test
    fun trendAnalysis_computesRunRateForecastForCurrentPeriod() = runBlocking {
        val now = System.currentTimeMillis()
        transactionDao.insert(Transaction("t1", "acc_1", -60000L, "Expense", now, "Food & Dining"))

        viewModel = AnalysisViewModel(transactionRepository, categoryRepository, accountRepository)
        val state = viewModel.uiState.value

        assertEquals(0, state.periodOffset)
        val insight = state.trendInsight
        assertTrue(insight.dailyBurnRatePaise > 0L)
        assertTrue(insight.projectedPeriodExpensePaise >= insight.dailyBurnRatePaise)
    }

    @Test
    fun periodComparison_yearOverYearCalculatesRangesCorrectly() {
        val now = System.currentTimeMillis()
        val currentRange = AnalysisViewModel.calculateDateRange(AnalysisPeriod.YEAR, 0, now)
        val previousYearRange = AnalysisViewModel.calculateDateRange(AnalysisPeriod.YEAR, -1, now)

        val calCur = Calendar.getInstance().apply { timeInMillis = currentRange.startTimestamp }
        val calPrev = Calendar.getInstance().apply { timeInMillis = previousYearRange.startTimestamp }

        assertEquals(1, calCur.get(Calendar.DAY_OF_YEAR))
        assertEquals(1, calPrev.get(Calendar.DAY_OF_YEAR))
        assertEquals(calCur.get(Calendar.YEAR) - 1, calPrev.get(Calendar.YEAR))
    }

    @Test
    fun chartType_toggleLineAndBar() = runBlocking {
        viewModel = AnalysisViewModel(transactionRepository, categoryRepository, accountRepository)
        assertEquals(TrendChartType.LINE, viewModel.uiState.value.chartType)

        viewModel.setChartType(TrendChartType.BAR)
        assertEquals(TrendChartType.BAR, viewModel.uiState.value.chartType)

        viewModel.setChartType(TrendChartType.LINE)
        assertEquals(TrendChartType.LINE, viewModel.uiState.value.chartType)
    }
}
