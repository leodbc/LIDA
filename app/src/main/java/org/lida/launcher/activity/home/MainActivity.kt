package org.lida.launcher.activity.home

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material.*

import kotlinx.coroutines.launch
import org.lida.launcher.components.SetStatusBarColor
import org.lida.launcher.activity.setup.LegalInfo
import org.lida.launcher.ui.theme.LIDATheme
import org.lida.launcher.utils.AccountViewModel
import org.lida.launcher.utils.AppUsageServiceManager

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private lateinit var accountViewModel: AccountViewModel

    @RequiresApi(Build.VERSION_CODES.Q)
    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "Role granted successfully")
        } else {
            Log.d(TAG, "Role not granted")
        }
    }

    private fun logout(){
        lifecycleScope.launch {
            accountViewModel.logoutCurrentUser()
            val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val alreadySet = this.getSharedPreferences("settings", MODE_PRIVATE).getBoolean("alreadySet", false)

        if (!alreadySet) {
            val setupIntent = Intent(this, LegalInfo::class.java)
            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(setupIntent)
            finish()
            return
        }

        lifecycleScope.launch {
            if (!accountViewModel.isUserLoggedIn()) {
                val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(loginIntent)
                finish()
                return@launch
            }
        }

        AppUsageServiceManager.startMonitoringService(this)
        setContent {
            LIDATheme(dynamicColor = false) {
                SetStatusBarColor(MaterialTheme.colorScheme.background.toArgb())
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AndroidLauncherHomeScreen(accountViewModel, logout_fun = { logout() })
                    }

                }
            }
        }

        val roleManager = getSystemService(RoleManager::class.java)
        if (roleManager != null &&
            roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        ) {
            roleRequestLauncher.launch(
                roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val alreadySet = this.getSharedPreferences("settings", MODE_PRIVATE).getBoolean("alreadySet", false)
        if (!alreadySet) {
            val setupIntent = Intent(this, LegalInfo::class.java)
            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(setupIntent)
            finish()
        }
    }
}

