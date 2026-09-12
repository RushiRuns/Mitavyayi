package com.rushi.mitavyay.data

import com.rushi.mitavyay.data.db.DatabaseTransactionRunner
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import com.rushi.mitavyay.data.repository.TransferRepositoryImpl
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TransferRepositoryTest {

    private class FakeTransactionRunner : DatabaseTransactionRunner {
        override suspend fun <R> invoke(block: suspend () -> R): R = block()
    }

    private class FakeTransactionDao : TransactionDao {
        val list = mutableListOf<Transaction>()

        override suspend fun insert(transaction: Transaction) {
            list.add(transaction)
        }

        override suspend fun insertAll(transactions: List<Transaction>) {
            list.addAll(transactions)
        }

        override suspend fun update(transaction: Transaction) {}
        override suspend fun delete(transaction: Transaction) {}
        override suspend fun deleteById(id: String) {}
        override suspend fun getById(id: String): Transaction? = null
        override fun getAll() = flowOf(emptyList<Transaction>())
        override fun getByAccount(accountId: String) = flowOf(emptyList<Transaction>())
        override fun getByDateRange(start: Long, end: Long) = flowOf(emptyList<Transaction>())
        override fun getByCategory(category: String) = flowOf(emptyList<Transaction>())

        override suspend fun getByTransferId(transferId: String): List<Transaction> =
            list.filter { it.transferId == transferId }

        override suspend fun deleteByTransferId(transferId: String) {
            list.removeAll { it.transferId == transferId }
        }

        override fun getBalanceForAccount(accountId: String) = flowOf(null)
        override fun getTotalExpensesByDateRange(start: Long, end: Long) = flowOf(null)
        override fun getTotalIncomeByDateRange(start: Long, end: Long) = flowOf(null)
        override fun getTransactionCount() = flowOf(0)
        override suspend fun getTransactionCountForAccount(accountId: String) =
            list.count { it.accountId == accountId }
    }

    @Test
    fun createTransfer_generatesDualLinkedTransactions() = runBlocking {
        val fakeDao = FakeTransactionDao()
        val runner = FakeTransactionRunner()
        val repository = TransferRepositoryImpl(runner, fakeDao)

        val transferAmountPaise = 500000L // ₹5,000.00
        val transferId = repository.createTransfer(
            fromAccountId = "acc_bank",
            toAccountId = "acc_cash",
            amountPaise = transferAmountPaise,
            timestamp = 1725792000000L,
            notes = "ATM withdrawal"
        )

        assertNotNull(transferId)
        assertEquals(2, fakeDao.list.size)

        val debit = fakeDao.list.find { it.accountId == "acc_bank" }
        val credit = fakeDao.list.find { it.accountId == "acc_cash" }

        assertNotNull(debit)
        assertNotNull(credit)

        assertEquals(-transferAmountPaise, debit!!.amount)
        assertEquals(transferAmountPaise, credit!!.amount)
        assertEquals(transferId, debit.transferId)
        assertEquals(transferId, credit.transferId)

        // Test deleteTransfer removes both records atomically
        repository.deleteTransfer(transferId)
        assertEquals(0, fakeDao.list.size)
    }
}
