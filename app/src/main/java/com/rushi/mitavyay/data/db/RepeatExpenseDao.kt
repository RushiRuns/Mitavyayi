package com.rushi.mitavyay.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RepeatExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repeatExpense: RepeatExpense)

    @Update
    suspend fun update(repeatExpense: RepeatExpense)

    @Delete
    suspend fun delete(repeatExpense: RepeatExpense)

    @Query("DELETE FROM repeat_expenses WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM repeat_expenses WHERE id = :id")
    suspend fun getById(id: String): RepeatExpense?

    @Query("SELECT * FROM repeat_expenses ORDER BY description ASC")
    fun getAll(): Flow<List<RepeatExpense>>

    @Query("SELECT * FROM repeat_expenses WHERE isActive = 1")
    fun getActive(): Flow<List<RepeatExpense>>

    @Query("UPDATE repeat_expenses SET lastGenerated = :timestamp WHERE id = :id")
    suspend fun updateLastGenerated(id: String, timestamp: Long)
}
