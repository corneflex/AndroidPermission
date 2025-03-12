package com.corneflex.permissions.model

/**
 * Data class representing an installed application
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?,
    val permissions: List<PermissionInfo>
)

/**
 * Data class representing a permission
 */
data class PermissionInfo(
    val name: String,
    val description: String?,
    val dangerLevel: DangerLevel
)

/**
 * Enum representing the danger level of a permission
 */
enum class DangerLevel {
    NORMAL,     // Regular permissions that don't pose privacy risks
    DANGEROUS,  // Permissions that can affect user privacy or device operation
    SIGNATURE,  // Permissions that are granted only to apps with the same signature
    UNKNOWN     // Permissions with unknown danger level
}

/**
 * Data class to group apps by permission
 */
data class PermissionGroup(
    val permissionInfo: PermissionInfo,
    val apps: List<AppInfo>
) 