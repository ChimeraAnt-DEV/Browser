package com.browseglass.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.browseglass.engine.BrowserEngine

/**
 * Safari-style floating glass address bar, bottom-anchored. Expands with a
 * glass effect when focused, hides on scroll (via [expanded] param from the
 * screen), bouncy spring animations throughout.
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
    val active = engine.sessions.all.firstOrNull { it.tab.id == engine.sessions.currentId }

    // Spring-based physical resize: bouncy when focusing, gentle when idle.
    val corner by animateDpAsState(
        targetValue = if (focused) 14.dp else 28.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "toolbarCorner",
    )
    val verticalPad by animateDpAsState(
        targetValue = if (focused) 14.dp else 8.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "toolbarPad",
    )

    val isHttps = currentUrl.startsWith("https://")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = verticalPad)
            .height(if (focused) 56.dp else 52.dp)
            .glassPanel(RoundedCornerShape(corner), fillAlpha = if (focused) 0.26f else 0.16f)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (focused) {
            IconButtonCompat({ focusManager.clearFocus() }) { Icon(Icons.Default.Close, "Close") }
        } else {
            IconButtonCompat(onOpenHome) {
                Icon(Icons.Default.Home, "Home")
            }
        }

        OutlinedTextField(
            value = if (focused) url else displayUrl(currentUrl, active?.tab?.title),
            onValueChange = onUrlChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.06f)),
            singleLine = true,
            leadingIcon = {
                Icon(
                    if (isHttps) Icons.Default.Lock else Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (isHttps) Color(0xFF4ADE80) else Color(0xFFFBBF24),
                )
            },
            trailingIcon = {
                if (!focused) {
                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onNavigate(url.ifEmpty { currentUrl }) }),
            shape = RoundedCornerShape(16.dp),
        )

        if (focused) {
            IconButtonCompat({ onNavigate(url) }) { Icon(Icons.Default.Search, "Go") }
        } else {
            IconButtonCompat(onOpenTabs) {
                Icon(Icons.Default.Tab, "Tabs")
            }
            IconButtonCompat(onOpenSettings) {
                Icon(Icons.Default.MoreVert, "Settings")
            }
        }
    }
}

@Composable
private fun IconButtonCompat(onClick: () -> Unit, icon: @Composable () -> Unit) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape),
    ) {
        boxIcon(icon)
    }
}

@Composable
private fun boxIcon(icon: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
        icon()
    }
}

fun displayUrl(currentUrl: String, title: String?): String {
    if (currentUrl.isEmpty()) return ""
    if (currentUrl.startsWith("https://")) {
        val rest = currentUrl.removePrefix("https://")
        val slash = rest.indexOf('/')
        return if (slash > 0) rest.substring(0, slash) else rest
    }
    return currentUrl
}