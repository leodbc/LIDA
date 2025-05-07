package org.lida.launcher.components

//import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.graphics.painter.Painter
//import androidx.compose.ui.graphics.painter.BitmapPainter
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.core.graphics.drawable.toBitmap
import org.lida.launcher.database.AppItem

@Composable
fun AppList(apps: List<AppItem>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        apps.forEach { app ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
//                Image(
//                    bitmap = app.icon.toBitmap().asImageBitmap(),
//                    contentDescription = app.name,
//                    modifier = Modifier
//                        .size(48.dp)
//                        .padding(end = 8.dp)
//                )
            }
        }
    }
}