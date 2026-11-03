package com.browseglass.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---- Brand palette -------------------------------------------------------
object BastionPalette {
    val Ink = Color(0xFF070B14)           // deep background
    val InkRaised = Color(0xFF0C1220)     // panels
    val InkSoft = Color(0xFF131B2E)       // hover / cards
    val GlassFill = Color(0xFF161F36)
    val Border = Color(0xFF2A3550)
    val BorderSoft = Color(0xFF1E283F)

    val TextPrimary = Color(0xFFF2F5FF)
    val TextSecondary = Color(0xFF97A3BF)
    val TextMuted = Color(0xFF606B85)

    // Signature gradient (used on the hero, active pill, accents).
    val BrandStart = Color(0xFF5EEAD4)
    val BrandEnd = Color(0xFF38BDF8)
    val Brand = Color(0xFF38BDF8)

    val Success = Color(0xFF34D399)
    val Warning = Color(0xFFFBBF24)

    val brandGradient = Brush.linearGradient(listOf(BrandStart, BrandEnd))

    // Distinct favicon-tile colors.
    val TileColors = listOf(
        Color(0xFF10A37F), Color(0xFF6366F1), Color(0xFFEF4444), Color(0xFFF59E0B),
        Color(0xFF06B6D4), Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF22C55E),
    )
}

private val DarkScheme = darkColorScheme(
    primary = BastionPalette.Brand,
    onPrimary = Color(0xFF03101C),
    primaryContainer = Color(0xFF0E2B3D),
    onPrimaryContainer = BastionPalette.BrandStart,
    secondary = BastionPalette.BrandEnd,
    tertiary = BastionPalette.BrandStart,
    background = BastionPalette.Ink,
    onBackground = BastionPalette.TextPrimary,
    surface = BastionPalette.InkRaised,
    onSurface = BastionPalette.TextPrimary,
    surfaceVariant = BastionPalette.InkSoft,
    onSurfaceVariant = BastionPalette.TextSecondary,
    outline = BastionPalette.Border,
    outlineVariant = BastionPalette.BorderSoft,
    error = Color(0xFFF87171),
)

// Rounded, airy, modern type — nothing like stock M3.
private val BastionTypography = Typography(
    displayMedium = TextStyle(
        fontSize = 44.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp,
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp,
    ),
    headlineSmall = TextStyle(
        fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
    ),
    titleSmall = TextStyle(
        fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
)

/** Extra chrome-level tokens consumed by custom components. */
@Immutable
data class BastionChrome(
    val ink: Color,
    val inkRaised: Color,
    val glassFill: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val brand: Color,
    val brandStart: Color,
    val brandEnd: Color,
)

val LocalBastionChrome = staticCompositionLocalOf {
    BastionChrome(
        ink = BastionPalette.Ink,
        inkRaised = BastionPalette.InkRaised,
        glassFill = BastionPalette.GlassFill,
        border = BastionPalette.Border,
        textPrimary = BastionPalette.TextPrimary,
        textSecondary = BastionPalette.TextSecondary,
        textMuted = BastionPalette.TextMuted,
        brand = BastionPalette.Brand,
        brandStart = BastionPalette.BrandStart,
        brandEnd = BastionPalette.BrandEnd,
    )
}

@Composable
fun BastionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Bastion is a dark-first premium browser; ignore light/dynamic for the
    // chrome to keep the brand consistent.
    val chrome = BastionChrome(
        ink = BastionPalette.Ink,
        inkRaised = BastionPalette.InkRaised,
        glassFill = BastionPalette.GlassFill,
        border = BastionPalette.Border,
        textPrimary = BastionPalette.TextPrimary,
        textSecondary = BastionPalette.TextSecondary,
        textMuted = BastionPalette.TextMuted,
        brand = BastionPalette.Brand,
        brandStart = BastionPalette.BrandStart,
        brandEnd = BastionPalette.BrandEnd,
    )
    CompositionLocalProvider(LocalBastionChrome provides chrome) {
        MaterialTheme(
            colorScheme = DarkScheme,
            typography = BastionTypography,
            content = content,
        )
    }
}

/** Dominant tint extracted from a favicon (Palette API result). */
@Immutable
data class FaviconTint(
    val dominant: Color = Color.Unspecified,
    val muted: Color = Color.Unspecified,
)