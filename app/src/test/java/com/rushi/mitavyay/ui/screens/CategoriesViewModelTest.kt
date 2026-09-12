package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.ui.screens.Categories.CategoriesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeCategoryRepository : CategoryRepository {
        val categories = mutableListOf<Category>()
        val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

        fun notifyChange() {
            categoriesFlow.value = categories.toList()
        }

        override fun getAllCategories(): Flow<List<Category>> = categoriesFlow

        override fun getCustomCategories(): Flow<List<Category>> = MutableStateFlow(
            categories.filter { it.isCustom }
        )

        override suspend fun getCategoryById(id: String): Category? = categories.find { it.id == id }

        override suspend fun getCategoryByName(name: String): Category? =
            categories.find { it.name.equals(name, ignoreCase = true) }

        override suspend fun addCategory(category: Category) {
            categories.add(category)
            notifyChange()
        }

        override suspend fun updateCategory(category: Category) {
            categories.removeAll { it.id == category.id }
            categories.add(category)
            notifyChange()
        }

        override suspend fun deleteCategory(id: String) {
            categories.removeAll { it.id == id && it.isCustom }
            notifyChange()
        }

        override suspend fun seedDefaultCategories() {
            if (categories.isEmpty()) {
                categories.addAll(
                    listOf(
                        Category("cat_food", "Food & Dining", "star", "#FF7043", false),
                        Category("cat_groceries", "Groceries", "star", "#42A5F5", false)
                    )
                )
                notifyChange()
            }
        }

        override suspend fun createCustomCategory(
            name: String,
            colorHex: String,
            icon: String
        ): Result<Category> {
            val trimmed = name.trim()
            if (trimmed.isBlank()) {
                return Result.failure(IllegalArgumentException("Category name cannot be blank"))
            }
            if (categories.any { it.name.equals(trimmed, ignoreCase = true) }) {
                return Result.failure(IllegalArgumentException("Category '$trimmed' already exists"))
            }
            val newCat = Category(
                id = UUID.randomUUID().toString(),
                name = trimmed,
                icon = icon,
                color = colorHex,
                isCustom = true
            )
            categories.add(newCat)
            notifyChange()
            return Result.success(newCat)
        }

        override suspend fun updateCustomCategory(
            id: String,
            name: String,
            colorHex: String,
            icon: String
        ): Result<Unit> {
            val index = categories.indexOfFirst { it.id == id }
            if (index == -1) return Result.failure(IllegalArgumentException("Not found"))
            val existing = categories[index]
            if (!existing.isCustom) return Result.failure(IllegalStateException("Cannot edit default"))

            val trimmed = name.trim()
            if (categories.any { it.name.equals(trimmed, ignoreCase = true) && it.id != id }) {
                return Result.failure(IllegalArgumentException("Category '$trimmed' already exists"))
            }

            categories[index] = existing.copy(name = trimmed, color = colorHex, icon = icon)
            notifyChange()
            return Result.success(Unit)
        }

        override suspend fun canDeleteCategory(id: String): Boolean {
            return categories.find { it.id == id }?.isCustom == true
        }
    }

    private lateinit var fakeRepository: FakeCategoryRepository
    private lateinit var viewModel: CategoriesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCategoryRepository()
        runBlocking { fakeRepository.seedDefaultCategories() }
        viewModel = CategoriesViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_separatesCustomAndDefaultCategories() = runBlocking {
        // Initially 2 default categories
        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(2, state.defaultCategories.size)
        assertEquals(0, state.customCategories.size)

        // Add a custom category
        fakeRepository.createCustomCategory("Fitness", "#66BB6A", "favorite")

        val updatedState = viewModel.uiState.first { it.customCategories.isNotEmpty() }
        assertEquals(2, updatedState.defaultCategories.size)
        assertEquals(1, updatedState.customCategories.size)
        assertEquals("Fitness", updatedState.customCategories.first().name)
        assertTrue(updatedState.customCategories.first().isCustom)
    }

    @Test
    fun onAddCategoryClick_andDismissDialog() = runBlocking {
        viewModel.onAddCategoryClick()
        val state = viewModel.uiState.first { it.showAddDialog }
        assertTrue(state.showAddDialog)

        viewModel.onDismissDialog()
        val dismissedState = viewModel.uiState.first { !it.showAddDialog }
        assertFalse(dismissedState.showAddDialog)
    }

    @Test
    fun saveCategory_createsCustomCategorySuccessfully() = runBlocking {
        viewModel.onAddCategoryClick()
        viewModel.saveCategory("Gaming", "#AB47BC", "star")

        val state = viewModel.uiState.first { it.customCategories.any { cat -> cat.name == "Gaming" } }
        assertFalse(state.showAddDialog)
        assertNull(state.errorMessage)
        assertEquals(1, state.customCategories.size)
        assertEquals("Gaming", state.customCategories[0].name)
    }

    @Test
    fun saveCategory_showsErrorOnDuplicateName() = runBlocking {
        viewModel.onAddCategoryClick()
        // Attempt duplicate of Food & Dining
        viewModel.saveCategory("food & dining", "#FF0000", "star")

        val state = viewModel.uiState.first { it.errorMessage != null }
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("already exists"))
    }

    @Test
    fun onEditCategoryClick_andSaveCategory_updatesCategory() = runBlocking {
        val created = fakeRepository.createCustomCategory("Books", "#111111", "star").getOrThrow()
        val displayItem = created.toDisplayItem()

        viewModel.uiState.first { it.customCategories.any { cat -> cat.name == "Books" } }
        viewModel.onEditCategoryClick(displayItem)
        val editingState = viewModel.uiState.first { it.editingCategory != null }
        assertEquals("Books", editingState.editingCategory?.name)

        viewModel.saveCategory("Audiobooks", "#222222", "favorite")

        val state = viewModel.uiState.first { it.customCategories.any { cat -> cat.name == "Audiobooks" } }
        assertNull(state.editingCategory)
        assertTrue(state.customCategories.any { it.name == "Audiobooks" && it.colorHex == "#222222" })
    }

    @Test
    fun onEditCategoryClick_ignoresDefaultCategory() = runBlocking {
        val defaultCat = fakeRepository.categories.first { !it.isCustom }.toDisplayItem()
        viewModel.onEditCategoryClick(defaultCat)

        assertNull(viewModel.uiState.value.editingCategory)
    }

    @Test
    fun onDeleteCategoryClick_andConfirmDelete_removesCustomCategory() = runBlocking {
        val created = fakeRepository.createCustomCategory("Temporary", "#999999", "star").getOrThrow()
        val displayItem = created.toDisplayItem()

        viewModel.uiState.first { it.customCategories.any { cat -> cat.name == "Temporary" } }
        viewModel.onDeleteCategoryClick(displayItem)
        val deletingState = viewModel.uiState.first { it.deletingCategory != null }
        assertEquals("Temporary", deletingState.deletingCategory?.name)

        viewModel.confirmDeleteCategory()

        val state = viewModel.uiState.first { it.deletingCategory == null && it.customCategories.none { cat -> cat.name == "Temporary" } }
        assertNull(state.deletingCategory)
        assertEquals(0, state.customCategories.size)
    }
}
