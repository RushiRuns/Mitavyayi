package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.RepeatExpense
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.RepeatExpenseRepository
import com.rushi.mitavyay.ui.screens.RepeatExpense.RepeatExpenseViewModel
import com.rushi.mitavyay.ui.screens.RepeatExpense.RepeatFilterTab
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
class RepeatExpenseViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeRepeatRepository : RepeatExpenseRepository {
        val list = mutableListOf<RepeatExpense>()
        val flow = MutableStateFlow<List<RepeatExpense>>(emptyList())
        var dueOccurrencesToGenerate: Int = 0

        fun notifyChange() {
            flow.value = list.toList()
        }

        override fun getAllRepeatExpenses(): Flow<List<RepeatExpense>> = flow

        override fun getActiveRepeatExpenses(): Flow<List<RepeatExpense>> = flowOf(list.filter { it.isActive })

        override suspend fun getRepeatExpenseById(id: String): RepeatExpense? = list.find { it.id == id }

        override suspend fun addRepeatExpense(repeatExpense: RepeatExpense) {
            list.add(repeatExpense)
            notifyChange()
        }

        override suspend fun updateRepeatExpense(repeatExpense: RepeatExpense) {
            val idx = list.indexOfFirst { it.id == repeatExpense.id }
            if (idx != -1) {
                list[idx] = repeatExpense
                notifyChange()
            }
        }

        override suspend fun deleteRepeatExpense(id: String) {
            list.removeAll { it.id == id }
            notifyChange()
        }

        override suspend fun toggleActive(id: String): Boolean {
            val idx = list.indexOfFirst { it.id == id }
            if (idx != -1) {
                val newStatus = !list[idx].isActive
                list[idx] = list[idx].copy(isActive = newStatus)
                notifyChange()
                return newStatus
            }
            return false
        }

        override suspend fun generateNextOccurrence(
            repeatExpenseId: String,
            accountId: String,
            currentTimeMs: Long
        ): Transaction? = null

