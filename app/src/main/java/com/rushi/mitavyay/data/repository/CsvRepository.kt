package com.rushi.mitavyay.data.repository

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import com.rushi.mitavyay.data.db.Account
import com.rushi.mitavyay.data.db.AccountDao
import com.rushi.mitavyay.data.db.DatabaseTransactionRunner
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToLong

sealed class CsvImportResult {
    data class Success(
        val importedCount: Int,
        val accountsCreatedCount: Int
    ) : CsvImportResult()

    data class Error(
        val message: String,
        val rowNumber: Int? = null
    ) : CsvImportResult()
}

interface CsvRepository {
    suspend fun exportTransactionsToCsv(outputStream: OutputStream): Result<Int>
    suspend fun importTransactionsFromCsv(inputStream: InputStream): CsvImportResult
}

@Singleton
class CsvRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val transactionRunner: DatabaseTransactionRunner
) : CsvRepository {

    private val supportedDateFormats = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy HH:mm",
        "dd-MM-yyyy HH:mm:ss",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "yyyy/MM/dd"
    )

    override suspend fun exportTransactionsToCsv(outputStream: OutputStream): Result<Int> {
        return runCatching {
            val transactions = transactionDao.getAll().first()
            val accounts = accountDao.getAll().first().associateBy { it.id }

            val writer = CSVWriter(OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
            try {
                // Header
                writer.writeNext(
                    arrayOf(
                        "Date",
                        "Time",
                        "Amount",
                        "Type",
                        "Category",
                        "Account",
                        "Description",
                        "Notes",
                        "Id",
                        "TransferId"
                    )
                )

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

                for (tx in transactions) {
                    val dateStr = dateFormat.format(tx.timestamp)
                    val timeStr = timeFormat.format(tx.timestamp)
                    val amountDecimal = String.format(Locale.US, "%.2f", abs(tx.amount) / 100.0)
                    val typeStr = if (tx.amount >= 0) "INCOME" else "EXPENSE"
                    val accountName = accounts[tx.accountId]?.name ?: tx.accountId

                    writer.writeNext(
                        arrayOf(
                            dateStr,
                            timeStr,
                            amountDecimal,
                            typeStr,
                            tx.category,
                            accountName,
                            tx.description,
                            tx.notes ?: "",
                            tx.id,
                            tx.transferId ?: ""
                        )
                    )
                }
                writer.flush()
                transactions.size
            } finally {
                // Do not close outputStream directly if caller manages it, but flush
                writer.flush()
            }
        }
    }

    override suspend fun importTransactionsFromCsv(inputStream: InputStream): CsvImportResult {
        return try {
            val reader = CSVReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
            val allRows = reader.readAll()
            if (allRows.isEmpty()) {
                return CsvImportResult.Error("CSV file is empty")
            }

            val header = allRows[0]
            val columnMap = mutableMapOf<String, Int>()
            header.forEachIndexed { index, col ->
                columnMap[col.trim().lowercase(Locale.ROOT)] = index
            }

            val dateCol = columnMap["date"] ?: columnMap["timestamp"]
            val timeCol = columnMap["time"]
            val amountCol = columnMap["amount"] ?: columnMap["amt"]
            val typeCol = columnMap["type"]
            val categoryCol = columnMap["category"] ?: columnMap["cat"]
            val accountCol = columnMap["account"] ?: columnMap["acc"]
            val descCol = columnMap["description"] ?: columnMap["desc"] ?: columnMap["title"]
            val notesCol = columnMap["notes"] ?: columnMap["note"] ?: columnMap["comment"]
            val idCol = columnMap["id"] ?: columnMap["txid"]
            val transferIdCol = columnMap["transferid"] ?: columnMap["transfer_id"]

            if (dateCol == null) {
                return CsvImportResult.Error("Missing required header: 'Date'")
            }
            if (amountCol == null) {
                return CsvImportResult.Error("Missing required header: 'Amount'")
            }
            if (categoryCol == null) {
                return CsvImportResult.Error("Missing required header: 'Category'")
            }
            if (accountCol == null) {
                return CsvImportResult.Error("Missing required header: 'Account'")
            }

            data class ValidatedParsedRow(
                val id: String,
                val accountNameOrId: String,
                val amountPaise: Long,
                val description: String,
                val timestamp: Long,
                val category: String,
                val transferId: String?,
                val notes: String?
            )

            val parsedRows = mutableListOf<ValidatedParsedRow>()

            for (rowIndex in 1 until allRows.size) {
                val row = allRows[rowIndex]
                val rowNum = rowIndex + 1

                // Skip entirely blank rows
                if (row.all { it.isBlank() }) continue

                val dateStr = row.getOrNull(dateCol)?.trim().orEmpty()
                if (dateStr.isBlank()) {
                    return CsvImportResult.Error("Row $rowNum: Date cannot be empty", rowNum)
                }

                val timeStr = if (timeCol != null) row.getOrNull(timeCol)?.trim() else null
                val timestamp = parseDateStringToTimestamp(dateStr, timeStr)
                    ?: return CsvImportResult.Error("Row $rowNum: Invalid date format '$dateStr'", rowNum)

                val amountRaw = row.getOrNull(amountCol)?.trim().orEmpty()
                if (amountRaw.isBlank()) {
                    return CsvImportResult.Error("Row $rowNum: Amount cannot be empty", rowNum)
                }

                val typeRaw = if (typeCol != null) row.getOrNull(typeCol)?.trim() else null
                val amountPaise = parseAmountToPaise(amountRaw, typeRaw)
                    ?: return CsvImportResult.Error("Row $rowNum: Invalid amount format '$amountRaw'", rowNum)

                if (amountPaise == 0L) {
                    return CsvImportResult.Error("Row $rowNum: Amount cannot be zero", rowNum)
                }

                val categoryStr = row.getOrNull(categoryCol)?.trim().orEmpty()
                if (categoryStr.isBlank()) {
                    return CsvImportResult.Error("Row $rowNum: Category cannot be empty", rowNum)
                }

                val accountStr = row.getOrNull(accountCol)?.trim().orEmpty()
                if (accountStr.isBlank()) {
                    return CsvImportResult.Error("Row $rowNum: Account cannot be empty", rowNum)
                }

                val descStr = if (descCol != null) {
                    row.getOrNull(descCol)?.trim()?.ifBlank { categoryStr } ?: categoryStr
                } else {
                    categoryStr
                }

                val notesStr = if (notesCol != null) {
                    row.getOrNull(notesCol)?.trim()?.ifBlank { null }
                } else null

                val idStr = if (idCol != null) {
                    row.getOrNull(idCol)?.trim()?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
                } else {
                    UUID.randomUUID().toString()
                }

                val transferIdStr = if (transferIdCol != null) {
                    row.getOrNull(transferIdCol)?.trim()?.takeIf { it.isNotBlank() }
                } else null

                parsedRows.add(
                    ValidatedParsedRow(
                        id = idStr,
                        accountNameOrId = accountStr,
                        amountPaise = amountPaise,
                        description = descStr,
                        timestamp = timestamp,
                        category = categoryStr,
                        transferId = transferIdStr,
                        notes = notesStr
                    )
                )
            }

            if (parsedRows.isEmpty()) {
                return CsvImportResult.Error("No valid transaction rows found in CSV")
            }

            // All rows 100% validated — execute atomic commit
            var accountsCreated = 0
            transactionRunner {
                val existingAccounts = accountDao.getAll().first().toMutableList()
                val accountByName = existingAccounts.associateBy { it.name.lowercase(Locale.ROOT) }.toMutableMap()
                val accountById = existingAccounts.associateBy { it.id }.toMutableMap()

                val resolvedTransactions = mutableListOf<Transaction>()
                val accountBalanceDeltas = mutableMapOf<String, Long>()

                for (row in parsedRows) {
                    val key = row.accountNameOrId.lowercase(Locale.ROOT)
                    var targetAccount = accountByName[key] ?: accountById[row.accountNameOrId]

                    if (targetAccount == null) {
                        // Auto-create missing account
                        val newAccount = Account(
                            id = UUID.randomUUID().toString(),
                            name = row.accountNameOrId.trim(),
                            type = "general",
                            balance = 0L,
                            currency = "INR",
                            createdAt = System.currentTimeMillis(),
                            isActive = true
                        )
                        accountDao.insert(newAccount)
                        existingAccounts.add(newAccount)
                        accountByName[newAccount.name.lowercase(Locale.ROOT)] = newAccount
                        accountById[newAccount.id] = newAccount
                        targetAccount = newAccount
                        accountsCreated++
                    }

                    resolvedTransactions.add(
                        Transaction(
                            id = row.id,
                            accountId = targetAccount.id,
                            amount = row.amountPaise,
                            description = row.description,
                            timestamp = row.timestamp,
                            category = row.category,
                            transferId = row.transferId,
                            notes = row.notes
                        )
                    )

                    val currentDelta = accountBalanceDeltas.getOrDefault(targetAccount.id, 0L)
                    accountBalanceDeltas[targetAccount.id] = currentDelta + row.amountPaise
                }

                // Batch insert all transactions
                transactionDao.insertAll(resolvedTransactions)

                // Update account balances atomically
                accountBalanceDeltas.forEach { (accountId, delta) ->
                    val acc = accountById[accountId]
                    if (acc != null) {
                        accountDao.updateBalance(accountId, acc.balance + delta)
                    }
                }
            }

            CsvImportResult.Success(
                importedCount = parsedRows.size,
                accountsCreatedCount = accountsCreated
            )
        } catch (e: Exception) {
            CsvImportResult.Error("Import failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun parseDateStringToTimestamp(dateStr: String, timeStr: String?): Long? {
        val dateRaw = dateStr.trim()
        val numeric = dateRaw.toLongOrNull()
        if (numeric != null && numeric > 100000000000L) {
            return numeric
        }

        // If time is given separately and date has no space
        val combinedStr = if (!timeStr.isNullOrBlank() && !dateRaw.contains(" ")) {
            "$dateRaw ${timeStr.trim()}"
        } else {
            dateRaw
        }

        for (pattern in supportedDateFormats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.isLenient = false
                val parsed = sdf.parse(combinedStr)
                if (parsed != null) {
                    return parsed.time
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun parseAmountToPaise(amountStr: String, typeStr: String?): Long? {
        val sanitized = amountStr
            .replace("₹", "")
            .replace("$", "")
            .replace("€", "")
            .replace("£", "")
            .replace(",", "")
            .trim()

        val parsedDouble = sanitized.toDoubleOrNull() ?: return null
        val paise = (parsedDouble * 100.0).roundToLong()

        val upperType = typeStr?.uppercase(Locale.ROOT)?.trim()
        return when (upperType) {
            "EXPENSE", "DEBIT" -> -abs(paise)
            "INCOME", "CREDIT" -> abs(paise)
            else -> paise
        }
    }
}
