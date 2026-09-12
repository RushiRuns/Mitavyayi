package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.AccountDao
import com.rushi.mitavyay.data.db.CategoryDao
import com.rushi.mitavyay.data.db.DatabaseTransactionRunner
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

enum class AnalysisPeriod {
    WEEK,
    MONTH,
    YEAR
}

data class CategorySpending(
    val category: String,
    val totalExpensePaise: Long,
    val transactionCount: Int,
    val colorHex: String? = null,
    val percentage: Float = 0f
)

data class TimeSpendingPoint(
    val label: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val expensePaise: Long,
    val incomePaise: Long
)

data class AnalysisSummary(
    val totalIncomePaise: Long = 0L,
    val totalExpensePaise: Long = 0L,
    val netSavingsPaise: Long = 0L,
    val savingsRate: Float = 0f
)

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>>
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>
    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>>
    fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?>
    fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?>

    // Analysis Aggregations
    fun getCategorySpending(start: Long, end: Long): Flow<List<CategorySpending>>
    fun getTimeSpendingTrend(start: Long, end: Long, period: AnalysisPeriod): Flow<List<TimeSpendingPoint>>
    fun getAnalysisSummary(start: Long, end: Long): Flow<AnalysisSummary>
}

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao? = null,
    private val transactionRunner: DatabaseTransactionRunner? = null,
    private val categoryDao: CategoryDao? = null
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAll()

    override suspend fun getTransactionById(id: String): Transaction? = transactionDao.getById(id)

    override suspend fun addTransaction(transaction: Transaction) {
        val action: suspend () -> Unit = {
            transactionDao.insert(transaction)
            accountDao?.let { dao ->
                val account = dao.getById(transaction.accountId)
                if (account != null) {
                    dao.updateBalance(account.id, account.balance + transaction.amount)
                }
            }
        }

        if (transactionRunner != null) {
            transactionRunner { action() }
        } else {
            action()
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val action: suspend () -> Unit = {
            val oldTx = transactionDao.getById(transaction.id)
            transactionDao.update(transaction)
            if (oldTx != null && accountDao != null) {
                if (oldTx.accountId == transaction.accountId) {
                    val delta = transaction.amount - oldTx.amount
                    val account = accountDao.getById(transaction.accountId)
                    if (account != null) {
                        accountDao.updateBalance(account.id, account.balance + delta)
                    }
                } else {
                    val oldAccount = accountDao.getById(oldTx.accountId)
                    if (oldAccount != null) {
                        accountDao.updateBalance(oldAccount.id, oldAccount.balance - oldTx.amount)
                    }
                    val newAccount = accountDao.getById(transaction.accountId)
                    if (newAccount != null) {
                        accountDao.updateBalance(newAccount.id, newAccount.balance + transaction.amount)
                    }
                }
            }
        }

        if (transactionRunner != null) {
            transactionRunner { action() }
        } else {
            action()
        }
    }

    override suspend fun deleteTransaction(id: String) {
        val action: suspend () -> Unit = {
            val oldTx = transactionDao.getById(id)
            if (oldTx != null) {
                accountDao?.let { dao ->
                    val account = dao.getById(oldTx.accountId)
                    if (account != null) {
                        dao.updateBalance(account.id, account.balance - oldTx.amount)
                    }
                }
                transactionDao.deleteById(id)
            }
        }

        if (transactionRunner != null) {
            transactionRunner { action() }
        } else {
            action()
        }
    }

    override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> =
        transactionDao.getByDateRange(start, end)

    override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> =
        transactionDao.getByCategory(category)

    override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> =
        transactionDao.getByAccount(accountId)

    override fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?> =
        transactionDao.getTotalExpensesByDateRange(start, end)

    override fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?> =
        transactionDao.getTotalIncomeByDateRange(start, end)

    override fun getCategorySpending(start: Long, end: Long): Flow<List<CategorySpending>> =
        combine(
            transactionDao.getCategoryExpensesByDateRange(start, end),
            categoryDao?.getAll() ?: flowOf(emptyList())
        ) { rawList, categories ->
            val total = rawList.sumOf { it.totalExpensePaise }
            val colorMap = categories.associate { it.name to it.color }
            rawList.map { raw ->
                val pct = if (total > 0L) {
                    (raw.totalExpensePaise.toFloat() / total.toFloat()) * 100f
                } else 0f
                CategorySpending(
                    category = raw.category,
                    totalExpensePaise = raw.totalExpensePaise,
                    transactionCount = raw.transactionCount,
                    colorHex = colorMap[raw.category],
                    percentage = pct
                )
            }
        }

    override fun getTimeSpendingTrend(
        start: Long,
        end: Long,
        period: AnalysisPeriod
    ): Flow<List<TimeSpendingPoint>> =
        transactionDao.getByDateRange(start, end).map { transactions ->
            aggregateTimeSpending(transactions, start, end, period)
        }

    override fun getAnalysisSummary(start: Long, end: Long): Flow<AnalysisSummary> =
        combine(
            transactionDao.getTotalIncomeByDateRange(start, end),
            transactionDao.getTotalExpensesByDateRange(start, end)
        ) { income, expense ->
            val totalIncome = income ?: 0L
            val totalExpense = abs(expense ?: 0L)
            val netSavings = totalIncome - totalExpense
            val savingsRate = if (totalIncome > 0L) {
                (netSavings.toFloat() / totalIncome.toFloat()).coerceIn(0f, 1f)
            } else 0f
            AnalysisSummary(
                totalIncomePaise = totalIncome,
                totalExpensePaise = totalExpense,
                netSavingsPaise = netSavings,
                savingsRate = savingsRate
            )
        }

    private fun aggregateTimeSpending(
        transactions: List<Transaction>,
        start: Long,
        end: Long,
        period: AnalysisPeriod
    ): List<TimeSpendingPoint> {
        if (start > end) return emptyList()

        val points = mutableListOf<TimeSpendingPoint>()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = start
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (period) {
            AnalysisPeriod.WEEK -> {
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                while (calendar.timeInMillis <= end) {
                    val dayStart = calendar.timeInMillis
                    val label = dayFormat.format(Date(dayStart))
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    val dayEnd = calendar.timeInMillis - 1

                    val txs = transactions.filter { it.timestamp in dayStart..dayEnd }
                    val expense = txs.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                    val income = txs.filter { it.amount > 0 }.sumOf { it.amount }

                    points.add(TimeSpendingPoint(label, dayStart, dayEnd, expense, income))
                }
            }

            AnalysisPeriod.MONTH -> {
                val dayFormat = SimpleDateFormat("d", Locale.getDefault())
                while (calendar.timeInMillis <= end) {
                    val dayStart = calendar.timeInMillis
                    val label = dayFormat.format(Date(dayStart))
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    val dayEnd = calendar.timeInMillis - 1

                    val txs = transactions.filter { it.timestamp in dayStart..dayEnd }
                    val expense = txs.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                    val income = txs.filter { it.amount > 0 }.sumOf { it.amount }

                    points.add(TimeSpendingPoint(label, dayStart, dayEnd, expense, income))
                }
            }

            AnalysisPeriod.YEAR -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                while (calendar.timeInMillis <= end) {
                    val monthStart = calendar.timeInMillis
                    val label = monthFormat.format(Date(monthStart))
                    calendar.add(Calendar.MONTH, 1)
                    val monthEnd = calendar.timeInMillis - 1

                    val txs = transactions.filter { it.timestamp in monthStart..monthEnd }
                    val expense = txs.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                    val income = txs.filter { it.amount > 0 }.sumOf { it.amount }

                    points.add(TimeSpendingPoint(label, monthStart, monthEnd, expense, income))
                }
            }
        }

        return points
    }
}
