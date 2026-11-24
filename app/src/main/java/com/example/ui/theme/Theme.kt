package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = FarmGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = FarmGreenContainer,
    onPrimaryContainer = FarmOnGreenContainer,
    secondary = FarmGreenSecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = FarmSecondaryContainer,
    onSecondaryContainer = FarmOnSecondaryContainer,
    tertiary = FarmWarning,
    onTertiary = Color.White,
    tertiaryContainer = FarmWarningContainer,
    onTertiaryContainer = FarmOnWarningContainer,
    background = FarmBackground,
    onBackground = FarmTextPrimary,
    surface = FarmSurface,
    onSurface = FarmTextPrimary,
    surfaceVariant = FarmSurfaceVariant,
    onSurfaceVariant = FarmTextMuted,
    outline = FarmOutline,
    outlineVariant = FarmOutline,
    error = FarmDanger,
    onError = Color.White,
    errorContainer = FarmDangerContainer,
    onErrorContainer = FarmOnDangerContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = FarmGreenSecondary,
    onPrimary = Color(0xFF0A290D),
    primaryContainer = FarmGreenPrimaryDark,
    onPrimaryContainer = FarmGreenContainer,
    secondary = FarmGreenSecondary,
    onSecondary = Color(0xFF0D2E10),
    secondaryContainer = Color(0xFF203B24),
    onSecondaryContainer = FarmGreenContainer,
    tertiary = FarmWarning,
    onTertiary = Color(0xFF3E2200),
    tertiaryContainer = Color(0xFF563400),
    onTertiaryContainer = FarmWarningContainer,
    background = FarmDarkBackground,
    onBackground = FarmDarkTextPrimary,
    surface = FarmDarkSurface,
    onSurface = FarmDarkTextPrimary,
    surfaceVariant = FarmDarkSurfaceVariant,
    onSurfaceVariant = FarmDarkTextMuted,
    outline = FarmDarkOutline,
    outlineVariant = FarmDarkOutline,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = FarmDangerContainer
)

val FarmShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FarmCareTheme(
    themeMode: String = "SYSTEM",
    largeText: Boolean = false,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDark) DarkColorScheme else LightColorScheme,
        typography = farmCareTypography(largeText = largeText),
        shapes = FarmShapes,
        content = content
    )
}
