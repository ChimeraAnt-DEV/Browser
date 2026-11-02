package com.browseglass.engine

import android.graphics.Bitmap
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings

/**
 * Holder linking a [Tab] to its live [GeckoSession].
 */
class GeckoSessionHolder internal constructor(
    val tab: Tab,
    geckoSettings: GeckoSessionSettings,
) {
    @Volatile
    var session: GeckoSession = GeckoSession(geckoSettings)
        private set

    /** True while the session is attached to a runtime (rendering). */
    @Volatile
    var isOpen: Boolean = false
        private set

    /** Open against the runtime (starts the rendering process). */
    fun open(rt: GeckoRuntime) {
        if (isOpen) return
        session.open(rt)
        isOpen = true
    }

    fun close() {
        if (isOpen) {
            session.close()
        }
        session = GeckoSession(session.settings)
        isOpen = false
    }

    fun load(url: String) {
        session.loadUri(url)
        tab.url = url
        tab.progress = 0
        tab.needsReload = false
        tab.suspended = false
        tab.lastActiveMs = System.currentTimeMillis()
    }

    fun goBack() = session.goBack()
    fun goForward() = session.goForward()
    fun reload() = session.reload()

    fun captureThumbnail(): Bitmap? = null // filled via GeckoDisplay.screenshot in practice
}