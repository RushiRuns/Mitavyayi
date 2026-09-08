package com.rushi.mitavyay.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal)

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: String): Goal?

    @Query("SELECT * FROM goals WHERE id = :id")
    fun getByIdFlow(id: String): Flow<Goal?>

    @Query("SELECT * FROM goals ORDER BY deadline ASC")
    fun getAll(): Flow<List<Goal>>

    @Query("UPDATE goals SET currentAmount = :amount WHERE id = :id")
    suspend fun updateCurrentAmount(id: String, amount: Long)
}
