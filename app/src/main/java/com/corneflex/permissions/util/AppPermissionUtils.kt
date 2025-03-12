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

    // Google Play Store installer package
    const val PLAY_STORE_INSTALLER_PACKAGE = "com.android.vending"
    
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
            createAppInfo(packageManager, packageInfo, context)
        }
    }

    /**
     * Create an AppInfo object from a PackageInfo
     */
    private fun createAppInfo(packageManager: PackageManager, packageInfo: PackageInfo, context: Context): AppInfo? {
        // Set the isSystemApp flag
        val isSystem = (packageInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0)
        
        // Skip system apps if needed - commented out to allow showing system apps
        // if (isSystem) {
        //     return null
        // }

        val appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString()
        val packageName = packageInfo.packageName
        val icon = packageInfo.applicationInfo?.loadIcon(packageManager)
        val permissions = getPermissionsForPackage(packageManager, packageInfo)
        
        // Get the installer package name
        val installerPackageName = getInstallerPackageName(context, packageName)

        return AppInfo(
            packageName = packageName,
            appName = appName,
            icon = icon,
            permissions = permissions,
            installerPackageName = installerPackageName
        )
    }
    
    /**
     * Check if an app is a system app
     */
    fun isSystemApp(app: AppInfo, context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(app.packageName, 0)
            (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the installer package name for an app (who installed this app)
     */
    private fun getInstallerPackageName(context: Context, packageName: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val packageManager = context.packageManager
                val installerPackageInfo = packageManager.getInstallSourceInfo(packageName)
                installerPackageInfo.initiatingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(packageName)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if an app was installed from the Play Store
     */
    fun isInstalledFromPlayStore(app: AppInfo): Boolean {
        return app.installerPackageName == PLAY_STORE_INSTALLER_PACKAGE
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