package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.AnalysisPeriod
import com.rushi.mitavyay.data.repository.AnalysisSummary
import com.rushi.mitavyay.data.repository.CategorySpending
import com.rushi.mitavyay.data.repository.CsvImportResult
import com.rushi.mitavyay.data.repository.CsvRepository
import com.rushi.mitavyay.data.repository.TimeSpendingPoint
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.ui.screens.Settings.ImportExportViewModel
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class ImportExportViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeTxRepo: FakeTransactionRepository
    private lateinit var fakeCsvRepo: FakeCsvRepository
    private lateinit var viewModel: ImportExportViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeTxRepo = FakeTransactionRepository()
        fakeCsvRepo = FakeCsvRepository()
        viewModel = ImportExportViewModel(fakeCsvRepo, fakeTxRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeCsvRepository : CsvRepository {
        var exportResult: Result<Int> = Result.success(5)
        var importResult: CsvImportResult = CsvImportResult.Success(5, 1)

        override suspend fun exportTransactionsToCsv(outputStream: OutputStream): Result<Int> = exportResult

        override suspend fun importTransactionsFromCsv(inputStream: InputStream): CsvImportResult = importResult
    }

    private class FakeTransactionRepository : TransactionRepository {
        val list = mutableListOf<Transaction>()
        private val flow = MutableStateFlow<List<Transaction>>(emptyList())

        fun setTransactions(txs: List<Transaction>) {
            list.clear()
            list.addAll(txs)
            flow.value = list.toList()
        }

        override fun getAllTransactions(): Flow<List<Transaction>> = flow
        override suspend fun getTransactionById(id: String): Transaction? = list.find { it.id == id }
        override suspend fun addTransaction(transaction: Transaction) {
            list.add(transaction)
            flow.value = list.toList()
        }
        override suspend fun updateTransaction(transaction: Transaction) {}
        override suspend fun deleteTransaction(id: String) {}
        override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(null)
        override fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?> = flowOf(null)
        override fun getCategorySpending(start: Long, end: Long): Flow<List<CategorySpending>> = flowOf(emptyList())
        override fun getTimeSpendingTrend(start: Long, end: Long, period: AnalysisPeriod): Flow<List<TimeSpendingPoint>> = flowOf(emptyList())
        override fun getAnalysisSummary(start: Long, end: Long): Flow<AnalysisSummary> = flowOf(AnalysisSummary())
    }

    @Test
    fun uiState_reflectsTotalTransactionCount() = runBlocking {
        fakeTxRepo.setTransactions(
            listOf(
                Transaction("1", "acc_1", -1000L, "Coffee", 1000L, "Food"),
                Transaction("2", "acc_1", -2000L, "Lunch", 2000L, "Food"),
                Transaction("3", "acc_1", 50000L, "Salary", 3000L, "Income")
            )
        )

        val state = viewModel.uiState.first { it.totalTransactionsCount == 3 }
        assertEquals(3, state.totalTransactionsCount)
        assertFalse(state.isLoading)
    }

    @Test
    fun dismissDialogs_clearsActionState() = runBlocking {
        viewModel.dismissDialogs()
        val state = viewModel.uiState.value
        assertNull(state.exportSuccessMessage)
        assertNull(state.importResult)
        assertNull(state.errorMessage)
    }
}
