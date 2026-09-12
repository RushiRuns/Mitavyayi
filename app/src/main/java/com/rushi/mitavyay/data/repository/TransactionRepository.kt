package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.AccountDao
import com.rushi.mitavyay.data.db.DatabaseTransactionRunner
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
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao? = null,
    private val transactionRunner: DatabaseTransactionRunner? = null
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
}
