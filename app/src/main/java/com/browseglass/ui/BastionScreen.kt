package com.browseglass.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.browseglass.BastionApp
import com.browseglass.data.Prefs
import com.browseglass.engine.GeckoSessionHolder
import com.browseglass.ui.components.FloatingToolbar
import com.browseglass.ui.components.GlassShimmer
import com.browseglass.ui.home.HomePage
import com.browseglass.ui.home.HomeTile
import com.browseglass.ui.settings.SettingsScreen
import com.browseglass.ui.tabs.TabGrid
import com.browseglass.ui.theme.BastionPalette
import kotlinx.coroutines.delay

/**
 * Interface implemented by [com.browseglass.MainActivity] so the UI can trigger
 * app-level actions (permissions, etc.).
 */
interface BastionScreen {
    fun requestCamera()
    fun requestMicrophone()
    fun requestLocation()
}

/** A tab is "on the start page" when it has no real URL yet. */
private fun GeckoSessionHolder?.onStartPage(): Boolean =
    this != null && tab.url.isBlank()

@Composable
fun BastionScreen(host: BastionScreen) {
    val context = LocalContext.current
    val app = context.applicationContext as BastionApp
    val engine = app.engine

    val showTabs = remember { mutableStateOf(false) }
    val showSettings = remember { mutableStateOf(false) }
    val urlState = remember { mutableStateOf("") }
    val addressBarFocus = remember { mutableStateOf(false) }
    val currentHolder = remember { mutableStateOf<GeckoSessionHolder?>(null) }
    val prefs = remember { Prefs(context) }
    val defaultEngine by prefs.defaultEngine.collectAsStateWithLifecycle(
        initialValue = com.browseglass.data.SearchEnginesStore.DEFAULT_ENGINE_NAME,
    )
    val textScale by prefs.textScale.collectAsStateWithLifecycle(initialValue = 1f)

    LaunchedEffect(textScale) {
        engine.applyAppearance(textScale)
    }

    // Create one tab on first run; blank URL = start page is shown.
    LaunchedEffect(Unit) {
        if (engine.sessions.all.isEmpty()) {
            val holder = engine.createSession(private = false)
            currentHolder.value = holder
            engine.sessions.activate(holder.tab.id)
        } else {
            val holder = engine.sessions.all.first()
            currentHolder.value = holder
            engine.sessions.activate(holder.tab.id)
        }
    }

    // Home is the default landing whenever the active tab is blank.
    val homepageShown = !showTabs.value && !showSettings.value &&
        currentHolder.value.onStartPage()

    BackHandler {
        when {
            showTabs.value -> showTabs.value = false
            showSettings.value -> showSettings.value = false
            else -> {
                val h = currentHolder.value
                if (h != null && !h.tab.url.isBlank()) h.goBack() else return@BackHandler
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BastionPalette.Ink),
    ) {
        // Web layer — only attach when there's an actual URL to show. This
        // avoids compositing an empty SurfaceView over the start page.
        val holder = currentHolder.value
        val showWeb = holder != null && !holder.tab.url.isBlank()
        if (showWeb) {
            GeckoSurface(
                holder = holder,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 82.dp),
            )
        }

        val active = engine.sessions.all.firstOrNull()
        val loading = active != null &&
            active.tab.progress in 1 until 100 &&
            !showTabs.value && !showSettings.value
        // Skeleton for at most a second — never an endless shimmer.
        var shimmerVisible = remember { mutableStateOf(false) }
        if (loading) {
            LaunchedEffect(active.tab.progress) {
                shimmerVisible.value = true
                delay(400)
                shimmerVisible.value = false
            }
            if (shimmerVisible.value && showWeb) {
                GlassShimmer(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp)
                        .padding(horizontal = 16.dp),
                )
            }
        }

        FloatingToolbar(
            engine = engine,
            currentUrl = currentHolder.value?.tab?.url ?: "",
            url = urlState.value,
            focused = addressBarFocus.value,
            onUrlChange = { urlState.value = it },
            onOpenTabs = { showTabs.value = true },
            onOpenHome = {
                // Home = a fresh tab showing the start page.
                val h = engine.createSession(private = false)
                currentHolder.value = h
                engine.sessions.activate(h.tab.id)
            },
            onOpenSettings = {
                showSettings.value = !showSettings.value
            },
            onNavigate = { raw ->
                val (target, isSearch) = resolveInput(raw)
                val url = if (isSearch) {
                    prefs.searchUrlFor(defaultEngine)
                        .replace("{query}", java.net.URLEncoder.encode(target, "UTF-8"))
                } else {
                    target
                }
                val h = currentHolder.value
                if (h != null) {
                    h.load(url)
                } else {
                    val holder = engine.createSession(private = false)
                    currentHolder.value = holder
                    engine.sessions.activate(holder.tab.id)
                    holder.load(url)
                }
                urlState.value = ""
                addressBarFocus.value = false
                showSettings.value = false
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
                onNewTab = {
                    val h = engine.createSession(private = false)
                    currentHolder.value = h
                    engine.sessions.activate(h.tab.id)
                    showTabs.value = false
                },
            )
        }

        if (homepageShown) {
            HomePage(
                topSites = rememberDefaultTiles(),
                recent = engine.sessions.all,
                defaultEngineName = defaultEngine,
                onSearch = { raw ->
                    val url = prefs.searchUrlFor(defaultEngine)
                        .replace("{query}", java.net.URLEncoder.encode(raw, "UTF-8"))
                    loadInCurrent(engine, currentHolder, url)
                },
                onOpen = { url -> loadInCurrent(engine, currentHolder, url) },
                onNewTab = {
                    val h = engine.createSession(private = false)
                    currentHolder.value = h
                    engine.sessions.activate(h.tab.id)
                },
                onOpenTab = { id ->
                    engine.sessions.activate(id)
                    currentHolder.value = engine.sessions.all.firstOrNull { it.tab.id == id }
                },
            )
        }

        if (showSettings.value && !showTabs.value) {
            androidx.compose.foundation.layout.Box(
                Modifier
                    .fillMaxSize()
                    .background(BastionPalette.Ink),
            ) {
                SettingsScreen(prefs = prefs)
            }
        }
    }
}

