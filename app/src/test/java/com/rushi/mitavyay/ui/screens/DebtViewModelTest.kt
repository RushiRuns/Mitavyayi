package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Debt
import com.rushi.mitavyay.data.repository.DebtRepository
import com.rushi.mitavyay.ui.screens.Debt.DebtTab
import com.rushi.mitavyay.ui.screens.Debt.DebtViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
class DebtViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeDebtRepository : DebtRepository {
        val debts = mutableListOf<Debt>()
        val debtsFlow = MutableStateFlow<List<Debt>>(emptyList())

        fun notifyChange() {
            debtsFlow.value = debts.toList()
        }

        override fun getAllDebts(): Flow<List<Debt>> = debtsFlow

        override fun getActiveDebts(): Flow<List<Debt>> = MutableStateFlow(
            debts.filter { it.settledAt == null }
        )

        override fun getSettledDebts(): Flow<List<Debt>> = MutableStateFlow(
            debts.filter { it.settledAt != null }
        )

        override suspend fun getDebtById(id: String): Debt? = debts.find { it.id == id }

        override suspend fun addDebt(debt: Debt) {
            debts.add(debt)
            notifyChange()
        }

        override suspend fun updateDebt(debt: Debt) {
            val idx = debts.indexOfFirst { it.id == debt.id }
            if (idx != -1) {
                debts[idx] = debt
                notifyChange()
            }
        }

