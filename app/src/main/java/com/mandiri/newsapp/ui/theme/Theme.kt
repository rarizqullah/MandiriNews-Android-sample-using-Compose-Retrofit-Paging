package com.mandiri.newsapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Yellow = Color(0xFFFFC400)
private val YellowContainer = Color(0xFFFFF3C4)

private val LightColors = lightColorScheme(
    primary = Yellow,
    onPrimary = Color(0xFF1E1E1E),
    primaryContainer = YellowContainer,
    onPrimaryContainer = Color(0xFF201A00),

    secondary = Color(0xFF5F5F5F),
    onSecondary = Color.White,

    surface = Color(0xFFF7F7FA),
    onSurface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFFEDEFF3),
    onSurfaceVariant = Color(0xFF51565E),

    outline = Color(0xFFD7DAE0),
    outlineVariant = Color(0xFFE1E4EA)
)

private val DarkColors = darkColorScheme(
    primary = Yellow,
    onPrimary = Color.Black
)

@Composable
fun NewsappTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography, // biarkan sesuai punyamu
        content = content
    )
}
