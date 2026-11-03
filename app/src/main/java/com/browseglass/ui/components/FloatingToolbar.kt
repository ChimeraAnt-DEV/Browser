package com.browseglass.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.browseglass.engine.BrowserEngine
import com.browseglass.ui.theme.BastionPalette

/**
 * Modern floating navigation dock (bottom, Safari-style). A compact rounded
 * pill holds back, home, the address search-pill, tabs and settings — floating
 * on its own hairline-bordered material. Nothing like a 2016 full-width bar.
 */
@Composable
fun FloatingToolbar(
    engine: BrowserEngine,
    currentUrl: String,
    url: String,
    focused: Boolean,
    onUrlChange: (String) -> Unit,
    onOpenTabs: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    val corner by animateDpAsState(
        targetValue = if (focused) 18.dp else 30.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "dockCorner",
    )
    val verticalPad by animateDpAsState(
        targetValue = if (focused) 16.dp else 10.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "dockPad",
    )

    val isHttps = currentUrl.startsWith("https://")
    val shape = RoundedCornerShape(corner)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = verticalPad)
            .height(if (focused) 60.dp else 56.dp)
            .shadow(18.dp, shape, spotColor = Color.Black.copy(alpha = 0.4f))
            .background(
                Brush.verticalGradient(
                    listOf(BastionPalette.InkRaised.copy(alpha = 0.97f), BastionPalette.Ink.copy(alpha = 0.92f))
                ),
                shape,
            )
            .border(1.dp, BastionPalette.Border, shape)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (focused) {
            GlassIconButton(
                icon = Icons.Default.Close,
                contentDescription = "Close",
                onClick = { focusManager.clearFocus() },
                size = 38,
            )
        } else {
            // Back
            GlassIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = {
                    val id = engine.sessions.currentId ?: return@GlassIconButton
                    engine.sessions.find(id)?.goBack()
                },
                size = 38,
            )
            // Home
            GlassIconButton(
                icon = Icons.Default.Home,
                contentDescription = "Home",
                onClick = onOpenHome,
                size = 38,
            )
        }

        SearchPill(
            value = if (focused) url else displayUrl(currentUrl),
            placeholder = "Search or type a URL",
            isHttps = isHttps,
            focused = focused,
            onValueChange = onUrlChange,
            onClick = {},
            onGo = { onNavigate(url.ifEmpty { currentUrl }) },
            modifier = Modifier.weight(1f),
        )

        if (focused) {
            GlassIconButton(
                icon = Icons.Default.Search,
                contentDescription = "Go",
                onClick = { onNavigate(url.ifEmpty { currentUrl }) },
                size = 38,
                tint = BastionPalette.Ink,
                background = BastionPalette.Brand,
            )
        } else {
            // Tabs
            GlassIconButton(
                icon = Icons.Default.GridView,
                contentDescription = "Tabs",
                onClick = onOpenTabs,
                size = 38,
            )
            // Settings
            GlassIconButton(
                icon = Icons.Default.Tune,
                contentDescription = "Settings",
                onClick = onOpenSettings,
                size = 38,
            )
        }
    }
}

fun displayUrl(currentUrl: String): String {
    if (currentUrl.isEmpty()) return ""
    if (currentUrl.startsWith("https://")) {
        val rest = currentUrl.removePrefix("https://")
        val slash = rest.indexOf('/')
        return if (slash > 0) rest.substring(0, slash) else rest
    }
    return currentUrl
}