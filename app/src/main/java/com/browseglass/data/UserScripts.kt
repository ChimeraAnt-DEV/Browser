package com.browseglass.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * User scripts and custom CSS injected into matching sites (Phase 4.24).
 *
 * [matchPattern] supports a subset of Match Pattern syntax. URL patterns are
 * plain strings such as star colon slash slash example dot com slash path.
 */
@Serializable
data class UserScript(
    val id: Long = System.currentTimeMillis(),
    val matchPattern: String = "*",
    val js: String = "",
    val css: String = "",
    val enabled: Boolean = true,
)

object UserScripts {

    val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    fun parse(raw: String): List<UserScript> =
        try {
            json.decodeFromString<List<UserScript>>(raw)
        } catch (e: Exception) {
            emptyList()
        }

    fun serialize(scripts: List<UserScript>): String =
        json.encodeToString(scripts)

    /** True when [url] matches the (simplified) match pattern. */
    fun matches(url: String, pattern: String): Boolean {
        if (pattern == "*" || pattern == "<all_urls>") return true
        var pat = pattern.trim()
        if (!pat.contains("://")) pat = "*://$pat"
        // Build a regex from the pattern.
        val regex = buildString {
            append("^")
            var i = 0
            while (i < pat.length) {
                val c = pat[i]
                when {
                    c == '*' -> append(".*")
                    c == '.' -> append("\\.")
                    c == '?' -> append('.')
                    else -> append(java.util.regex.Pattern.quote(c.toString()))
                }
                i++
            }
            append("$")
        }
        return Regex(regex).matches(url)
    }

    fun forUrl(scripts: List<UserScript>, url: String): List<UserScript> =
        scripts.filter { it.enabled && matches(url, it.matchPattern) }
}