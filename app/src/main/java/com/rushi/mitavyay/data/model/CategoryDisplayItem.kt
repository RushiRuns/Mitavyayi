package com.rushi.mitavyay.data.model

import com.rushi.mitavyay.data.db.Category

data class CategoryDisplayItem(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String,
    val isCustom: Boolean
)

fun Category.toDisplayItem(): CategoryDisplayItem {
    return CategoryDisplayItem(
        id = id,
        name = name,
        icon = icon,
        colorHex = color,
        isCustom = isCustom
    )
}
