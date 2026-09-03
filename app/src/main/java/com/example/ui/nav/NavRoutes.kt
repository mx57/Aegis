package com.example.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val titleRu: String,
    val icon: ImageVector
) {
    data object NameStave : BottomNavItem("name_stave", "Имя", Icons.Default.TextFields)
    data object Builder : BottomNavItem("builder", "Конструктор", Icons.Default.Build)
    data object Library : BottomNavItem("library", "Библиотека", Icons.Default.Bookmark)
    data object Encyclopedia : BottomNavItem("encyclopedia", "Руны", Icons.Default.MenuBook)
    data object Divination : BottomNavItem("divination", "Гадание", Icons.Default.AutoAwesome)
}

object AppDestinations {
    const val ONBOARDING = "onboarding"
    const val SKETCH = "sketch/{runeIds}/{layoutType}"
    const val TRY_ON = "try_on/{runeIds}/{layoutType}/{seed}/{style}"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val AI_TATTOO_GALLERY = "ai_tattoo_gallery"

    fun buildSketchRoute(runeIds: List<String>, layoutType: String): String =
        "sketch/${runeIds.joinToString(",")}/$layoutType"

    fun buildTryOnRoute(runeIds: List<String>, layoutType: String, seed: Long, style: String): String =
        "try_on/${runeIds.joinToString(",")}/$layoutType/$seed/$style"
}
