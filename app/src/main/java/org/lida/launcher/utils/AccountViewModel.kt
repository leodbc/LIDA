package org.lida.launcher.utils

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import org.lida.launcher.database.UserEntity
import org.lida.launcher.R

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val accountManager = AccountManager.getInstance(application)

    suspend fun createUser(
        username: String,
        displayName: String,
        password: String,
        educationLevel: Int,
        accountType: String
    ): Result<Long> {
        return try {
            val userId = accountManager.createUser(
                username = username,
                displayName = displayName,
                password = password,
                educationLevel = educationLevel,
                accountType = accountType,
                iconResId = R.drawable.default_icon,
                iconColor = Color.BLUE
            )
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(username: String, password: String): Result<UserEntity> {
        return try {
            val user = accountManager.loginUser(username, password)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logoutCurrentUser() {
        accountManager.logoutCurrentUser()
    }

    suspend fun getCurrentUser(): UserEntity? {
        return accountManager.getCurrentUser()
    }

    suspend fun isUserLoggedIn(): Boolean {
        return accountManager.getCurrentUser() != null
    }

    suspend fun hideApp(packageName: String): Boolean {
        return accountManager.hideApp(packageName)
    }

    suspend fun unhideApp(packageName: String): Boolean {
        return accountManager.unhideApp(packageName)
    }

    suspend fun getHiddenApps(): List<String> {
        return accountManager.getHiddenApps()
    }

    suspend fun isAppHidden(packageName: String): Boolean {
        return accountManager.isAppHidden(packageName)
    }

    suspend fun recommendApp(packageName: String): Boolean {
        return accountManager.recommendApp(packageName)
    }

    suspend fun unrecommendApp(packageName: String): Boolean {
        return accountManager.unrecommendApp(packageName)
    }

    suspend fun getRecommendedApps(): List<String> {
        return accountManager.getRecommendedApps()
    }

    suspend fun isAppRecommended(packageName: String): Boolean {
        return accountManager.isAppRecommended(packageName)
    }

    suspend fun getVisibleApps(packageManager: PackageManager): List<ApplicationInfo> {
        val hiddenApps = getHiddenApps()
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        return installedApps.filter { app ->
            !hiddenApps.contains(app.packageName)
        }
    }

    suspend fun getRecommendedInstalledApps(packageManager: PackageManager): List<ApplicationInfo> {
        val recommendedApps = getRecommendedApps()
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        return installedApps.filter { app ->
            recommendedApps.contains(app.packageName)
        }
    }

    suspend fun getWhoForAppUsage(): Int {
        return accountManager.getWhoForAppUsage()
    }

    suspend fun getIconResId(): Int {
        return accountManager.getIconResId()
    }

    suspend fun getIconResColor(): Int {
        return accountManager.getIconColor()
    }
}