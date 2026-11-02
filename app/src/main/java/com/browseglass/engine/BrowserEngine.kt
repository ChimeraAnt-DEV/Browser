package com.browseglass.engine

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.browseglass.data.BastionNative
import org.mozilla.geckoview.GeckoPreferenceController
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSessionSettings

/**
 * Owns the single Gecko runtime and the session lifecycle.
 *
 * Phase 1 core engine architecture:
 *  - Singleton runtime (enforced by [com.browseglass.BastionApp]).
 *  - [SessionManager]: one [org.mozilla.geckoview.GeckoSession] per tab,
 *    tracking active vs suspended tabs.
 *  - Fission (site isolation) on, a hard cap on content processes,
 *    hardware acceleration (SurfaceView + GPU).
 *  - ComponentCallbacks2 memory-pressure listener that kills the LRU tab first
 *    (Phase 3.17).
 */
class BrowserEngine(
    private val appContext: Context,
) : DefaultLifecycleObserver, SessionManager.Host {

    private var runtime: GeckoRuntime? = null
    private var settings: GeckoRuntimeSettings? = null

    val sessions = SessionManager(this)

    @Volatile
    var isReady: Boolean = false
        private set

    override fun runtime(): GeckoRuntime? = runtime
    override fun context(): Context = appContext

    /** Called by BastionApp right after `GeckoRuntime.create()`. */
    fun attach(runtime: GeckoRuntime, settings: GeckoRuntimeSettings) {
        this.runtime = runtime
        this.settings = settings
        isReady = true

        adjustProcessLimit()
        installMemoryPressureForwarder()

        if (BastionNative.isNativeLoaded()) {
            Log.i(TAG, "native core: engine ${BastionNative.engineVersion()}, " +
                "abis=${BastionNative.nativeAbis().joinToString(",")}")
        }
    }

    /**
     * Phase 1.4: hard cap on content processes so low-end devices don't run out
     * of RAM.
     */
    private fun adjustProcessLimit() {
        GeckoPreferenceController.setGeckoPref(
            "dom.ipc.processCount.override",
            MAX_CONTENT_PROCESSES,
            GeckoPreferenceController.PREF_BRANCH_DEFAULT,
        ).accept {
            Log.d(TAG, "content-process ceiling set to $MAX_CONTENT_PROCESSES")
        }
    }

    /**
     * Memory pressure hook. On TRIM_MEMORY_RUNNING_CRITICAL / *_COMPLETE we
     * release the least-recently-used tab immediately (Phase 3.17).
     */
    private fun installMemoryPressureForwarder() {
        appContext.registerComponentCallbacks(object : android.content.ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) {}

            override fun onLowMemory() {
                onTrimMemory(android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
            }

            override fun onTrimMemory(level: Int) {
                sessions.onTrimMemory(level)
            }
        })
    }

    // ---------------------------------------------------------------- tabs

    /** Creates a new tab (optionally private / desktop-mode) and opens it. */
    fun createSession(private: Boolean = false, desktopMode: Boolean = false): GeckoSessionHolder {
        val holder = sessions.createSession(private = private, desktopMode = desktopMode)
        val rt = runtime ?: throw IllegalStateException("Engine not attached yet")
        holder.open(rt)
        return holder
    }

    fun closeTab(tabId: Long) = sessions.closeTab(tabId, discardOnly = true)

    fun gracefulClose(tabId: Long) = sessions.closeTab(tabId, discardOnly = false)

    fun engineVersion(): String = "GeckoView-${org.mozilla.geckoview.BuildConfig.MOZILLA_VERSION}"

    fun contentProcessLimit(): Int = MAX_CONTENT_PROCESSES

    /**
     * Installs the bundled WebExtension (cookie-banner blocker + content
     * helpers). Must be called after the runtime is ready.
     */
    fun installBuiltInExtension(
        onResult: (Boolean) -> Unit = {},
    ) {
        val rt = runtime ?: return onResult(false)
        rt.webExtensionController
            .ensureBuiltIn(
                "resource://android/assets/extensions/bastion-tools/",
                "bastion-tools@browseglass",
            )
            .accept(
                { onResult(true) },
                { err ->
                    Log.e(TAG, "extension install failed", err)
                    onResult(false)
                },
            )
    }

    /* -------------------------------------------------- lifecycle */

    override fun onStop(owner: LifecycleOwner) {
        // Background: freeze hidden tabs older than 30 minutes (Phase 3.16).
        sessions.freezeBackgroundTabs()
    }

    companion object {
        private const val TAG = "BastionEngine"
        const val MAX_CONTENT_PROCESSES = 4
    }
}

/** Describes a new session's settings. */
class SessionSettings(
    val private: Boolean = false,
    val desktopMode: Boolean = false,
    val allowJavascript: Boolean = true,
) {
    fun toGeckoSettings(): GeckoSessionSettings {
        val builder = GeckoSessionSettings.Builder()
            .usePrivateMode(private)
            .allowJavascript(allowJavascript)
            .useTrackingProtection(true)
            .viewportMode(
                if (desktopMode) GeckoSessionSettings.VIEWPORT_MODE_DESKTOP
                else GeckoSessionSettings.VIEWPORT_MODE_MOBILE
            )
            .userAgentMode(
                if (desktopMode) GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                else GeckoSessionSettings.USER_AGENT_MODE_MOBILE
            )
        return builder.build()
    }
}