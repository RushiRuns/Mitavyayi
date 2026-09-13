package com.rushi.mitavyay.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Main Room Database for Mitavyay.
 *
 * Invariant Rule:
 * fallbackToDestructiveMigration() is strictly banned by ARCHITECTURE.md.
 * Version 2: Added budgets entity and MIGRATION_1_2.
 * Version 3: Added isNeed column to transactions and MIGRATION_2_3.
 */
@Database(
    entities = [
        Transaction::class,
        Account::class,
        Transfer::class,
        Goal::class,
        Debt::class,
        RepeatExpense::class,
        Category::class,
        Budget::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun goalDao(): GoalDao
    abstract fun debtDao(): DebtDao
    abstract fun repeatExpenseDao(): RepeatExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DATABASE_NAME = "mitavyay.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `budgets` (
                        `id` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `monthYear` TEXT NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_monthYear` ON `budgets` (`monthYear`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_category_monthYear` ON `budgets` (`category`, `monthYear`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN isNeed INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
