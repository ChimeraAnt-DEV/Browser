package com.browseglass.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.browseglass.engine.BrowserEngine
import com.browseglass.engine.Tab
import com.browseglass.ui.components.glassPanel

/**
 * Mission-Control style tab switcher: glassmorphic cards in a grid with a live
 * thumbnail (or a glass placeholder), bouncy entrance, close + new-tab.
 */
@Composable
fun TabGrid(
    engine: BrowserEngine,
    onCloseTab: (Long) -> Unit,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val sessions = engine.sessions.all
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC0F1220).copy(alpha = 0.92f))
            .statusBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${sessions.size} tabs",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                androidx.compose.material3.IconButton(onDismiss) {
                    Icon(Icons.Default.Close, "Close tabs", tint = Color.White)
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 24.dp
                ),
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
                    NewTabCard(onClick = {
                        val h = engine.createSession(private = false)
                        onSelect(h.tab.id)
                    })
                }
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
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .glassPanel(fillAlpha = 0.14f, blurAmount = 24)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        // Live preview area (thumbnail or glass placeholder).
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.04f)),
            contentAlignment = Alignment.Center,
        ) {
            tab.thumbnail?.let {
                androidx.compose.foundation.Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            } ?: Text(
                if (tab.suspended) "Suspended" else "about:blank",
                color = Color.White.copy(alpha = 0.4f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    tab.title.ifEmpty { "New tab" },
                    color = if (active) MaterialTheme.colorScheme.primary else Color.White,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    tab.url.replace("https://", "").replace("http://", ""),
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            androidx.compose.material3.IconButton(
                onClick = onClose,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
            ) {
                Icon(Icons.Default.Close, "Close tab", tint = Color.White)
            }
        }
    }
}

@Composable
private fun NewTabCard(onClick: () -> Unit) {
    Box(
        Modifier
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(20.dp))
            .glassPanel(fillAlpha = 0.10f, blurAmount = 24)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "New tab",
            tint = Color.White.copy(alpha = 0.6f),
        )
    }
}