        override suspend fun processAllDueOccurrences(
            defaultAccountId: String,
            currentTimeMs: Long
        ): List<Transaction> {
            val result = mutableListOf<Transaction>()
            repeat(dueOccurrencesToGenerate) { i ->
                result.add(
                    Transaction(
                        id = "tx_due_$i",
                        accountId = defaultAccountId,
                        amount = -1000L,
                        description = "Recurring Due",
                        timestamp = currentTimeMs,
                        category = "Bills"
                    )
                )
            }
            return result
        }
    }

    private class FakeCategoryRepository : CategoryRepository {
        val categories = mutableListOf(
            Category("cat_bills", "Bills & Utilities", "star", "#FF9800", false),
            Category("cat_sub", "Subscriptions", "star", "#E91E63", false)
        )
        override fun getAllCategories(): Flow<List<Category>> = flowOf(categories)
        override fun getCustomCategories(): Flow<List<Category>> = flowOf(emptyList())
        override suspend fun getCategoryById(id: String): Category? = categories.find { it.id == id }
        override suspend fun getCategoryByName(name: String): Category? = categories.find { it.name == name }
        override suspend fun addCategory(category: Category) {}
        override suspend fun updateCategory(category: Category) {}
        override suspend fun deleteCategory(id: String) {}
        override suspend fun seedDefaultCategories() {}
        override suspend fun createCustomCategory(name: String, colorHex: String, icon: String): Result<Category> =
            Result.success(Category(UUID.randomUUID().toString(), name, icon, colorHex, true))
        override suspend fun updateCustomCategory(id: String, name: String, colorHex: String, icon: String): Result<Unit> =
            Result.success(Unit)
        override suspend fun canDeleteCategory(id: String): Boolean = true
    }

    private class FakeAccountRepository : AccountRepository {
        val accounts = mutableListOf(
            Account("acc_main", "Checking Account", "bank", 500000L, "INR", 1000L, true)
        )
        override fun getAllAccounts(): Flow<List<Account>> = flowOf(accounts)
        override fun getActiveAccounts(): Flow<List<Account>> = flowOf(accounts.filter { it.isActive })
        override fun getAccountById(id: String): Flow<Account?> = flowOf(accounts.find { it.id == id })
        override suspend fun getAccount(id: String): Account? = accounts.find { it.id == id }
        override suspend fun addAccount(account: Account) {}
        override suspend fun createAccount(name: String, type: String, initialBalancePaise: Long): Account = accounts[0]
        override suspend fun editAccount(id: String, name: String, type: String) {}
        override suspend fun updateAccount(account: Account) {}
        override suspend fun deleteAccount(id: String): Boolean = true
        override suspend fun canDeleteAccount(id: String): Boolean = true
        override suspend fun archiveAccount(id: String) {}
        override suspend fun unarchiveAccount(id: String) {}
        override suspend fun updateBalance(id: String, newBalance: Long) {}
        override fun getAccountBalance(id: String): Flow<Long?> = flowOf(500000L)
    }

    private lateinit var repeatRepo: FakeRepeatRepository
    private lateinit var catRepo: FakeCategoryRepository
    private lateinit var accountRepo: FakeAccountRepository
    private lateinit var viewModel: RepeatExpenseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repeatRepo = FakeRepeatRepository()
        catRepo = FakeCategoryRepository()
        accountRepo = FakeAccountRepository()
        viewModel = RepeatExpenseViewModel(repeatRepo, catRepo, accountRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_loadsExpensesAndCalculatesMonthlyCommitment() = runBlocking {
        repeatRepo.addRepeatExpense(
            RepeatExpense("r1", "Broadband", 100000L, "MONTHLY", 1000L, "Bills & Utilities", true) // ₹1,000/mo
        )
        repeatRepo.addRepeatExpense(
            RepeatExpense("r2", "Streaming Service", 1200000L, "YEARLY", 1000L, "Subscriptions", true) // ₹1,000/mo equivalent
        )
        repeatRepo.addRepeatExpense(
            RepeatExpense("r3", "Gym Membership", 200000L, "MONTHLY", 1000L, "Fitness", false) // Paused -> excluded
        )

        val state = viewModel.uiState.first { it.allExpenses.size == 3 }
        assertEquals(3, state.allExpenses.size)
        assertEquals(2, state.activeExpenses.size)
        assertEquals(1, state.pausedExpenses.size)
        assertEquals(3, state.displayedExpenses.size)

        // Monthly commitment: 100,000 (monthly) + 1,200,000 / 12 (yearly) = 200,000 paise
        assertEquals(200000L, state.totalMonthlyCommitmentPaise)
    }

    @Test
    fun selectTab_filtersDisplayedExpenses() = runBlocking {
        repeatRepo.addRepeatExpense(RepeatExpense("r1", "Active Item", 5000L, "DAILY", 1000L, "Bills", true))
        repeatRepo.addRepeatExpense(RepeatExpense("r2", "Paused Item", 5000L, "DAILY", 1000L, "Bills", false))

        viewModel.uiState.first { it.allExpenses.size == 2 }

        viewModel.selectTab(RepeatFilterTab.ACTIVE)
        val activeState = viewModel.uiState.value
        assertEquals(RepeatFilterTab.ACTIVE, activeState.selectedTab)
        assertEquals(1, activeState.displayedExpenses.size)
        assertEquals("Active Item", activeState.displayedExpenses[0].description)

        viewModel.selectTab(RepeatFilterTab.PAUSED)
        val pausedState = viewModel.uiState.value
        assertEquals(RepeatFilterTab.PAUSED, pausedState.selectedTab)
        assertEquals(1, pausedState.displayedExpenses.size)
        assertEquals("Paused Item", pausedState.displayedExpenses[0].description)

        viewModel.selectTab(RepeatFilterTab.ALL)
        assertEquals(2, viewModel.uiState.value.displayedExpenses.size)
    }

    @Test
    fun createRepeatExpense_success_persistsAndResets() = runBlocking {
        viewModel.onAddClick()
        assertTrue(viewModel.uiState.value.isAddDialogOpen)

        var success = false
        viewModel.createRepeatExpense(
            description = "Milk Delivery",
            amountPaise = 6000L,
            frequency = "DAILY",
            category = "Groceries",
            isActive = true,
            onSuccess = { success = true }
        )

        assertTrue(success)
        val state = viewModel.uiState.first { it.allExpenses.isNotEmpty() }
        assertFalse(state.isAddDialogOpen)
        assertNull(state.errorMessage)
        assertEquals(1, state.allExpenses.size)
        assertEquals("Milk Delivery", state.allExpenses[0].description)
        assertEquals("DAILY", state.allExpenses[0].frequency)
        assertTrue(state.allExpenses[0].isActive)
        // Daily: 6000 * 30 = 180,000
        assertEquals(180000L, state.totalMonthlyCommitmentPaise)
    }

    @Test
    fun createRepeatExpense_validationErrors() = runBlocking {
        // Blank description
        var errorMsg: String? = null
        viewModel.createRepeatExpense(
            description = "   ",
            amountPaise = 1000L,
            frequency = "MONTHLY",
            category = "Bills",
            isActive = true,
            onError = { errorMsg = it }
        )
        assertNotNull(errorMsg)
        assertTrue(errorMsg!!.contains("Description"))

        // Zero amount
        errorMsg = null
        viewModel.createRepeatExpense(
            description = "Test",
            amountPaise = 0L,
            frequency = "MONTHLY",
            category = "Bills",
            isActive = true,
            onError = { errorMsg = it }
        )
        assertNotNull(errorMsg)
        assertTrue(errorMsg!!.contains("greater than zero"))

        // Invalid frequency
        errorMsg = null
        viewModel.createRepeatExpense(
            description = "Test",
            amountPaise = 1000L,
            frequency = "HOURLY",
            category = "Bills",
            isActive = true,
            onError = { errorMsg = it }
        )
        assertNotNull(errorMsg)
        assertTrue(errorMsg!!.contains("Frequency"))
    }

    @Test
    fun toggleActive_switchesStateAndUpdatesCommitment() = runBlocking {
        repeatRepo.addRepeatExpense(
            RepeatExpense("r_toggle", "Cloud Storage", 50000L, "MONTHLY", 1000L, "Tech", true)
        )

        val initialState = viewModel.uiState.first { it.allExpenses.isNotEmpty() }
        assertEquals(50000L, initialState.totalMonthlyCommitmentPaise)

        // Toggle to paused
        viewModel.toggleActive("r_toggle")
        val pausedState = viewModel.uiState.first { it.pausedExpenses.isNotEmpty() }
        assertFalse(pausedState.allExpenses[0].isActive)
        assertEquals(0L, pausedState.totalMonthlyCommitmentPaise)

        // Toggle back to active
        viewModel.toggleActive("r_toggle")
        val activeAgainState = viewModel.uiState.first { it.activeExpenses.isNotEmpty() }
        assertTrue(activeAgainState.allExpenses[0].isActive)
        assertEquals(50000L, activeAgainState.totalMonthlyCommitmentPaise)
    }

    @Test
    fun deleteRepeatExpense_flow() = runBlocking {
        repeatRepo.addRepeatExpense(
            RepeatExpense("r_del", "Obsolete Service", 20000L, "MONTHLY", 1000L, "Other", true)
        )

        val item = viewModel.uiState.first { it.allExpenses.isNotEmpty() }.allExpenses[0]
        viewModel.onDeleteClick(item)
        assertEquals(item.id, viewModel.uiState.value.expenseToDelete?.id)

        viewModel.confirmDelete()
        val afterDelete = viewModel.uiState.first { it.allExpenses.isEmpty() }
        assertEquals(0, afterDelete.allExpenses.size)
        assertNull(afterDelete.expenseToDelete)
    }

    @Test
    fun evaluateDueOccurrences_generatesTransactionsAndNotifies() = runBlocking {
        repeatRepo.dueOccurrencesToGenerate = 3

        var generatedCount = -1
        viewModel.evaluateDueOccurrences { count -> generatedCount = count }

        assertEquals(3, generatedCount)
        val state = viewModel.uiState.first { it.successMessage != null }
        assertTrue(state.successMessage!!.contains("3"))
    }
}
