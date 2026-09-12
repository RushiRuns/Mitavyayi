package com.rushi.mitavyay.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconOption(
    val id: String,
    val icon: ImageVector,
    val label: String
)

val CategoryColorOptions = listOf(
    "#FF7043", // Coral / Food
    "#42A5F5", // Blue / Groceries
    "#AB47BC", // Purple / Transport
    "#EC407A", // Pink / Bills
    "#26A69A", // Teal / Shopping
    "#EF5350", // Red / Healthcare
    "#FFA726", // Orange / Entertainment
    "#66BB6A", // Green / Salary
    "#29B6F6", // Light Blue / Investments
    "#78909C", // Blue Grey / Transfer
    "#8D6E63", // Brown / Other
    "#5C6BC0", // Indigo
    "#8E24AA", // Deep Purple
    "#00897B", // Deep Teal
    "#FDD835", // Yellow
    "#D81B60"  // Deep Pink
)

val CategoryIconOptions = listOf(
    CategoryIconOption("shopping_cart", Icons.Default.ShoppingCart, "Shopping"),
    CategoryIconOption("star", Icons.Default.Star, "Special"),
    CategoryIconOption("home", Icons.Default.Home, "Home"),
    CategoryIconOption("favorite", Icons.Default.Favorite, "Health"),
    CategoryIconOption("build", Icons.Default.Build, "Maintenance"),
    CategoryIconOption("date_range", Icons.Default.DateRange, "Schedule"),
    CategoryIconOption("notifications", Icons.Default.Notifications, "Bills"),
    CategoryIconOption("place", Icons.Default.Place, "Travel"),
    CategoryIconOption("phone", Icons.Default.Phone, "Mobile"),
    CategoryIconOption("email", Icons.Default.Email, "Digital"),
    CategoryIconOption("person", Icons.Default.Person, "Personal"),
    CategoryIconOption("thumb_up", Icons.Default.ThumbUp, "Leisure"),
    CategoryIconOption("account_circle", Icons.Default.AccountCircle, "Self")
)

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "shopping_cart", "cart", "storefront" -> Icons.Default.ShoppingCart
        "star" -> Icons.Default.Star
        "home" -> Icons.Default.Home
        "favorite", "heart", "medical_services" -> Icons.Default.Favorite
        "build", "tools" -> Icons.Default.Build
        "date_range", "calendar" -> Icons.Default.DateRange
        "notifications", "receipt", "bell" -> Icons.Default.Notifications
        "place", "location", "directions_car" -> Icons.Default.Place
        "phone" -> Icons.Default.Phone
        "email", "mail" -> Icons.Default.Email
        "person", "account" -> Icons.Default.Person
        "thumb_up" -> Icons.Default.ThumbUp
        "account_circle" -> Icons.Default.AccountCircle
        else -> Icons.Default.Star
    }
}
