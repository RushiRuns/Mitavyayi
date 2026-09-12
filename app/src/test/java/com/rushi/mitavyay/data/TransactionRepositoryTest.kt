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

        override fun search(query: String) =
            flowOf(list.filter {
                it.description.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                (it.notes != null && it.notes.contains(query, ignoreCase = true))
            }.sortedByDescending { it.timestamp })

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

        override suspend fun getTransactionCountForAccount(accountId: String) =
            list.count { it.accountId == accountId }

        override fun getCategoryExpensesByDateRange(start: Long, end: Long) =
            flowOf(
                list.filter { it.timestamp in start..end && it.amount < 0 }
                    .groupBy { it.category }
                    .map { (cat, txs) ->
                        com.rushi.mitavyay.data.db.CategorySpendingRaw(
                            category = cat,
                            totalExpensePaise = txs.sumOf { kotlin.math.abs(it.amount) },
                            transactionCount = txs.size
                        )
                    }
                    .sortedByDescending { it.totalExpensePaise }
            )
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

    private class FakeAccountDao : com.rushi.mitavyay.data.db.AccountDao {
        val map = mutableMapOf<String, com.rushi.mitavyay.data.db.Account>()

        override suspend fun insert(account: com.rushi.mitavyay.data.db.Account) { map[account.id] = account }
        override suspend fun insertAll(accounts: List<com.rushi.mitavyay.data.db.Account>) { accounts.forEach { insert(it) } }
        override suspend fun update(account: com.rushi.mitavyay.data.db.Account) { map[account.id] = account }
        override suspend fun delete(account: com.rushi.mitavyay.data.db.Account) { map.remove(account.id) }
        override suspend fun deleteById(id: String) { map.remove(id) }
        override suspend fun getById(id: String): com.rushi.mitavyay.data.db.Account? = map[id]
        override fun getByIdFlow(id: String) = flowOf(map[id])
        override fun getAll() = flowOf(map.values.toList())
        override fun getActiveAccounts() = flowOf(map.values.filter { it.isActive })
        override suspend fun updateBalance(id: String, newBalance: Long) {
            val existing = map[id] ?: return
            map[id] = existing.copy(balance = newBalance)
        }
        override fun getAccountBalance(id: String) = flowOf(map[id]?.balance)
        override suspend fun updateActiveStatus(id: String, isActive: Boolean) {
            val existing = map[id] ?: return
            map[id] = existing.copy(isActive = isActive)
        }
    }

    @Test
    fun transaction_addTransactionUpdatesAccountBalance() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val fakeAccountDao = FakeAccountDao()
        val repository = TransactionRepositoryImpl(fakeTxDao, fakeAccountDao)

        val account = com.rushi.mitavyay.data.db.Account(
            id = "acc_test",
            name = "Test Account",
            type = "bank",
            balance = 10000L, // ₹100.00
            currency = "INR",
            createdAt = 1000L,
            isActive = true
        )
        fakeAccountDao.insert(account)

        // Add expense transaction (-₹30.00)
        repository.addTransaction(
            Transaction("tx_exp", "acc_test", -3000L, "Coffee", 2000L, "Food")
        )

        assertEquals(7000L, fakeAccountDao.getById("acc_test")?.balance)

        // Add income transaction (+₹50.00)
        repository.addTransaction(
            Transaction("tx_inc", "acc_test", 5000L, "Refund", 3000L, "Other")
        )

        assertEquals(12000L, fakeAccountDao.getById("acc_test")?.balance)
    }

    @Test
    fun transaction_addTransactionsBatchUpdatesAccountBalances() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val fakeAccountDao = FakeAccountDao()
        val repository = TransactionRepositoryImpl(fakeTxDao, fakeAccountDao)

        fakeAccountDao.insert(
            com.rushi.mitavyay.data.db.Account(
                id = "acc_1",
                name = "Bank",
                type = "bank",
                balance = 50000L, // ₹500
                currency = "INR",
                createdAt = 1000L,
                isActive = true
            )
        )
        fakeAccountDao.insert(
            com.rushi.mitavyay.data.db.Account(
                id = "acc_2",
                name = "Cash",
                type = "cash",
                balance = 20000L, // ₹200
                currency = "INR",
                createdAt = 1000L,
                isActive = true
            )
        )

        val batch = listOf(
            Transaction("tx_1", "acc_1", -5000L, "Groceries", 2000L, "Food"),
            Transaction("tx_2", "acc_1", -3000L, "Fuel", 2001L, "Transport"),
            Transaction("tx_3", "acc_1", 10000L, "Salary Bonus", 2002L, "Income"),
            Transaction("tx_4", "acc_2", -2000L, "Snacks", 2003L, "Food"),
            Transaction("tx_5", "acc_2", -1000L, "Bus Ticket", 2004L, "Transport")
        )

        repository.addTransactions(batch)

        // All 5 transactions saved
        assertEquals(5, fakeTxDao.list.size)
        // acc_1: 50000 - 5000 - 3000 + 10000 = 52000
        assertEquals(52000L, fakeAccountDao.getById("acc_1")?.balance)
        // acc_2: 20000 - 2000 - 1000 = 17000
        assertEquals(17000L, fakeAccountDao.getById("acc_2")?.balance)
    }
}

