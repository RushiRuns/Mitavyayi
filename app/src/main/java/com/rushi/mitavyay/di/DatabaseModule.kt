package com.rushi.mitavyay.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rushi.mitavyay.data.db.AccountDao
import com.rushi.mitavyay.data.db.AppDatabase
import com.rushi.mitavyay.data.db.BudgetDao
import com.rushi.mitavyay.data.db.CategoryDao
import com.rushi.mitavyay.data.db.DebtDao
import com.rushi.mitavyay.data.db.GoalDao
import com.rushi.mitavyay.data.db.RepeatExpenseDao
import com.rushi.mitavyay.data.db.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                val defaults = listOf(
                    "('cat_food', 'Food & Dining', 'restaurant', '#FF7043', 0)",
                    "('cat_groceries', 'Groceries', 'shopping_cart', '#42A5F5', 0)",
                    "('cat_transport', 'Transport', 'directions_car', '#AB47BC', 0)",
                    "('cat_bills', 'Bills & Utilities', 'receipt', '#EC407A', 0)",
                    "('cat_shopping', 'Shopping', 'storefront', '#26A69A', 0)",
                    "('cat_health', 'Healthcare', 'medical_services', '#EF5350', 0)",
                    "('cat_entertainment', 'Entertainment', 'movie', '#FFA726', 0)",
                    "('cat_salary', 'Salary & Income', 'payments', '#66BB6A', 0)",
                    "('cat_investment', 'Investments', 'trending_up', '#29B6F6', 0)",
                    "('cat_transfer', 'Transfer', 'swap_horiz', '#78909C', 0)",
                    "('cat_other', 'Other', 'more_horiz', '#8D6E63', 0)"
                )
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, icon, color, isCustom) VALUES ${defaults.joinToString(",")}")
            }
        }).build()
    }

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideGoalDao(database: AppDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideDebtDao(database: AppDatabase): DebtDao = database.debtDao()

    @Provides
    fun provideRepeatExpenseDao(database: AppDatabase): RepeatExpenseDao = database.repeatExpenseDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideBudgetDao(database: AppDatabase): BudgetDao = database.budgetDao()

    @Provides
    @Singleton
    fun provideDatabaseTransactionRunner(database: AppDatabase): com.rushi.mitavyay.data.db.DatabaseTransactionRunner =
        com.rushi.mitavyay.data.db.RoomDatabaseTransactionRunner(database)
}
