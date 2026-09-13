package com.rushi.mitavyay.data

import androidx.sqlite.db.SupportSQLiteDatabase
import com.rushi.mitavyay.data.db.AppDatabase
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class Migration2To3Test {

    @Test
    fun migration2To3_executesAlterTableWithDefault1() {
        val executedSql = mutableListOf<String>()
        val dbProxy = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL") {
                executedSql.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        AppDatabase.MIGRATION_2_3.migrate(dbProxy)

        assertTrue(executedSql.isNotEmpty())
        assertTrue(
            executedSql.any {
                it.contains("ALTER TABLE transactions ADD COLUMN isNeed INTEGER NOT NULL DEFAULT 1")
            }
        )
    }
}
