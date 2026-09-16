package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VfRed,
    onPrimary = Color.White,
    primaryContainer = VfRedDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF22C55E),
    onSecondary = Color.Black,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkCard,
    onSurface = DarkText,
    surfaceVariant = DarkCardAlt,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkCardBorder,
    error = DarkErrorFg,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = VfRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCDA),
    onPrimaryContainer = VfRedDark,
    secondary = Color(0xFF16A34A),
    onSecondary = Color.White,
    background = LightBg,
    onBackground = LightText,
    surface = LightCard,
    onSurface = LightText,
    surfaceVariant = LightCardAlt,
    onSurfaceVariant = LightTextMuted,
    outline = LightCardBorder,
    error = LightErrorFg,
    onError = Color.White
)

@Composable
fun VodafoneCashTheme(
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
