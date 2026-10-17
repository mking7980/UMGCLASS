package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UmgDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = PureBlack,
    primaryContainer = GoldSubtle,
    onPrimaryContainer = GoldLight,
    secondary = EmeraldGreen,
    onSecondary = PureBlack,
    secondaryContainer = EmeraldSubtle,
    onSecondaryContainer = EmeraldGreen,
    tertiary = DeepBlueEnd,
    onTertiary = TextPrimary,
    background = PureBlack,
    onBackground = TextPrimary,
    surface = DarkSlate,
    onSurface = TextPrimary,
    surfaceVariant = DarkSlateCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF383E4E)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = UmgDarkColorScheme,
        typography = Typography,
        content = content
    )
}
