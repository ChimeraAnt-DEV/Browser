package com.browseglass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

/**
 * Glassmorphic surface: frosted translucent layer with a subtle inner border,
 * used for the address bar, tab cards, permission dialogs and the swipable
 * glass chrome. Uses the `Haze` library for real content-aware blur.
 */
object Glass {

    /** Shared blur state for the whole screen so every panel blurs the same content. */
    val hazeState = HazeState()

    fun fill(alpha: Float = 0.12f) = Color(0xFF0F1220).copy(alpha = alpha + 0.25f)
    fun stroke(alpha: Float = 0.18f) = Color.White.copy(alpha = alpha)
    val gradient = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.02f))
    )
}

/**
 * Wraps content in a frosted rounded panel.
 *
 * @param shape corner radius
 * @param fillAlpha glass fill opacity
 * @param blurAmount how strong the background blur is (dp)
 */
fun Modifier.glassPanel(
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    fillAlpha: Float = 0.18f,
    blurAmount: Int = 18,
): Modifier = this
    .shadow(24.dp, shape, spotColor = Color.Black.copy(alpha = 0.35f))
    .clip(shape)
    .hazeChild(
        state = Glass.hazeState,
        style = HazeStyle(
            backgroundColor = Glass.fill(fillAlpha),
            tint = HazeTint(color = Color.White.copy(alpha = 0.04f)),
            blurRadius = blurAmount.dp,
        ),
    )
    .background(Glass.gradient, shape)
    .border(1.dp, Glass.stroke(), shape)