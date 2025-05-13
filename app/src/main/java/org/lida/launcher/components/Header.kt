package org.lida.launcher.components

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class Options {
    var have_back_button = false
    var title = ""
    var have_secondary_button = false
    var secondary_button: @Composable () -> Unit = {}
    var onBackClick: (() -> Unit)? = null
}

@Composable
fun Header(opts: Options) {
    val context = LocalContext.current
    val activity = context as? Activity

    Row(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (opts.have_back_button) {
            Button(
                onClick = {
                    opts.onBackClick?.invoke() ?: activity?.finish() // If null, go back
                },
                modifier = Modifier.size(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Go back",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text(
            text = opts.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (opts.have_secondary_button) {
            opts.secondary_button()
        } else {
            Button(onClick = {}) {}
        }
    }
}