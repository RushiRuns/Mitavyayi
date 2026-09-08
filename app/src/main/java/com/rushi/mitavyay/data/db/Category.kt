package com.rushi.mitavyay.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing expense and income categories.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String = "category_default",
    val color: String = "#006C4C",
    val isCustom: Boolean = false
)
