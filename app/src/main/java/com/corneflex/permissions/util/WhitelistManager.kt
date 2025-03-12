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
     * Get the set of whitelisted package names
     */
    fun getWhitelistedApps(): Set<String> {
        val customWhitelist = sharedPreferences.getStringSet(KEY_WHITELISTED_APPS, emptySet()) ?: emptySet()
        return if (includeSystemWhitelist()) {
            customWhitelist.union(defaultSystemWhitelist)
        } else {
            customWhitelist
        }
    }
    
    /**
     * Add an app to the whitelist
     */
    fun addToWhitelist(packageName: String) {
        val current = sharedPreferences.getStringSet(KEY_WHITELISTED_APPS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(packageName)
        sharedPreferences.edit {
            putStringSet(KEY_WHITELISTED_APPS, current)
        }
    }
    
    /**
     * Remove an app from the whitelist
     */
    fun removeFromWhitelist(packageName: String) {
        val current = sharedPreferences.getStringSet(KEY_WHITELISTED_APPS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(packageName)
        sharedPreferences.edit {
            putStringSet(KEY_WHITELISTED_APPS, current)
        }
    }
    
    /**
     * Clear the custom whitelist
     */
    fun clearWhitelist() {
        sharedPreferences.edit {
            putStringSet(KEY_WHITELISTED_APPS, emptySet())
        }
    }
    
    /**
     * Check if an app is whitelisted
     */
    fun isWhitelisted(packageName: String): Boolean {
        return packageName in getWhitelistedApps()
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
        private const val KEY_INCLUDE_SYSTEM_WHITELIST = "include_system_whitelist"
    }
} 