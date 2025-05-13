package org.lida.launcher.database

import android.content.Context
import androidx.room.*

@Database(
    entities = [
        UserEntity::class,
        HiddenAppEntity::class,
        RecommendedAppEntity::class,
        AppUsageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun hiddenAppDao(): HiddenAppDao
    abstract fun recommendedAppDao(): RecommendedAppDao
    abstract fun appUsageDao(): AppUsageDao

    companion object {
        @Volatile
        private var INSTANCE: LauncherDatabase? = null

        fun getDatabase(context: Context): LauncherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "launcher_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}