package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.Goal
import com.rushi.mitavyay.data.db.GoalDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface GoalRepository {
    fun getAllGoals(): Flow<List<Goal>>
    fun getGoalById(id: String): Flow<Goal?>
    suspend fun getGoal(id: String): Goal?
    suspend fun addGoal(goal: Goal)
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(id: String)
    suspend fun updateCurrentAmount(id: String, amount: Long)
}

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {

    override fun getAllGoals(): Flow<List<Goal>> = goalDao.getAll()

    override fun getGoalById(id: String): Flow<Goal?> = goalDao.getByIdFlow(id)

    override suspend fun getGoal(id: String): Goal? = goalDao.getById(id)

    override suspend fun addGoal(goal: Goal) = goalDao.insert(goal)

    override suspend fun updateGoal(goal: Goal) = goalDao.update(goal)

    override suspend fun deleteGoal(id: String) = goalDao.deleteById(id)

    override suspend fun updateCurrentAmount(id: String, amount: Long) =
        goalDao.updateCurrentAmount(id, amount)
}
