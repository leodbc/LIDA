package org.lida.launcher

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import org.lida.launcher.components.SetStatusBarColor
import org.lida.launcher.ui.theme.LIDATheme
import org.lida.launcher.utils.AppUsageServiceManager

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val alreadySet = this.getSharedPreferences("settings", MODE_PRIVATE).getBoolean("alreadySet", false)

        if (!alreadySet) {
            val setupIntent = Intent(this, Setup::class.java)
            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(setupIntent)
            finish()
            return
        }

        AppUsageServiceManager.startMonitoringService(this)
        setContent {
            LIDATheme(dynamicColor = false) {
                SetStatusBarColor(MaterialTheme.colorScheme.background.toArgb())
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidLauncherHomeScreen()
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
            val setupIntent = Intent(this, Setup::class.java)
            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(setupIntent)
            finish()
        }
    }
}

@Composable
fun AndroidLauncherHomeScreen(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column (
            modifier = Modifier
                .align(Alignment.Center)
        ){
            Text(
                text = "ALOOOO",
                color = MaterialTheme.colorScheme.onBackground // Ensure text color contrasts with background
            )
        }
    }
}