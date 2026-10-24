package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SahityaRed,
    onPrimary = Color.White,
    primaryContainer = SahityaRedDark,
    onPrimaryContainer = Color.White,
    secondary = SahityaGold,
    onSecondary = Color.Black,
    secondaryContainer = SahityaGoldDark,
    onSecondaryContainer = Color.White,
    background = PitchBlack,
    onBackground = TextCrispWhite,
    surface = SurfaceDark,
    onSurface = TextCrispWhite,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMutedGray,
    outline = CardBorderDark
)

@Composable
fun SahityaTheme(
    darkTheme: Boolean = true, // Force dark mode as requested for pitch black aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
