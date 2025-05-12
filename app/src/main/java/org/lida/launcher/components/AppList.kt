package org.lida.launcher.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import org.lida.launcher.database.AppItem
import androidx.compose.ui.unit.sp

@Composable
fun AppList(apps: List<AppItem>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        apps.forEach { app ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Image(
                    painter = painterResource(app.iconref),
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(84.dp)
                        .padding(end = 8.dp)
                )
            }
        }
    }
}