package com.rushi.mitavyay.ui.screens.Categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val isLoading: Boolean = false,
    val customCategories: List<CategoryDisplayItem> = emptyList(),
    val defaultCategories: List<CategoryDisplayItem> = emptyList(),
    val errorMessage: String? = null,
    val showAddDialog: Boolean = false,
    val editingCategory: CategoryDisplayItem? = null,
    val deletingCategory: CategoryDisplayItem? = null
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _dialogState = MutableStateFlow(DialogState())

    private data class DialogState(
        val showAddDialog: Boolean = false,
        val editingCategory: CategoryDisplayItem? = null,
        val deletingCategory: CategoryDisplayItem? = null,
        val errorMessage: String? = null
    )

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.getAllCategories(),
        _dialogState
    ) { categories, dialog ->
        val displayItems = categories.map { it.toDisplayItem() }
        CategoriesUiState(
            isLoading = false,
            customCategories = displayItems.filter { it.isCustom },
            defaultCategories = displayItems.filter { !it.isCustom },
            showAddDialog = dialog.showAddDialog,
            editingCategory = dialog.editingCategory,
            deletingCategory = dialog.deletingCategory,
            errorMessage = dialog.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CategoriesUiState(isLoading = true)
    )

    fun onAddCategoryClick() {
        _dialogState.value = DialogState(showAddDialog = true)
    }

    fun onEditCategoryClick(category: CategoryDisplayItem) {
        if (category.isCustom) {
            _dialogState.value = DialogState(editingCategory = category)
        }
    }

    fun onDeleteCategoryClick(category: CategoryDisplayItem) {
        if (category.isCustom) {
            _dialogState.value = DialogState(deletingCategory = category)
        }
    }

    fun onDismissDialog() {
        _dialogState.value = DialogState()
    }

    fun saveCategory(name: String, colorHex: String, icon: String) {
        viewModelScope.launch {
            val editing = _dialogState.value.editingCategory
            if (editing != null) {
                val result = categoryRepository.updateCustomCategory(editing.id, name, colorHex, icon)
                result.fold(
                    onSuccess = { _dialogState.value = DialogState() },
                    onFailure = { error ->
                        _dialogState.value = _dialogState.value.copy(errorMessage = error.localizedMessage)
                    }
                )
            } else {
                val result = categoryRepository.createCustomCategory(name, colorHex, icon)
                result.fold(
                    onSuccess = { _dialogState.value = DialogState() },
                    onFailure = { error ->
                        _dialogState.value = _dialogState.value.copy(errorMessage = error.localizedMessage)
                    }
                )
            }
        }
    }

    fun confirmDeleteCategory() {
        val deleting = _dialogState.value.deletingCategory ?: return
        viewModelScope.launch {
            categoryRepository.deleteCategory(deleting.id)
            _dialogState.value = DialogState()
        }
    }
}
