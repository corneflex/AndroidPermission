package com.corneflex.permissions.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Manages the whitelist functionality for applications
 */
class WhitelistManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        WHITELIST_PREFS_NAME, Context.MODE_PRIVATE
    )
    
    /**
     * Predefined system packages that are always whitelisted
     */
    private val defaultSystemWhitelist = setOf(
        "com.android.systemui",
        "com.android.settings",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.android.providers.settings",
        "com.android.providers.media",
        "com.android.providers.contacts",
        "com.android.providers.telephony",
        "com.android.providers.calendar",
        "com.android.providers.downloads"
    )
    
    /**
     * Get the set of exact package names in the whitelist (non-regex patterns)
     */
    fun getExactWhitelistedApps(): Set<String> {
        val customWhitelist = sharedPreferences.getStringSet(KEY_WHITELISTED_APPS, emptySet()) ?: emptySet()
        return if (includeSystemWhitelist()) {
            customWhitelist.union(defaultSystemWhitelist)
        } else {
            customWhitelist
        }
    }
    
    /**
     * Get the set of regex patterns in the whitelist
     */
    fun getRegexPatterns(): Set<String> {
        return sharedPreferences.getStringSet(KEY_REGEX_PATTERNS, emptySet()) ?: emptySet()
    }
    
    /**
     * Check if an app is whitelisted using exact match or regex patterns
     */
    fun isWhitelisted(packageName: String): Boolean {
        // Check exact matches first (more efficient)
        if (packageName in getExactWhitelistedApps()) {
            return true
        }
        
        // Check regex patterns
        return getRegexPatterns().any { pattern ->
            try {
                val regex = Regex(pattern)
                regex.matches(packageName)
            } catch (e: Exception) {
                // If regex is invalid, just do a simple wildcard matching
                if (pattern.endsWith("*")) {
                    val prefix = pattern.substring(0, pattern.length - 1)
                    packageName.startsWith(prefix)
                } else {
                    false
                }
            }
        }
    }
    
    /**
     * Get all whitelisted packages (for UI display purposes)
     * This combines exact matches and doesn't include regex patterns
     */
    fun getWhitelistedApps(): Set<String> {
        return getExactWhitelistedApps()
    }
    
    /**
     * Add an app to the whitelist (exact match)
     */
    fun addToWhitelist(packageName: String) {
        val current = sharedPreferences.getStringSet(KEY_WHITELISTED_APPS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(packageName)
        sharedPreferences.edit {
            putStringSet(KEY_WHITELISTED_APPS, current)
        }
    }
    
    /**
     * Add a regex pattern to the whitelist
     */
    fun addRegexPattern(pattern: String) {
        val current = sharedPreferences.getStringSet(KEY_REGEX_PATTERNS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(pattern)
        sharedPreferences.edit {
            putStringSet(KEY_REGEX_PATTERNS, current)
        }
    }
    
    /**
     * Remove an exact match from the whitelist
     */
    fun removeFromWhitelist(packageName: String) {
        val current = sharedPreferences.getStringSet(KEY_WHITELISTED_APPS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(packageName)
        sharedPreferences.edit {
            putStringSet(KEY_WHITELISTED_APPS, current)
        }
    }
    
    /**
     * Remove a regex pattern from the whitelist
     */
    fun removeRegexPattern(pattern: String) {
        val current = sharedPreferences.getStringSet(KEY_REGEX_PATTERNS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(pattern)
        sharedPreferences.edit {
            putStringSet(KEY_REGEX_PATTERNS, current)
        }
    }
    
    /**
     * Clear the custom whitelist (both exact matches and regex patterns)
     */
    fun clearWhitelist() {
        sharedPreferences.edit {
            putStringSet(KEY_WHITELISTED_APPS, emptySet())
            putStringSet(KEY_REGEX_PATTERNS, emptySet())
        }
    }
    
    /**
     * Set whether to include system whitelist with custom whitelist
     */
    fun setIncludeSystemWhitelist(include: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_INCLUDE_SYSTEM_WHITELIST, include)
        }
    }
    
    /**
     * Check if system whitelist is included
     */
    fun includeSystemWhitelist(): Boolean {
        return sharedPreferences.getBoolean(KEY_INCLUDE_SYSTEM_WHITELIST, true)
    }
    
    companion object {
        private const val WHITELIST_PREFS_NAME = "app_whitelist_prefs"
        private const val KEY_WHITELISTED_APPS = "whitelisted_apps"
        private const val KEY_REGEX_PATTERNS = "regex_patterns"
        private const val KEY_INCLUDE_SYSTEM_WHITELIST = "include_system_whitelist"
    }
} 