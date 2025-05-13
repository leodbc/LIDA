package org.lida.launcher.utils

import android.content.Context
import org.lida.launcher.database.*
import org.lida.launcher.repo.*

class AccountManager private constructor(context: Context) {

    private val database = LauncherDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val hiddenAppDao = database.hiddenAppDao()
    private val recommendedAppDao = database.recommendedAppDao()

    private val userRepository = UserRepository(userDao)
    private val hiddenAppRepository = HiddenAppRepository(hiddenAppDao)
    private val recommendedAppRepository = RecommendedAppRepository(recommendedAppDao)

    suspend fun createUser(
        username: String,
        displayName: String,
        password: String,
        educationLevel: Int,
        accountType: String,
        iconResId: Int,
        iconColor: Int
    ): Long {
        return userRepository.createUser(
            username = username,
            displayName = displayName,
            password = password,
            educationLevel = educationLevel,
            accountType = accountType,
            iconResId = iconResId,
            iconColor = iconColor
        )
    }

    suspend fun loginUser(username: String, password: String): UserEntity {
        return userRepository.loginUser(username, password)
    }

    suspend fun logoutCurrentUser() {
        userRepository.logoutCurrentUser()
    }

    suspend fun getCurrentUser(): UserEntity? {
        return userRepository.getCurrentUser()
    }

    suspend fun getCurrentUserId(): Long? {
        return getCurrentUser()?.userId
    }

    suspend fun deleteUser(userId: Long) {
        userRepository.deleteUser(userId)
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return userRepository.getAllUsers()
    }

    suspend fun hideApp(packageName: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        hiddenAppRepository.addHiddenApp(currentUserId, packageName)
        return true
    }

    suspend fun unhideApp(packageName: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        hiddenAppRepository.removeHiddenApp(currentUserId, packageName)
        return true
    }

    suspend fun getHiddenApps(): List<String> {
        val currentUserId = getCurrentUserId() ?: return emptyList()
        return hiddenAppRepository.getHiddenApps(currentUserId)
    }


    suspend fun isAppHidden(packageName: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        return hiddenAppRepository.isAppHidden(currentUserId, packageName)
    }

    suspend fun recommendApp(packageName: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        recommendedAppRepository.addRecommendedApp(currentUserId, packageName)
        return true
    }

    suspend fun unrecommendApp(packageName: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        recommendedAppRepository.removeRecommendedApp(currentUserId, packageName)
        return true
    }

    suspend fun getRecommendedApps(): List<String> {
        val currentUserId = getCurrentUserId() ?: return emptyList()
        return recommendedAppRepository.getRecommendedApps(currentUserId)
    }

    suspend fun isAppRecommended(packageName: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        return recommendedAppRepository.isAppRecommended(currentUserId, packageName)
    }


    suspend fun getWhoForAppUsage(): Int {
        val currentUser = getCurrentUser()
        return currentUser?.userId?.toInt() ?: 0
    }

    suspend fun getIconResId(): Int {
        val currentUser = getCurrentUser()
        return currentUser?.iconResId ?: 0
    }

    suspend fun getIconColor(): Int {
        val currentUser = getCurrentUser()
        return currentUser?.iconColor ?: 0
    }


    companion object {
        @Volatile
        private var INSTANCE: AccountManager? = null

        fun getInstance(context: Context): AccountManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AccountManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}