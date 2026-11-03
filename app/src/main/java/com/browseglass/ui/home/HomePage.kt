package com.browseglass.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.browseglass.engine.GeckoSessionHolder
import com.browseglass.ui.components.SearchPill
import com.browseglass.ui.theme.BastionPalette

/**
 * The real start page — the default landing for a new/flat tab. A gradient
 * search hero with the capsule pill, a grid of top sites, and a row of recent
 * tabs. No more "about:blank".
 */
@Composable
fun HomePage(
    topSites: List<HomeTile>,
    recent: List<GeckoSessionHolder>,
    defaultEngineName: String,
    onSearch: (String) -> Unit,
    onOpen: (String) -> Unit,
    onNewTab: () -> Unit,
    onOpenTab: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BastionPalette.Ink)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.size(34.dp))

        // Hero: wordmark + search pill
        Text(
            "Bastion",
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            color = BastionPalette.TextPrimary,
        )
        Text(
            "Private by design",
            fontSize = 14.sp,
            color = BastionPalette.TextMuted,
            modifier = Modifier.padding(top = 2.dp),
        )

        Spacer(Modifier.size(26.dp))

        // Hero search
        SearchPill(
            value = query,
            placeholder = "Search $defaultEngineName or type a URL",
            isHttps = true,
            focused = false,
            onValueChange = { query = it },
            onClick = {},
            onGo = {
                if (query.isNotBlank()) {
                    onSearch(query.trim())
                    query = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.size(30.dp))

        // Top sites grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Top sites",
                color = BastionPalette.TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            HomePillButton("New tab", onClick = onNewTab)
        }

        Spacer(Modifier.size(6.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            items(topSites, key = { it.label }) { tile ->
                TopSiteTile(tile) { onOpen(tile.url) }
            }
            // Always leave a "+" slot to add.
            item(key = "__add") {
                TopSiteTile(
                    HomeTile("Add", "", HomeAction.Add),
                    onTap = onNewTab,
                )
            }
        }

        if (recent.isNotEmpty()) {
            Spacer(Modifier.size(20.dp))
            Text(
                "Recent",
                color = BastionPalette.TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.size(4.dp))
            recent.take(3).forEach { holder ->
                RecentRow(holder) { onOpenTab(holder.tab.id) }
            }
        }
    }
}

/** A tile in the grid: a site, or a synthetic action (+). */
data class HomeTile(
    val label: String,
    val url: String,
    val action: HomeAction = HomeAction.Open,
)

enum class HomeAction { Open, Add }

@Composable
private fun TopSiteTile(tile: HomeTile, onTap: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(tileColor(tile.label)),
            contentAlignment = Alignment.Center,
        ) {
            if (tile.action == HomeAction.Add) {
                Icon(
                    Icons.Default.Search, // stand-in glyph
                    contentDescription = null,
                    tint = BastionPalette.TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Text(
                    tile.label.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                )
            }
        }
        Spacer(Modifier.size(8.dp))
        Text(
            tile.label,
            color = BastionPalette.TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
}

private val tileColors = listOf(
    Color(0xFF10A37F), Color(0xFF6366F1), Color(0xFFEF4444), Color(0xFFF59E0B),
    Color(0xFF06B6D4), Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF22C55E),
)

@Composable
private fun tileColor(label: String): Color {
    val idx = kotlin.math.abs(label.hashCode()) % tileColors.size
    return tileColors[idx]
}

@Composable
private fun RecentRow(holder: GeckoSessionHolder, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BastionPalette.InkSoft.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(BastionPalette.InkRaised),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "G".takeIf { _ -> holder.tab.url.isNotBlank() } ?: "G",
                color = BastionPalette.BrandStart,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                holder.tab.title.ifBlank { "Untitled" },
                color = BastionPalette.TextPrimary,
                fontSize = 14.sp,
                maxLines = 1,
            )
            Text(
                holder.tab.url.ifBlank { "" }.removePrefix("https://").removePrefix("http://"),
                color = BastionPalette.TextMuted,
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun HomePillButton(label: String, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            label,
            color = BastionPalette.BrandStart,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}