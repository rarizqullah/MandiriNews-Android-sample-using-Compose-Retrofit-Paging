package com.mandiri.newsapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
private val LightColors = lightColorScheme(
    primary            = MandiriYellow,
    onPrimary          = Color(0xFF1E1E1E),
    primaryContainer   = MandiriYellowContainer,
    onPrimaryContainer = Color(0xFF201A00),

    surface            = WarmWhite,
    onSurface          = OnLight,
    surfaceVariant     = Gray80,
    onSurfaceVariant   = Gray60,

    background         = WarmWhiteHigh,
    outline            = WarmOutline,
    outlineVariant     = WarmOutline.copy(alpha = 0.8f),

    secondary          = Gray20,
    onSecondary        = Color.White,

    error              = Color(0xFFB3261E),
    onError            = Color.White
)


private val DarkColors = darkColorScheme(
    primary            = MandiriYellow,
    onPrimary          = Color.Black,
    primaryContainer   = Color(0xFF3B2D00),
    onPrimaryContainer = MandiriYellowContainer,

    surface            = RichBlack,
    onSurface          = OnDark,
    surfaceVariant     = RichBlackHigh,
    onSurfaceVariant   = OnDark.copy(alpha = 0.7f),

    background         = RichBlack,
    outline            = RichOutline,
    outlineVariant     = RichOutline.copy(alpha = 0.7f),

    secondary          = Color(0xFFCACACA),
    onSecondary        = Color.Black,

    error              = Color(0xFFF2B8B5),
    onError            = Color(0xFF601410)
)


@Composable
fun NewsappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = AppTypography,
        content     = content
    )
}
