package com.browseglass.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.browseglass.engine.BrowserEngine
import com.browseglass.engine.Tab
import com.browseglass.ui.components.GlassIconButton
import com.browseglass.ui.theme.BastionPalette

/**
 * Mission-Control tab switcher: flat premium cards (hairline border, no
 * per-card blur — far cheaper than glass over a full-screen grid), live
 * thumbnail where available, "+" new-tab tile.
 */
@Composable
fun TabGrid(
    engine: BrowserEngine,
    onCloseTab: (Long) -> Unit,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
    onNewTab: () -> Unit = {
        val h = engine.createSession(private = false)
        onSelect(h.tab.id)
    },
) {
    val sessions = engine.sessions.all
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BastionPalette.Ink)
            .statusBarsPadding()
            .padding(top = 12.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = "Close tabs",
                onClick = onDismiss,
                size = 38,
            )
            Text(
                "${sessions.size} tabs",
                color = BastionPalette.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            GlassIconButton(
                icon = Icons.Default.Add,
                contentDescription = "New tab",
                onClick = onNewTab,
                size = 38,
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(sessions, key = { it.tab.id }) { holder ->
                TabCard(
                    tab = holder.tab,
                    active = holder.tab.id == engine.sessions.currentId,
                    onClick = { onSelect(holder.tab.id) },
                    onClose = { onCloseTab(holder.tab.id) },
                )
            }
            item(key = "new") {
                NewTabTile(onClick = onNewTab)
            }
        }
    }
}

@Composable
private fun TabCard(
    tab: Tab,
    active: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    val cardBg = if (active) BastionPalette.InkSoft else BastionPalette.InkRaised
    val cardBorder = if (active) BastionPalette.Brand.copy(alpha = 0.6f) else BastionPalette.BorderSoft

    Column(
        modifier = Modifier
            .clip(shape)
            .background(cardBg)
            .border(1.dp, cardBorder, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(12.dp),
    ) {
        // Preview area (thumbnail or a gradient placeholder).
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(tabCardGradient(tab.id.toInt(), tab.suspended)),
            contentAlignment = Alignment.Center,
        ) {
            tab.thumbnail?.let {
                androidx.compose.foundation.Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            } ?: Text(
                if (tab.suspended) "Suspended" else tab.url.hostnameOnly() ?: "New tab",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 12.sp,
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    tab.title.ifEmpty { "New tab" },
                    color = if (active) BastionPalette.BrandStart else BastionPalette.TextPrimary,
                    maxLines = 1,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    tab.url.hostnameOnly() ?: " ",
                    color = BastionPalette.TextMuted,
                    maxLines = 1,
                    fontSize = 11.sp,
                )
            }
            GlassIconButton(
                icon = Icons.Default.Close,
                contentDescription = "Close tab",
                onClick = onClose,
                size = 30,
            )
        }
    }
}

@Composable
private fun tabCardGradient(seed: Int, suspended: Boolean): Brush {
    if (suspended) return Brush.verticalGradient(listOf(BastionPalette.Ink, BastionPalette.Ink))
    val palettes = listOf(
        listOf(Color(0xFF0E2B3D), Color(0xFF0C1220)),
        listOf(Color(0xFF1E2440), Color(0xFF0C1220)),
        listOf(Color(0xFF103A2B), Color(0xFF0C1220)),
    )
    val p = palettes[kotlin.math.abs(seed) % palettes.size]
    return Brush.verticalGradient(p)
}

@Composable
private fun NewTabTile(onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .aspectRatio(4f / 3.6f)
            .clip(shape)
            .background(BastionPalette.InkSoft.copy(alpha = 0.5f))
            .border(1.dp, BastionPalette.BorderSoft, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(BastionPalette.Brand.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = "New tab", tint = BastionPalette.Brand, modifier = Modifier.size(24.dp))
        }
    }
}

private fun String.hostnameOnly(): String? {
    val trimmed = trim()
    if (trimmed.isEmpty()) return null
    val core = trimmed.removePrefix("https://").removePrefix("http://")
    val slash = core.indexOf('/')
    return if (slash > 0) core.substring(0, slash) else core.ifEmpty { null }
}