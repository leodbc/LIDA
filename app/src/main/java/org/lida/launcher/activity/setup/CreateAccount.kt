package org.lida.launcher.activity.setup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.launch
import org.lida.launcher.components.Logo
import org.lida.launcher.ui.theme.LIDATheme
import org.lida.launcher.components.Header
import org.lida.launcher.components.Options
import org.lida.launcher.components.SetStatusBarColor
import org.lida.launcher.utils.AppUsageServiceManager
import org.lida.launcher.utils.UsagePermissionHelper
import org.lida.launcher.R
import org.lida.launcher.activity.home.MainActivity
import org.lida.launcher.utils.AccountViewModel
import androidx.core.content.edit


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

        val settingsSharedPreferences = this.getSharedPreferences("settings", MODE_PRIVATE)
        setContent {
            val viewModel = AccountViewModel(application)
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            var formData by remember { mutableStateOf(AccountFormData()) }

            LIDATheme(dynamicColor = false) {
                SetStatusBarColor(MaterialTheme.colorScheme.background.toArgb())
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CreateAccountScreen(
                        formData = formData,
                        onFormDataChange = { formData = it },
                        onSaveClick = {
                            scope.launch {
                                saveUserData(
                                    formData = formData,
                                    viewModel = viewModel,
                                    context = context,
                                    onSuccess = {
                                        settingsSharedPreferences.edit {putBoolean("alreadySet", true).apply()}
                                        val mainIntent = Intent(context, MainActivity::class.java)
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        context.startActivity(mainIntent)
                                        finish()
                                    },
                                    onFailure = { message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        onCreateAnotherClick = {
                            scope.launch {
                                saveUserData(
                                    formData = formData,
                                    viewModel = viewModel,
                                    context = context,
                                    onSuccess = {
                                        formData = AccountFormData()
                                        (context as Activity).recreate()
                                    },
                                    onFailure = { message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    private suspend fun saveUserData(
        formData: AccountFormData,
        viewModel: AccountViewModel,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (formData.profileName.isBlank() || formData.age.isBlank() || formData.accountType.isBlank()) {
            onFailure("Preencha todos os campos obrigatórios")
            return
        }

        try {
            val result = viewModel.createUser(
                username = formData.profileName,
                password = formData.password,
                educationLevel = formData.educationLevel,
                age = formData.age.toIntOrNull() ?: 0,
                accountType = formData.accountType
            )

            result.onSuccess { userId ->
                Toast.makeText(context, "Usuário criado com sucesso! ID: $userId", Toast.LENGTH_SHORT).show()
                onSuccess()
            }.onFailure { e ->
                onFailure("Erro ao criar usuário: ${e.message}")
            }
        } catch (e: Exception) {
            onFailure("Erro ao criar usuário: ${e.message}")
        }
    }
}

data class AccountFormData(
    val selectedAvatarId: Int = R.drawable.ic_launcher_background,
    val profileName: String = "",
    val age: String = "",
    val accountType: String = "student",
    val password: String = "",
    val educationLevel: Int = 0,
    val showForm: Boolean = false
)


@Composable
fun CreateAccountScreen(
    formData: AccountFormData,
    onFormDataChange: (AccountFormData) -> Unit,
    onSaveClick: () -> Unit,
    onCreateAnotherClick: () -> Unit,
    context: Context = LocalContext.current
) {
    val headerOptions = Options().apply {
        have_back_button = true
        onBackClick = {
            if (formData.showForm) {
                onFormDataChange(formData.copy(showForm = false))
            } else {
                (context as Activity).finish()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Header(headerOptions)
            }
            Spacer(modifier = Modifier.height(48.dp))
            Logo()
            Spacer(modifier = Modifier.height(88.dp))

            AnimatedVisibility(visible = !formData.showForm) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Gray,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = formData.selectedAvatarId),
                                    contentDescription = "Selected Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            ProfileRow(
                                profileName = formData.profileName,
                                onNameChange = { onFormDataChange(formData.copy(profileName = it)) }
                            )
                            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(16.dp))
                            AvatarRow(
                                selectedAvatarId = formData.selectedAvatarId,
                                onAvatarSelected = { onFormDataChange(formData.copy(selectedAvatarId = it)) }
                            )
                        }
                        Button(
                            onClick = {
                                if (formData.profileName.isNotBlank())
                                    onFormDataChange(formData.copy(showForm = true))
                                else
                                    Toast.makeText(context, "Preencha o nome do perfil", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier
                                .width(336.dp)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Text("Continuar", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            AnimatedVisibility(visible = formData.showForm) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
                ) {
                    AccountForm(
                        formData = formData,
                        onFormDataChange = onFormDataChange,
                        onSaveClick = onSaveClick,
                        onCreateAnotherClick = onCreateAnotherClick
                    )
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
            .padding(8.dp, 0.dp),
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

@Composable
fun AvatarRow(selectedAvatarId: Int, onAvatarSelected: (Int) -> Unit) {
    val avatarList = List(6) { R.drawable.default_icon }
    val chunkedAvatars = avatarList.chunked(3)
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

@Composable
fun AccountForm(
    formData: AccountFormData,
    onFormDataChange: (AccountFormData) -> Unit,
    onSaveClick: () -> Unit,
    onCreateAnotherClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        TextField(
            value = formData.age,
            onValueChange = { if (it.isDigitsOnly()) onFormDataChange(formData.copy(age = it)) },
            label = { Text("Idade") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Tipo de conta:", color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("student", "parent").forEach { type ->
                Button(
                    onClick = {
                        onFormDataChange(formData.copy(accountType = type))
                        onFormDataChange(formData.copy(password = ""))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (formData.accountType == type) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (type == "student") "Criança" else "Responsável", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        if (formData.accountType == "parent") {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Senha de configurações:", color = MaterialTheme.colorScheme.onSurface)

            TextField(
                value = formData.password,
                onValueChange = { onFormDataChange(formData.copy(password = it)) },
                label = { Text("Senha (Responsável)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (formData.accountType == "student") {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Nível de educação digital:", color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Básico", "Médio", "Avançado").forEachIndexed { index, label ->
                    Button(
                        onClick = { onFormDataChange(formData.copy(educationLevel = index)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (formData.educationLevel == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(label, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
        ) {
            Text("Finalizar Cadastro", color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateAnotherClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
        ) {
            Text("Salvar e Criar Outro Usuário", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}