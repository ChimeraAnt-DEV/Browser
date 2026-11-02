package com.browseglass

import android.app.Application
import android.util.Log
import androidx.core.os.TraceCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.browseglass.engine.BrowserEngine
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings

/**
 * Singleton Application: owns the one-and-only [GeckoRuntime] instance.
 *
 * Android allows only a single runtime per process — creating more than one
 * doubles memory. Everything gecko touches goes through this class.
 */
class BastionApp : Application() {

    val engine: BrowserEngine by lazy {
        BrowserEngine(this)
    }

    override fun onCreate() {
        TraceCompat.beginSection("BastionApp.onCreate")
        super.onCreate()
        TraceCompat.beginSection("GeckoRuntime.init")
        initGecko()
        TraceCompat.endSection()
        ProcessLifecycleOwner.get().lifecycle.addObserver(engine)
        TraceCompat.endSection()
    }

    /**
     * Hardware-accelerated GeckoRuntime with site-isolation (Fission) enabled,
     * a hard cap on content processes (via the Fission process count override
     * pref), HTTPS-only by default, and DoH resolvers wired in as TRR.
     */
    private fun initGecko() {
        val contentBlocking = ContentBlocking.Settings.Builder()
            // Native content blocker: block ads, analytics, social, crypto/fingerprinters.
            .antiTracking(
                ContentBlocking.AntiTracking.AD or
                    ContentBlocking.AntiTracking.ANALYTIC or
                    ContentBlocking.AntiTracking.SOCIAL or
                    ContentBlocking.AntiTracking.CONTENT or
                    ContentBlocking.AntiTracking.CRYPTOMINING or
                    ContentBlocking.AntiTracking.FINGERPRINTING
            )
            .build()
            // Setters that accept the category constants live on `Settings`
            // itself (the Builder only mirrors a subset).
            .setSafeBrowsing(ContentBlocking.SafeBrowsing.DEFAULT)
            // Third-party tracker cookies only (keeps real sites working).
            .setCookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS)
            .setCookieBehaviorPrivateMode(ContentBlocking.CookieBehavior.ACCEPT_NONE)

        val settings = GeckoRuntimeSettings.Builder()
            // Fission: each tab gets its own content process so a crash on one
            // site can't take the whole browser down.
            .fissionEnabled(true)
            // Limit the number of content processes so low-end devices don't
            // run out of RAM. The actual process count is enforced via the
            // Fission override pref set at runtime by
            // BrowserEngine.adjustProcessLimit().
            .remoteDebuggingEnabled(false)
            .consoleOutput(false)
            .aboutConfigEnabled(false)
            // HTTPS-only: ALLOW_ALL in debug so our local test page works,
            // HTTPS_ONLY in release. (0 = ALLOW_ALL, 2 = HTTPS_ONLY)
            .allowInsecureConnections(
                if (BuildConfig.DEBUG) GeckoRuntimeSettings.ALLOW_ALL
                else GeckoRuntimeSettings.HTTPS_ONLY
            )
            .contentBlocking(contentBlocking)
            // DoH (DNS-over-HTTPS) bootstrap + TRR: our ultra-fast DNS path.
            // The actual resolver is a custom one; we point Gecko's TRR at the
            // same endpoints our Rust core exposes.
            .defaultRecursiveResolverUri("https://dns.quad9.net/dns-query")
            .trustedRecursiveResolverUri("https://cloudflare-dns.com/dns-query")
            .trustedRecursiveResolverMode(GeckoRuntimeSettings.TRR_MODE_FIRST)
            .build()

        val runtime = GeckoRuntime.create(this, settings)
        engine.attach(runtime, settings)

        Log.i(TAG, "Gecko init complete — engineVersion=${engine.engineVersion()}")
    }

    companion object {
        private const val TAG = "BastionApp"
    }
}