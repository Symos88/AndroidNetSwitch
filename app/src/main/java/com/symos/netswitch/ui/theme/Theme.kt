package com.symos.netswitch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NetColorScheme = darkColorScheme(
    primary = Teal,
    onPrimary = androidx.compose.ui.graphics.Color(0xFF00382E),
    primaryContainer = TealDark,
    onPrimaryContainer = Teal,
    secondary = Orange,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF301B00),
    tertiary = Pink,
    onTertiary = androidx.compose.ui.graphics.Color(0xFF3B0014),
    background = Bg,
    onBackground = TextMain,
    surface = Card,
    onSurface = TextMain,
    surfaceVariant = CardHigh,
    onSurfaceVariant = TextDim,
    outline = Line,
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
