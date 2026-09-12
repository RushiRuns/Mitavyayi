package com.rushi.mitavyay.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<Budget>)

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: String): Budget?

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear AND isActive = 1 ORDER BY amount DESC")
    fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>>

    @Query("SELECT * FROM budgets ORDER BY monthYear DESC, amount DESC")
    fun getAll(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE category = :category AND monthYear = :monthYear LIMIT 1")
    suspend fun getByCategoryAndMonth(category: String, monthYear: String): Budget?

    @Query("SELECT * FROM budgets WHERE category = :category AND monthYear = :monthYear LIMIT 1")
    fun getByCategoryAndMonthFlow(category: String, monthYear: String): Flow<Budget?>
}
