package com.chronoplan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBFE9FF),
    onPrimaryContainer = Color(0xFF001F2A),

    secondary = Color(0xFF4A6572),
    onSecondary = Color.White,

    background = BackgroundLight,
    onBackground = TextDark, // Text utama

    surface = BackgroundWhite,
    onSurface = TextDark, // Text di card

    surfaceVariant = Color(0xFFDFE3EB),
    onSurfaceVariant = TextGray, // Text secondary

    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun ChronoplanTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}