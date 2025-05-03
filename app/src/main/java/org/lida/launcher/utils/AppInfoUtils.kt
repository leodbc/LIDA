package org.lida.launcher.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable


object AppInfoUtils {

    fun getAppName(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }


    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            val packageManager = context.packageManager
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun isSystemApp(context: Context, packageName: String): Boolean {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getInstalledApps(context: Context, includeSystemApps: Boolean = false): List<String> {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(0)

        return installedApps
            .filterNot { !includeSystemApps && (it.flags and ApplicationInfo.FLAG_SYSTEM != 0) }
            .map { it.packageName }
    }
}