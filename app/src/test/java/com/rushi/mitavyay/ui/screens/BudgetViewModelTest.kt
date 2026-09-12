package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Budget
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.model.BudgetAlertLevel
import com.rushi.mitavyay.data.repository.BudgetRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.CategorySpending
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.ui.screens.Budget.BudgetViewModel
import com.rushi.mitavyay.util.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var viewModel: BudgetViewModel

    private class FakeBudgetRepository : BudgetRepository {
        val list = mutableListOf<Budget>()
        private val flow = MutableStateFlow<List<Budget>>(emptyList())

        fun updateFlow() {
            flow.value = list.toList()
        }

        override fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>> =
            flow.map { all -> all.filter { it.monthYear == monthYear && it.isActive } }

        override fun getAllBudgets(): Flow<List<Budget>> = flow

        override suspend fun getBudgetById(id: String): Budget? = list.find { it.id == id }

        override suspend fun getBudgetByCategoryAndMonth(category: String, monthYear: String): Budget? =
            list.find { it.category == category && it.monthYear == monthYear }

        override suspend fun setBudget(category: String, monthYear: String, amountPaise: Long): Budget {
            val existing = list.find { it.category == category && it.monthYear == monthYear }
            val budget = if (existing != null) {
                val updated = existing.copy(amount = amountPaise, isActive = true)
                list[list.indexOf(existing)] = updated
                updated
            } else {
                val created = Budget(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    monthYear = monthYear,
                    amount = amountPaise,
                    isActive = true
                )
                list.add(created)
                created
            }
            updateFlow()
            return budget
        }

        override suspend fun updateBudget(budget: Budget) {
            val idx = list.indexOfFirst { it.id == budget.id }
            if (idx >= 0) list[idx] = budget
            updateFlow()
        }

        override suspend fun deleteBudget(id: String) {
            list.removeAll { it.id == id }
            updateFlow()
        }

        override suspend fun copyBudgetsToMonth(sourceMonthYear: String, targetMonthYear: String): Int {
            val sourceBudgets = list.filter { it.monthYear == sourceMonthYear && it.isActive }
            var copied = 0
            for (src in sourceBudgets) {
                if (list.none { it.category == src.category && it.monthYear == targetMonthYear }) {
                    list.add(
                        Budget(
                            id = UUID.randomUUID().toString(),
                            category = src.category,
                            monthYear = targetMonthYear,
                            amount = src.amount,
                            isActive = true
                        )
                    )
                    copied++
                }
            }
            updateFlow()
            return copied
        }
    }

    private class FakeTransactionRepository : TransactionRepository {
        val categorySpendings = mutableListOf<CategorySpending>()
        val spendingsFlow = MutableStateFlow<List<CategorySpending>>(emptyList())

        fun updateSpendings() {
            spendingsFlow.value = categorySpendings.toList()
        }

        override fun getCategorySpending(start: Long, end: Long): Flow<List<CategorySpending>> =
            spendingsFlow

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

    private class FakeCategoryRepository : CategoryRepository {
        val categories = mutableListOf<Category>()
        val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

        fun updateCategories() {
            categoriesFlow.value = categories.toList()
        }

        override fun getAllCategories(): Flow<List<Category>> = categoriesFlow
        override fun getCustomCategories(): Flow<List<Category>> = flowOf(emptyList())
        override suspend fun getCategoryById(id: String): Category? = categories.find { it.id == id }
        override suspend fun getCategoryByName(name: String): Category? = categories.find { it.name == name }
        override suspend fun addCategory(category: Category) {
            categories.add(category)
            updateCategories()
        }
        override suspend fun updateCategory(category: Category) {}
        override suspend fun deleteCategory(id: String) {}
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
        fakeBudgetRepository = FakeBudgetRepository()
        fakeTransactionRepository = FakeTransactionRepository()
        fakeCategoryRepository = FakeCategoryRepository()

        // Seed default categories
        fakeCategoryRepository.categories.add(Category("cat_food", "Food & Dining", "restaurant", "#FF7043", false))
        fakeCategoryRepository.categories.add(Category("cat_transport", "Transport", "directions_car", "#AB47BC", false))
        fakeCategoryRepository.categories.add(Category("cat_shopping", "Shopping", "storefront", "#26A69A", false))
        fakeCategoryRepository.categories.add(Category("cat_bills", "Bills & Utilities", "receipt", "#EC407A", false))
        fakeCategoryRepository.updateCategories()

        viewModel = BudgetViewModel(
            fakeBudgetRepository,
            fakeTransactionRepository,
            fakeCategoryRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Feature requirement: "Test: Set budget"
    @Test
    fun testSetBudget() = runBlocking {
        val currentMonth = DateTimeFormatter.getCurrentMonthYear()
        assertEquals(currentMonth, viewModel.uiState.value.selectedMonthYear)
        assertTrue(viewModel.uiState.value.isEmpty)

        // Set budget: ₹10,000.00 (1000000 paise) for Food & Dining
        viewModel.setBudget("Food & Dining", 1000000L)

        val state = viewModel.uiState.value
        assertFalse(state.isEmpty)
        assertEquals(1, state.items.size)
        val item = state.items[0]
        assertEquals("Food & Dining", item.category)
        assertEquals(1000000L, item.budgetAmountPaise)
        assertEquals("₹10,000.00", item.budgetAmountFormatted)
        assertEquals(currentMonth, item.monthYear)
    }

    // Feature requirement: "Test: Actual spending compared to budget"
    @Test
    fun testActualSpendingComparedToBudget() = runBlocking {
        val currentMonth = DateTimeFormatter.getCurrentMonthYear()

        // Budget: ₹10,000.00 for Food & Dining
        fakeBudgetRepository.setBudget("Food & Dining", currentMonth, 1000000L)

        // Actual spending: ₹4,500.00 (450000 paise)
        fakeTransactionRepository.categorySpendings.add(
            CategorySpending(
                category = "Food & Dining",
                totalExpensePaise = 450000L,
                transactionCount = 5,
                percentage = 0.45f
            )
        )
        fakeTransactionRepository.updateSpendings()

        // Trigger state refresh
        viewModel.selectMonth(currentMonth)

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        val item = state.items[0]

        // Compare actual vs. budgeted
        assertEquals(1000000L, item.budgetAmountPaise)
        assertEquals(450000L, item.spentAmountPaise)
        assertEquals(550000L, item.remainingAmountPaise)
        assertEquals("₹4,500.00", item.spentAmountFormatted)
        assertEquals("₹5,500.00", item.remainingAmountFormatted)
        assertFalse(item.isOverBudget)
        assertEquals(45, item.progressPercentage)
        assertEquals(0.45, item.progress.toDouble(), 0.001)

        // Overall monthly summary
        assertEquals(1000000L, state.totalBudgetedPaise)
        assertEquals(450000L, state.totalSpentPaise)
        assertEquals(550000L, state.totalRemainingPaise)
    }

    // Feature requirement: "Test: Alert logic at thresholds"
    @Test
    fun testAlertLogicAtThresholds() = runBlocking {
        val currentMonth = DateTimeFormatter.getCurrentMonthYear()

        // 1. Safe budget (< 80%): Budget ₹10,000.00, Spent ₹5,000.00 (50%)
        fakeBudgetRepository.setBudget("Food & Dining", currentMonth, 1000000L)
        fakeTransactionRepository.categorySpendings.add(
            CategorySpending(
                category = "Food & Dining",
                totalExpensePaise = 500000L,
                transactionCount = 4,
                percentage = 0.50f
            )
        )

        // 2. Warning budget (>= 80% and < 100%): Budget ₹5,000.00, Spent ₹4,250.00 (85%)
        fakeBudgetRepository.setBudget("Transport", currentMonth, 500000L)
        fakeTransactionRepository.categorySpendings.add(
            CategorySpending(
                category = "Transport",
                totalExpensePaise = 425000L,
                transactionCount = 6,
                percentage = 0.85f
            )
        )

        // 3. Exceeded budget (>= 100%): Budget ₹2,000.00, Spent ₹2,500.00 (125%)
        fakeBudgetRepository.setBudget("Shopping", currentMonth, 200000L)
        fakeTransactionRepository.categorySpendings.add(
            CategorySpending(
                category = "Shopping",
                totalExpensePaise = 250000L,
                transactionCount = 3,
                percentage = 1.25f
            )
        )
        fakeTransactionRepository.updateSpendings()

        viewModel.selectMonth(currentMonth)

        val state = viewModel.uiState.value
        assertEquals(3, state.items.size)

        val foodItem = state.items.find { it.category == "Food & Dining" }!!
        assertEquals(BudgetAlertLevel.SAFE, foodItem.alertLevel)
        assertEquals(50, foodItem.progressPercentage)
        assertFalse(foodItem.isOverBudget)

        val transportItem = state.items.find { it.category == "Transport" }!!
        assertEquals(BudgetAlertLevel.WARNING_80, transportItem.alertLevel)
        assertEquals(85, transportItem.progressPercentage)
        assertFalse(transportItem.isOverBudget)

        val shoppingItem = state.items.find { it.category == "Shopping" }!!
        assertEquals(BudgetAlertLevel.EXCEEDED_100, shoppingItem.alertLevel)
        assertEquals(125, shoppingItem.progressPercentage)
        assertTrue(shoppingItem.isOverBudget)
        assertEquals(-50000L, shoppingItem.remainingAmountPaise) // Over by ₹500.00

        // Verify summary alert counts
        assertEquals(1, state.warningCount)
        assertEquals(1, state.exceededCount)
    }

    @Test
    fun testMonthNavigation() {
        val currentMonth = DateTimeFormatter.getCurrentMonthYear()
        assertEquals(currentMonth, viewModel.uiState.value.selectedMonthYear)

        // Navigate previous month
        viewModel.previousMonth()
        val prevMonth = DateTimeFormatter.getAdjacentMonthYear(currentMonth, -1)
        assertEquals(prevMonth, viewModel.uiState.value.selectedMonthYear)

        // Navigate next month back to current
        viewModel.nextMonth()
        assertEquals(currentMonth, viewModel.uiState.value.selectedMonthYear)

        // Navigate next month into future
        viewModel.nextMonth()
        val nextMonth = DateTimeFormatter.getAdjacentMonthYear(currentMonth, 1)
        assertEquals(nextMonth, viewModel.uiState.value.selectedMonthYear)

        // Go to current month jump
        viewModel.goToCurrentMonth()
        assertEquals(currentMonth, viewModel.uiState.value.selectedMonthYear)
    }

    @Test
    fun testCopyPreviousMonthBudgets() = runBlocking {
        val currentMonth = DateTimeFormatter.getCurrentMonthYear()
        val prevMonth = DateTimeFormatter.getAdjacentMonthYear(currentMonth, -1)

        // Set 2 budgets in previous month
        fakeBudgetRepository.setBudget("Food & Dining", prevMonth, 800000L)
        fakeBudgetRepository.setBudget("Bills & Utilities", prevMonth, 600000L)

        assertTrue(viewModel.uiState.value.isEmpty)

        viewModel.copyPreviousMonthBudgets()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertNotNull(state.items.find { it.category == "Food & Dining" && it.budgetAmountPaise == 800000L })
        assertNotNull(state.items.find { it.category == "Bills & Utilities" && it.budgetAmountPaise == 600000L })
    }

    @Test
    fun testDeleteBudget() = runBlocking {
        val currentMonth = DateTimeFormatter.getCurrentMonthYear()
        val budget = fakeBudgetRepository.setBudget("Transport", currentMonth, 500000L)

        viewModel.selectMonth(currentMonth)
        assertEquals(1, viewModel.uiState.value.items.size)

        viewModel.deleteBudget(budget.id)
        viewModel.selectMonth(currentMonth)
        assertEquals(0, viewModel.uiState.value.items.size)
    }
}
