package com.browseglass.ui.theme

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts the dominant color from a website's favicon (AndroidX Palette) so
 * the glass UI (address bar, tab cards) can tint itself to match the site
 * (Phase 2.14).
 */
object FaviconPalette {

    suspend fun extract(favicon: Bitmap?): FaviconTint? {
        if (favicon == null) return null
        return withContext(Dispatchers.Default) {
            val palette = Palette.from(favicon).generate()

            val dominant = palette.dominantSwatch?.rgb
            val muted = palette.mutedSwatch?.rgb

            if (dominant == null && muted == null) null
            else FaviconTint(
                dominant = dominant?.let { Color(it) } ?: Color.Unspecified,
                muted = muted?.let { Color(it) } ?: Color.Unspecified,
            )
        }
    }

    /**
     * Coverts the dominant color to a translucent glass tint that stays readable
     * over both light and dark chrome.
     */
    fun glassTint(color: Color, alpha: Float = 0.22f): Color =
        color.copy(alpha = alpha)
}