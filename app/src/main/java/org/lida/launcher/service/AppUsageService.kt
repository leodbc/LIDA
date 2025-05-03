package org.lida.launcher.service

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.room.Room
import org.lida.launcher.database.AppUsageDatabase
import org.lida.launcher.database.AppUsageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AppUsageService : Service() {
    private val TAG = "AppUsageService"
    private val POLLING_INTERVAL = TimeUnit.SECONDS.toMillis(30)

    private lateinit var usageStatsManager: UsageStatsManager
    private val handler = Handler(Looper.getMainLooper())
    private var lastForegroundApp = ""
    private var lastTimeStamp = 0L

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppUsageDatabase::class.java,
            "app_usage_database"
        ).build()
    }

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        startMonitoring()
        Log.d(TAG, "AppUsageService created")
    }

    private fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                checkCurrentApp()
                handler.postDelayed(this, POLLING_INTERVAL)
            }
        })
    }

    private fun checkCurrentApp() {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 5000

        val currentUserId = 1 // need to get the current user ID

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
            saveAppUsageStart(currentApp, currentTime, currentUserId)
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
        handler.removeCallbacksAndMessages(null)
        serviceJob.cancel()
        Log.d(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // We don't provide binding
    }
}