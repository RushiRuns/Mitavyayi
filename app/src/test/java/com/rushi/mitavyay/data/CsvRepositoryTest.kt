package com.rushi.mitavyay.data

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.AccountDao
import com.rushi.mitavyay.data.db.CategorySpendingRaw
import com.rushi.mitavyay.data.db.DatabaseTransactionRunner
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import com.rushi.mitavyay.data.repository.CsvImportResult
import com.rushi.mitavyay.data.repository.CsvRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

class CsvRepositoryTest {

    private class SnapshottingTransactionRunner(
        private val getTxSnapshot: () -> List<Transaction>,
        private val setTxSnapshot: (List<Transaction>) -> Unit,
        private val getAccSnapshot: () -> Map<String, Account>,
        private val setAccSnapshot: (Map<String, Account>) -> Unit
    ) : DatabaseTransactionRunner {
        override suspend fun <R> invoke(block: suspend () -> R): R {
            val txBak = getTxSnapshot()
            val accBak = getAccSnapshot()
            return try {
                block()
            } catch (e: Throwable) {
                setTxSnapshot(txBak)
                setAccSnapshot(accBak)
                throw e
            }
        }
    }

    private class FakeAccountDao : AccountDao {
        val accounts = mutableMapOf<String, Account>()
        private val flow = MutableStateFlow<List<Account>>(emptyList())

        private fun updateFlow() {
            flow.value = accounts.values.toList()
        }

        override suspend fun insert(account: Account) {
            accounts[account.id] = account
            updateFlow()
        }

        override suspend fun insertAll(accounts: List<Account>) {
            accounts.forEach { this.accounts[it.id] = it }
            updateFlow()
        }

        override suspend fun update(account: Account) {
            accounts[account.id] = account
            updateFlow()
        }

        override suspend fun delete(account: Account) {
            accounts.remove(account.id)
            updateFlow()
        }

        override suspend fun deleteById(id: String) {
            accounts.remove(id)
            updateFlow()
        }

        override suspend fun getById(id: String): Account? = accounts[id]
        override fun getByIdFlow(id: String): Flow<Account?> = flowOf(accounts[id])
        override fun getAll(): Flow<List<Account>> = flow
        override fun getActiveAccounts(): Flow<List<Account>> =
            flowOf(accounts.values.filter { it.isActive })

        override suspend fun updateBalance(id: String, newBalance: Long) {
            accounts[id]?.let { acc ->
                accounts[id] = acc.copy(balance = newBalance)
                updateFlow()
            }
        }

        override fun getAccountBalance(id: String): Flow<Long?> =
            flowOf(accounts[id]?.balance)

        override suspend fun updateActiveStatus(id: String, isActive: Boolean) {
            accounts[id]?.let { acc ->
                accounts[id] = acc.copy(isActive = isActive)
                updateFlow()
            }
        }
    }

    private class FakeTransactionDao : TransactionDao {
        val list = mutableListOf<Transaction>()
        private val flow = MutableStateFlow<List<Transaction>>(emptyList())

        private fun updateFlow() {
            flow.value = list.toList()
        }

        override suspend fun insert(transaction: Transaction) {
            list.add(transaction)
            updateFlow()
        }

        override suspend fun insertAll(transactions: List<Transaction>) {
            list.addAll(transactions)
            updateFlow()
        }

        override suspend fun update(transaction: Transaction) {
            val idx = list.indexOfFirst { it.id == transaction.id }
            if (idx >= 0) {
                list[idx] = transaction
                updateFlow()
            }
        }

        override suspend fun delete(transaction: Transaction) {
            list.remove(transaction)
            updateFlow()
        }

        override suspend fun deleteById(id: String) {
            list.removeAll { it.id == id }
            updateFlow()
        }

        override suspend fun getById(id: String): Transaction? = list.find { it.id == id }
        override fun getAll(): Flow<List<Transaction>> = flow
        override fun getByAccount(accountId: String) = flowOf(emptyList<Transaction>())
        override fun getByDateRange(start: Long, end: Long) = flowOf(emptyList<Transaction>())
        override fun getByCategory(category: String) = flowOf(emptyList<Transaction>())
        override fun search(query: String): Flow<List<Transaction>> =
            flowOf(list.filter {
                it.description.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                (it.notes != null && it.notes.contains(query, ignoreCase = true))
            }.sortedByDescending { it.timestamp })
        override suspend fun getByTransferId(transferId: String): List<Transaction> =
            list.filter { it.transferId == transferId }

        override suspend fun deleteByTransferId(transferId: String) {
            list.removeAll { it.transferId == transferId }
            updateFlow()
        }

