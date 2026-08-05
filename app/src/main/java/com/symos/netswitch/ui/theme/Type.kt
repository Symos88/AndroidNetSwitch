package com.symos.netswitch.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val NetTypography = Typography(
    displayLarge = TextStyle(FontFamily.Monospace, FontWeight.Bold, 42.sp, letterSpacing = (-1).sp),
    headlineMedium = TextStyle(FontFamily.Monospace, FontWeight.Bold, 24.sp),
    titleLarge = TextStyle(FontFamily.Monospace, FontWeight.ExtraBold, 20.sp, letterSpacing = 3.sp),
    titleMedium = TextStyle(FontFamily.Monospace, FontWeight.SemiBold, 14.sp, letterSpacing = 1.sp),
    labelSmall = TextStyle(FontFamily.Monospace, FontWeight.Medium, 10.sp, letterSpacing = 2.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp)
)
