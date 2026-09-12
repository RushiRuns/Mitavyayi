package com.rushi.mitavyay.ui.screens

import androidx.compose.ui.graphics.Color
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.CategoryDao
import com.rushi.mitavyay.data.db.CategorySpendingRaw
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import com.rushi.mitavyay.data.repository.AnalysisPeriod
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.TransactionRepositoryImpl
import com.rushi.mitavyay.ui.components.parseCategoryColor
import com.rushi.mitavyay.ui.screens.Analysis.AnalysisViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeTestTransactionDao(
        val transactions: MutableList<Transaction> = mutableListOf()
    ) : TransactionDao {
        override suspend fun insert(transaction: Transaction) {
            transactions.add(transaction)
        }

        override suspend fun insertAll(transactions: List<Transaction>) {
            this.transactions.addAll(transactions)
        }

        override suspend fun update(transaction: Transaction) {}
        override suspend fun delete(transaction: Transaction) {}
        override suspend fun deleteById(id: String) {}
        override suspend fun getById(id: String): Transaction? = transactions.find { it.id == id }
        override fun getAll(): Flow<List<Transaction>> = flowOf(transactions)
        override fun getByAccount(accountId: String): Flow<List<Transaction>> = flowOf(transactions.filter { it.accountId == accountId })

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

    private class FakeTestCategoryDao(
        val categories: List<Category> = listOf(
            Category(id = "c1", name = "Food & Dining", color = "#FF7043"),
            Category(id = "c2", name = "Groceries", color = "#42A5F5"),
            Category(id = "c3", name = "Bills & Utilities", color = "#EC407A")
        )
    ) : CategoryDao {
        override suspend fun insert(category: Category) {}
        override suspend fun insertAll(categories: List<Category>) {}
        override suspend fun update(category: Category) {}
        override suspend fun delete(category: Category) {}
        override suspend fun deleteById(id: String) {}
        override suspend fun getById(id: String): Category? = categories.find { it.id == id }
        override fun getAll(): Flow<List<Category>> = flowOf(categories)
        override suspend fun getByName(name: String): Category? =
            categories.find { it.name.equals(name, ignoreCase = true) }
        override fun getCustomCategories(): Flow<List<Category>> =
            flowOf(categories.filter { it.isCustom })
    }

    @Test
    fun transactionRepository_aggregatesCategorySpendingAccurately() = runBlocking {
        val now = System.currentTimeMillis()
        val dao = FakeTestTransactionDao(
            mutableListOf(
                Transaction("1", "acc_1", -60000L, "Dinner", now, "Food & Dining"),
                Transaction("2", "acc_1", -40000L, "Snack", now, "Food & Dining"),
                Transaction("3", "acc_1", -100000L, "Supermarket", now, "Groceries"),
                Transaction("4", "acc_1", 500000L, "Salary", now, "Salary") // Income, should not be in category expense
            )
        )
        val catDao = FakeTestCategoryDao()
        val repo = TransactionRepositoryImpl(dao, categoryDao = catDao)

        val categorySpending = repo.getCategorySpending(now - 10000, now + 10000).first()

        assertEquals(2, categorySpending.size)

        // Groceries: 100,000 paise (50%), 1 txn
        val groceries = categorySpending.find { it.category == "Groceries" }
        assertNotNull(groceries)
        assertEquals(100000L, groceries!!.totalExpensePaise)
        assertEquals(1, groceries.transactionCount)
        assertEquals(50f, groceries.percentage, 0.01f)
        assertEquals("#42A5F5", groceries.colorHex)

        // Food & Dining: 100,000 paise (50%), 2 txns
        val food = categorySpending.find { it.category == "Food & Dining" }
        assertNotNull(food)
        assertEquals(100000L, food!!.totalExpensePaise)
        assertEquals(2, food.transactionCount)
        assertEquals(50f, food.percentage, 0.01f)
        assertEquals("#FF7043", food.colorHex)
    }

    @Test
    fun transactionRepository_aggregatesSummaryCorrectly() = runBlocking {
        val now = System.currentTimeMillis()
        val dao = FakeTestTransactionDao(
            mutableListOf(
                Transaction("1", "acc_1", -150000L, "Rent", now, "Bills"),
                Transaction("2", "acc_1", 500000L, "Salary", now, "Salary")
            )
        )
        val repo = TransactionRepositoryImpl(dao)

        val summary = repo.getAnalysisSummary(now - 1000, now + 1000).first()

        assertEquals(500000L, summary.totalIncomePaise)
        assertEquals(150000L, summary.totalExpensePaise)
        assertEquals(350000L, summary.netSavingsPaise)
        assertEquals(0.70f, summary.savingsRate, 0.01f)
    }

    @Test
    fun transactionRepository_aggregatesTimeSpendingTrendForWeek() = runBlocking {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfWeek = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfWeek = cal.timeInMillis

        val dao = FakeTestTransactionDao(
            mutableListOf(
                Transaction("1", "acc_1", -50000L, "Mon Lunch", startOfWeek + 1000, "Food"),
                Transaction("2", "acc_1", 200000L, "Mon Refund", startOfWeek + 2000, "Refund")
            )
        )
        val repo = TransactionRepositoryImpl(dao)

        val trend = repo.getTimeSpendingTrend(startOfWeek, endOfWeek, AnalysisPeriod.WEEK).first()

        assertEquals(7, trend.size)
        assertEquals(50000L, trend[0].expensePaise)
        assertEquals(200000L, trend[0].incomePaise)
        assertEquals(0L, trend[1].expensePaise)
    }

    @Test
    fun analysisViewModel_switchesPeriodsAndNavigatesOffsets() = runBlocking {
        val now = System.currentTimeMillis()
        val dao = FakeTestTransactionDao(
            mutableListOf(
                Transaction("1", "acc_1", -50000L, "Expense", now, "Food & Dining")
            )
        )
        val catRepo = object : CategoryRepository {
            override fun getAllCategories() = flowOf(emptyList<Category>())
            override fun getCustomCategories() = flowOf(emptyList<Category>())
            override suspend fun getCategoryById(id: String) = null
            override suspend fun getCategoryByName(name: String) = null
            override suspend fun addCategory(category: Category) {}
            override suspend fun updateCategory(category: Category) {}
            override suspend fun deleteCategory(id: String) {}
            override suspend fun seedDefaultCategories() {}
            override suspend fun createCustomCategory(name: String, colorHex: String, icon: String) = Result.failure<Category>(NotImplementedError())
            override suspend fun updateCustomCategory(id: String, name: String, colorHex: String, icon: String) = Result.failure<Unit>(NotImplementedError())
            override suspend fun canDeleteCategory(id: String) = false
        }
        val txRepo = TransactionRepositoryImpl(dao)
        val viewModel = AnalysisViewModel(txRepo, catRepo)

        val initialState = viewModel.uiState.first { !it.isLoading }
        assertEquals(AnalysisPeriod.MONTH, initialState.selectedPeriod)
        assertEquals(0, initialState.periodOffset)
        assertFalse(initialState.isEmpty)
        assertEquals(50000L, initialState.totalExpensePaise)

        // Switch to WEEK
        viewModel.selectPeriod(AnalysisPeriod.WEEK)
        val weekState = viewModel.uiState.first { it.selectedPeriod == AnalysisPeriod.WEEK }
        assertEquals(AnalysisPeriod.WEEK, weekState.selectedPeriod)
        assertEquals(0, weekState.periodOffset)

        // Navigate back (previous period)
        viewModel.previousPeriod()
        val prevWeekState = viewModel.uiState.first { it.periodOffset == -1 }
        assertEquals(-1, prevWeekState.periodOffset)

        // Navigate forward (next period)
        viewModel.nextPeriod()
        val nextWeekState = viewModel.uiState.first { it.periodOffset == 0 }
        assertEquals(0, nextWeekState.periodOffset)

        // Ensure nextPeriod cannot go beyond 0 (future)
        viewModel.nextPeriod()
        assertEquals(0, viewModel.uiState.value.periodOffset)

        // Switch to YEAR
        viewModel.selectPeriod(AnalysisPeriod.YEAR)
        val yearState = viewModel.uiState.first { it.selectedPeriod == AnalysisPeriod.YEAR }
        assertEquals(AnalysisPeriod.YEAR, yearState.selectedPeriod)
        assertEquals(0, yearState.periodOffset)
    }

    @Test
    fun parseCategoryColor_handlesValidHexAndFallback() {
        val c1 = parseCategoryColor("#FF7043", 0)
        assertEquals(Color(0xFFFF7043), c1)

        val c2 = parseCategoryColor("42A5F5", 1)
        assertEquals(Color(0xFF42A5F5), c2)

        val c3 = parseCategoryColor("invalid-hex", 0)
        assertNotNull(c3)

        val c4 = parseCategoryColor(null, 1)
        assertNotNull(c4)
    }

    @Test
    fun calculateDateRange_generatesNonEmptyLabels() {
        val week = AnalysisViewModel.calculateDateRange(AnalysisPeriod.WEEK, 0)
        assertTrue(week.label.contains("Week"))
        assertTrue(week.startTimestamp < week.endTimestamp)

        val month = AnalysisViewModel.calculateDateRange(AnalysisPeriod.MONTH, 0)
        assertTrue(month.label.contains("Month"))
        assertTrue(month.startTimestamp < month.endTimestamp)

        val year = AnalysisViewModel.calculateDateRange(AnalysisPeriod.YEAR, 0)
        assertTrue(year.label.contains("Year"))
        assertTrue(year.startTimestamp < year.endTimestamp)
    }
}
