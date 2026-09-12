package com.rushi.mitavyay.data

import com.rushi.mitavyay.data.db.Budget
import com.rushi.mitavyay.data.db.BudgetDao
import com.rushi.mitavyay.data.db.DatabaseTransactionRunner
import com.rushi.mitavyay.data.repository.BudgetRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class BudgetRepositoryTest {

    private lateinit var fakeDao: FakeBudgetDao
    private lateinit var fakeRunner: FakeDatabaseTransactionRunner
    private lateinit var repository: BudgetRepositoryImpl

    private class FakeDatabaseTransactionRunner : DatabaseTransactionRunner {
        override suspend operator fun <R> invoke(block: suspend () -> R): R = block()
    }

    private class FakeBudgetDao : BudgetDao {
        val list = mutableListOf<Budget>()

        override suspend fun insert(budget: Budget) {
            list.removeAll { it.id == budget.id || (it.category == budget.category && it.monthYear == budget.monthYear) }
            list.add(budget)
        }

        override suspend fun insertAll(budgets: List<Budget>) {
            budgets.forEach { insert(it) }
        }

        override suspend fun update(budget: Budget) {
            val idx = list.indexOfFirst { it.id == budget.id }
            if (idx >= 0) {
                list[idx] = budget
            } else {
                list.add(budget)
            }
        }

        override suspend fun delete(budget: Budget) {
            list.removeAll { it.id == budget.id }
        }

        override suspend fun deleteById(id: String) {
            list.removeAll { it.id == id }
        }

        override suspend fun getById(id: String): Budget? =
            list.find { it.id == id }

        override fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>> =
            flowOf(list.filter { it.monthYear == monthYear && it.isActive }.sortedByDescending { it.amount })

        override fun getAll(): Flow<List<Budget>> =
            flowOf(list.sortedByDescending { it.amount })

        override suspend fun getByCategoryAndMonth(category: String, monthYear: String): Budget? =
            list.find { it.category == category && it.monthYear == monthYear }

        override fun getByCategoryAndMonthFlow(category: String, monthYear: String): Flow<Budget?> =
            flowOf(list.find { it.category == category && it.monthYear == monthYear })
    }

    @Before
    fun setUp() {
        fakeDao = FakeBudgetDao()
        fakeRunner = FakeDatabaseTransactionRunner()
        repository = BudgetRepositoryImpl(fakeDao, fakeRunner)
    }

    @Test
    fun setBudget_createsNewBudgetWhenNoneExists() = runBlocking {
        val budget = repository.setBudget("Food & Dining", "2026-09", 1000000L) // ₹10,000.00
        assertNotNull(budget.id)
        assertEquals("Food & Dining", budget.category)
        assertEquals("2026-09", budget.monthYear)
        assertEquals(1000000L, budget.amount)

        val retrieved = repository.getBudgetById(budget.id)
        assertEquals(1000000L, retrieved?.amount)
    }

    @Test
    fun setBudget_updatesExistingBudgetForSameCategoryAndMonth() = runBlocking {
        val initial = repository.setBudget("Transport", "2026-09", 300000L) // ₹3,000.00
        val updated = repository.setBudget("Transport", "2026-09", 500000L) // ₹5,000.00

        assertEquals(initial.id, updated.id)
        assertEquals(500000L, updated.amount)

        val monthBudgets = repository.getBudgetsForMonth("2026-09").first()
        assertEquals(1, monthBudgets.size)
        assertEquals(500000L, monthBudgets[0].amount)
    }

    @Test
    fun getBudgetsForMonth_filtersByMonth() = runBlocking {
        repository.setBudget("Food", "2026-08", 800000L)
        repository.setBudget("Food", "2026-09", 900000L)
        repository.setBudget("Transport", "2026-09", 400000L)

        val augBudgets = repository.getBudgetsForMonth("2026-08").first()
        assertEquals(1, augBudgets.size)
        assertEquals("Food", augBudgets[0].category)

        val sepBudgets = repository.getBudgetsForMonth("2026-09").first()
        assertEquals(2, sepBudgets.size)
    }

    @Test
    fun deleteBudget_removesBudget() = runBlocking {
        val budget = repository.setBudget("Entertainment", "2026-09", 200000L)
        assertEquals(1, repository.getBudgetsForMonth("2026-09").first().size)

        repository.deleteBudget(budget.id)
        assertEquals(0, repository.getBudgetsForMonth("2026-09").first().size)
        assertNull(repository.getBudgetById(budget.id))
    }

    @Test
    fun copyBudgetsToMonth_copiesNonExistingBudgetsToTargetMonth() = runBlocking {
        repository.setBudget("Food", "2026-08", 800000L)
        repository.setBudget("Bills", "2026-08", 500000L)

        // September already has a custom "Food" budget
        repository.setBudget("Food", "2026-09", 1000000L)

        val copiedCount = repository.copyBudgetsToMonth("2026-08", "2026-09")
        // Only "Bills" should be copied
        assertEquals(1, copiedCount)

        val sepBudgets = repository.getBudgetsForMonth("2026-09").first()
        assertEquals(2, sepBudgets.size)
        // Food budget remains at ₹10,000.00
        val foodBudget = sepBudgets.find { it.category == "Food" }
        assertEquals(1000000L, foodBudget?.amount)
        // Bills budget copied at ₹5,000.00
        val billsBudget = sepBudgets.find { it.category == "Bills" }
        assertEquals(500000L, billsBudget?.amount)
    }
}
