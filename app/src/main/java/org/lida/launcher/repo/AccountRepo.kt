package org.lida.launcher.repo

import org.lida.launcher.database.HiddenAppDao
import org.lida.launcher.database.HiddenAppEntity
import org.lida.launcher.database.RecommendedAppDao
import org.lida.launcher.database.RecommendedAppEntity
import org.lida.launcher.database.UserDao
import org.lida.launcher.database.UserEntity


class UserRepository(private val userDao: UserDao) {

    suspend fun createUser(
        username: String,
        displayName: String,
        password: String,
        educationLevel: Int,
        accountType: String,
        iconResId: Int,
        iconColor: Int
    ): Long {
        val existingUser = userDao.getUserByUsername(username)
        if (existingUser != null) {
            throw IllegalArgumentException("Username already exists")
        }

        val user = UserEntity(
            username = username,
            displayName = displayName,
            password = password,
            educationLevel = educationLevel,
            accountType = accountType,
            iconResId = iconResId,
            iconColor = iconColor
        )

        return userDao.insertUser(user)
    }

    suspend fun loginUser(username: String, password: String): UserEntity {
        val user = userDao.getUserByUsername(username)
            ?: throw IllegalArgumentException("User not found")

        if (user.password != password) {
            throw IllegalArgumentException("Invalid password")
        }

        userDao.clearCurrentUserFlag()
        userDao.setCurrentUser(user.userId)

        return user
    }

    suspend fun logoutCurrentUser() {
        userDao.clearCurrentUserFlag()
    }

    suspend fun getCurrentUser(): UserEntity? {
        return userDao.getCurrentUser()
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(userId: Long) {
        userDao.deleteUserById(userId)
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAllUsers()
    }
}

class HiddenAppRepository(private val hiddenAppDao: HiddenAppDao) {

    suspend fun addHiddenApp(userId: Long, packageName: String) {
        val existingApp = hiddenAppDao.getHiddenApp(userId, packageName)
        if (existingApp == null) {
            val hiddenApp = HiddenAppEntity(
                userId = userId,
                packageName = packageName
            )
            hiddenAppDao.insertHiddenApp(hiddenApp)
        }
    }

    suspend fun removeHiddenApp(userId: Long, packageName: String) {
        hiddenAppDao.deleteHiddenApp(userId, packageName)
    }

    suspend fun getHiddenApps(userId: Long): List<String> {
        return hiddenAppDao.getHiddenAppsForUser(userId).map { it.packageName }
    }

    suspend fun isAppHidden(userId: Long, packageName: String): Boolean {
        return hiddenAppDao.getHiddenApp(userId, packageName) != null
    }

    suspend fun clearHiddenApps(userId: Long) {
        hiddenAppDao.deleteAllHiddenAppsForUser(userId)
    }
}

class RecommendedAppRepository(private val recommendedAppDao: RecommendedAppDao) {

    suspend fun addRecommendedApp(userId: Long, packageName: String) {
        val existingApp = recommendedAppDao.getRecommendedApp(userId, packageName)
        if (existingApp == null) {
            val recommendedApp = RecommendedAppEntity(
                userId = userId,
                packageName = packageName
            )
            recommendedAppDao.insertRecommendedApp(recommendedApp)
        }
    }

    suspend fun removeRecommendedApp(userId: Long, packageName: String) {
        recommendedAppDao.deleteRecommendedApp(userId, packageName)
    }

    suspend fun getRecommendedApps(userId: Long): List<String> {
        return recommendedAppDao.getRecommendedAppsForUser(userId).map { it.packageName }
    }

    suspend fun isAppRecommended(userId: Long, packageName: String): Boolean {
        return recommendedAppDao.getRecommendedApp(userId, packageName) != null
    }

    suspend fun clearRecommendedApps(userId: Long) {
        recommendedAppDao.deleteAllRecommendedAppsForUser(userId)
    }
}
