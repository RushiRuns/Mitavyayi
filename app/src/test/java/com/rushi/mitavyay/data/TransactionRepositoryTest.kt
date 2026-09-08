package com.rushi.mitavyay.data

import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.TransactionRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class TransactionRepositoryTest {

    private class FakeTransactionDao : TransactionDao {
        val list = mutableListOf<Transaction>()

        override suspend fun insert(transaction: Transaction) {
            list.removeAll { it.id == transaction.id }
            list.add(transaction)
        }

        override suspend fun insertAll(transactions: List<Transaction>) {
            transactions.forEach { insert(it) }
        }

        override suspend fun update(transaction: Transaction) {
            insert(transaction)
        }

        override suspend fun delete(transaction: Transaction) {
            list.removeAll { it.id == transaction.id }
        }

        override suspend fun deleteById(id: String) {
            list.removeAll { it.id == id }
        }

        override suspend fun getById(id: String): Transaction? = list.find { it.id == id }

        override fun getAll() = flowOf(list.sortedByDescending { it.timestamp })

        override fun getByAccount(accountId: String) =
            flowOf(list.filter { it.accountId == accountId }.sortedByDescending { it.timestamp })

        override fun getByDateRange(start: Long, end: Long) =
            flowOf(list.filter { it.timestamp in start..end }.sortedByDescending { it.timestamp })

        override fun getByCategory(category: String) =
            flowOf(list.filter { it.category == category }.sortedByDescending { it.timestamp })

        override suspend fun getByTransferId(transferId: String) =
            list.filter { it.transferId == transferId }

        override suspend fun deleteByTransferId(transferId: String) {
            list.removeAll { it.transferId == transferId }
        }

        override fun getBalanceForAccount(accountId: String) =
            flowOf(list.filter { it.accountId == accountId }.map { it.amount }.sum().takeIf { list.isNotEmpty() })

        override fun getTotalExpensesByDateRange(start: Long, end: Long) =
            flowOf(list.filter { it.timestamp in start..end && it.amount < 0 }.map { it.amount }.sum())

        override fun getTotalIncomeByDateRange(start: Long, end: Long) =
            flowOf(list.filter { it.timestamp in start..end && it.amount > 0 }.map { it.amount }.sum())

        override fun getTransactionCount() = flowOf(list.size)
    }

    @Test
    fun transaction_storesMonetaryValuesAsLongPaise() = runBlocking {
        val fakeDao = FakeTransactionDao()
        val repository = TransactionRepositoryImpl(fakeDao)

        val largeAmountPaise = 10_000_000_50L // ₹1,00,00,000.50 (1 crore amount)
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            accountId = "acc_main",
            amount = largeAmountPaise,
            description = "Investment Dividend",
            timestamp = 1725792000000L,
            category = "Investment"
        )

        repository.addTransaction(transaction)

        val retrieved = repository.getTransactionById(transaction.id)
        assertEquals(largeAmountPaise, retrieved?.amount)

        // Verify conversion to UI model maintains exact paise formatting without float drift
        val displayItem = retrieved!!.toDisplayItem()
        assertEquals("₹1,00,00,000.50", displayItem.amountFormatted)
        assertTrue(displayItem.isCredit)
    }

    @Test
    fun transaction_debitMapsCorrectly() = runBlocking {
        val fakeDao = FakeTransactionDao()
        val repository = TransactionRepositoryImpl(fakeDao)

        val expensePaise = -1550L // -₹15.50
        val transaction = Transaction(
            id = "tx_coffee",
            accountId = "acc_cash",
            amount = expensePaise,
            description = "Coffee",
            timestamp = 1725792000000L,
            category = "Food & Dining"
        )

        repository.addTransaction(transaction)

        val displayItem = repository.getTransactionById("tx_coffee")!!.toDisplayItem()
        assertEquals("-₹15.50", displayItem.amountFormatted)
        assertFalse(displayItem.isCredit)
    }

    @Test
    fun transaction_dateRangeFiltering() = runBlocking {
        val fakeDao = FakeTransactionDao()
        val repository = TransactionRepositoryImpl(fakeDao)

        repository.addTransaction(
            Transaction("1", "acc_1", -100L, "Day 1", 1000L, "Food")
        )
        repository.addTransaction(
            Transaction("2", "acc_1", -200L, "Day 2", 2000L, "Food")
        )
        repository.addTransaction(
            Transaction("3", "acc_1", -300L, "Day 3", 3000L, "Food")
        )

        val inRange = repository.getTransactionsByDateRange(1500L, 2500L).first()
        assertEquals(1, inRange.size)
        assertEquals("2", inRange[0].id)
    }
}
