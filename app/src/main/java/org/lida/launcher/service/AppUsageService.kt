package org.lida.launcher.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import org.lida.launcher.database.AppUsageEntity
import org.lida.launcher.database.LauncherDatabase
import org.lida.launcher.utils.AccountViewModel

class AppUsageService() : Service() {
    private val TAG = "AppUsageService"
    private val POLLING_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1)

    private lateinit var usageStatsManager: UsageStatsManager
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var lastForegroundApp = ""
    private var lastTimeStamp = 0L

    private val database by lazy { LauncherDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        startForegroundServiceWithNotification()
        startMonitoring()
        Log.d(TAG, "AppUsageService created")
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                checkCurrentApp()
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkCurrentApp() {
        val accountViewModel = AccountViewModel(application)
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 5000
        val currentUserId = accountViewModel.getCurrentUser()?.userId

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var currentApp: String? = null

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentApp = event.packageName
            }
        }

        if (currentApp != null && currentApp != lastForegroundApp) {
            val currentTime = System.currentTimeMillis()

            if (lastForegroundApp.isNotEmpty() && lastTimeStamp > 0) {
                val usageDuration = currentTime - lastTimeStamp
                updateAppUsageDuration(lastForegroundApp, lastTimeStamp, usageDuration)
            }

            lastForegroundApp = currentApp
            lastTimeStamp = currentTime

            Log.d(TAG, "Current foreground app: $currentApp")
            saveAppUsageStart(currentApp, currentTime, currentUserId?.toInt() ?: 0 )
        }
    }

    private fun saveAppUsageStart(packageName: String, timestamp: Long, who: Int) {
        serviceScope.launch {
            try {
                val appUsage = AppUsageEntity(
                    packageName = packageName,
                    startTime = timestamp,
                    endTime = 0L,
                    durationMs = 0L,
                    who = who
                )
                database.appUsageDao().insert(appUsage)
                Log.d(TAG, "Saved app usage start: $packageName at $timestamp")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving app usage data", e)
            }
        }
    }

    private fun updateAppUsageDuration(packageName: String, startTime: Long, duration: Long) {
        serviceScope.launch {
            try {
                val records = database.appUsageDao().getLatestByPackage(packageName, 1)
                if (records.isNotEmpty()) {
                    val record = records[0]
                    record.endTime = startTime + duration
                    record.durationMs = duration
                    database.appUsageDao().update(record)

                    Log.d(TAG, "Updated usage for $packageName: ${TimeUnit.MILLISECONDS.toSeconds(duration)} seconds")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating app usage duration", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.d(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "app_usage_service_channel"
        val channelName = "App Usage Monitoring"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Monitoring App Usage")
            .setContentText("This service is tracking your app usage.")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }
}