package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.CategoryDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: String): Category?
    suspend fun addCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: String)
    suspend fun seedDefaultCategories()
}

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAll()

    override suspend fun getCategoryById(id: String): Category? = categoryDao.getById(id)

    override suspend fun addCategory(category: Category) = categoryDao.insert(category)

    override suspend fun updateCategory(category: Category) = categoryDao.update(category)

    override suspend fun deleteCategory(id: String) = categoryDao.deleteById(id)

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
    }
}
