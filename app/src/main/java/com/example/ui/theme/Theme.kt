package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GeometricPrimaryDark,
    onPrimary = GeometricOnPrimaryDark,
    primaryContainer = GeometricPrimaryContainerDark,
    onPrimaryContainer = GeometricOnPrimaryContainerDark,
    secondary = GeometricSecondaryDark,
    onSecondary = GeometricOnSecondaryDark,
    secondaryContainer = GeometricSecondaryContainerDark,
    onSecondaryContainer = GeometricOnSecondaryContainerDark,
    tertiary = GeometricTertiaryDark,
    onTertiary = GeometricOnTertiaryDark,
    background = GeometricBackgroundDark,
    onBackground = GeometricOnBackgroundDark,
    surface = GeometricSurfaceDark,
    onSurface = GeometricOnSurfaceDark,
    surfaceVariant = GeometricSurfaceVariantDark,
    onSurfaceVariant = GeometricOnSurfaceVariantDark,
    outline = GeometricOutlineDark,
    outlineVariant = GeometricOutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = GeometricPrimary,
    onPrimary = GeometricOnPrimary,
    primaryContainer = GeometricPrimaryContainer,
    onPrimaryContainer = GeometricOnPrimaryContainer,
    secondary = GeometricSecondary,
    onSecondary = GeometricOnSecondary,
    secondaryContainer = GeometricSecondaryContainer,
    onSecondaryContainer = GeometricOnSecondaryContainer,
    tertiary = GeometricTertiary,
    onTertiary = GeometricOnTertiary,
    background = GeometricBackground,
    onBackground = GeometricOnBackground,
    surface = GeometricSurface,
    onSurface = GeometricOnSurface,
    surfaceVariant = GeometricSurfaceVariant,
    onSurfaceVariant = GeometricOnSurfaceVariant,
    outline = GeometricOutline,
    outlineVariant = GeometricOutlineVariant
)

@Composable
fun RunicStaveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
