package org.lida.launcher.activity.home

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.lida.launcher.components.AppIcon
import org.lida.launcher.components.AppItem
import org.lida.launcher.ui.theme.LIDATheme
import org.lida.launcher.components.SetStatusBarColor
import kotlinx.coroutines.delay
import org.lida.launcher.utils.drawableToBitmap


// https://stackoverflow.com/questions/28679346/how-do-i-uninstall-and-re-run-an-app-on-a-device-using-android-studio
// para testar
class AppList : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LIDATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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

        var searchQuery by remember { mutableStateOf("") }
        var debouncedSearch by remember { mutableStateOf("") }

        LaunchedEffect(searchQuery) {
            delay(300)
            debouncedSearch = searchQuery
        }

        val filteredApps by remember(apps, debouncedSearch) {
            derivedStateOf {
                apps.filter { it.name.contains(debouncedSearch, ignoreCase = true) }
            }
        }


        if (apps.isNotEmpty()) {
            val appCountText = if (searchQuery.isNotBlank() && filteredApps.size != apps.size) {
                "${filteredApps.size} of ${apps.size} apps found"
            } else {
                "${apps.size} apps"
            }
            Text(
                text = appCountText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Set to 2 columns
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp), // Add padding around the grid
                verticalArrangement = Arrangement.spacedBy(8.dp), // Spacing between rows
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between columns
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredApps) { app ->
                    AppIcon(app = app) {
                        openApp(context, app.packageName)
                    }
                }
            }
        }
    }
}

fun loadInstalledApps(packageManager: PackageManager): List<AppItem> {
    val apps = mutableListOf<AppItem>()
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val resolveInfoList = packageManager.queryIntentActivities(mainIntent, 0)

    for (resolveInfo in resolveInfoList) {
        val packageName = resolveInfo.activityInfo.packageName
        val appName = resolveInfo.loadLabel(packageManager).toString()
        val drawable = resolveInfo.loadIcon(packageManager)
        val bitmap = drawableToBitmap(drawable).asImageBitmap()

        apps.add(AppItem(appName, bitmap, packageName, ""))
    }

    return apps.sortedBy { it.name.lowercase() }
}

fun openApp(context: android.content.Context, packageName: String) {
    val launchIntent: Intent? = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    }
}
