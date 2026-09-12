package com.rushi.mitavyay.data

import com.rushi.mitavyay.data.db.Debt
import com.rushi.mitavyay.data.db.DebtDao
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.DebtRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebtRepositoryTest {

    private class FakeDebtDao : DebtDao {
        val list = mutableListOf<Debt>()
        override suspend fun insert(debt: Debt) { list.add(debt) }
        override suspend fun update(debt: Debt) {
            val idx = list.indexOfFirst { it.id == debt.id }
            if (idx != -1) list[idx] = debt
        }
        override suspend fun getById(id: String): Debt? = list.find { it.id == id }
        override fun getByIdFlow(id: String): Flow<Debt?> = flowOf(list.find { it.id == id })
        override fun getAll(): Flow<List<Debt>> = flowOf(list)
        override fun getActiveDebts(): Flow<List<Debt>> = flowOf(list.filter { it.settledAt == null })
        override fun getSettledDebts(): Flow<List<Debt>> = flowOf(list.filter { it.settledAt != null })
        override suspend fun markSettled(id: String, settledAt: Long) {
            val idx = list.indexOfFirst { it.id == id }
            if (idx != -1) {
                list[idx] = list[idx].copy(settledAt = settledAt)
            }
        }
    }

    @Test
    fun debt_lifecycleAndImmutability() = runBlocking {
        val fakeDao = FakeDebtDao()
        val repository = DebtRepositoryImpl(fakeDao)

        val debt = Debt(
            id = "debt_1",
            type = "lent",
            counterparty = "Rahul",
            amount = 200000L, // ₹2,000.00
            createdAt = 1725792000000L
        )
        repository.addDebt(debt)

        val activeList = repository.getActiveDebts().first()
        assertEquals(1, activeList.size)
        assertFalse(activeList[0].toDisplayItem().isSettled)

        // Mark as settled
        val settleTime = 1725878400000L
        repository.markDebtAsSettled("debt_1", settleTime)

        val afterSettleActive = repository.getActiveDebts().first()
        val afterSettleSettled = repository.getSettledDebts().first()

        assertEquals(0, afterSettleActive.size)
        assertEquals(1, afterSettleSettled.size)
        assertTrue(afterSettleSettled[0].toDisplayItem().isSettled)
    }

    @Test
    fun debt_getAllAndGetByIdAndUpdate() = runBlocking {
        val fakeDao = FakeDebtDao()
        val repository = DebtRepositoryImpl(fakeDao)

        val debt = Debt(
            id = "debt_2",
            type = "borrowed",
            counterparty = "Vikram",
            amount = 100000L,
            createdAt = 1725800000000L
        )
        repository.addDebt(debt)

        val retrieved = repository.getDebtById("debt_2")
        assertEquals("Vikram", retrieved?.counterparty)
        assertEquals(100000L, retrieved?.amount)

        val allDebts = repository.getAllDebts().first()
        assertEquals(1, allDebts.size)

        // Update notes
        val updated = debt.copy(notes = "Partially discussed")
        repository.updateDebt(updated)

        val retrievedAfter = repository.getDebtById("debt_2")
        assertEquals("Partially discussed", retrievedAfter?.notes)
    }
}