        override fun getBalanceForAccount(accountId: String) = flowOf(null)
        override fun getTotalExpensesByDateRange(start: Long, end: Long) = flowOf(null)
        override fun getTotalIncomeByDateRange(start: Long, end: Long) = flowOf(null)
        override fun getTransactionCount() = flowOf(list.size)
        override suspend fun getTransactionCountForAccount(accountId: String) =
            list.count { it.accountId == accountId }
        override fun getCategoryExpensesByDateRange(start: Long, end: Long) =
            flowOf(emptyList<CategorySpendingRaw>())
    }

    private fun createRepository(
        fakeTxDao: FakeTransactionDao,
        fakeAccountDao: FakeAccountDao
    ): CsvRepositoryImpl {
        val runner = SnapshottingTransactionRunner(
            getTxSnapshot = { fakeTxDao.list.toList() },
            setTxSnapshot = {
                fakeTxDao.list.clear()
                fakeTxDao.list.addAll(it)
            },
            getAccSnapshot = { fakeAccountDao.accounts.toMap() },
            setAccSnapshot = {
                fakeAccountDao.accounts.clear()
                fakeAccountDao.accounts.putAll(it)
            }
        )
        return CsvRepositoryImpl(fakeTxDao, fakeAccountDao, runner)
    }

    @Test
    fun exportTransactions_writesValidCsvHeadersAndRows() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val fakeAccountDao = FakeAccountDao()
        val repository = createRepository(fakeTxDao, fakeAccountDao)

        fakeAccountDao.insert(
            Account(id = "acc_bank", name = "HDFC Bank", type = "bank", balance = 500000L, createdAt = 1000L)
        )

        fakeTxDao.insert(
            Transaction(
                id = "tx_1",
                accountId = "acc_bank",
                amount = -45000L, // -₹450.00
                description = "Grocery Store",
                timestamp = 1725792000000L, // 2024-09-08
                category = "Groceries",
                notes = "Weekly grocery"
            )
        )

        fakeTxDao.insert(
            Transaction(
                id = "tx_2",
                accountId = "acc_bank",
                amount = 2500000L, // ₹25,000.00
                description = "Salary",
                timestamp = 1725878400000L, // 2024-09-09
                category = "Salary",
                notes = "Monthly deposit"
            )
        )

