package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.Budget
import com.rushi.mitavyay.data.db.BudgetDao
import com.rushi.mitavyay.data.db.DatabaseTransactionRunner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface BudgetRepository {
    fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>>
    fun getAllBudgets(): Flow<List<Budget>>
    suspend fun getBudgetById(id: String): Budget?
    suspend fun getBudgetByCategoryAndMonth(category: String, monthYear: String): Budget?
    suspend fun setBudget(category: String, monthYear: String, amountPaise: Long): Budget
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(id: String)
    suspend fun copyBudgetsToMonth(sourceMonthYear: String, targetMonthYear: String): Int
}

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionRunner: DatabaseTransactionRunner
) : BudgetRepository {

    override fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>> =
        budgetDao.getBudgetsForMonth(monthYear)

    override fun getAllBudgets(): Flow<List<Budget>> =
        budgetDao.getAll()

    override suspend fun getBudgetById(id: String): Budget? =
        budgetDao.getById(id)

    override suspend fun getBudgetByCategoryAndMonth(category: String, monthYear: String): Budget? =
        budgetDao.getByCategoryAndMonth(category, monthYear)

    override suspend fun setBudget(category: String, monthYear: String, amountPaise: Long): Budget =
        transactionRunner {
            val existing = budgetDao.getByCategoryAndMonth(category, monthYear)
            if (existing != null) {
                val updated = existing.copy(amount = amountPaise, isActive = true)
                budgetDao.update(updated)
                updated
            } else {
                val newBudget = Budget(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    monthYear = monthYear,
                    amount = amountPaise,
                    isActive = true
                )
                budgetDao.insert(newBudget)
                newBudget
            }
        }

    override suspend fun updateBudget(budget: Budget) =
        budgetDao.update(budget)

    override suspend fun deleteBudget(id: String) =
        budgetDao.deleteById(id)

    override suspend fun copyBudgetsToMonth(sourceMonthYear: String, targetMonthYear: String): Int =
        transactionRunner {
            val sourceBudgets = budgetDao.getBudgetsForMonth(sourceMonthYear).first()
            var copiedCount = 0
            val newBudgets = mutableListOf<Budget>()

            for (src in sourceBudgets) {
                val existing = budgetDao.getByCategoryAndMonth(src.category, targetMonthYear)
                if (existing == null) {
                    newBudgets.add(
                        Budget(
                            id = UUID.randomUUID().toString(),
                            category = src.category,
                            monthYear = targetMonthYear,
                            amount = src.amount,
                            isActive = true
                        )
                    )
                    copiedCount++
                }
            }

            if (newBudgets.isNotEmpty()) {
                budgetDao.insertAll(newBudgets)
            }
            copiedCount
        }
}
