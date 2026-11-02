package com.browseglass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.browseglass.BastionApp
import com.browseglass.engine.GeckoSessionHolder
import com.browseglass.ui.components.FloatingToolbar
import com.browseglass.ui.components.Glass
import com.browseglass.ui.components.GlassShimmer
import com.browseglass.ui.tabs.TabGrid
import dev.chrisbanes.haze.haze

/**
 * Interface implemented by [com.browseglass.MainActivity] so the UI can trigger
 * app-level actions (permissions, etc.).
 */
interface BastionScreen {
    fun requestCamera()
    fun requestMicrophone()
    fun requestLocation()
}

/**
 * Top-level composable: engine-owned GeckoView surface plus the floating glass
 * toolbar (bottom-anchored, Safari-style) and the tab grid.
 */
@Composable
fun BastionScreen(host: BastionScreen) {
    val context = LocalContext.current
    val app = context.applicationContext as BastionApp
    val engine = app.engine

    val showTabs = remember { mutableStateOf(false) }
    val urlState = remember { mutableStateOf("") }
    val addressBarFocus = remember { mutableStateOf(false) }
    val currentHolder = remember { mutableStateOf<GeckoSessionHolder?>(null) }

    LaunchedEffect(Unit) {
        if (engine.sessions.all.isEmpty()) {
            val holder = engine.createSession(private = false)
            currentHolder.value = holder
            engine.sessions.activate(holder.tab.id)
            holder.load("about:blank")
        } else {
            val holder = engine.sessions.all.first()
            currentHolder.value = holder
            engine.sessions.activate(holder.tab.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to Color(0xFF0F1220),
                    1.0f to Color(0xFF1B2445),
                )
            ),
    ) {
        currentHolder.value?.let { holder ->
            GeckoSurface(
                holder = holder,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 84.dp),
            )
        }

        val active = engine.sessions.all.firstOrNull()
        if (active != null && active.tab.progress in 1 until 100 && !showTabs.value) {
            // Never a blank white screen — skeleton shimmer while loading.
            GlassShimmer(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
                    .padding(horizontal = 16.dp),
            )
        }

        FloatingToolbar(
            engine = engine,
            currentUrl = currentHolder.value?.tab?.url ?: "",
            url = urlState.value,
            focused = addressBarFocus.value,
            onUrlChange = { urlState.value = it },
            onOpenTabs = { showTabs.value = true },
            onNavigate = { raw ->
                val h = currentHolder.value
                if (h != null) {
                    h.load(normaliseUrl(raw))
                } else {
                    val holder = engine.createSession(private = false)
                    currentHolder.value = holder
                    engine.sessions.activate(holder.tab.id)
                    holder.load(normaliseUrl(raw))
                }
                urlState.value = ""
                addressBarFocus.value = false
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding(),
        )

        if (showTabs.value) {
            TabGrid(
                engine = engine,
                onCloseTab = { id -> engine.gracefulClose(id) },
                onSelect = { id ->
                    engine.sessions.activate(id)
                    currentHolder.value = engine.sessions.all.firstOrNull { it.tab.id == id }
                    showTabs.value = false
                },
                onDismiss = { showTabs.value = false },
            )
        }
    }
}

@Composable
private fun GeckoSurface(
    holder: GeckoSessionHolder,
    modifier: Modifier = Modifier,
) {
    val session = holder.session
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            org.mozilla.geckoview.GeckoView(ctx).apply {
                setSession(session)
            }
        },
        update = {},
        // Mark the content layer as the Haze blur source so glass panels
        // actually blur the underlying page, not just a flat color.
        modifier = modifier.haze(Glass.hazeState),
    )
}

private fun normaliseUrl(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return "about:blank"
    if (trimmed.startsWith("about:") || trimmed.startsWith("data:") ||
        trimmed.startsWith("http://") || trimmed.startsWith("https://") ||
        trimmed.startsWith("file://")
    ) {
        return trimmed
    }
    if (trimmed.contains(".") && !trimmed.contains(" ")) {
        return "https://$trimmed"
    }
    return "https://duckduckgo.com/?q=${java.net.URLEncoder.encode(trimmed, "UTF-8")}"
}