package com.rushi.mitavyay.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): Transaction?

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getByAccount(accountId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getByCategory(category: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE transferId = :transferId")
    suspend fun getByTransferId(transferId: String): List<Transaction>

    @Query("DELETE FROM transactions WHERE transferId = :transferId")
    suspend fun deleteByTransferId(transferId: String)

    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accountId")
    fun getBalanceForAccount(accountId: String): Flow<Long?>

    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp BETWEEN :start AND :end AND amount < 0")
    fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?>

    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp BETWEEN :start AND :end AND amount > 0")
    fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?>

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionCount(): Flow<Int>
}
