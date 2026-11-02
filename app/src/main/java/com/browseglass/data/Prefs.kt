package com.browseglass.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.browseglass.engine.SessionSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "bastion_prefs")

/**
 * All user preferences backed by DataStore.
 *
 * Phases covered: search engines (default), HTTPS-only toggle, cookie-banner
 * blocker, ad-block toggle, data saver, text scale / font, private browsing,
 * biometric lock, tab-bar placement (bottom / top / hidden).
 */
class Prefs(private val context: Context) {

    private object K {
        val defaultEngine = stringPreferencesKey("search.default_engine")
        val httpsOnly = booleanPreferencesKey("privacy.https_only")
        val cookieBannerBlocking = booleanPreferencesKey("privacy.cookie_banner_blocking")
        val blockAds = booleanPreferencesKey("privacy.block_ads")
        val dataSaver = booleanPreferencesKey("privacy.data_saver")
        val privateBrowsing = booleanPreferencesKey("privacy.private_browsing")
        val biometricLock = booleanPreferencesKey("privacy.biometric_lock")
        val textScale = floatPreferencesKey("appearance.text_scale")
        val fontFamily = stringPreferencesKey("appearance.font_family")
        val toolbarLayout = stringPreferencesKey("appearance.toolbar_layout")
        val homePage = stringPreferencesKey("homepage.url")
        val darkMode = intPreferencesKey("appearance.dark_mode")
        val desktopMode = booleanPreferencesKey("appearance.desktop_mode")
        val userScriptsJson = stringPreferencesKey("userscripts.json")
    }

    val httpsOnly: Flow<Boolean> = context.dataStore.data.map { it[K.httpsOnly] ?: true }
    val cookieBannerBlocking: Flow<Boolean> =
        context.dataStore.data.map { it[K.cookieBannerBlocking] ?: true }
    val blockAds: Flow<Boolean> = context.dataStore.data.map { it[K.blockAds] ?: true }
    val dataSaver: Flow<Boolean> = context.dataStore.data.map { it[K.dataSaver] ?: false }
    val privateBrowsing: Flow<Boolean> = context.dataStore.data.map { it[K.privateBrowsing] ?: false }
    val biometricLock: Flow<Boolean> = context.dataStore.data.map { it[K.biometricLock] ?: true }
    val textScale: Flow<Float> = context.dataStore.data.map { it[K.textScale] ?: 1.0f }
    val fontFamily: Flow<String> = context.dataStore.data.map { it[K.fontFamily] ?: DEFAULT_FONT }
    val toolbarLayout: Flow<String> = context.dataStore.data.map { it[K.toolbarLayout] ?: LAYOUT_BOTTOM }
    val homePage: Flow<String> = context.dataStore.data.map { it[K.homePage] ?: DEFAULT_HOME }
    val darkMode: Flow<Int> = context.dataStore.data.map { it[K.darkMode] ?: -1 }
    val desktopMode: Flow<Boolean> = context.dataStore.data.map { it[K.desktopMode] ?: false }
    val defaultEngine: Flow<String> =
        context.dataStore.data.map { it[K.defaultEngine] ?: SearchEnginesStore.DEFAULT_ENGINE_NAME }
    val userScriptsJson: Flow<String> =
        context.dataStore.data.map { it[K.userScriptsJson] ?: "[]" }

    suspend fun setHttpsOnly(v: Boolean) = context.dataStore.edit { it[K.httpsOnly] = v }
    suspend fun setCookieBannerBlocking(v: Boolean) = context.dataStore.edit { it[K.cookieBannerBlocking] = v }
    suspend fun setBlockAds(v: Boolean) = context.dataStore.edit { it[K.blockAds] = v }
    suspend fun setDataSaver(v: Boolean) = context.dataStore.edit { it[K.dataSaver] = v }
    suspend fun setPrivateBrowsing(v: Boolean) = context.dataStore.edit { it[K.privateBrowsing] = v }
    suspend fun setBiometricLock(v: Boolean) = context.dataStore.edit { it[K.biometricLock] = v }
    suspend fun setTextScale(v: Float) = context.dataStore.edit { it[K.textScale] = v }
    suspend fun setFontFamily(v: String) = context.dataStore.edit { it[K.fontFamily] = v }
    suspend fun setToolbarLayout(v: String) = context.dataStore.edit { it[K.toolbarLayout] = v }
    suspend fun setHomePage(v: String) = context.dataStore.edit { it[K.homePage] = v }
    suspend fun setDarkMode(v: Int) = context.dataStore.edit { it[K.darkMode] = v }
    suspend fun setDesktopMode(v: Boolean) = context.dataStore.edit { it[K.desktopMode] = v }
    suspend fun setDefaultEngine(v: String) = context.dataStore.edit { it[K.defaultEngine] = v }
    suspend fun setUserScriptsJson(v: String) = context.dataStore.edit { it[K.userScriptsJson] = v }

    /** Search URL template for a chosen engine (mirrors the Rust defaults). */
    fun searchUrlFor(engineName: String): String = when (engineName) {
        "Google" -> "https://www.google.com/search?q={query}"
        "Bing" -> "https://www.bing.com/search?q={query}"
        "Brave" -> "https://search.brave.com/search?q={query}"
        else -> "https://duckduckgo.com/?q={query}"
    }

    companion object {
        const val LAYOUT_BOTTOM = "bottom"
        const val LAYOUT_TOP = "top"
        const val LAYOUT_HIDDEN = "hidden"
        const val DEFAULT_FONT = "system"
        const val DEFAULT_HOME = "about:home"

        /** Text scale slider 0.5x–2.0x clipped by the UI. */
        fun clampTextScale(v: Float) = v.coerceIn(0.5f, 2.0f)
    }
}

/** Copy of the in-Rust engine name so UI can share defaults. */
object SearchEnginesStore {
    const val DEFAULT_ENGINE_NAME = "DuckDuckGo"
}