package com.rushi.mitavyay.di

import android.content.Context
import androidx.room.Room
import com.rushi.mitavyay.data.db.AccountDao
import com.rushi.mitavyay.data.db.AppDatabase
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
        ).build()
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
    @Singleton
    fun provideDatabaseTransactionRunner(database: AppDatabase): com.rushi.mitavyay.data.db.DatabaseTransactionRunner =
        com.rushi.mitavyay.data.db.RoomDatabaseTransactionRunner(database)
}
