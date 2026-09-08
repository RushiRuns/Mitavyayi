package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.Debt
import com.rushi.mitavyay.data.db.DebtDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface DebtRepository {
    fun getAllDebts(): Flow<List<Debt>>
    fun getActiveDebts(): Flow<List<Debt>>
    fun getSettledDebts(): Flow<List<Debt>>
    suspend fun getDebtById(id: String): Debt?
    suspend fun addDebt(debt: Debt)
    suspend fun updateDebt(debt: Debt)
    suspend fun markDebtAsSettled(id: String, settledAt: Long = System.currentTimeMillis())
}

/**
 * Repository for Debt management.
 * Invariant: Debts are never deleted—only marked as settled.
 */
@Singleton
class DebtRepositoryImpl @Inject constructor(
    private val debtDao: DebtDao
) : DebtRepository {

    override fun getAllDebts(): Flow<List<Debt>> = debtDao.getAll()

    override fun getActiveDebts(): Flow<List<Debt>> = debtDao.getActiveDebts()

    override fun getSettledDebts(): Flow<List<Debt>> = debtDao.getSettledDebts()

    override suspend fun getDebtById(id: String): Debt? = debtDao.getById(id)

    override suspend fun addDebt(debt: Debt) = debtDao.insert(debt)

    override suspend fun updateDebt(debt: Debt) = debtDao.update(debt)

    override suspend fun markDebtAsSettled(id: String, settledAt: Long) =
        debtDao.markSettled(id, settledAt)
}
