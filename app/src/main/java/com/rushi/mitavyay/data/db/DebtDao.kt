package com.rushi.mitavyay.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Debts and Loans.
 * Note: [Delete] is intentionally omitted to mechanically enforce the invariant that debts are never deleted.
 */
@Dao
interface DebtDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: Debt)

    @Update
    suspend fun update(debt: Debt)

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getById(id: String): Debt?

    @Query("SELECT * FROM debts WHERE id = :id")
    fun getByIdFlow(id: String): Flow<Debt?>

    @Query("SELECT * FROM debts ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE settledAt IS NULL ORDER BY createdAt DESC")
    fun getActiveDebts(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE settledAt IS NOT NULL ORDER BY settledAt DESC")
    fun getSettledDebts(): Flow<List<Debt>>

    @Query("UPDATE debts SET settledAt = :settledAt WHERE id = :id")
    suspend fun markSettled(id: String, settledAt: Long)
}
