package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.datastore.PreferencesRepository
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.CategoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getCustomCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: String): Category?
    suspend fun getCategoryByName(name: String): Category?
    suspend fun addCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: String)
    suspend fun seedDefaultCategories()

    suspend fun createCustomCategory(name: String, colorHex: String, icon: String): Result<Category>
    suspend fun updateCustomCategory(id: String, name: String, colorHex: String, icon: String): Result<Unit>
    suspend fun canDeleteCategory(id: String): Boolean
}

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val preferencesRepository: PreferencesRepository
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAll().onEach { list ->
            if (list.isNotEmpty()) {
                val hasSeeded = preferencesRepository.hasSeededDefaultCategories.first()
                if (!hasSeeded) {
                    preferencesRepository.setHasSeededDefaultCategories(true)
                }
            } else {
                val hasSeeded = preferencesRepository.hasSeededDefaultCategories.first()
                if (!hasSeeded) {
                    seedDefaultCategories()
                }
            }
        }

    override fun getCustomCategories(): Flow<List<Category>> = categoryDao.getCustomCategories()

    override suspend fun getCategoryById(id: String): Category? = categoryDao.getById(id)

    override suspend fun getCategoryByName(name: String): Category? = categoryDao.getByName(name)

    override suspend fun addCategory(category: Category) = categoryDao.insert(category)

    override suspend fun updateCategory(category: Category) = categoryDao.update(category)

    override suspend fun deleteCategory(id: String) {
        val cat = categoryDao.getById(id)
        if (cat != null) {
            categoryDao.deleteById(id)
        }
    }

    override suspend fun canDeleteCategory(id: String): Boolean {
        val cat = categoryDao.getById(id)
        return cat != null
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
        val existing = categoryDao.getByName(trimmed)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Category '$trimmed' already exists"))
        }

        val category = Category(
            id = UUID.randomUUID().toString(),
            name = trimmed,
            icon = icon.ifBlank { "star" },
            color = colorHex.ifBlank { "#006C4C" },
            isCustom = true
        )
        categoryDao.insert(category)
        return Result.success(category)
    }

    override suspend fun updateCustomCategory(
        id: String,
        name: String,
        colorHex: String,
        icon: String
    ): Result<Unit> {
        val cat = categoryDao.getById(id)
            ?: return Result.failure(IllegalArgumentException("Category not found"))

        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Category name cannot be blank"))
        }

        val existing = categoryDao.getByName(trimmed)
        if (existing != null && existing.id != id) {
            return Result.failure(IllegalArgumentException("Category '$trimmed' already exists"))
        }

        val updated = cat.copy(
            name = trimmed,
            color = colorHex.ifBlank { cat.color },
            icon = icon.ifBlank { cat.icon }
        )
        categoryDao.update(updated)
        return Result.success(Unit)
    }

    override suspend fun seedDefaultCategories() {
        val defaults = listOf(
            Category(id = "cat_food", name = "Food & Dining", icon = "restaurant", color = "#FF7043", isCustom = false),
            Category(id = "cat_groceries", name = "Groceries", icon = "shopping_cart", color = "#42A5F5", isCustom = false),
            Category(id = "cat_transport", name = "Transport", icon = "directions_car", color = "#AB47BC", isCustom = false),
            Category(id = "cat_bills", name = "Bills & Utilities", icon = "receipt", color = "#EC407A", isCustom = false),
            Category(id = "cat_shopping", name = "Shopping", icon = "storefront", color = "#26A69A", isCustom = false),
            Category(id = "cat_health", name = "Healthcare", icon = "medical_services", color = "#EF5350", isCustom = false),
            Category(id = "cat_entertainment", name = "Entertainment", icon = "movie", color = "#FFA726", isCustom = false),
            Category(id = "cat_salary", name = "Salary & Income", icon = "payments", color = "#66BB6A", isCustom = false),
            Category(id = "cat_investment", name = "Investments", icon = "trending_up", color = "#29B6F6", isCustom = false),
            Category(id = "cat_transfer", name = "Transfer", icon = "swap_horiz", color = "#78909C", isCustom = false),
            Category(id = "cat_other", name = "Other", icon = "more_horiz", color = "#8D6E63", isCustom = false)
        )
        categoryDao.insertAll(defaults)
        preferencesRepository.setHasSeededDefaultCategories(true)
    }
}
