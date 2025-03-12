package com.corneflex.permissions.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.corneflex.permissions.model.AppInfo
import com.corneflex.permissions.model.DangerLevel
import com.corneflex.permissions.model.PermissionGroup
import com.corneflex.permissions.model.PermissionInfo
import com.corneflex.permissions.util.AppPermissionUtils
import com.corneflex.permissions.util.WhitelistManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppPermissionsViewModel(application: Application) : AndroidViewModel(application) {

    private val _installedApps = MutableLiveData<List<AppInfo>>()
    val installedApps: LiveData<List<AppInfo>> = _installedApps

    private val _permissionsByDangerLevel = MutableLiveData<Map<DangerLevel, List<PermissionGroup>>>()
    val permissionsByDangerLevel: LiveData<Map<DangerLevel, List<PermissionGroup>>> = _permissionsByDangerLevel

    private val _searchResults = MutableLiveData<List<PermissionGroup>>()
    val searchResults: LiveData<List<PermissionGroup>> = _searchResults

    private val _isSearchActive = MutableLiveData<Boolean>(false)
    val isSearchActive: LiveData<Boolean> = _isSearchActive

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _isWhitelistFilterActive = MutableLiveData<Boolean>(false)
    val isWhitelistFilterActive: LiveData<Boolean> = _isWhitelistFilterActive
    
    private val _whitelistedApps = MutableLiveData<Set<String>>(emptySet())
    val whitelistedApps: LiveData<Set<String>> = _whitelistedApps
    
    private val _whitelistFilterMode = MutableLiveData<WhitelistFilterMode>(WhitelistFilterMode.SHOW_ONLY_WHITELISTED)
    val whitelistFilterMode: LiveData<WhitelistFilterMode> = _whitelistFilterMode

    // Keep all permission groups in memory for faster searching
    private var allPermissionGroups = listOf<PermissionGroup>()
    
    // All apps loaded from the device
    private var allApps = listOf<AppInfo>()
    
    // Whitelist manager
    private val whitelistManager = WhitelistManager(application)

    /**
     * Load all installed apps and their permissions
     */
    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                withContext(Dispatchers.IO) {
                    // Load all apps
                    allApps = AppPermissionUtils.getInstalledApps(getApplication())
                    
                    // Get current whitelist
                    val whitelist = whitelistManager.getWhitelistedApps()
                    _whitelistedApps.postValue(whitelist)
                    
                    // Apply filters if active
                    val filteredApps = if (_isWhitelistFilterActive.value == true) {
                        applyWhitelistFilter(allApps)
                    } else {
                        allApps
                    }
                    
                    _installedApps.postValue(filteredApps)
                    
                    // Group permissions by danger level and associate apps
                    val permissionGroups = createPermissionGroups(filteredApps)
                    _permissionsByDangerLevel.postValue(permissionGroups)
                    
                    // Store all permission groups for searching
                    allPermissionGroups = permissionGroups.values.flatten()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Apply whitelist filtering to apps list
     */
    private fun applyWhitelistFilter(apps: List<AppInfo>): List<AppInfo> {
        val whitelist = whitelistManager.getWhitelistedApps()
        
        return when (_whitelistFilterMode.value) {
            WhitelistFilterMode.SHOW_ONLY_WHITELISTED -> {
                apps.filter { it.packageName in whitelist }
            }
            WhitelistFilterMode.EXCLUDE_WHITELISTED -> {
                apps.filter { it.packageName !in whitelist }
            }
            else -> apps
        }
    }
    
    /**
     * Toggle whitelist filtering
     */
    fun toggleWhitelistFilter(enabled: Boolean) {
        _isWhitelistFilterActive.value = enabled
        refreshData()
    }
    
    /**
     * Set whitelist filter mode
     */
    fun setWhitelistFilterMode(mode: WhitelistFilterMode) {
        _whitelistFilterMode.value = mode
        if (_isWhitelistFilterActive.value == true) {
            refreshData()
        }
    }
    
    /**
     * Add an app to the whitelist
     */
    fun addToWhitelist(packageName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                whitelistManager.addToWhitelist(packageName)
                _whitelistedApps.postValue(whitelistManager.getWhitelistedApps())
                if (_isWhitelistFilterActive.value == true) {
                    refreshData()
                }
            }
        }
    }
    
    /**
     * Remove an app from the whitelist
     */
    fun removeFromWhitelist(packageName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                whitelistManager.removeFromWhitelist(packageName)
                _whitelistedApps.postValue(whitelistManager.getWhitelistedApps())
                if (_isWhitelistFilterActive.value == true) {
                    refreshData()
                }
            }
        }
    }
    
    /**
     * Clear the whitelist
     */
    fun clearWhitelist() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                whitelistManager.clearWhitelist()
                _whitelistedApps.postValue(whitelistManager.getWhitelistedApps())
                if (_isWhitelistFilterActive.value == true) {
                    refreshData()
                }
            }
        }
    }
    
    /**
     * Toggle system whitelist inclusion
     */
    fun setIncludeSystemWhitelist(include: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                whitelistManager.setIncludeSystemWhitelist(include)
                _whitelistedApps.postValue(whitelistManager.getWhitelistedApps())
                if (_isWhitelistFilterActive.value == true) {
                    refreshData()
                }
            }
        }
    }
    
    /**
     * Refresh data with current filter settings
     */
    private fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                withContext(Dispatchers.IO) {
                    // Apply filters if active
                    val filteredApps = if (_isWhitelistFilterActive.value == true) {
                        applyWhitelistFilter(allApps)
                    } else {
                        allApps
                    }
                    
                    _installedApps.postValue(filteredApps)
                    
                    // Group permissions by danger level and associate apps
                    val permissionGroups = createPermissionGroups(filteredApps)
                    _permissionsByDangerLevel.postValue(permissionGroups)
                    
                    // Update all permission groups for searching
                    allPermissionGroups = permissionGroups.values.flatten()
                    
                    // Clear search if active
                    if (_isSearchActive.value == true) {
                        searchPermissions(_searchQuery.value ?: "")
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Search permissions by query text, app name, or app package name (bundle ID)
     */
    fun searchPermissions(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            _isSearchActive.value = false
            return
        }
        
        _isSearchActive.value = true
        
        val lowerCaseQuery = query.lowercase()
        val filteredPermissions = allPermissionGroups.filter { permissionGroup ->
            // Search in permission name
            permissionGroup.permissionInfo.name.lowercase().contains(lowerCaseQuery) ||
            // Search in permission description
            permissionGroup.permissionInfo.description?.lowercase()?.contains(lowerCaseQuery) == true ||
            // Search in app names
            permissionGroup.apps.any { it.appName.lowercase().contains(lowerCaseQuery) } ||
            // Search in app package names (bundle IDs)
            permissionGroup.apps.any { it.packageName.lowercase().contains(lowerCaseQuery) }
        }
        
        _searchResults.value = filteredPermissions
    }

    /**
     * Clear the current search
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _isSearchActive.value = false
    }

    /**
     * Create permission groups categorized by danger level
     */
    private fun createPermissionGroups(apps: List<AppInfo>): Map<DangerLevel, List<PermissionGroup>> {
        // Get all unique permissions
        val allPermissions = mutableSetOf<PermissionInfo>()
        apps.forEach { app -> allPermissions.addAll(app.permissions) }
        
        // Group permissions by danger level
        val groupedByDangerLevel = mutableMapOf<DangerLevel, MutableList<PermissionGroup>>()
        
        // Initialize all danger levels
        DangerLevel.values().forEach { dangerLevel ->
            groupedByDangerLevel[dangerLevel] = mutableListOf()
        }
        
        // For each permission, find all apps that use it
        allPermissions.forEach { permission ->
            val appsWithPermission = apps.filter { app ->
                app.permissions.any { it.name == permission.name }
            }
            
            if (appsWithPermission.isNotEmpty()) {
                val permissionGroup = PermissionGroup(
                    permissionInfo = permission,
                    apps = appsWithPermission
                )
                
                groupedByDangerLevel[permission.dangerLevel]?.add(permissionGroup)
            }
        }
        
        return groupedByDangerLevel
    }
    
    /**
     * Whitelist filter modes
     */
    enum class WhitelistFilterMode {
        SHOW_ONLY_WHITELISTED,  // Show only whitelisted apps
        EXCLUDE_WHITELISTED     // Exclude whitelisted apps (blacklist mode)
    }
} 