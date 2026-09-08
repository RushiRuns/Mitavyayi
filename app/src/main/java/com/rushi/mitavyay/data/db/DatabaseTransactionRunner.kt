package com.rushi.mitavyay.data.db

import androidx.room.withTransaction
import javax.inject.Inject
import javax.inject.Singleton

interface DatabaseTransactionRunner {
    suspend operator fun <R> invoke(block: suspend () -> R): R
}

@Singleton
class RoomDatabaseTransactionRunner @Inject constructor(
    private val database: AppDatabase
) : DatabaseTransactionRunner {
    override suspend operator fun <R> invoke(block: suspend () -> R): R {
        return database.withTransaction(block)
    }
}
