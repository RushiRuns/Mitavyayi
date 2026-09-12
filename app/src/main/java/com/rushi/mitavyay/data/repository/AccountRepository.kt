package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.AccountDao
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    fun getActiveAccounts(): Flow<List<Account>>
    fun getAccountById(id: String): Flow<Account?>
    suspend fun getAccount(id: String): Account?
    suspend fun addAccount(account: Account)
    suspend fun createAccount(name: String, type: String, initialBalancePaise: Long): Account
    suspend fun editAccount(id: String, name: String, type: String)
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(id: String): Boolean
    suspend fun canDeleteAccount(id: String): Boolean
    suspend fun archiveAccount(id: String)
    suspend fun unarchiveAccount(id: String)
    suspend fun updateBalance(id: String, newBalance: Long)
    fun getAccountBalance(id: String): Flow<Long?>
}

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) : AccountRepository {

    override fun getAllAccounts(): Flow<List<Account>> = accountDao.getAll()

    override fun getActiveAccounts(): Flow<List<Account>> = accountDao.getActiveAccounts()

    override fun getAccountById(id: String): Flow<Account?> = accountDao.getByIdFlow(id)

    override suspend fun getAccount(id: String): Account? = accountDao.getById(id)

    override suspend fun addAccount(account: Account) = accountDao.insert(account)

    override suspend fun createAccount(name: String, type: String, initialBalancePaise: Long): Account {
        val account = Account(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            type = type.lowercase().trim(),
            balance = initialBalancePaise,
            currency = "INR",
            createdAt = System.currentTimeMillis(),
            isActive = true
        )
        accountDao.insert(account)

        if (initialBalancePaise != 0L) {
            val initialTx = Transaction(
                id = UUID.randomUUID().toString(),
                accountId = account.id,
                amount = initialBalancePaise,
                description = "Initial Balance",
                timestamp = account.createdAt,
                category = if (initialBalancePaise > 0) "Income" else "Other",
                tags = "[]",
                transferId = null,
                notes = "Opening balance for ${account.name}"
            )
            transactionDao.insert(initialTx)
        }

        return account
    }

    override suspend fun editAccount(id: String, name: String, type: String) {
        val existing = accountDao.getById(id) ?: return
        val updated = existing.copy(
            name = name.trim(),
            type = type.lowercase().trim()
        )
        accountDao.update(updated)
    }

    override suspend fun updateAccount(account: Account) = accountDao.update(account)

    override suspend fun canDeleteAccount(id: String): Boolean {
        val txCount = transactionDao.getTransactionCountForAccount(id)
        return txCount == 0
    }

    override suspend fun deleteAccount(id: String): Boolean {
        return if (canDeleteAccount(id)) {
            accountDao.deleteById(id)
            true
        } else {
            false
        }
    }

    override suspend fun archiveAccount(id: String) {
        accountDao.updateActiveStatus(id, false)
    }

    override suspend fun unarchiveAccount(id: String) {
        accountDao.updateActiveStatus(id, true)
    }

    override suspend fun updateBalance(id: String, newBalance: Long) =
        accountDao.updateBalance(id, newBalance)

    override fun getAccountBalance(id: String): Flow<Long?> =
        accountDao.getAccountBalance(id)
}
