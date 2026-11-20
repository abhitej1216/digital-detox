package com.example.digitaldetox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightPrimary = Color(0xFF4A148C)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFE8DEFF)
private val LightOnPrimaryContainer = Color(0xFF1D0021)
private val LightSecondary = Color(0xFF42494E)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFD4E1E8)
private val LightOnSecondaryContainer = Color(0xFF0D161B)
private val LightTertiary = Color(0xFF7C4BFF)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightTertiaryContainer = Color(0xFFF3E5FF)
private val LightOnTertiaryContainer = Color(0xFF380068)
private val LightError = Color(0xFFB00020)
private val LightOnError = Color(0xFFFFFFFF)
private val LightErrorContainer = Color(0xFFCDBDB5)
private val LightOnErrorContainer = Color(0xFF49000F)
private val LightBackground = Color(0xFFF9F9F9)
private val LightOnBackground = Color(0xFF0A0A0A)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF0A0A0A)
private val LightSurfaceVariant = Color(0xFFE0E0E0)
private val LightOnSurfaceVariant = Color(0xFF424242)
private val LightOutline = Color(0xFF797979)
private val LightInverseOn = Color(0xFFF0F0F0)
private val LightInverseSurface = Color(0xFF252525)
private val LightInverseTop = Color(0xFF381E57)

private val DarkPrimary = Color(0xFFD0BAFF)
private val DarkOnPrimary = Color(0xFF2D0049)
private val DarkPrimaryContainer = Color(0xFF461F6F)
private val DarkOnPrimaryContainer = Color(0xFFEEB2FF)
private val DarkSecondary = Color(0xFFB6C0C9)
private val DarkOnSecondary = Color(0xFF23292E)
private val DarkSecondaryContainer = Color(0xFF39444A)
private val DarkOnSecondaryContainer = Color(0xFFD4E1E8)
private val DarkTertiary = Color(0xFFC695FF)
private val DarkOnTertiary = Color(0xFF380068)
private val DarkTertiaryContainer = Color(0xFF58329B)
private val DarkOnTertiaryContainer = Color(0xFFFFEBFF)
private val DarkError = Color(0xFFF2B8B5)
private val DarkOnError = Color(0xFF600F0C)
private val DarkErrorContainer = Color(0xFF8C4141)
private val DarkOnErrorContainer = Color(0xFFDF7C79)
private val DarkBackground = Color(0xFF121212)
private val DarkOnBackground = Color(0xFFF9F9F9)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkOnSurface = Color(0xFFF9F9F9)
private val DarkSurfaceVariant = Color(0xFF454545)
private val DarkOnSurfaceVariant = Color(0xFFB6B6B6)
private val DarkOutline = Color(0xFF8F8F8F)
private val DarkInverseOn = Color(0xFF0A0A0A)
private val DarkInverseSurface = Color(0xFFF0F0F0)
private val DarkInverseTop = Color(0xFFD0BAFF)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    inverseOnSurface = LightInverseOn,
    inverseSurface = LightInverseSurface,
    inversePrimary = LightInverseTop,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    inverseOnSurface = DarkInverseOn,
    inverseSurface = DarkInverseSurface,
    inversePrimary = DarkInverseTop,
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
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}