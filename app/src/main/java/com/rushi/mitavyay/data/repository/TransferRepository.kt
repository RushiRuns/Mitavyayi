package com.rushi.mitavyay.data.repository

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
}

/**
 * Manages transfers as dual paired transaction records (debit and credit).
 * Ensures consistency: debit + credit are always committed or deleted atomically.
 */
@Singleton
class TransferRepositoryImpl @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val transactionDao: TransactionDao
) : TransferRepository {

    override suspend fun createTransfer(
        fromAccountId: String,
        toAccountId: String,
        amountPaise: Long,
        timestamp: Long,
        notes: String?
    ): String {
        val transferId = UUID.randomUUID().toString()
        val absAmount = kotlin.math.abs(amountPaise)

        val debitTransaction = Transaction(
            id = UUID.randomUUID().toString(),
            accountId = fromAccountId,
            amount = -absAmount,
            description = "Transfer Out",
            timestamp = timestamp,
            category = "Transfer",
            transferId = transferId,
            notes = notes
        )

        val creditTransaction = Transaction(
            id = UUID.randomUUID().toString(),
            accountId = toAccountId,
            amount = absAmount,
            description = "Transfer In",
            timestamp = timestamp,
            category = "Transfer",
            transferId = transferId,
            notes = notes
        )

        transactionRunner {
            transactionDao.insertAll(listOf(debitTransaction, creditTransaction))
        }

        return transferId
    }

    override suspend fun deleteTransfer(transferId: String) {
        transactionRunner {
            transactionDao.deleteByTransferId(transferId)
        }
    }
}
