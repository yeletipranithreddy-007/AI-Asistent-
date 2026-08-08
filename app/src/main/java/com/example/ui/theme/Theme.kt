package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = ObsidianDark,
    primaryContainer = Color(0xFF003847),
    onPrimaryContainer = CyberCyan,
    secondary = CyberPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF280057),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = NeonTeal,
    onTertiary = ObsidianDark,
    background = ObsidianDark,
    onBackground = TextPrimaryDark,
    surface = ObsidianCard,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianBorder,
    onSurfaceVariant = TextSecondaryDark,
    outline = TextMutedDark,
    error = NeonPink,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00687A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA6EEFF),
    onPrimaryContainer = Color(0xFF001F26),
    secondary = CyberPurple,
    onSecondary = Color.White,
    background = LightCanvas,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightBorder,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFF94A3B8)
)

@Composable
fun NaniAiTheme(
    darkTheme: Boolean = true, // Default to futuristic dark theme
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

