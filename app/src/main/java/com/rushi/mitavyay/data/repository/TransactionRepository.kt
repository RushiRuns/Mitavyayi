package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

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
}

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAll()

    override suspend fun getTransactionById(id: String): Transaction? = transactionDao.getById(id)

    override suspend fun addTransaction(transaction: Transaction) = transactionDao.insert(transaction)

    override suspend fun updateTransaction(transaction: Transaction) = transactionDao.update(transaction)

    override suspend fun deleteTransaction(id: String) = transactionDao.deleteById(id)

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
}
