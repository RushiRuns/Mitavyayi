package com.rushi.mitavyay.data

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.AccountDao
import com.rushi.mitavyay.data.db.CategorySpendingRaw
import com.rushi.mitavyay.data.db.DatabaseTransactionRunner
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import com.rushi.mitavyay.data.repository.TransferRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class TransferRepositoryTest {

    private class FakeTransactionRunner : DatabaseTransactionRunner {
        override suspend fun <R> invoke(block: suspend () -> R): R = block()
    }

    private class FakeAccountDao : AccountDao {
        val accounts = mutableMapOf<String, Account>()

        override suspend fun insert(account: Account) {
            accounts[account.id] = account
        }

        override suspend fun insertAll(accounts: List<Account>) {
            accounts.forEach { this.accounts[it.id] = it }
        }

        override suspend fun update(account: Account) {
            accounts[account.id] = account
        }

        override suspend fun delete(account: Account) {
            accounts.remove(account.id)
        }

        override suspend fun deleteById(id: String) {
            accounts.remove(id)
        }

        override suspend fun getById(id: String): Account? = accounts[id]

        override fun getByIdFlow(id: String): Flow<Account?> = flowOf(accounts[id])

        override fun getAll(): Flow<List<Account>> = flowOf(accounts.values.toList())

        override fun getActiveAccounts(): Flow<List<Account>> =
            flowOf(accounts.values.filter { it.isActive })

        override suspend fun updateBalance(id: String, newBalance: Long) {
            accounts[id]?.let { acc ->
                accounts[id] = acc.copy(balance = newBalance)
            }
        }

        override fun getAccountBalance(id: String): Flow<Long?> =
            flowOf(accounts[id]?.balance)

        override suspend fun updateActiveStatus(id: String, isActive: Boolean) {
            accounts[id]?.let { acc ->
                accounts[id] = acc.copy(isActive = isActive)
            }
        }
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
        override fun getCategoryExpensesByDateRange(start: Long, end: Long) =
            flowOf(emptyList<CategorySpendingRaw>())
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
        assertEquals("Transfer", debit.category)
        assertEquals("Transfer", credit.category)

        // Test deleteTransfer removes both records atomically
        repository.deleteTransfer(transferId)
        assertEquals(0, fakeDao.list.size)
    }

    @Test
    fun createTransfer_updatesAccountBalances() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val fakeAccountDao = FakeAccountDao()
        val runner = FakeTransactionRunner()
        val repository = TransferRepositoryImpl(runner, fakeTxDao, fakeAccountDao)

        // Source account: ₹10,000.00 (1,000,000 paise)
        fakeAccountDao.insert(
            Account(id = "acc_bank", name = "HDFC Bank", type = "bank", balance = 1000000L, createdAt = 1000L)
        )
        // Destination account: ₹2,000.00 (200,000 paise)
        fakeAccountDao.insert(
            Account(id = "acc_cash", name = "Cash Wallet", type = "cash", balance = 200000L, createdAt = 1000L)
        )

        val transferAmountPaise = 300000L // ₹3,000.00
        val transferId = repository.createTransfer(
            fromAccountId = "acc_bank",
            toAccountId = "acc_cash",
            amountPaise = transferAmountPaise
        )

        assertNotNull(transferId)
        // Source should have 1,000,000 - 300,000 = 700,000 paise
        assertEquals(700000L, fakeAccountDao.getById("acc_bank")!!.balance)
        // Destination should have 200,000 + 300,000 = 500,000 paise
        assertEquals(500000L, fakeAccountDao.getById("acc_cash")!!.balance)

        // Deleting transfer reverts balances and deletes both transactions
        repository.deleteTransfer(transferId)
        assertEquals(1000000L, fakeAccountDao.getById("acc_bank")!!.balance)
        assertEquals(200000L, fakeAccountDao.getById("acc_cash")!!.balance)
        assertEquals(0, fakeTxDao.list.size)
    }

    @Test
    fun createTransfer_rejectsIdenticalAccounts() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val runner = FakeTransactionRunner()
        val repository = TransferRepositoryImpl(runner, fakeTxDao)

        try {
            repository.createTransfer(
                fromAccountId = "acc_bank",
                toAccountId = "acc_bank",
                amountPaise = 100000L
            )
            fail("Expected IllegalArgumentException when source and destination are identical")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("same account", ignoreCase = true) == true)
        }
    }

    @Test
    fun createTransfer_rejectsZeroOrNegativeAmount() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val runner = FakeTransactionRunner()
        val repository = TransferRepositoryImpl(runner, fakeTxDao)

        try {
            repository.createTransfer(
                fromAccountId = "acc_bank",
                toAccountId = "acc_cash",
                amountPaise = 0L
            )
            fail("Expected IllegalArgumentException for zero amount")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("greater than zero", ignoreCase = true) == true)
        }

        try {
            repository.createTransfer(
                fromAccountId = "acc_bank",
                toAccountId = "acc_cash",
                amountPaise = -5000L
            )
            fail("Expected IllegalArgumentException for negative amount")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("greater than zero", ignoreCase = true) == true)
        }
    }

    @Test
    fun getTransferTransactions_returnsBothRecords() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val runner = FakeTransactionRunner()
        val repository = TransferRepositoryImpl(runner, fakeTxDao)

        val transferId = repository.createTransfer(
            fromAccountId = "acc_bank",
            toAccountId = "acc_cash",
            amountPaise = 100000L
        )

        val transactions = repository.getTransferTransactions(transferId)
        assertEquals(2, transactions.size)
        assertTrue(transactions.any { it.amount < 0 && it.accountId == "acc_bank" })
        assertTrue(transactions.any { it.amount > 0 && it.accountId == "acc_cash" })
    }
}
