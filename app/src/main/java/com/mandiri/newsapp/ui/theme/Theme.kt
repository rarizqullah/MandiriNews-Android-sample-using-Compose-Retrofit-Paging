package com.mandiri.newsapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

/* ============ LIGHT: Putih Hangat + Abu-abu Elegan ============ */
private val LightColors = lightColorScheme(
    primary            = MandiriYellow,
    onPrimary          = Color(0xFF1E1E1E),
    primaryContainer   = MandiriYellowContainer,
    onPrimaryContainer = Color(0xFF201A00),

    surface            = WarmWhite,         // latar utama hangat
    onSurface          = OnLight,           // teks utama gelap
    surfaceVariant     = Gray80,            // kartu/sheet subtle
    onSurfaceVariant   = Gray60,            // teks sekunder

    background         = WarmWhiteHigh,     // window background
    outline            = WarmOutline,       // border, divider
    outlineVariant     = WarmOutline.copy(alpha = 0.8f),

    secondary          = Gray20,
    onSecondary        = Color.White,

    error              = Color(0xFFB3261E),
    onError            = Color.White
)

/* ============ DARK: Hitam Mewah + Abu-abu Netral ============ */
private val DarkColors = darkColorScheme(
    primary            = MandiriYellow,
    onPrimary          = Color.Black,
    primaryContainer   = Color(0xFF3B2D00),
    onPrimaryContainer = MandiriYellowContainer,

    surface            = RichBlack,         // hitam mewah
    onSurface          = OnDark,            // teks lembut (bukan putih murni)
    surfaceVariant     = RichBlackHigh,     // elevated surfaces
    onSurfaceVariant   = OnDark.copy(alpha = 0.7f),

    background         = RichBlack,
    outline            = RichOutline,
    outlineVariant     = RichOutline.copy(alpha = 0.7f),

    secondary          = Color(0xFFCACACA),
    onSecondary        = Color.Black,

    error              = Color(0xFFF2B8B5),
    onError            = Color(0xFF601410)
)

/**
 * Pakai tema sesuai sistem. Komponen “glass” (nav bar) akan otomatis
 * mengambil warna dari MaterialTheme.colorScheme.surface + outline, jadi
 * tampil bening hangat di Light, dan smoky di Dark.
 */
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
