package com.browseglass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.browseglass.ui.theme.BastionPalette

/**
 * A quiet glass circle-icon button used across the chrome (tab bar, tab grid,
 * settings). No ripple, custom border — cheaper and more premium than M3's
 * IconButton.
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 40,
    tint: Color = BastionPalette.TextPrimary,
    background: Color = BastionPalette.InkSoft.copy(alpha = 0.65f),
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, BastionPalette.Border.copy(alpha = 0.6f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size((size * 0.44).dp),
        )
    }
}