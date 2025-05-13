package org.lida.launcher.repo

import android.content.Context
import org.lida.launcher.database.AppUsageEntity
import org.lida.launcher.database.AppUsageSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lida.launcher.database.LauncherDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit


class AppUsageRepository(context: Context) {
    private val database = LauncherDatabase.getDatabase(context)

    private val appUsageDao = database.appUsageDao()


    suspend fun getTodayUsageSummary(): List<AppUsageSummary> = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis
        val endOfDay = System.currentTimeMillis()

        appUsageDao.getAppUsageSummary(startOfDay, endOfDay)
    }

    suspend fun getUsageSummary(startTime: Long, endTime: Long): List<AppUsageSummary> =
        withContext(Dispatchers.IO) {
            appUsageDao.getAppUsageSummary(startTime, endTime)
        }


    suspend fun getLastNDaysUsageSummary(days: Int): List<AppUsageSummary> = withContext(Dispatchers.IO) {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(days.toLong())

        appUsageDao.getAppUsageSummary(startTime, endTime)
    }

    suspend fun getTodayUsageTimeForApp(packageName: String): Long = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis
        val endOfDay = System.currentTimeMillis()

        appUsageDao.getTotalUsageTime(packageName, startOfDay, endOfDay) ?: 0L
    }

    suspend fun getHourlyUsagePattern(packageName: String, days: Int): Map<String, Long> =
        withContext(Dispatchers.IO) {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(days.toLong())

            appUsageDao.getUsageByHourOfDay(packageName, startTime, endTime)
        }

    suspend fun getDetailedUsageRecords(startTime: Long, endTime: Long): List<AppUsageEntity> =
        withContext(Dispatchers.IO) {
            appUsageDao.getUsageInTimeRange(startTime, endTime)
        }
}