package com.rushi.mitavyay.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<Account>)

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: String): Account?

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getByIdFlow(id: String): Flow<Account?>

    @Query("SELECT * FROM accounts ORDER BY createdAt ASC")
    fun getAll(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveAccounts(): Flow<List<Account>>

    @Query("UPDATE accounts SET balance = :newBalance WHERE id = :id")
    suspend fun updateBalance(id: String, newBalance: Long)

    @Query("SELECT balance FROM accounts WHERE id = :id")
    fun getAccountBalance(id: String): Flow<Long?>
}
