package com.browseglass.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.browseglass.ui.components.glassPanel

/**
 * Customisable start page (Phase 4.25): top sites, bookmarks, and a weather
 * widget. Widgets are additive — shown/hidden by the user in settings.
 */
@Composable
fun HomePage(onOpen: (String) -> Unit) {
    val topSites = remember {
        listOf(
            TopSite("Wikipedia", "https://wikipedia.org", Color(0xFF10A37F)),
            TopSite("GitHub", "https://github.com", Color(0xFF6E5494)),
            TopSite("YouTube", "https://youtube.com", Color(0xFFFF0033)),
            TopSite("Hacker News", "https://news.ycombinator.com", Color(0xFFFF6600)),
            TopSite("DuckDuckGo", "https://duckduckgo.com", Color(0xFFDE5833)),
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "Good day 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
        }
        item {
            WeatherWidget()
        }
        item {
            Text("Top sites", style = MaterialTheme.typography.titleMedium)
        }
        items(topSites) { site ->
            TopSiteRow(site) { onOpen(site.url) }
        }
        item {
            Text(
                "Press the search bar to browse",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

data class TopSite(val name: String, val url: String, val color: Color)

@Composable
private fun TopSiteRow(site: TopSite, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .glassPanel(fillAlpha = 0.10f)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(site.color),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                site.name.take(1),
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            Modifier
                .padding(start = 12.dp)
                .weight(1f),
        ) {
            Text(site.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                site.url.removePrefix("https://"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(Icons.Default.Star, null, tint = Color(0xFFFBBF24))
    }
}

/**
 * A placeholder weather widget backed by a static (city-entered) value today.
 * In production this would fetch a public weather API; kept as a widget shell
 * so the home page never blocks on network.
 */
@Composable
private fun WeatherWidget() {
    Row(
        Modifier
            .fillMaxWidth()
            .glassPanel(fillAlpha = 0.10f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("☀️ 22°", style = MaterialTheme.typography.headlineMedium)
        Text(
            "San Francisco",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}