package org.lida.launcher.activity.home

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import org.lida.launcher.R
import org.lida.launcher.components.AppIcon
import org.lida.launcher.components.AppItem
import org.lida.launcher.ui.theme.LIDATheme
import org.lida.launcher.utils.AccountViewModel
import org.lida.launcher.utils.drawableToBitmap

@Composable
fun AndroidLauncherHomeScreen(viewModel: AccountViewModel) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var currentUser by remember { mutableStateOf<String?>(null) }
    val defaultIconDrawable = ContextCompat.getDrawable(context, R.drawable.default_icon)!!
    val bitmap = drawableToBitmap(defaultIconDrawable).asImageBitmap()
    val educationalApps = remember { getEducationalApps(bitmap) }
    val systemApps = remember { getSystemApps(bitmap) }

    val allApps = remember { educationalApps + systemApps }

    LaunchedEffect(Unit) {
        viewModel.getCurrentUser()?.let { user ->
            currentUser = user.username
        }
    }

    LIDATheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(allApps) { app ->
                        AppIcon(
                            app = app,
                            onClick = {
                                if (isAppInstalled(app.packageName, packageManager)) {
                                    val intent = packageManager.getLaunchIntentForPackage(app.packageName)
                                    intent?.let { context.startActivity(it) }
                                } else {
                                    val intent = Intent(Intent.ACTION_VIEW, app.url.toUri())
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }

            // Dock - Bottom favorites
            DockBar()
        }
    }
}

@Composable
fun DockBar() {
    val context = LocalContext.current
    val defaultIconDrawable = ContextCompat.getDrawable(context, R.drawable.default_icon)!!
    val bitmap = drawableToBitmap(defaultIconDrawable).asImageBitmap()
    val dockApps = remember { getDockApps(bitmap) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                val intent = Intent(context, AppList::class.java)
                context.startActivity(intent)
            },
        ) {
            Text(text = "Ir para lista de apps")
        }
        Button(
            onClick = {
                val intent = Intent(context, DashboardActivity::class.java)
                context.startActivity(intent)
            },
        ) {
            Text(text = "Ir para monitoramento")
        }
        dockApps.forEach { app ->
            IconButton(
                onClick = {
                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    intent?.let { context.startActivity(it) }
                },
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (app.name) {
                            "Phone" -> Icons.Default.Call
                            "Messages" -> Icons.Default.Email
                            "Browser" -> Icons.Default.LocationOn
                            "Settings" -> Icons.Default.Settings
                            else -> Icons.Default.Face
                        },
                        contentDescription = app.name,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// Sample app data
fun getEducationalApps(default_icon: ImageBitmap): List<AppItem> {

    return listOf(
        AppItem(
            "Khan Academy",
            default_icon,
            "org.khanacademy.android",
            "https://play.google.com/store/apps/details?id=org.khanacademy.android"
        ),
        AppItem(
            "Bedtime Math",
            default_icon,
            "org.BedtimeMath.BedtimeMath",
            "https://play.google.com/store/apps/details?id=org.BedtimeMath.BedtimeMath"
        ),
        AppItem(
            "DragonBox",
            default_icon,
            "com.kahoot.numbers",
            "https://play.google.com/store/apps/details?id=com.kahoot.numbers"
        ),
        AppItem(
            "Duolingo",
            default_icon,
            "com.duolingo",
            "https://play.google.com/store/apps/details?id=com.duolingo"
        ),
        AppItem(
            "Mimo",
            default_icon,
            "com.getmimo",
            "https://play.google.com/store/apps/details?id=com.getmimo"
        ),
        AppItem(
            "Photomath",
            default_icon,
            "com.microblink.photomath",
            "https://play.google.com/store/apps/details?id=com.microblink.photomath"
        )
    )
}

fun getSystemApps(default_icon: ImageBitmap): List<AppItem> {
    return listOf(
        AppItem(
            "Calculator",
            default_icon,
            "com.android.calculator2",
            ""
        ),
        AppItem(
            "Calendar",
            default_icon,
            "com.android.calendar",
            ""
        ),
        AppItem(
            "Camera",
            default_icon,
            "com.android.camera",
            ""
        ),
        AppItem(
            "Gallery",
            default_icon,
            "com.android.gallery3d",
            ""
        )
    )
}

fun getDockApps(default_icon: ImageBitmap): List<AppItem> {
    return listOf(
        AppItem(
            "Phone",
            default_icon,
            "com.android.dialer",
            ""
        ),
        AppItem(
            "Messages",
            default_icon,
            "com.android.messaging",
            ""
        ),
        AppItem(
            "Browser",
            default_icon,
            "com.android.chrome",
            ""
        ),
        AppItem(
            "Settings",
            default_icon,
            "com.android.settings",
            ""
        )
    )
}

private fun isAppInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}