        override suspend fun markDebtAsSettled(id: String, settledAt: Long) {
            val idx = debts.indexOfFirst { it.id == id }
            if (idx != -1) {
                debts[idx] = debts[idx].copy(settledAt = settledAt)
                notifyChange()
            }
        }
    }

    private lateinit var fakeRepository: FakeDebtRepository
    private lateinit var viewModel: DebtViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeDebtRepository()
        viewModel = DebtViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_separatesActiveAndSettledDebts_andCalculatesTotals() = runBlocking {
        val debt1 = Debt(
            id = "d1",
            type = "lent",
            counterparty = "Alice",
            amount = 50000L, // 500.00
            createdAt = 1000L,
            settledAt = null
        )
        val debt2 = Debt(
            id = "d2",
            type = "borrowed",
            counterparty = "Bob",
            amount = 30000L, // 300.00
            createdAt = 1000L,
            settledAt = null
        )
        val debt3 = Debt(
            id = "d3",
            type = "lent",
            counterparty = "Charlie",
            amount = 20000L,
            createdAt = 1000L,
            settledAt = 2000L
        )

        fakeRepository.addDebt(debt1)
        fakeRepository.addDebt(debt2)
        fakeRepository.addDebt(debt3)

        val state = viewModel.uiState.first { it.activeDebts.size == 2 }
        assertFalse(state.isLoading)
        assertEquals(2, state.activeDebts.size)
        assertEquals(1, state.settledDebts.size)
        assertEquals(50000L, state.totalLentActivePaise)
        assertEquals(30000L, state.totalBorrowedActivePaise)
        assertEquals("Charlie", state.settledDebts[0].counterparty)
        assertTrue(state.settledDebts[0].isSettled)
    }

    @Test
    fun selectTab_updatesSelectedTab() = runBlocking {
        assertEquals(DebtTab.ACTIVE, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(DebtTab.SETTLED)
        assertEquals(DebtTab.SETTLED, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(DebtTab.ACTIVE)
        assertEquals(DebtTab.ACTIVE, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun onAddDebtClick_andDismissDialogs() = runBlocking {
        assertFalse(viewModel.uiState.value.isAddDebtOpen)

        viewModel.onAddDebtClick()
        assertTrue(viewModel.uiState.value.isAddDebtOpen)

        viewModel.dismissDialogs()
        assertFalse(viewModel.uiState.value.isAddDebtOpen)
    }

    @Test
    fun createDebt_validLentDebt_persistsAndCalculates() = runBlocking {
        var successCalled = false
        viewModel.onAddDebtClick()

        viewModel.createDebt(
            type = "lent",
            counterparty = "David",
            amountPaise = 150000L,
            notes = "Dinner split",
            onSuccess = { successCalled = true }
        )

        assertTrue(successCalled)
        val state = viewModel.uiState.first { it.activeDebts.isNotEmpty() }
        assertFalse(state.isAddDebtOpen)
        assertNull(state.errorMessage)
        assertEquals(1, state.activeDebts.size)
        assertEquals("David", state.activeDebts[0].counterparty)
        assertEquals(150000L, state.activeDebts[0].amountPaise)
        assertTrue(state.activeDebts[0].isLent)
        assertEquals("Dinner split", state.activeDebts[0].notes)
        assertEquals(150000L, state.totalLentActivePaise)
        assertEquals(0L, state.totalBorrowedActivePaise)
    }

    @Test
    fun createDebt_validBorrowedDebt_persistsAndCalculates() = runBlocking {
        viewModel.createDebt(
            type = "borrowed",
            counterparty = "Elena",
            amountPaise = 75000L,
            notes = null
        )

        val state = viewModel.uiState.first { it.activeDebts.isNotEmpty() }
        assertEquals(1, state.activeDebts.size)
        assertEquals("Elena", state.activeDebts[0].counterparty)
        assertFalse(state.activeDebts[0].isLent)
        assertEquals(75000L, state.totalBorrowedActivePaise)
        assertEquals(0L, state.totalLentActivePaise)
    }

    @Test
    fun createDebt_validationErrors() = runBlocking {
        // Blank counterparty
        var errorOccurred: String? = null
        viewModel.createDebt(
            type = "lent",
            counterparty = "   ",
            amountPaise = 1000L,
            notes = null,
            onError = { errorOccurred = it }
        )
        assertNotNull(errorOccurred)
        assertTrue(errorOccurred!!.contains("Counterparty"))
        assertEquals(errorOccurred, viewModel.uiState.value.errorMessage)

        // Zero or negative amount
        errorOccurred = null
        viewModel.createDebt(
            type = "lent",
            counterparty = "Frank",
            amountPaise = 0L,
            notes = null,
            onError = { errorOccurred = it }
        )
        assertNotNull(errorOccurred)
        assertTrue(errorOccurred!!.contains("greater than zero"))

        // Invalid type
        errorOccurred = null
        viewModel.createDebt(
            type = "invalid_type",
            counterparty = "Frank",
            amountPaise = 5000L,
            notes = null,
            onError = { errorOccurred = it }
        )
        assertNotNull(errorOccurred)
        assertTrue(errorOccurred!!.contains("lent") && errorOccurred!!.contains("borrowed"))
    }

    @Test
    fun settleDebt_marksDebtSettledAndUpdatesListsAndTotals() = runBlocking {
        fakeRepository.addDebt(
            Debt(
                id = "d_settle",
                type = "lent",
                counterparty = "Grace",
                amount = 80000L,
                createdAt = 500L,
                settledAt = null
            )
        )

        val activeState = viewModel.uiState.first { it.activeDebts.isNotEmpty() }
        val itemToSettle = activeState.activeDebts[0]
        assertEquals(80000L, activeState.totalLentActivePaise)

        viewModel.onSettleClick(itemToSettle)
        assertEquals(itemToSettle.id, viewModel.uiState.value.debtToSettle?.id)

        var settledSuccess = false
        viewModel.confirmSettleDebt(onSuccess = { settledSuccess = true })

        assertTrue(settledSuccess)
        val settledState = viewModel.uiState.first { it.settledDebts.isNotEmpty() }
        assertEquals(0, settledState.activeDebts.size)
        assertEquals(1, settledState.settledDebts.size)
        assertEquals(0L, settledState.totalLentActivePaise)
        assertNull(settledState.debtToSettle)
        assertTrue(settledState.settledDebts[0].isSettled)
    }

    @Test
    fun verifyNoDeleteOperationExposed() {
        // Enforce ADR-007: Debt records are immutable and never deleted.
        // Confirm via reflection that neither DebtViewModel nor DebtRepository has delete methods.
        val vmMethods = DebtViewModel::class.java.declaredMethods.map { it.name.lowercase() }
        val repoMethods = DebtRepository::class.java.declaredMethods.map { it.name.lowercase() }

        assertFalse(
            "DebtViewModel must never expose delete operations per ADR-007",
            vmMethods.any { it.contains("delete") || it.contains("remove") }
        )
        assertFalse(
            "DebtRepository must never expose delete operations per ADR-007",
            repoMethods.any { it.contains("delete") || it.contains("remove") }
        )
    }
}
