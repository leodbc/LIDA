package org.lida.launcher.activity.setup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import org.lida.launcher.components.Logo
import org.lida.launcher.ui.theme.LIDATheme
import org.lida.launcher.components.SetStatusBarColor
import org.lida.launcher.utils.AppUsageServiceManager
import org.lida.launcher.utils.UsagePermissionHelper
import androidx.compose.foundation.clickable



class LegalInfo(): ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        AppUsageServiceManager.startMonitoringService(this)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LIDATheme(dynamicColor = false) {
                SetStatusBarColor(MaterialTheme.colorScheme.background.toArgb())
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LegalInfoScreen(this)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun LegalInfoScreen(context: ComponentActivity) {
    val usageAccessChecked = remember { mutableStateOf(false) }
    var hasLaunchedUsageIntent by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Spacer(modifier = Modifier.height(32.dp))

                Logo()

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Bem-vindo ao LIDA",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Antes de continuar, leia nossas informações legais sobre coleta de dados e privacidade.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LegalSection(
                            title = "Política de Privacidade",
                            content = "Coletamos dados de uso do aplicativo para melhorar a experiência do usuário. Esses dados não são compartilhados com terceiros sem consentimento."
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LegalSection(
                            title = "Termos de Uso",
                            content = "Ao utilizar este aplicativo, você concorda com os termos que regulam o uso, suporte e limitações de responsabilidade."
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LegalSection(
                            title = "Coleta de Dados",
                            content = "A coleta de dados de uso é utilizada para entender padrões de navegação, preferências e otimizar recursos oferecidos pelo LIDA.",
                            showCheckbox = true,
                            checkboxState = usageAccessChecked,
                            onCheckboxChecked = {
                                if (!hasLaunchedUsageIntent) {
                                    if (!UsagePermissionHelper.hasUsageStatsPermission(context)) {
                                        UsagePermissionHelper.requestUsageStatsPermission(context)
                                        hasLaunchedUsageIntent = true
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Column {
                Button(
                    onClick = {
                        val intent = Intent(context, CreateAccount::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    enabled = usageAccessChecked.value,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        "Aceito e Continuar",
                        color = if (usageAccessChecked.value)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}


@Composable
fun LegalSection(
    title: String,
    content: String,
    showCheckbox: Boolean = false,
    checkboxState: MutableState<Boolean>? = null,
    onCheckboxChecked: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (showCheckbox && checkboxState != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = checkboxState.value,
                        onCheckedChange = {
                            checkboxState.value = it
                            if (it) onCheckboxChecked?.invoke()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Permitir acesso ao uso do dispositivo",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
