package com.symos.netswitch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NetColorScheme = darkColorScheme(
    primary = Teal,
    onPrimary = Color(0xFF00251F),
    primaryContainer = TealDark,
    onPrimaryContainer = Teal,
    secondary = Cyan,
    onSecondary = Color(0xFF00232C),
    secondaryContainer = Color(0xFF12343D),
    onSecondaryContainer = Cyan,
    tertiary = Orange,
    onTertiary = Color(0xFF2A1700),
    background = Bg,
    onBackground = TextMain,
    surface = Card,
    onSurface = TextMain,
    surfaceVariant = CardHigh,
    onSurfaceVariant = TextDim,
    outline = Line,
    outlineVariant = Line,
    error = Pink
)

@Composable
fun NetSwitchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NetColorScheme,
        typography = NetTypography,
        content = content
    )
}
