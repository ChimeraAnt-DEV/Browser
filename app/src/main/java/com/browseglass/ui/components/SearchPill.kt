package com.browseglass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.browseglass.ui.theme.BastionPalette

/**
 * Bespoke capsule address/search field — replaces Material3's stock
 * OutlinedTextField. A soft inset "pill" with a secure-state leading icon,
 * subtle border and muted placeholder. Cheap to render (no M3 field chrome).
 */
@Composable
fun SearchPill(
    value: String,
    placeholder: String,
    isHttps: Boolean,
    focused: Boolean,
    onValueChange: (String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onGo: () -> Unit = {},
) {
    val textStyle = TextStyle(
        color = BastionPalette.TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
    )

    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(
                if (focused) BastionPalette.InkRaised
                else BastionPalette.Ink.copy(alpha = 0.6f)
            )
            .then(
                if (focused) Modifier.border(1.5.dp, BastionPalette.Brand.copy(alpha = 0.7f), RoundedCornerShape(23.dp))
                else Modifier.border(1.dp, BastionPalette.Border, RoundedCornerShape(23.dp))
            )
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .then(Modifier.noRippleClickable(onClick)),
        ) {
            // Leading icon chip: a subtle circle; HTTPS shown as a green lock.
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        if (focused) BastionPalette.InkSoft
                        else BastionPalette.InkSoft.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isHttps) Icons.Default.Lock else Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (isHttps) BastionPalette.Success else BastionPalette.Warning,
                    modifier = Modifier.size(16.dp),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .then(Modifier.noRippleClickable(onClick)),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty() && !focused) {
                    Text(
                        placeholder,
                        style = textStyle.copy(color = BastionPalette.TextMuted),
                        maxLines = 1,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    textStyle = textStyle,
                    cursorBrush = SolidColor(BastionPalette.Brand),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onGo() }),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 8.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) { inner() }
                    },
                )
            }

            // Trailing search glyph — a "go" pill while focused, tappable.
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (focused) BastionPalette.Brand
                        else Color.Transparent
                    )
                    .noRippleClickable(onGo),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = if (focused) BastionPalette.Ink else BastionPalette.TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/**
 * Lightweight clickable without ripple (cheaper to render and visually
 * quieter). Uses the no-indication clickable overload, so no interaction
 * source state is tracked.
 */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    clickable(enabled = true, onClickLabel = null, role = null, onClick = onClick)