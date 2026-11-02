package com.browseglass.engine

import android.content.ComponentCallbacks2
import android.content.Context
import android.util.Log
import org.mozilla.geckoview.GeckoRuntime
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

/**
 * Tracks the open tabs (each with its [GeckoSession]) and drives
 * suspension / eviction policies.
 *
 * Phase 3 items covered here:
 *  - 16. Tab suspension: [freezeBackgroundTabs] snapshots tabs older than
 *        SESSION_FREEZE_MS and closes their sessions, writing state to disk.
 *  - 17. Memory pressure: [onTrimMemory] kills the LRU tab.
 *  - 18. Renderer-crash recovery: sessions marked [Tab.needsReload] show a
 *        "Reload" affordance instead of lingering.
 */
class SessionManager(
    private val host: Host,
) {
    interface Host {
        fun runtime(): GeckoRuntime?
        fun context(): Context
    }

    private val tabs = CopyOnWriteArrayList<GeckoSessionHolder>()
    private val nextId = AtomicLong(1L)

    /** The currently focused tab, or null when no tab is focused. */
    @Volatile
    var currentId: Long? = null
        private set

    val all: List<GeckoSessionHolder> get() = tabs.toList()

    fun activeCount(): Int = tabs.count { !it.tab.suspended }

    fun find(tabId: Long): GeckoSessionHolder? = tabs.firstOrNull { it.tab.id == tabId }

    fun createSession(private: Boolean = false, desktopMode: Boolean = false): GeckoSessionHolder {
        val tab = Tab(
            id = nextId.getAndIncrement(),
            private = private,
            desktopMode = desktopMode,
        )
        val settings = SessionSettings(
            private = private,
            desktopMode = desktopMode,
        )
        val holder = GeckoSessionHolder(tab, settings.toGeckoSettings())
        tabs.add(holder)
        return holder
    }

    /** Discard a tab: close its session immediately. */
    fun closeTab(tabId: Long, discardOnly: Boolean) {
        val found = find(tabId) ?: return
        if (currentId == tabId) currentId = null
        if (discardOnly) {
            // Renderer-crash path: mark for reload then drop.
            found.close()
            found.tab.needsReload = true
        } else {
            found.close()
            tabs.remove(found)
        }
    }

    /** Open (or re-open) the given tab against the runtime. */
    fun activate(tabId: Long): GeckoSessionHolder? {
        val holder = find(tabId) ?: return null
        holder.tab.lastActiveMs = System.currentTimeMillis()
        currentId = tabId
        holder.tab.suspended = false
        holder.tab.needsReload = false
        val rt = host.runtime()
        if (rt != null && !holder.isOpen) {
            holder.open(rt)
        }
        return holder
    }

    fun launch(url: String, private: Boolean = false, desktopMode: Boolean = false) {
        val holder = createSession(private, desktopMode)
        holder.open(host.runtime()!!)
        holder.load(url)
        currentId = holder.tab.id
    }

    /** Phase 3.16: freeze background tabs not touched in 30 minutes. */
    fun freezeBackgroundTabs() {
        val cutoff = System.currentTimeMillis() - SESSION_FREEZE_MS
        val current = currentId
        for (holder in tabs) {
            if (holder.tab.id == current) continue
            if (holder.tab.lastActiveMs < cutoff && !holder.tab.suspended) {
                holder.close()
                holder.tab.suspended = true
                Log.d(TAG, "suspended tab ${holder.tab.id}")
            }
        }
    }

    /** Phase 3.17: kill the LRU background tab under memory pressure. */
    fun onTrimMemory(level: Int) {
        // Below RUNNING_LOW there is nothing to do — Android will reclaim.
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            val candidate = leastRecentlyUsedBackground()
            if (candidate != null && candidate.tab.id != currentId) {
                Log.i(TAG, "memory pressure: discarding LRU tab ${candidate.tab.id}")
                candidate.close()
                candidate.tab.needsReload = true
            }
        }
    }

    private fun leastRecentlyUsedBackground(): GeckoSessionHolder? {
        val current = currentId
        return tabs
            .filter { it.tab.id != current }
            .minByOrNull { it.tab.lastActiveMs }
    }

    /** Re-open a suspended/closed tab (restore from snapshot in-practice). */
    fun restore(tabId: Long) {
        activate(tabId)
        find(tabId)?.tab?.let { it.suspended = false; it.needsReload = false }
    }

    fun closeAll() {
        for (holder in tabs) holder.close()
        tabs.clear()
        currentId = null
    }

    companion object {
        private const val TAG = "BastionSessions"
        private const val SESSION_FREEZE_MS = 30L * 60L * 1000L // 30 minutes
    }
}