package org.lida.launcher.activity.home

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.lida.launcher.R
import org.lida.launcher.components.AppIcon
import org.lida.launcher.components.AppItem
import org.lida.launcher.ui.theme.LIDATheme

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LIDATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Assuming SetStatusBarColor is a Composable you have elsewhere
                    SetStatusBarColor(MaterialTheme.colorScheme.background.toArgb())
                    AppListScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf(emptyList<AppItem>()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        apps = loadInstalledApps(context.packageManager)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Apps") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        val filteredApps = apps.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }

        if (filteredApps.isEmpty() && searchQuery.isNotBlank()) {
            Text(
                text = "No apps found for \"$searchQuery\"",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else if (apps.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredApps) { app ->
                    AppIcon(app = app) {
                        openApp(context, app.packageName)
                    }
                    Divider()
                }
            }
        }
    }
}

fun loadInstalledApps(packageManager: PackageManager): List<AppItem> {
    val apps = mutableListOf<AppItem>()
    val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

    for (p in packages) {
        val appName = p.applicationInfo.loadLabel(packageManager).toString()
        val packageName = p.packageName
        val defaultIconResId = R.drawable.default_icon
        apps.add(AppItem(appName, defaultIconResId, packageName, ""))
    }
    return apps.sortedBy { it.name.lowercase() }
}

fun openApp(context: android.content.Context, packageName: String) {
    val launchIntent: Intent? = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    }
}
