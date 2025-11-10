package com.chronoplan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ✅ HIGH CONTRAST COLORS - Text selalu terlihat jelas
val BackgroundLight = Color(0xFFF0F5FF)
val BackgroundWhite = Color(0xFFFFFFFF)

// Text dengan kontras SANGAT tinggi
val TextPrimary = Color(0xFF000000) // Pure black - selalu terlihat
val TextSecondary = Color(0xFF424242) // Dark gray - kontras tinggi
val TextTertiary = Color(0xFF616161) // Medium gray
val TextDisabled = Color(0xFF9E9E9E) // Light gray untuk disabled

// Primary colors - Biru lebih gelap untuk kontras
val PrimaryBlue = Color(0xFF0D47A1) // Darker blue
val PrimaryBlueDark = Color(0xFF002171)
val PrimaryBlueLight = Color(0xFF1976D2)
val PrimaryContainer = Color(0xFFBFE9FF)

// Status colors
val SuccessGreen = Color(0xFF1B5E20) // Darker green
val WarningYellow = Color(0xFFF57F17) // Darker yellow
val ErrorRed = Color(0xFFB71C1C) // Darker red

// Accent
val AccentOrange = Color(0xFFE65100)
val AccentPurple = Color(0xFF4A148C)

// ✅ FIXED COLOR SCHEME
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = Color(0xFF001F2A),

    secondary = Color(0xFF37474F),
    onSecondary = Color.White,

    background = BackgroundLight,
    onBackground = TextPrimary, // ✅ BLACK TEXT ON BACKGROUND

    surface = BackgroundWhite,
    onSurface = TextPrimary, // ✅ BLACK TEXT ON WHITE

    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = TextSecondary, // ✅ DARK GRAY

    error = ErrorRed,
    onError = Color.White,

    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0)
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