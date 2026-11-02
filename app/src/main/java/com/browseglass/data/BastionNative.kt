package com.browseglass.data

import android.util.Log

/**
 * Mirrors the Rust JNI bridge in `bastion/src/jni.rs`.
 *
 * All functions are safe to call even if the native lib failed to load
 * (defensive: a browser should never crash because its adblock helper was
 * missing) — [isBlocked] falls back to a small Kotlin-side classifier and
 * version/ABI functions return safe defaults.
 */
object BastionNative {

    // Try loading eagerly once; remember the result.
    private var loaded: Boolean = false
    private var resolved: Boolean = false

    init {
        resolve()
    }

    private fun resolve() {
        if (resolved) return
        resolved = true
        try {
            System.loadLibrary("bastion")
            loaded = true
        } catch (t: Throwable) {
            Log.w(TAG, "libbastion not loaded; falling back to Kotlin adblock", t)
            loaded = false
        }
    }

    fun isNativeLoaded(): Boolean {
        if (!resolved) resolve()
        return loaded
    }

    fun isBlocked(url: String): Boolean =
        if (isNativeLoaded()) nativeIsBlocked(url) else kotlinFallbackBlocked(url)

    fun dohServers(): Array<String> =
        if (isNativeLoaded()) nativeDohServers() else DEFAULTS

    fun engineVersion(): String =
        if (isNativeLoaded()) nativeEngineVersion() else "kotlin-fallback"

    fun engineMajor(): Int =
        if (isNativeLoaded()) nativeEngineMajor() else 0

    fun nativeAbis(): Array<String> {
        if (!isNativeLoaded()) return KOTLIN_ABIS
        return try {
            nativeAbiList()
        } catch (t: Throwable) {
            Log.w(TAG, "abi query failed", t)
            KOTLIN_ABIS
        }
    }

    // ------------------------------------------------------------- native

    // These external functions only exist when libbastion is present; guard all
    // calls through the loaded check above.
    private external fun nativeIsBlocked(url: String): Boolean
    private external fun nativeDohServers(): Array<String>
    private external fun nativeEngineVersion(): String
    private external fun nativeEngineMajor(): Int
    private external fun nativeAbiList(): Array<String>

    // ------------------------------------------------------------- fallback

    private val DEFAULTS = arrayOf(
        "https://dns.quad9.net/dns-query",
        "https://cloudflare-dns.com/dns-query",
    )
    private val KOTLIN_ABIS = arrayOf("arm64-v8a", "armeabi-v7a")

    // Tiny EasyList-ish fallback so ad-blocking still works without the .so.
    private val FALLBACK_RULES = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "connect.facebook.net",
        "adnxs.com",
        "rubiconproject.com",
        "criteo.com",
        "taboola.com",
        "outbrain.com",
        "scorecardresearch.com",
    )

    private fun kotlinFallbackBlocked(url: String): Boolean {
        val lower = url.lowercase()
        return FALLBACK_RULES.any { host ->
            lower.contains("://$host/") ||
                lower.contains("://$host.") ||
                lower.contains("://$host?") ||
                lower.endsWith("://$host")
        }
    }

    private const val TAG = "BastionNative"
}