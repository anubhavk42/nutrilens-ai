package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BentoPurpleMedium,
    onPrimary = Color.White,
    primaryContainer = BentoPurpleContainer,
    onPrimaryContainer = BentoPurpleDark,
    secondary = BentoTextGrey,
    onSecondary = Color.White,
    secondaryContainer = BentoSecondaryContainer,
    onSecondaryContainer = BentoPurpleDark,
    tertiary = SpiceBerry,
    onTertiary = Color.White,
    background = BentoBackground,
    onBackground = BentoTextDark,
    surface = BentoBackground,
    onSurface = BentoTextDark,
    surfaceVariant = BentoSecondaryContainer,
    onSurfaceVariant = BentoTextGrey,
    outline = BentoBorderGrey,
    outlineVariant = OutlineVariantGrey
)

private val DarkColorScheme = lightColorScheme(
    primary = BentoPurpleMedium,
    onPrimary = Color.White,
    primaryContainer = BentoPurpleContainer,
    onPrimaryContainer = BentoPurpleDark,
    secondary = BentoTextGrey,
    onSecondary = Color.White,
    secondaryContainer = BentoSecondaryContainer,
    onSecondaryContainer = BentoPurpleDark,
    tertiary = SpiceBerry,
    onTertiary = Color.White,
    background = BentoBackground,
    onBackground = BentoTextDark,
    surface = BentoBackground,
    onSurface = BentoTextDark,
    surfaceVariant = BentoSecondaryContainer,
    onSurfaceVariant = BentoTextGrey,
    outline = BentoBorderGrey,
    outlineVariant = OutlineVariantGrey
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