        val outStream = ByteArrayOutputStream()
        val result = repository.exportTransactionsToCsv(outStream)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())

        val csvString = outStream.toString(StandardCharsets.UTF_8.name())
        assertTrue(csvString.contains("Date"))
        assertTrue(csvString.contains("Amount"))
        assertTrue(csvString.contains("Category"))
        assertTrue(csvString.contains("Account"))
        assertTrue(csvString.contains("450.00"))
        assertTrue(csvString.contains("EXPENSE"))
        assertTrue(csvString.contains("Groceries"))
        assertTrue(csvString.contains("HDFC Bank"))
        assertTrue(csvString.contains("Grocery Store"))
        assertTrue(csvString.contains("25000.00"))
        assertTrue(csvString.contains("INCOME"))
        assertTrue(csvString.contains("Salary"))
    }

    @Test
    fun importTransactions_validCsv_importsAllAndReconcilesBalances() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val fakeAccountDao = FakeAccountDao()
        val repository = createRepository(fakeTxDao, fakeAccountDao)

        // Pre-create Bank account with initial 100,000 paise (₹1,000.00)
        fakeAccountDao.insert(
            Account(id = "acc_bank", name = "HDFC Bank", type = "bank", balance = 100000L, createdAt = 1000L)
        )

        val csvData = """
            Date,Amount,Type,Category,Account,Description,Notes
            2024-09-08,450.00,EXPENSE,Food,HDFC Bank,Lunch,Subway
            2024-09-09,1200.00,INCOME,Freelance,HDFC Bank,Consulting,Client A
        """.trimIndent()

        val inStream = ByteArrayInputStream(csvData.toByteArray(StandardCharsets.UTF_8))
        val result = repository.importTransactionsFromCsv(inStream)

        assertTrue(result is CsvImportResult.Success)
        val success = result as CsvImportResult.Success
        assertEquals(2, success.importedCount)
        assertEquals(0, success.accountsCreatedCount)

        assertEquals(2, fakeTxDao.list.size)
        val expense = fakeTxDao.list.find { it.description == "Lunch" }
        val income = fakeTxDao.list.find { it.description == "Consulting" }

        assertNotNull(expense)
        assertNotNull(income)
        assertEquals(-45000L, expense!!.amount)
        assertEquals(120000L, income!!.amount)

        // Balance updated: 100,000 - 45,000 + 120,000 = 175,000 paise
        assertEquals(175000L, fakeAccountDao.getById("acc_bank")!!.balance)
    }

    @Test
    fun importTransactions_invalidRow_abortsAndTouchesZeroRows() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val fakeAccountDao = FakeAccountDao()
        val repository = createRepository(fakeTxDao, fakeAccountDao)

        fakeAccountDao.insert(
            Account(id = "acc_bank", name = "HDFC Bank", type = "bank", balance = 50000L, createdAt = 1000L)
        )

        // Line 3 has invalid amount format "not_a_number"
        val csvData = """
            Date,Amount,Type,Category,Account,Description
            2024-09-08,50.00,EXPENSE,Food,HDFC Bank,Valid Row
            2024-09-09,not_a_number,EXPENSE,Food,HDFC Bank,Corrupt Row
            2024-09-10,30.00,EXPENSE,Food,HDFC Bank,Another Valid Row
        """.trimIndent()

        val inStream = ByteArrayInputStream(csvData.toByteArray(StandardCharsets.UTF_8))
        val result = repository.importTransactionsFromCsv(inStream)

        assertTrue(result is CsvImportResult.Error)
        val error = result as CsvImportResult.Error
        assertEquals(3, error.rowNumber)
        assertTrue(error.message.contains("Row 3"))
        assertTrue(error.message.contains("Invalid amount format"))

        // ADR-006 invariant: strictly 0 transactions added, balance untouched!
        assertEquals(0, fakeTxDao.list.size)
        assertEquals(50000L, fakeAccountDao.getById("acc_bank")!!.balance)
    }

    @Test
    fun importTransactions_missingRequiredColumn_returnsError() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val fakeAccountDao = FakeAccountDao()
        val repository = createRepository(fakeTxDao, fakeAccountDao)

        // Missing 'Category' column
        val csvData = """
            Date,Amount,Type,Account,Description
            2024-09-08,50.00,EXPENSE,Bank,Missing Category
        """.trimIndent()

        val inStream = ByteArrayInputStream(csvData.toByteArray(StandardCharsets.UTF_8))
        val result = repository.importTransactionsFromCsv(inStream)

        assertTrue(result is CsvImportResult.Error)
        val error = result as CsvImportResult.Error
        assertTrue(error.message.contains("Missing required header: 'Category'"))
        assertEquals(0, fakeTxDao.list.size)
    }

    @Test
    fun importTransactions_autoCreatesMissingAccounts() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val fakeAccountDao = FakeAccountDao()
        val repository = createRepository(fakeTxDao, fakeAccountDao)

        // Database starts with zero accounts
        assertEquals(0, fakeAccountDao.accounts.size)

        val csvData = """
            Date,Amount,Type,Category,Account,Description
            2024-09-08,100.00,INCOME,Salary,New Bank Account,First Salary
        """.trimIndent()

        val inStream = ByteArrayInputStream(csvData.toByteArray(StandardCharsets.UTF_8))
        val result = repository.importTransactionsFromCsv(inStream)

        assertTrue(result is CsvImportResult.Success)
        val success = result as CsvImportResult.Success
        assertEquals(1, success.importedCount)
        assertEquals(1, success.accountsCreatedCount)

        assertEquals(1, fakeAccountDao.accounts.size)
        val createdAccount = fakeAccountDao.accounts.values.first()
        assertEquals("New Bank Account", createdAccount.name)
        assertEquals(10000L, createdAccount.balance)
    }

    @Test
    fun importTransactions_largeDataset_performance() = runBlocking {
        val fakeTxDao = FakeTransactionDao()
        val fakeAccountDao = FakeAccountDao()
        val repository = createRepository(fakeTxDao, fakeAccountDao)

        fakeAccountDao.insert(
            Account(id = "acc_perf", name = "Test Account", type = "bank", balance = 0L, createdAt = 1000L)
        )

        val sb = StringBuilder()
        sb.append("Date,Amount,Type,Category,Account,Description\n")
        val totalRows = 1000
        for (i in 1..totalRows) {
            sb.append("2024-09-01,10.00,INCOME,Category_$i,Test Account,Description_$i\n")
        }

        val inStream = ByteArrayInputStream(sb.toString().toByteArray(StandardCharsets.UTF_8))
        val startTime = System.currentTimeMillis()
        val result = repository.importTransactionsFromCsv(inStream)
        val elapsed = System.currentTimeMillis() - startTime

        assertTrue(result is CsvImportResult.Success)
        val success = result as CsvImportResult.Success
        assertEquals(totalRows, success.importedCount)
        assertEquals(totalRows, fakeTxDao.list.size)
        // 1000 rows * 1,000 paise (₹10.00) = 1,000,000 paise
        assertEquals(1000000L, fakeAccountDao.getById("acc_perf")!!.balance)
        // Ensure execution was efficient (under 3 seconds)
        assertTrue("Import of 1000 rows took ${elapsed}ms, expected under 3000ms", elapsed < 3000)
    }
}
