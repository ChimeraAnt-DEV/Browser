package com.browseglass.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Skeleton loader: a glass panel with a shimmer sweep. Shown over the web
 * surface while a page loads so the user never sees a blank white screen
 * (Phase 2.13).
 */
@Composable
fun GlassShimmer(
    modifier: Modifier = Modifier,
    lines: Int = 5,
    height: Int = 16,
) {
    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val dx by shimmer.animateFloat(
        initialValue = -600f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerX",
    )

    fun shimmerBrush(base: Color): Brush = Brush.linearGradient(
        colors = listOf(
            base.copy(alpha = 0.55f),
            Color.White.copy(alpha = 0.10f),
            base.copy(alpha = 0.55f),
        ),
        start = Offset(dx, 0f),
        end = Offset(dx + 260f, 0f),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(fillAlpha = 0.20f)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Fake favicon + title line.
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(24.dp)
                    .height(24.dp)
                    .background(shimmerBrush(Color(0xFF3A4468)), RoundedCornerShape(6.dp))
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(12.dp)
                    .background(shimmerBrush(Color(0xFF3A4468)), RoundedCornerShape(6.dp))
            )
        }
        repeat(lines) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(height = height.dp)
                    .background(shimmerBrush(Color(0xFF3A4468)), RoundedCornerShape(6.dp))
            )
        }
    }
}