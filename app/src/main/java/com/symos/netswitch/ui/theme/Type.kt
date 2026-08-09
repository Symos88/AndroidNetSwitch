package com.symos.netswitch.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppFont = FontFamily.SansSerif

val NetTypography = Typography(
    displayLarge = TextStyle(AppFont, FontWeight.Bold, 42.sp, letterSpacing = (-1.2).sp),
    headlineMedium = TextStyle(AppFont, FontWeight.Bold, 24.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(AppFont, FontWeight.Bold, 21.sp, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(AppFont, FontWeight.Bold, 20.sp, letterSpacing = 0.2.sp),
    titleMedium = TextStyle(AppFont, FontWeight.SemiBold, 15.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(AppFont, FontWeight.SemiBold, 11.sp, letterSpacing = 0.7.sp),
    labelSmall = TextStyle(AppFont, FontWeight.SemiBold, 10.sp, letterSpacing = 1.1.sp),
    bodyMedium = TextStyle(AppFont, FontWeight.Normal, 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(AppFont, FontWeight.Normal, 12.sp, lineHeight = 17.sp)
)
