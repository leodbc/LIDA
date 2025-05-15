package org.lida.launcher.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import org.lida.launcher.database.AppItem
import androidx.compose.ui.unit.sp

@Composable
fun AppList(apps: List<AppItem>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        apps.forEach { app ->
            AppItemView(app = app)
        }
    }
}

@Composable
fun AppItemView(app: AppItem) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Image(
            painter = painterResource(app.iconref),
            contentDescription = app.name,
            modifier = Modifier
                .size(84.dp)
                .padding(end = 8.dp)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.url))
                    context.startActivity(intent)
                }
        )
        Text(
            text = app.name,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
