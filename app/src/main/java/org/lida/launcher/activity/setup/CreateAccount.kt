package org.lida.launcher.activity.setup

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.lida.launcher.components.Logo
import org.lida.launcher.ui.theme.LIDATheme
import org.lida.launcher.components.Header
import org.lida.launcher.components.Options
import org.lida.launcher.components.SetStatusBarColor
import org.lida.launcher.utils.AppUsageServiceManager
import org.lida.launcher.utils.UsagePermissionHelper
import org.lida.launcher.R


class CreateAccount(): ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        AppUsageServiceManager.startMonitoringService(this)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!UsagePermissionHelper.hasUsageStatsPermission(this))
            UsagePermissionHelper.requestUsageStatsPermission(this)

        setContent {
            var selectedAvatarId by remember { mutableIntStateOf(R.drawable.ic_launcher_background) }
            var profileName by remember { mutableStateOf("") }
            LIDATheme(dynamicColor = false) {
                SetStatusBarColor(MaterialTheme.colorScheme.background.toArgb())
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CreateAccountScreen(this,
                        selectedAvatarId,
                        onAvatarSelected = {
                            selectedAvatarId = it
                        },
                        profileName = profileName,
                        onNameChange = {
                            profileName = it
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CreateAccountScreen(
    context: ComponentActivity,
    selectedAvatarId: Int,
    onAvatarSelected: (Int) -> Unit,
    profileName: String,
    onNameChange: (String) -> Unit = {}
) {
    val header_options = Options()
    header_options.have_back_button = true

    Box(
        modifier = Modifier
            .fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp, 24.dp, 0.dp, 0.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.padding(24.dp, 0.dp)
                ) {
                    Header(header_options)
                }
                Spacer(modifier = Modifier.height(48.dp))
                Logo()
                Spacer(modifier = Modifier.height(88.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val avatarList = List(6) { R.drawable.default_icon }
                        val chunkedAvatars = avatarList.chunked(3)
                        Column(
                            modifier = Modifier
                                .padding(24.dp, 24.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Gray,
                                modifier = Modifier
                                    .size(72.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = selectedAvatarId),
                                    contentDescription = "Selected Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            ProfileRow(profileName, onNameChange)
                            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(6.dp))

                            Spacer(modifier = Modifier.height(16.dp))
                            chunkedAvatars.forEach { rowAvatars ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowAvatars.forEach { avatarId ->
                                        Surface(
                                            shape = CircleShape,
                                            border = if (avatarId == selectedAvatarId) BorderStroke(
                                                2.dp,
                                                Color.White
                                            ) else null,
                                            modifier = Modifier
                                                .size(98.dp)
                                                .clickable { onAvatarSelected(avatarId) }
                                        ) {
                                            Image(
                                                painter = painterResource(id = avatarId),
                                                contentDescription = "Avatar",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        Button(
                            onClick = {
                            },
                            modifier = Modifier
                                .width((56*6).dp)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Text(
                                text = "Próximo",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileRow(profileName: String, onNameChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = profileName,
            onValueChange = { onNameChange(it) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            label = { Text("Nome Perfil") },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { /* Handle click */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inversePrimary
            ),
        ) {
            Text(
                text = "Editar cor",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