private fun loadInCurrent(
    engine: com.browseglass.engine.BrowserEngine,
    currentHolder: androidx.compose.runtime.MutableState<GeckoSessionHolder?>,
    url: String,
) {
    val h = currentHolder.value
    if (h != null) {
        h.load(url)
    } else {
        val holder = engine.createSession(private = false)
        currentHolder.value = holder
        engine.sessions.activate(holder.tab.id)
        holder.load(url)
    }
}

@Composable
private fun rememberDefaultTiles(): List<HomeTile> = androidx.compose.runtime.remember {
    listOf(
        HomeTile("Wikipedia", "https://wikipedia.org"),
        HomeTile("GitHub", "https://github.com"),
        HomeTile("YouTube", "https://youtube.com"),
        HomeTile("Hacker News", "https://news.ycombinator.com"),
        HomeTile("DuckDuckGo", "https://duckduckgo.com"),
        HomeTile("Reddit", "https://reddit.com"),
        HomeTile("X", "https://x.com"),
        HomeTile("Maps", "https://maps.google.com"),
    )
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
        modifier = modifier,
    )
}

/**
 * Splits address-bar input into a navigation target and a "this is a search"
 * flag. Returns (url, false) for explicit URLs / hosts and (query, true) for
 * search terms.
 */
private fun resolveInput(input: String): Pair<String, Boolean> {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return "about:blank" to false
    if (trimmed.startsWith("about:") || trimmed.startsWith("data:") ||
        trimmed.startsWith("http://") || trimmed.startsWith("https://") ||
        trimmed.startsWith("file://")
    ) {
        return trimmed to false
    }
    if (trimmed.contains(".") && !trimmed.contains(" ")) {
        return "https://$trimmed" to false
    }
    return trimmed to true
}