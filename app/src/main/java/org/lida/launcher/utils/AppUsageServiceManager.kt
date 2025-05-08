package org.lida.launcher.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.lida.launcher.service.AppUsageService


object AppUsageServiceManager {
    private const val TAG = "AppUsageServiceManager"

    @RequiresApi(Build.VERSION_CODES.Q)
    fun startMonitoringService(context: Context): Boolean {
        if (isRunning(context)) return true

        return if (UsagePermissionHelper.hasUsageStatsPermission(context)) {
            Log.d(TAG, "Starting app usage monitoring service")
            val serviceIntent = Intent(context, AppUsageService::class.java)
            context.startService(serviceIntent)
            true
        } else {
            Log.d(TAG, "Cannot start service: usage stats permission not granted")
            false
        }
    }

    fun stopMonitoringService(context: Context) {
        Log.d(TAG, "Stopping app usage monitoring service")
        val serviceIntent = Intent(context, AppUsageService::class.java)
        context.stopService(serviceIntent)
    }

    fun isRunning(context: Context): Boolean {
        val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == "AppUsageServiceManager" }
    }

}


