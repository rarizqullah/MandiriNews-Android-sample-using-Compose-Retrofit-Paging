package com.mandiri.newsapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val poppins = FontFamily.SansSerif

val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    titleLarge = TextStyle(
        fontFamily = poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
)
