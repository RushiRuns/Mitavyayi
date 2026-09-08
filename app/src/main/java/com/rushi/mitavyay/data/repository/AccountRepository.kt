package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.AccountDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    fun getActiveAccounts(): Flow<List<Account>>
    fun getAccountById(id: String): Flow<Account?>
    suspend fun getAccount(id: String): Account?
    suspend fun addAccount(account: Account)
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(id: String)
    suspend fun updateBalance(id: String, newBalance: Long)
    fun getAccountBalance(id: String): Flow<Long?>
}

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getAllAccounts(): Flow<List<Account>> = accountDao.getAll()

    override fun getActiveAccounts(): Flow<List<Account>> = accountDao.getActiveAccounts()

    override fun getAccountById(id: String): Flow<Account?> = accountDao.getByIdFlow(id)

    override suspend fun getAccount(id: String): Account? = accountDao.getById(id)

    override suspend fun addAccount(account: Account) = accountDao.insert(account)

    override suspend fun updateAccount(account: Account) = accountDao.update(account)

    override suspend fun deleteAccount(id: String) = accountDao.deleteById(id)

    override suspend fun updateBalance(id: String, newBalance: Long) =
        accountDao.updateBalance(id, newBalance)

    override fun getAccountBalance(id: String): Flow<Long?> =
        accountDao.getAccountBalance(id)
}
