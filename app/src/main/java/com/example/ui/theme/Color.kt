package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// UMG Theme Colors
val PureBlack = Color(0xFF000000)
val DarkSlate = Color(0xFF121212)
val DarkSlateCard = Color(0xFF181B20)
val DarkSlateElevated = Color(0xFF222630)
val DarkBorder = Color(0xFF2C313E)

// Gold Accents
val GoldPrimary = Color(0xFFD4AF37)
val GoldLight = Color(0xFFF3C642)
val GoldDark = Color(0xFFAA841C)
val GoldSubtle = Color(0x2AD4AF37)

// Emerald Green Accents
val EmeraldGreen = Color(0xFF4CAF50)
val EmeraldDark = Color(0xFF2E7D32)
val EmeraldSubtle = Color(0x2A4CAF50)

// Deep Blue
val DeepBlueStart = Color(0xFF1E3C72)
val DeepBlueEnd = Color(0xFF2A5298)

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA0A6B2)
val TextMuted = Color(0xFF6B7280)
val TextGold = Color(0xFFF3C642)

// Gradients
val DeepBlueGradient = Brush.horizontalGradient(listOf(DeepBlueStart, DeepBlueEnd))
val GoldGradient = Brush.horizontalGradient(listOf(GoldDark, GoldLight))
val CardGradient = Brush.verticalGradient(listOf(Color(0xFF1C2028), Color(0xFF121419)))
val HeaderGradient = Brush.verticalGradient(listOf(Color(0xFF14171E), Color(0xFF090A0C)))
