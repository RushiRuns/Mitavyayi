package com.rushi.mitavyay.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Main Room Database for Mitavyay.
 *
 * Invariant Rule:
 * fallbackToDestructiveMigration() is strictly banned by ARCHITECTURE.md.
 * Version 1 initial release.
 */
@Database(
    entities = [
        Transaction::class,
        Account::class,
        Transfer::class,
        Goal::class,
        Debt::class,
        RepeatExpense::class,
        Category::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun goalDao(): GoalDao
    abstract fun debtDao(): DebtDao
    abstract fun repeatExpenseDao(): RepeatExpenseDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "mitavyay.db"
    }
}
