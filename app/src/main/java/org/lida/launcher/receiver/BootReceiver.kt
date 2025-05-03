package org.lida.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.lida.launcher.utils.AppUsageServiceManager
import org.lida.launcher.utils.UsagePermissionHelper

class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, checking permission to start service")

            if (UsagePermissionHelper.hasUsageStatsPermission(context)) {
                Log.d(TAG, "Starting app usage monitoring service after boot")
                AppUsageServiceManager.startMonitoringService(context)
            } else {
                Log.d(TAG, "Usage stats permission not granted, service not started")
            }
        }
    }
}