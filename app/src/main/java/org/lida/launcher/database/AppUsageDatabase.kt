package org.lida.launcher.database

import androidx.room.*
import androidx.room.Entity

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val packageName: String,
    val startTime: Long,
    var endTime: Long,
    var durationMs: Long,
    val who: Int
)

data class AppUsageSummary(
    val packageName: String,
    val totalDuration: Long,
    val launchCount: Int,
    val who: Int
)

@Dao
interface AppUsageDao {
    @Insert
    suspend fun insert(appUsage: AppUsageEntity): Long

    @Update
    suspend fun update(appUsage: AppUsageEntity)

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName ORDER BY startTime DESC LIMIT :limit")
    suspend fun getLatestByPackage(packageName: String, limit: Int): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage WHERE startTime BETWEEN :startTime AND :endTime ORDER BY startTime ASC")
    suspend fun getUsageInTimeRange(startTime: Long, endTime: Long): List<AppUsageEntity>

    @Query("""
        SELECT packageName, who, 
        SUM(durationMs) as totalDuration, 
        COUNT(*) as launchCount 
        FROM app_usage 
        WHERE startTime BETWEEN :startTime AND :endTime 
        GROUP BY packageName 
        ORDER BY totalDuration DESC
    """)
    suspend fun getAppUsageSummary(startTime: Long, endTime: Long): List<AppUsageSummary>

    @Query("""
        SELECT SUM(durationMs) 
        FROM app_usage 
        WHERE packageName = :packageName AND startTime BETWEEN :startTime AND :endTime
    """)
    suspend fun getTotalUsageTime(packageName: String, startTime: Long, endTime: Long): Long?


    @Query("""
        SELECT 
        strftime('%H', datetime(startTime / 1000, 'unixepoch', 'localtime')) as hourOfDay,
        SUM(durationMs) as totalDuration
        FROM app_usage 
        WHERE packageName = :packageName AND startTime BETWEEN :startTime AND :endTime
        GROUP BY hourOfDay
        ORDER BY hourOfDay
    """)
    suspend fun getUsageByHourOfDay(packageName: String, startTime: Long, endTime: Long): Map<@MapColumn(columnName = "hourOfDay") String, @MapColumn(columnName = "totalDuration") Long>
}

@Database(entities = [AppUsageEntity::class], version = 1, exportSchema = false)
abstract class AppUsageDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
}