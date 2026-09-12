package com.rushi.mitavyay.ui.screens.Settings

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.repository.CsvImportResult
import com.rushi.mitavyay.data.repository.CsvRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ImportExportUiState(
    val isLoading: Boolean = false,
    val totalTransactionsCount: Int = 0,
    val exportSuccessMessage: String? = null,
    val importResult: CsvImportResult? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val csvRepository: CsvRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _actionState = MutableStateFlow(ActionState())

    private data class ActionState(
        val isLoading: Boolean = false,
        val exportSuccessMessage: String? = null,
        val importResult: CsvImportResult? = null,
        val errorMessage: String? = null
    )

    val uiState: StateFlow<ImportExportUiState> = combine(
        transactionRepository.getAllTransactions(),
        _actionState
    ) { transactions, action ->
        ImportExportUiState(
            isLoading = action.isLoading,
            totalTransactionsCount = transactions.size,
            exportSuccessMessage = action.exportSuccessMessage,
            importResult = action.importResult,
            errorMessage = action.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ImportExportUiState()
    )

    fun exportToCache(
        context: Context,
        onReadyToShare: (Uri) -> Unit
    ) {
        viewModelScope.launch {
            _actionState.value = _actionState.value.copy(isLoading = true, errorMessage = null)
            try {
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) {
                    exportDir.mkdirs()
                }
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val file = File(exportDir, "mitavyay_transactions_$timeStamp.csv")

                val count = withContext(Dispatchers.IO) {
                    file.outputStream().use { outStream ->
                        csvRepository.exportTransactionsToCsv(outStream).getOrThrow()
                    }
                }

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                _actionState.value = _actionState.value.copy(
                    isLoading = false,
                    exportSuccessMessage = "Exported $count transactions to ${file.name}"
                )
                onReadyToShare(contentUri)
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    isLoading = false,
                    errorMessage = "Export failed: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun exportToTargetUri(
        context: Context,
        targetUri: Uri,
        onSuccess: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            _actionState.value = _actionState.value.copy(isLoading = true, errorMessage = null)
            try {
                val count = withContext(Dispatchers.IO) {
                    val outStream = context.contentResolver.openOutputStream(targetUri)
                        ?: throw IllegalStateException("Unable to open output stream for selected location")
                    outStream.use { stream ->
                        csvRepository.exportTransactionsToCsv(stream).getOrThrow()
                    }
                }

                _actionState.value = _actionState.value.copy(
                    isLoading = false,
                    exportSuccessMessage = "Successfully exported $count transactions"
                )
                onSuccess(count)
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to save export: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun importFromUri(
        context: Context,
        sourceUri: Uri
    ) {
        viewModelScope.launch {
            _actionState.value = _actionState.value.copy(isLoading = true, errorMessage = null, importResult = null)
            try {
                val result = withContext(Dispatchers.IO) {
                    val inStream = context.contentResolver.openInputStream(sourceUri)
                        ?: throw IllegalStateException("Unable to open input stream for selected file")
                    inStream.use { stream ->
                        csvRepository.importTransactionsFromCsv(stream)
                    }
                }

                _actionState.value = _actionState.value.copy(
                    isLoading = false,
                    importResult = result
                )
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(
                    isLoading = false,
                    importResult = CsvImportResult.Error("Import error: ${e.localizedMessage ?: "Unknown error"}")
                )
            }
        }
    }

    fun dismissDialogs() {
        _actionState.value = ActionState()
    }
}
