package com.example.digitaldetox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = IndigoSecondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = IndigoContainer,
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = SlateLight,
    onTertiary = Color(0xFF0F172A),
    tertiaryContainer = SlateMuted,
    onTertiaryContainer = Color(0xFFF8FAFC),
    error = ErrorRose,
    onError = Color(0xFF4C0519),
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFFFFE4E6),
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    inverseSurface = LightSurface,
    inverseOnSurface = TextPrimaryLight,
    inversePrimary = EmeraldPrimaryLight
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF4F46E5),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF312E81),
    tertiary = SlateMuted,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF1F5F9),
    onTertiaryContainer = Color(0xFF0F172A),
    error = Color(0xFFE11D48),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF881337),
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    outlineVariant = LightBorderSubtle,
    inverseSurface = DarkSurface,
    inverseOnSurface = TextPrimaryDark,
    inversePrimary = EmeraldPrimary
)

@Composable
fun DigitalDetoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
