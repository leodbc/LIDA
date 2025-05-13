package org.lida.launcher.database

import android.graphics.Color
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import org.lida.launcher.R

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,

    val username: String,
    val password: String, // only for parents
    val educationLevel: Int,
    val age: Int,
    val accountType: String, // "student" or "parent"
    val isCurrentUser: Boolean = false,
    val iconResId: Int = R.drawable.default_icon,
    val iconColor: Int = Color.BLUE
)


@Entity(
    tableName = "hidden_apps",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class HiddenAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: Long,
    val packageName: String
)


@Entity(
    tableName = "recommended_apps",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class RecommendedAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: Long,
    val packageName: String
)

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("UPDATE users SET isCurrentUser = 0")
    suspend fun clearCurrentUserFlag()

    @Query("UPDATE users SET isCurrentUser = 1 WHERE userId = :userId")
    suspend fun setCurrentUser(userId: Long)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUserById(userId: Long)
}

@Dao
interface HiddenAppDao {
    @Insert
    suspend fun insertHiddenApp(hiddenApp: HiddenAppEntity): Long

    @Delete
    suspend fun deleteHiddenApp(hiddenApp: HiddenAppEntity)

    @Query("SELECT * FROM hidden_apps WHERE userId = :userId")
    suspend fun getHiddenAppsForUser(userId: Long): List<HiddenAppEntity>

    @Query("SELECT * FROM hidden_apps WHERE userId = :userId AND packageName = :packageName")
    suspend fun getHiddenApp(userId: Long, packageName: String): HiddenAppEntity?

    @Query("DELETE FROM hidden_apps WHERE userId = :userId AND packageName = :packageName")
    suspend fun deleteHiddenApp(userId: Long, packageName: String)

    @Query("DELETE FROM hidden_apps WHERE userId = :userId")
    suspend fun deleteAllHiddenAppsForUser(userId: Long)
}

@Dao
interface RecommendedAppDao {
    @Insert
    suspend fun insertRecommendedApp(recommendedApp: RecommendedAppEntity): Long

    @Delete
    suspend fun deleteRecommendedApp(recommendedApp: RecommendedAppEntity)

    @Query("SELECT * FROM recommended_apps WHERE userId = :userId")
    suspend fun getRecommendedAppsForUser(userId: Long): List<RecommendedAppEntity>

    @Query("SELECT * FROM recommended_apps WHERE userId = :userId AND packageName = :packageName")
    suspend fun getRecommendedApp(userId: Long, packageName: String): RecommendedAppEntity?

    @Query("DELETE FROM recommended_apps WHERE userId = :userId AND packageName = :packageName")
    suspend fun deleteRecommendedApp(userId: Long, packageName: String)

    @Query("DELETE FROM recommended_apps WHERE userId = :userId")
    suspend fun deleteAllRecommendedAppsForUser(userId: Long)
}
