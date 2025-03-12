package com.corneflex.permissions.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import com.corneflex.permissions.model.AppInfo
import com.corneflex.permissions.model.DangerLevel
import com.corneflex.permissions.model.PermissionInfo as AppPermissionInfo

/**
 * Utility class to work with app permissions
 */
object AppPermissionUtils {

    /**
     * Get all installed apps on the device with their permissions
     * @param context Android context
     * @return List of AppInfo objects
     */
    fun getInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val installedPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA)
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        }

        return installedPackages.mapNotNull { packageInfo ->
            createAppInfo(packageManager, packageInfo)
        }
    }

    /**
     * Create an AppInfo object from a PackageInfo
     */
    private fun createAppInfo(packageManager: PackageManager, packageInfo: PackageInfo): AppInfo? {
        // Skip system apps if needed
        if (packageInfo.applicationInfo?.flags == ApplicationInfo.FLAG_SYSTEM) {
            // Uncomment to skip system apps
             return null
        }

        val appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString()
        val packageName = packageInfo.packageName
        val icon = packageInfo.applicationInfo?.loadIcon(packageManager)
        val permissions = getPermissionsForPackage(packageManager, packageInfo)

        return AppInfo(
            packageName = packageName,
            appName = appName,
            icon = icon,
            permissions = permissions
        )
    }

    /**
     * Get all permissions for a package
     */
    private fun getPermissionsForPackage(
        packageManager: PackageManager,
        packageInfo: PackageInfo
    ): List<AppPermissionInfo> {
        val permissionList = mutableListOf<AppPermissionInfo>()
        
        val declaredPermissions = packageInfo.requestedPermissions ?: return emptyList()
        
        for (permission in declaredPermissions) {
            try {
                val permissionInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPermissionInfo(permission, PackageManager.GET_META_DATA)
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPermissionInfo(permission, 0)
                }
                
                val dangerLevel = getDangerLevel(permissionInfo)
                val description = permissionInfo.loadDescription(packageManager)?.toString()
                
                permissionList.add(
                    AppPermissionInfo(
                        name = permission,
                        description = description,
                        dangerLevel = dangerLevel
                    )
                )
            } catch (e: PackageManager.NameNotFoundException) {
                // Permission not found, add with unknown level
                permissionList.add(
                    AppPermissionInfo(
                        name = permission,
                        description = null,
                        dangerLevel = DangerLevel.UNKNOWN
                    )
                )
            }
        }
        
        return permissionList
    }

    /**
     * Determine the danger level of a permission
     */
    private fun getDangerLevel(permissionInfo: PermissionInfo): DangerLevel {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                when (permissionInfo.protection) {
                    PermissionInfo.PROTECTION_DANGEROUS -> DangerLevel.DANGEROUS
                    PermissionInfo.PROTECTION_NORMAL -> DangerLevel.NORMAL
                    PermissionInfo.PROTECTION_SIGNATURE -> DangerLevel.SIGNATURE
                    else -> DangerLevel.UNKNOWN
                }
            }
            else -> {
                @Suppress("DEPRECATION")
                when (permissionInfo.protectionLevel) {
                    PermissionInfo.PROTECTION_DANGEROUS -> DangerLevel.DANGEROUS
                    PermissionInfo.PROTECTION_NORMAL -> DangerLevel.NORMAL
                    PermissionInfo.PROTECTION_SIGNATURE -> DangerLevel.SIGNATURE
                    else -> DangerLevel.UNKNOWN
                }
            }
        }
    }

    /**
     * Group apps by permission
     */
    fun groupAppsByPermission(apps: List<AppInfo>): Map<AppPermissionInfo, List<AppInfo>> {
        val permissionGroups = mutableMapOf<AppPermissionInfo, MutableList<AppInfo>>()
        
        apps.forEach { app ->
            app.permissions.forEach { permission ->
                if (!permissionGroups.containsKey(permission)) {
                    permissionGroups[permission] = mutableListOf()
                }
                permissionGroups[permission]?.add(app)
            }
        }
        
        return permissionGroups
    }
    
    /**
     * Group permissions by danger level
     */
    fun groupPermissionsByDangerLevel(apps: List<AppInfo>): Map<DangerLevel, List<AppPermissionInfo>> {
        val allPermissions = mutableSetOf<AppPermissionInfo>()
        
        apps.forEach { app ->
            allPermissions.addAll(app.permissions)
        }
        
        return allPermissions.groupBy { it.dangerLevel }
    }
} 