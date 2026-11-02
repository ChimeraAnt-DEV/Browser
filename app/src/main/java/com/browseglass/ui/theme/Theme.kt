package com.browseglass.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class BastionColors(
    val glass: Color = Color.White.copy(alpha = 0.10f),
    val glassBorder: Color = Color.White.copy(alpha = 0.18f),
    val glassHighlight: Color = Color.White.copy(alpha = 0.08f),
    val surface: Color = Color(0xFF141A2E),
    val textPrimary: Color = Color(0xFFF5F7FF),
    val textSecondary: Color = Color(0xFF9AA3C0),
    val accent: Color = Color(0xFF38BDF8),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    secondary = Color(0xFF7DD3FC),
    background = Color(0xFF0F1220),
    surface = Color(0xFF141A2E),
    surfaceVariant = Color(0xFF1B2445),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    secondary = Color(0xFF38BDF8),
    background = Color(0xFFF5F7FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE2E8F0),
)

@Composable
fun BastionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

/** Dominant tint extracted from a favicon (Palette API result). */
@Immutable
data class FaviconTint(
    val dominant: Color = Color.Unspecified,
    val muted: Color = Color.Unspecified,
)