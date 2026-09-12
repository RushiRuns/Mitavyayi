package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.AccountDao
import com.rushi.mitavyay.data.db.DatabaseTransactionRunner
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface TransferRepository {
    suspend fun createTransfer(
        fromAccountId: String,
        toAccountId: String,
        amountPaise: Long,
        timestamp: Long = System.currentTimeMillis(),
        notes: String? = null
    ): String

    suspend fun deleteTransfer(transferId: String)
    suspend fun getTransferTransactions(transferId: String): List<Transaction>
}

/**
 * Manages transfers as dual paired transaction records (debit and credit).
 * Ensures consistency: debit + credit are always committed or deleted atomically,
 * and account balances are updated concurrently.
 */
@Singleton
class TransferRepositoryImpl @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao? = null
) : TransferRepository {

    override suspend fun createTransfer(
        fromAccountId: String,
        toAccountId: String,
        amountPaise: Long,
        timestamp: Long,
        notes: String?
    ): String {
        val trimmedFrom = fromAccountId.trim()
        val trimmedTo = toAccountId.trim()

        if (trimmedFrom.isBlank() || trimmedTo.isBlank()) {
            throw IllegalArgumentException("Source and destination accounts must be specified")
        }

        if (trimmedFrom == trimmedTo) {
            throw IllegalArgumentException("Cannot transfer to the same account")
        }

        if (amountPaise <= 0L) {
            throw IllegalArgumentException("Transfer amount must be greater than zero")
        }

        val transferId = UUID.randomUUID().toString()
        val absAmount = kotlin.math.abs(amountPaise)

        val fromAccount = accountDao?.getById(trimmedFrom)
        val toAccount = accountDao?.getById(trimmedTo)

        val debitDesc = if (!notes.isNullOrBlank()) {
            notes.trim()
        } else if (toAccount != null) {
            "Transfer to ${toAccount.name}"
        } else {
            "Transfer Out"
        }

        val creditDesc = if (!notes.isNullOrBlank()) {
            notes.trim()
        } else if (fromAccount != null) {
            "Transfer from ${fromAccount.name}"
        } else {
            "Transfer In"
        }

        val debitTransaction = Transaction(
            id = UUID.randomUUID().toString(),
            accountId = trimmedFrom,
            amount = -absAmount,
            description = debitDesc,
            timestamp = timestamp,
            category = "Transfer",
            transferId = transferId,
            notes = notes?.trim()?.ifBlank { null }
        )

        val creditTransaction = Transaction(
            id = UUID.randomUUID().toString(),
            accountId = trimmedTo,
            amount = absAmount,
            description = creditDesc,
            timestamp = timestamp,
            category = "Transfer",
            transferId = transferId,
            notes = notes?.trim()?.ifBlank { null }
        )

        transactionRunner {
            transactionDao.insertAll(listOf(debitTransaction, creditTransaction))
            accountDao?.let { dao ->
                if (fromAccount != null) {
                    dao.updateBalance(fromAccount.id, fromAccount.balance - absAmount)
                }
                if (toAccount != null) {
                    dao.updateBalance(toAccount.id, toAccount.balance + absAmount)
                }
            }
        }

        return transferId
    }

    override suspend fun deleteTransfer(transferId: String) {
        transactionRunner {
            val linked = transactionDao.getByTransferId(transferId)
            accountDao?.let { dao ->
                linked.forEach { tx ->
                    val account = dao.getById(tx.accountId)
                    if (account != null) {
                        dao.updateBalance(account.id, account.balance - tx.amount)
                    }
                }
            }
            transactionDao.deleteByTransferId(transferId)
        }
    }

    override suspend fun getTransferTransactions(transferId: String): List<Transaction> {
        return transactionDao.getByTransferId(transferId)
    }
}
