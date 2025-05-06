package org.lida.launcher.components

import android.app.Activity
import android.os.Build
import android.view.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

@Composable
fun SetStatusBarColor(color: Int) {
    val view = LocalView.current
    val window = (view.context as Activity).window
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
            view.setBackgroundColor(color)
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }
    } else {
        // For Android 14 and below
        @Suppress("DEPRECATION")
        window.statusBarColor = color
    }
}