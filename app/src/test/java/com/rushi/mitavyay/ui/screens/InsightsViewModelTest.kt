package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.BasicInsightsData
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.ui.screens.Insights.InsightsViewModel
import com.rushi.mitavyay.util.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var viewModel: InsightsViewModel

    private class FakeTransactionRepository : TransactionRepository {
        val insightsMap = mutableMapOf<String, BasicInsightsData>()

        override fun getBasicInsights(monthYear: String): Flow<BasicInsightsData> {
            val data = insightsMap[monthYear] ?: BasicInsightsData(
                monthYear = monthYear,
                monthYearFormatted = DateTimeFormatter.formatMonthYear(monthYear),
                totalSpentThisMonthPaise = 50000L,
                hasData = true
            )
            return flowOf(data)
        }

        // Unused stubs
        override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
        override suspend fun getTransactionById(id: String): Transaction? = null
        override suspend fun addTransaction(transaction: Transaction) {}
        override suspend fun updateTransaction(transaction: Transaction) {}
        override suspend fun deleteTransaction(id: String) {}
        override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(0L)
        override fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(0L)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeTransactionRepository = FakeTransactionRepository()
        viewModel = InsightsViewModel(fakeTransactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateLoadsCurrentMonth() = runTest {
        val state = viewModel.uiState.value
        assertEquals(DateTimeFormatter.getCurrentMonthYear(), state.selectedMonthYear)
        assertTrue(state.isCurrentMonth)
        assertFalse(state.isLoading)
        assertTrue(state.insights.hasData)
        assertEquals(50000L, state.insights.totalSpentThisMonthPaise)
    }

    @Test
    fun testMonthNavigation() = runTest {
        val currentMonth = DateTimeFormatter.getCurrentMonthYear()
        val prevMonth = DateTimeFormatter.getAdjacentMonthYear(currentMonth, -1)
        val nextMonth = DateTimeFormatter.getAdjacentMonthYear(currentMonth, 1)

        // Navigate to previous month
        viewModel.previousMonth()
        val prevState = viewModel.uiState.value
        assertEquals(prevMonth, prevState.selectedMonthYear)
        assertFalse(prevState.isCurrentMonth)

        // Navigate to next month (back to current)
        viewModel.nextMonth()
        val curState = viewModel.uiState.value
        assertEquals(currentMonth, curState.selectedMonthYear)
        assertTrue(curState.isCurrentMonth)

        // Navigate to next month (future month)
        viewModel.nextMonth()
        val nextState = viewModel.uiState.value
        assertEquals(nextMonth, nextState.selectedMonthYear)
        assertFalse(nextState.isCurrentMonth)

        // Reset to current month
        viewModel.resetToCurrentMonth()
        val resetState = viewModel.uiState.value
        assertEquals(currentMonth, resetState.selectedMonthYear)
        assertTrue(resetState.isCurrentMonth)
    }

    @Test
    fun testSelectSpecificMonth() = runTest {
        viewModel.selectMonth("2025-01")
        val state = viewModel.uiState.value
        assertEquals("2025-01", state.selectedMonthYear)
        assertFalse(state.isCurrentMonth)
    }
}
