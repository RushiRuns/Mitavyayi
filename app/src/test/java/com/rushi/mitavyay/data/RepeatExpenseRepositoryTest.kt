package com.rushi.mitavyay.data

import com.rushi.mitavyay.data.db.RepeatExpense
import com.rushi.mitavyay.data.db.RepeatExpenseDao
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.RepeatExpenseRepositoryImpl
import com.rushi.mitavyay.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class RepeatExpenseRepositoryTest {

    private class FakeTransactionRepo : TransactionRepository {
        val transactions = mutableListOf<Transaction>()
        override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(transactions)
        override suspend fun getTransactionById(id: String): Transaction? = transactions.find { it.id == id }
        override suspend fun addTransaction(transaction: Transaction) { transactions.add(transaction) }
        override suspend fun updateTransaction(transaction: Transaction) {}
        override suspend fun deleteTransaction(id: String) { transactions.removeAll { it.id == id } }
        override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(null)
        override fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(null)
        override fun getCategorySpending(start: Long, end: Long): Flow<List<com.rushi.mitavyay.data.repository.CategorySpending>> = flowOf(emptyList())
        override fun getTimeSpendingTrend(start: Long, end: Long, period: com.rushi.mitavyay.data.repository.AnalysisPeriod): Flow<List<com.rushi.mitavyay.data.repository.TimeSpendingPoint>> = flowOf(emptyList())
        override fun getAnalysisSummary(start: Long, end: Long): Flow<com.rushi.mitavyay.data.repository.AnalysisSummary> = flowOf(com.rushi.mitavyay.data.repository.AnalysisSummary())
    }

    private class FakeRepeatDao : RepeatExpenseDao {
        val list = mutableListOf<RepeatExpense>()
        override suspend fun insert(repeatExpense: RepeatExpense) { list.add(repeatExpense) }
        override suspend fun update(repeatExpense: RepeatExpense) {
            val idx = list.indexOfFirst { it.id == repeatExpense.id }
            if (idx != -1) list[idx] = repeatExpense
        }
        override suspend fun delete(repeatExpense: RepeatExpense) { list.remove(repeatExpense) }
        override suspend fun deleteById(id: String) { list.removeAll { it.id == id } }
        override suspend fun getById(id: String): RepeatExpense? = list.find { it.id == id }
        override fun getAll(): Flow<List<RepeatExpense>> = flowOf(list)
        override fun getActive(): Flow<List<RepeatExpense>> = flowOf(list.filter { it.isActive })
        override suspend fun updateLastGenerated(id: String, timestamp: Long) {
            val idx = list.indexOfFirst { it.id == id }
            if (idx != -1) {
                list[idx] = list[idx].copy(lastGenerated = timestamp)
            }
        }
    }

    @Test
    fun generateNextOccurrence_lazyEvaluation() = runBlocking {
        val fakeDao = FakeRepeatDao()
        val fakeTxRepo = FakeTransactionRepo()
        val repository = RepeatExpenseRepositoryImpl(fakeDao, fakeTxRepo)

        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sept1 = cal.timeInMillis

        val expense = RepeatExpense(
            id = "rep_wifi",
            description = "Broadband Bill",
            amount = 99900L, // ₹999.00
            frequency = "MONTHLY",
            lastGenerated = sept1,
            category = "Bills & Utilities",
            isActive = true
        )
        fakeDao.insert(expense)

        // 1. Current time is Sept 15 (less than 1 month from Sept 1) -> should NOT generate
        val sept15 = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val tooEarly = repository.generateNextOccurrence("rep_wifi", "acc_bank", sept15)
        assertNull(tooEarly)
        assertEquals(0, fakeTxRepo.transactions.size)

        // 2. Current time is Oct 5 (more than 1 month from Sept 1) -> SHOULD generate
        val oct5 = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 5, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dueTx = repository.generateNextOccurrence("rep_wifi", "acc_bank", oct5)
        assertNotNull(dueTx)
        assertEquals(1, fakeTxRepo.transactions.size)
        assertEquals(-99900L, dueTx!!.amount)
        assertEquals("Broadband Bill", dueTx.description)

        // Verify lastGenerated was updated
        val updatedRepeat = fakeDao.getById("rep_wifi")
        val oct1 = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertEquals(oct1, updatedRepeat?.lastGenerated)
    }

    @Test
    fun toggleActive_updatesStatus() = runBlocking {
        val fakeDao = FakeRepeatDao()
        val fakeTxRepo = FakeTransactionRepo()
        val repository = RepeatExpenseRepositoryImpl(fakeDao, fakeTxRepo)

        val expense = RepeatExpense(
            id = "rep_sub",
            description = "Magazine",
            amount = 50000L,
            frequency = "MONTHLY",
            lastGenerated = 1000L,
            category = "Entertainment",
            isActive = true
        )
        fakeDao.insert(expense)

        val newStatus = repository.toggleActive("rep_sub")
        org.junit.Assert.assertFalse(newStatus)
        org.junit.Assert.assertFalse(fakeDao.getById("rep_sub")!!.isActive)

        val restoredStatus = repository.toggleActive("rep_sub")
        org.junit.Assert.assertTrue(restoredStatus)
        org.junit.Assert.assertTrue(fakeDao.getById("rep_sub")!!.isActive)
    }

    @Test
    fun processAllDueOccurrences_evaluatesAllEligibleActiveExpenses() = runBlocking {
        val fakeDao = FakeRepeatDao()
        val fakeTxRepo = FakeTransactionRepo()
        val repository = RepeatExpenseRepositoryImpl(fakeDao, fakeTxRepo)

        val sept1 = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // 1. Due active monthly expense
        fakeDao.insert(
            RepeatExpense("r1", "Rent", 2000000L, "MONTHLY", sept1, "Housing", true)
        )
        // 2. Due but PAUSED monthly expense -> should NOT generate
        fakeDao.insert(
            RepeatExpense("r2", "Paused Gym", 150000L, "MONTHLY", sept1, "Fitness", false)
        )
        // 3. Due active daily expense
        fakeDao.insert(
            RepeatExpense("r3", "Coffee Pods", 5000L, "DAILY", sept1, "Food", true)
        )

        // Evaluate at Oct 2 (both r1 and r3 are due, r2 is paused)
        val oct2 = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 2, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val generated = repository.processAllDueOccurrences("acc_main", oct2)
        assertEquals(2, generated.size)
        assertEquals(2, fakeTxRepo.transactions.size)
        org.junit.Assert.assertTrue(generated.any { it.description == "Rent" })
        org.junit.Assert.assertTrue(generated.any { it.description == "Coffee Pods" })
        org.junit.Assert.assertFalse(generated.any { it.description == "Paused Gym" })
    }
}
