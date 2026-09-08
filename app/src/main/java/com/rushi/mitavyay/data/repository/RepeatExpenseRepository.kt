package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.RepeatExpense
import com.rushi.mitavyay.data.db.RepeatExpenseDao
import com.rushi.mitavyay.data.db.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface RepeatExpenseRepository {
    fun getAllRepeatExpenses(): Flow<List<RepeatExpense>>
    fun getActiveRepeatExpenses(): Flow<List<RepeatExpense>>
    suspend fun getRepeatExpenseById(id: String): RepeatExpense?
    suspend fun addRepeatExpense(repeatExpense: RepeatExpense)
    suspend fun updateRepeatExpense(repeatExpense: RepeatExpense)
    suspend fun deleteRepeatExpense(id: String)
    suspend fun generateNextOccurrence(
        repeatExpenseId: String,
        accountId: String,
        currentTimeMs: Long = System.currentTimeMillis()
    ): Transaction?
}

/**
 * Lazy generation of recurring expenses as mandated by ARCHITECTURE.md.
 * Only generates one future occurrence at a time when due.
 */
@Singleton
class RepeatExpenseRepositoryImpl @Inject constructor(
    private val repeatExpenseDao: RepeatExpenseDao,
    private val transactionRepository: TransactionRepository
) : RepeatExpenseRepository {

    override fun getAllRepeatExpenses(): Flow<List<RepeatExpense>> = repeatExpenseDao.getAll()

    override fun getActiveRepeatExpenses(): Flow<List<RepeatExpense>> = repeatExpenseDao.getActive()

    override suspend fun getRepeatExpenseById(id: String): RepeatExpense? =
        repeatExpenseDao.getById(id)

    override suspend fun addRepeatExpense(repeatExpense: RepeatExpense) =
        repeatExpenseDao.insert(repeatExpense)

    override suspend fun updateRepeatExpense(repeatExpense: RepeatExpense) =
        repeatExpenseDao.update(repeatExpense)

    override suspend fun deleteRepeatExpense(id: String) =
        repeatExpenseDao.deleteById(id)

    override suspend fun generateNextOccurrence(
        repeatExpenseId: String,
        accountId: String,
        currentTimeMs: Long
    ): Transaction? {
        val repeatExpense = repeatExpenseDao.getById(repeatExpenseId) ?: return null
        if (!repeatExpense.isActive) return null

        val nextDueTimestamp = calculateNextDueTime(
            lastGenerated = repeatExpense.lastGenerated,
            frequency = repeatExpense.frequency
        )

        // Generate only if current time is past or equal to due timestamp
        if (currentTimeMs >= nextDueTimestamp) {
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                accountId = accountId,
                amount = -kotlin.math.abs(repeatExpense.amount),
                description = repeatExpense.description,
                timestamp = nextDueTimestamp,
                category = repeatExpense.category,
                notes = "Auto-generated recurring expense (${repeatExpense.frequency})"
            )
            transactionRepository.addTransaction(transaction)
            repeatExpenseDao.updateLastGenerated(repeatExpenseId, nextDueTimestamp)
            return transaction
        }

        return null
    }

    private fun calculateNextDueTime(lastGenerated: Long, frequency: String): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = lastGenerated
        }
        when (frequency.uppercase()) {
            "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
            "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "MONTHLY" -> cal.add(Calendar.MONTH, 1)
            "YEARLY" -> cal.add(Calendar.YEAR, 1)
            else -> cal.add(Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }
}
