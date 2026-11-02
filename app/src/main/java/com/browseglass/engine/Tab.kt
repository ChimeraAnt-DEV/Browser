package com.browseglass.engine

import android.graphics.Bitmap

/**
 * A tab in the browser. Wraps a {@link GeckoSession} plus browser-level state.
 */
data class Tab(
    val id: Long,
    val private: Boolean,
    val desktopMode: Boolean,
) {
    var title: String = ""
    var url: String = ""
    var progress: Int = 0
    /** Last time the user interacted with / focused this tab (epoch millis). */
    var lastActiveMs: Long = System.currentTimeMillis()

    /** Thumbnail used by the tab-grid and the glass preview cards. */
    var thumbnail: Bitmap? = null

    /** True when the renderer was killed / suspended and needs a reload. */
    var needsReload: Boolean = false

    /** True when this tab's state was written to disk (suspended). */
    var suspended: Boolean = false

    val isLoaded: Boolean get() = url.isNotEmpty()
}