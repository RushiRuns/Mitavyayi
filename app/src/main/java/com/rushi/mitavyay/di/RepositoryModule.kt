package com.rushi.mitavyay.di

import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.AccountRepositoryImpl
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.CategoryRepositoryImpl
import com.rushi.mitavyay.data.repository.CsvRepository
import com.rushi.mitavyay.data.repository.CsvRepositoryImpl
import com.rushi.mitavyay.data.repository.DebtRepository
import com.rushi.mitavyay.data.repository.DebtRepositoryImpl
import com.rushi.mitavyay.data.repository.GoalRepository
import com.rushi.mitavyay.data.repository.GoalRepositoryImpl
import com.rushi.mitavyay.data.repository.RepeatExpenseRepository
import com.rushi.mitavyay.data.repository.RepeatExpenseRepositoryImpl
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.data.repository.TransactionRepositoryImpl
import com.rushi.mitavyay.data.repository.TransferRepository
import com.rushi.mitavyay.data.repository.TransferRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindTransferRepository(
        impl: TransferRepositoryImpl
    ): TransferRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        impl: GoalRepositoryImpl
    ): GoalRepository

    @Binds
    @Singleton
    abstract fun bindDebtRepository(
        impl: DebtRepositoryImpl
    ): DebtRepository

    @Binds
    @Singleton
    abstract fun bindRepeatExpenseRepository(
        impl: RepeatExpenseRepositoryImpl
    ): RepeatExpenseRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindCsvRepository(
        impl: CsvRepositoryImpl
    ): CsvRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        impl: com.rushi.mitavyay.data.repository.BudgetRepositoryImpl
    ): com.rushi.mitavyay.data.repository.BudgetRepository
}
