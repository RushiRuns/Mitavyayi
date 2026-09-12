package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.Goal
import com.rushi.mitavyay.data.model.GoalStatus
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.GoalRepository
import com.rushi.mitavyay.ui.screens.Goals.GoalFilterTab
import com.rushi.mitavyay.ui.screens.Goals.GoalViewModel
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
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class GoalViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeGoalRepository : GoalRepository {
        val goalsMap = mutableMapOf<String, Goal>()
        val goalsFlow = MutableStateFlow<List<Goal>>(emptyList())

        fun notifyChange() {
            goalsFlow.value = goalsMap.values.sortedBy { it.deadline }
        }

        override fun getAllGoals(): Flow<List<Goal>> = goalsFlow

        override fun getGoalById(id: String): Flow<Goal?> = flowOf(goalsMap[id])

        override suspend fun getGoal(id: String): Goal? = goalsMap[id]

        override suspend fun addGoal(goal: Goal) {
            goalsMap[goal.id] = goal
            notifyChange()
        }

        override suspend fun updateGoal(goal: Goal) {
            goalsMap[goal.id] = goal
            notifyChange()
        }

        override suspend fun deleteGoal(id: String) {
            goalsMap.remove(id)
            notifyChange()
        }

        override suspend fun updateCurrentAmount(id: String, amount: Long) {
            goalsMap[id]?.let {
                goalsMap[id] = it.copy(currentAmount = amount)
                notifyChange()
            }
        }

        override suspend fun addSavings(id: String, amountPaise: Long) {
            goalsMap[id]?.let {
                goalsMap[id] = it.copy(currentAmount = it.currentAmount + amountPaise)
                notifyChange()
            }
        }
    }

    private class FakeAccountRepository : AccountRepository {
        val accountsMap = mutableMapOf<String, Account>()
        val flow = MutableStateFlow<List<Account>>(emptyList())

        fun updateFlow() {
            flow.value = accountsMap.values.toList()
        }

        override fun getAllAccounts(): Flow<List<Account>> = flow
        override fun getActiveAccounts(): Flow<List<Account>> = flow
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
        override suspend fun editAccount(id: String, name: String, type: String) {}
        override suspend fun updateAccount(account: Account) {
            accountsMap[account.id] = account
            updateFlow()
        }
        override suspend fun canDeleteAccount(id: String): Boolean = true
        override suspend fun deleteAccount(id: String): Boolean = true
        override suspend fun archiveAccount(id: String) {}
        override suspend fun unarchiveAccount(id: String) {}
        override suspend fun updateBalance(id: String, newBalance: Long) {
            accountsMap[id]?.let {
                accountsMap[id] = it.copy(balance = newBalance)
                updateFlow()
            }
        }
        override fun getAccountBalance(id: String): Flow<Long?> = flowOf(accountsMap[id]?.balance)
    }

    private class FakeCategoryRepository : CategoryRepository {
        val categories = mutableListOf<Category>()
        val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

        fun notifyChange() {
            categoriesFlow.value = categories.toList()
        }

        override fun getAllCategories(): Flow<List<Category>> = categoriesFlow
        override fun getCustomCategories(): Flow<List<Category>> = flowOf(emptyList())
        override suspend fun getCategoryById(id: String): Category? = categories.find { it.id == id }
        override suspend fun getCategoryByName(name: String): Category? =
            categories.find { it.name.equals(name, ignoreCase = true) }
        override suspend fun addCategory(category: Category) {
            categories.add(category)
            notifyChange()
        }
        override suspend fun updateCategory(category: Category) {}
        override suspend fun deleteCategory(id: String) {}
        override suspend fun seedDefaultCategories() {}
        override suspend fun createCustomCategory(name: String, colorHex: String, icon: String): Result<Category> =
            Result.success(Category(id = "c_${UUID.randomUUID()}", name = name, icon = icon, color = colorHex, isCustom = true))
        override suspend fun updateCustomCategory(id: String, name: String, colorHex: String, icon: String): Result<Unit> =
            Result.success(Unit)
        override suspend fun canDeleteCategory(id: String): Boolean = true
    }

    private lateinit var goalRepository: FakeGoalRepository
    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var viewModel: GoalViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        goalRepository = FakeGoalRepository()
        accountRepository = FakeAccountRepository()
        categoryRepository = FakeCategoryRepository()
        viewModel = GoalViewModel(goalRepository, accountRepository, categoryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_dedicatedFundCalculatesProgressAndStatusCorrectly() = runBlocking {
        val futureDeadline = System.currentTimeMillis() + 60L * 24L * 60L * 60L * 1000L // 60 days out
        val goal = Goal(
            id = "g_1",
            name = "Emergency Fund",
            targetAmount = 1000000L, // ₹10,000.00
            deadline = futureDeadline,
            currentAmount = 500000L, // ₹5,000.00
            linkedAccountId = null
        )
        goalRepository.addGoal(goal)

        val state = viewModel.uiState.value
        assertEquals(1, state.allGoals.size)
        val item = state.allGoals[0]
        assertEquals("Emergency Fund", item.name)
        assertEquals(1000000L, item.targetAmountPaise)
        assertEquals(500000L, item.currentAmountPaise)
        assertEquals(50, item.progressPercentage)
        assertEquals(0.50, item.progress.toDouble(), 0.001)
        assertNull(item.linkedAccountId)
        assertNull(item.linkedAccountName)
        assertEquals(GoalStatus.ON_TRACK, item.status)
        assertTrue(item.timelineText.contains("remaining", ignoreCase = true))
    }

    @Test
    fun uiState_linkedAccountCalculatesProgressFromAccountBalance() = runBlocking {
        val account = Account(
            id = "acc_savings",
            name = "HDFC Savings",
            type = "bank",
            balance = 800000L, // ₹8,000.00
            currency = "INR",
            createdAt = 1000L,
            isActive = true
        )
        accountRepository.addAccount(account)

        val futureDeadline = System.currentTimeMillis() + 30L * 24L * 60L * 60L * 1000L
        val goal = Goal(
            id = "g_linked",
            name = "Car Downpayment",
            targetAmount = 1000000L, // ₹10,000.00
            deadline = futureDeadline,
            currentAmount = 0L, // Stored as 0 since linked to account
            linkedAccountId = "acc_savings"
        )
        goalRepository.addGoal(goal)

        val state = viewModel.uiState.value
        assertEquals(1, state.allGoals.size)
        val item = state.allGoals[0]
        assertEquals("Car Downpayment", item.name)
        assertEquals("acc_savings", item.linkedAccountId)
        assertEquals("HDFC Savings", item.linkedAccountName)
        // Current amount reflects the linked account balance
        assertEquals(800000L, item.currentAmountPaise)
        assertEquals(80, item.progressPercentage)
        assertEquals(0.80, item.progress.toDouble(), 0.001)
        assertEquals(GoalStatus.ON_TRACK, item.status)
    }

    @Test
    fun uiState_statusTransitions_achievedAndOverdue() = runBlocking {
        val now = System.currentTimeMillis()

        // 1. Achieved Goal (100% saved)
        val achievedGoal = Goal(
            id = "g_achieved",
            name = "New Phone",
            targetAmount = 500000L,
            deadline = now + 10000000L,
            currentAmount = 500000L
        )
        goalRepository.addGoal(achievedGoal)

        // 2. Overdue Goal (past deadline and not full)
        val overdueGoal = Goal(
            id = "g_overdue",
            name = "Trip to Goa",
            targetAmount = 2000000L,
            deadline = now - 5L * 24L * 60L * 60L * 1000L, // 5 days ago
            currentAmount = 500000L
        )
        goalRepository.addGoal(overdueGoal)

        val state = viewModel.uiState.value
        val achievedItem = state.allGoals.find { it.id == "g_achieved" }
        assertNotNull(achievedItem)
        assertEquals(GoalStatus.ACHIEVED, achievedItem?.status)
        assertEquals(100, achievedItem!!.progressPercentage)
        assertEquals(1.0, achievedItem.progress.toDouble(), 0.001)
        assertEquals("Goal Achieved!", achievedItem.timelineText)

        val overdueItem = state.allGoals.find { it.id == "g_overdue" }
        assertNotNull(overdueItem)
        assertEquals(GoalStatus.OVERDUE, overdueItem?.status)
        assertTrue(overdueItem!!.timelineText.contains("Overdue by", ignoreCase = true))
    }

    @Test
    fun uiState_tabFiltering() = runBlocking {
        val now = System.currentTimeMillis()

        // 1. Achieved
        goalRepository.addGoal(Goal("g1", "Achieved Goal", 100000L, now + 1000000L, 100000L))
        // 2. Overdue
        goalRepository.addGoal(Goal("g2", "Overdue Goal", 200000L, now - 5000000L, 50000L))
        // 3. In Progress (On Track)
        goalRepository.addGoal(Goal("g3", "In Progress Goal", 300000L, now + 60L * 24 * 3600 * 1000L, 150000L))

        // Tab: ALL
        viewModel.selectTab(GoalFilterTab.ALL)
        assertEquals(3, viewModel.uiState.value.displayedGoals.size)

        // Tab: IN_PROGRESS
        viewModel.selectTab(GoalFilterTab.IN_PROGRESS)
        assertEquals(1, viewModel.uiState.value.displayedGoals.size)
        assertEquals("g3", viewModel.uiState.value.displayedGoals[0].id)

        // Tab: ACHIEVED
        viewModel.selectTab(GoalFilterTab.ACHIEVED)
        assertEquals(1, viewModel.uiState.value.displayedGoals.size)
        assertEquals("g1", viewModel.uiState.value.displayedGoals[0].id)

        // Tab: OVERDUE
        viewModel.selectTab(GoalFilterTab.OVERDUE)
        assertEquals(1, viewModel.uiState.value.displayedGoals.size)
        assertEquals("g2", viewModel.uiState.value.displayedGoals[0].id)
    }

    @Test
    fun createGoal_validationAndSuccess() = runBlocking {
        var errorOccurred: String? = null

        // 1. Blank name validation
        viewModel.createGoal(
            name = "   ",
            targetAmountPaise = 500000L,
            deadlineMs = System.currentTimeMillis() + 1000000L,
            linkedAccountId = null,
            onError = { errorOccurred = it }
        )
        assertNotNull(errorOccurred)
        assertEquals("Goal name cannot be blank", viewModel.uiState.value.errorMessage)
        assertEquals(0, goalRepository.goalsMap.size)

        // 2. Zero target amount validation
        errorOccurred = null
        viewModel.createGoal(
            name = "Valid Name",
            targetAmountPaise = 0L,
            deadlineMs = System.currentTimeMillis() + 1000000L,
            linkedAccountId = null,
            onError = { errorOccurred = it }
        )
        assertNotNull(errorOccurred)
        assertEquals("Target amount must be greater than zero", viewModel.uiState.value.errorMessage)
        assertEquals(0, goalRepository.goalsMap.size)

        // 3. Invalid deadline validation
        errorOccurred = null
        viewModel.createGoal(
            name = "Valid Name",
            targetAmountPaise = 500000L,
            deadlineMs = 0L,
            linkedAccountId = null,
            onError = { errorOccurred = it }
        )
        assertNotNull(errorOccurred)
        assertEquals("Please select a valid deadline", viewModel.uiState.value.errorMessage)
        assertEquals(0, goalRepository.goalsMap.size)

        // 4. Successful creation (dedicated fund)
        var successCalled = false
        viewModel.createGoal(
            name = "Laptop Fund",
            targetAmountPaise = 8000000L,
            deadlineMs = System.currentTimeMillis() + 90L * 24 * 3600 * 1000L,
            linkedAccountId = null,
            initialDepositPaise = 1500000L,
            category = "Electronics",
            notes = "For work laptop",
            onSuccess = { successCalled = true }
        )
        assertTrue(successCalled)
        assertEquals(1, goalRepository.goalsMap.size)
        val created = goalRepository.goalsMap.values.first()
        assertEquals("Laptop Fund", created.name)
        assertEquals(8000000L, created.targetAmount)
        assertEquals(1500000L, created.currentAmount)
        assertNull(created.linkedAccountId)
        assertEquals("Electronics", created.category)
        assertEquals("For work laptop", created.notes)
    }

    @Test
    fun addSavings_validationAndExecution() = runBlocking {
        val goal = Goal(
            id = "g_dedicated",
            name = "Gadget",
            targetAmount = 5000000L,
            deadline = System.currentTimeMillis() + 10000000L,
            currentAmount = 1000000L
        )
        goalRepository.addGoal(goal)

        // 1. Zero amount validation
        viewModel.addSavings("g_dedicated", 0L)
        assertEquals("Deposit amount must be greater than zero", viewModel.uiState.value.errorMessage)
        assertEquals(1000000L, goalRepository.goalsMap["g_dedicated"]?.currentAmount)

        // 2. Valid deposit
        var depositSuccess = false
        viewModel.addSavings("g_dedicated", 750000L, onSuccess = { depositSuccess = true })
        assertTrue(depositSuccess)
        assertEquals(1750000L, goalRepository.goalsMap["g_dedicated"]?.currentAmount)
        assertEquals(1750000L, viewModel.uiState.value.allGoals[0].currentAmountPaise)
    }

    @Test
    fun deleteGoal_execution() = runBlocking {
        val goal = Goal(
            id = "g_delete_me",
            name = "Temporary",
            targetAmount = 1000000L,
            deadline = System.currentTimeMillis() + 10000000L,
            currentAmount = 200000L
        )
        goalRepository.addGoal(goal)

        val item = viewModel.uiState.value.allGoals[0]
        viewModel.onDeleteClick(item)
        assertEquals(item, viewModel.uiState.value.goalToDelete)

        viewModel.confirmDelete()
        assertEquals(0, goalRepository.goalsMap.size)
        assertNull(viewModel.uiState.value.goalToDelete)
        assertEquals(0, viewModel.uiState.value.allGoals.size)
    }

    @Test
    fun summaryMetrics_calculatedCorrectly() = runBlocking {
        val now = System.currentTimeMillis()

        // Goal 1: Active, target 10,000, current 4,000
        goalRepository.addGoal(
            Goal("g1", "Goal 1", 1000000L, now + 10000000L, 400000L)
        )
        // Goal 2: Achieved, target 5,000, current 5,000
        goalRepository.addGoal(
            Goal("g2", "Goal 2", 500000L, now + 10000000L, 500000L)
        )

        val state = viewModel.uiState.value
        assertEquals(1500000L, state.totalTargetPaise)
        assertEquals(900000L, state.totalSavedPaise)
        assertEquals(0.60, state.overallProgress.toDouble(), 0.001)
        assertEquals(1, state.activeCount)
        assertEquals(1, state.achievedCount)
    }
}
