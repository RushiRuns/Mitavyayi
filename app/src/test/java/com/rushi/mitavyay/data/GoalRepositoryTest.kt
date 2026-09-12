package com.rushi.mitavyay.data

import com.rushi.mitavyay.data.db.Goal
import com.rushi.mitavyay.data.db.GoalDao
import com.rushi.mitavyay.data.repository.GoalRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GoalRepositoryTest {

    private class FakeGoalDao : GoalDao {
        val list = mutableListOf<Goal>()

        override suspend fun insert(goal: Goal) {
            list.removeAll { it.id == goal.id }
            list.add(goal)
        }

        override suspend fun update(goal: Goal) {
            val idx = list.indexOfFirst { it.id == goal.id }
            if (idx != -1) list[idx] = goal
        }

        override suspend fun delete(goal: Goal) {
            list.removeAll { it.id == goal.id }
        }

        override suspend fun deleteById(id: String) {
            list.removeAll { it.id == id }
        }

        override suspend fun getById(id: String): Goal? = list.find { it.id == id }

        override fun getByIdFlow(id: String): Flow<Goal?> = flowOf(list.find { it.id == id })

        override fun getAll(): Flow<List<Goal>> = flowOf(list.sortedBy { it.deadline })

        override suspend fun updateCurrentAmount(id: String, amount: Long) {
            val idx = list.indexOfFirst { it.id == id }
            if (idx != -1) {
                list[idx] = list[idx].copy(currentAmount = amount)
            }
        }
    }

    @Test
    fun goal_addAndGetAllAndGetById() = runBlocking {
        val fakeDao = FakeGoalDao()
        val repository = GoalRepositoryImpl(fakeDao)

        val goal1 = Goal(
            id = "goal_1",
            name = "Emergency Fund",
            targetAmount = 5000000L, // ₹50,000.00
            deadline = 1750000000000L,
            currentAmount = 1000000L,
            category = "Emergency"
        )
        repository.addGoal(goal1)

        val retrieved = repository.getGoal("goal_1")
        assertNotNull(retrieved)
        assertEquals("Emergency Fund", retrieved?.name)
        assertEquals(5000000L, retrieved?.targetAmount)

        val retrievedFlow = repository.getGoalById("goal_1").first()
        assertEquals("goal_1", retrievedFlow?.id)

        val all = repository.getAllGoals().first()
        assertEquals(1, all.size)
    }

    @Test
    fun goal_updateAndDelete() = runBlocking {
        val fakeDao = FakeGoalDao()
        val repository = GoalRepositoryImpl(fakeDao)

        val goal = Goal(
            id = "goal_2",
            name = "Laptop",
            targetAmount = 8000000L,
            deadline = 1760000000000L,
            currentAmount = 2000000L
        )
        repository.addGoal(goal)

        val updated = goal.copy(name = "MacBook Pro", targetAmount = 12000000L)
        repository.updateGoal(updated)

        val retrieved = repository.getGoal("goal_2")
        assertEquals("MacBook Pro", retrieved?.name)
        assertEquals(12000000L, retrieved?.targetAmount)

        repository.deleteGoal("goal_2")
        assertNull(repository.getGoal("goal_2"))
        assertEquals(0, repository.getAllGoals().first().size)
    }

    @Test
    fun goal_updateCurrentAmountAndAddSavings() = runBlocking {
        val fakeDao = FakeGoalDao()
        val repository = GoalRepositoryImpl(fakeDao)

        val goal = Goal(
            id = "goal_3",
            name = "Vacation",
            targetAmount = 3000000L,
            deadline = 1770000000000L,
            currentAmount = 500000L
        )
        repository.addGoal(goal)

        // Directly set amount
        repository.updateCurrentAmount("goal_3", 1000000L)
        assertEquals(1000000L, repository.getGoal("goal_3")?.currentAmount)

        // Incrementally add savings
        repository.addSavings("goal_3", 750000L)
        assertEquals(1750000L, repository.getGoal("goal_3")?.currentAmount)

        // Adding savings to non-existent goal should safely do nothing
        repository.addSavings("non_existent", 500000L)
    }
}
