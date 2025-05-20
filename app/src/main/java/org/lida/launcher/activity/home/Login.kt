package org.lida.launcher.activity.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.lida.launcher.database.UserEntity
import org.lida.launcher.ui.theme.LIDATheme
import org.lida.launcher.utils.AccountViewModel

class LoginActivity : ComponentActivity() {
    private lateinit var accountViewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]

        setContent {
            LIDATheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ModernLoginScreen(
                        viewModel = accountViewModel,
                        onLoginSuccess = { navigateToMainActivity() }
                    )
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ModernLoginScreen(
        viewModel: AccountViewModel,
        onLoginSuccess: () -> Unit
    ) {
        var users by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
        var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
        var showPasswordField by remember { mutableStateOf(false) }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            lifecycleScope.launch {
                users = accountViewModel.getAllUsers()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Quem está usando",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
            ) {
                items(users) { user ->
                    UserProfileItem(
                        user = user,
                        isSelected = user == selectedUser,
                        onClick = {
                            selectedUser = user

                            if (user.accountType == "student") {
                                isLoading = true
                                errorMessage = null

                                lifecycleScope.launch {
                                    delay(500)
                                    try {
                                        val result = viewModel.loginUser(user.username, "")
                                        isLoading = false

                                        result.fold(
                                            onSuccess = {
                                                Toast.makeText(this@LoginActivity, "Welcome, ${user.username}!", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            },
                                            onFailure = { exception ->
                                                errorMessage = exception.message ?: "Login failed"
                                            }
                                        )
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = e.message
                                    }
                                }
                            } else {
                                showPasswordField = true
                            }
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = showPasswordField,
                enter = fadeIn(animationSpec = tween(300)) +
                        slideInVertically(animationSpec = tween(300)) { fullHeight -> fullHeight / 2 },
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Enter password for ${selectedUser?.username}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )

                    Button(
                        onClick = {
                            selectedUser?.let { user ->
                                isLoading = true
                                errorMessage = null

                                lifecycleScope.launch {
                                    try {
                                        val result = viewModel.loginUser(user.username, password)
                                        isLoading = false

                                        result.fold(
                                            onSuccess = {
                                                Toast.makeText(this@LoginActivity, "Welcome, ${user.username}!", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            },
                                            onFailure = { exception ->
                                                errorMessage = "Incorrect password"
                                            }
                                        )
                                    } catch (_: Exception) {
                                        isLoading = false
                                        errorMessage = "Login failed"
                                    }
                                }
                            }
                        },
                        enabled = !isLoading && password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Login")
                        }
                    }

                    TextButton(
                        onClick = {
                            showPasswordField = false
                            selectedUser = null
                            password = ""
                            errorMessage = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            if (isLoading && !showPasswordField) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }

    @Composable
    fun UserProfileItem(
        user: UserEntity,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.15f else 1f,
            animationSpec = tween(300)
        )

        val borderColor = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }

        val borderWidth = if (isSelected) 3.dp else 1.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onClick() }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(86.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color(user.iconColor).copy(alpha = 0.7f))
                    .border(borderWidth, borderColor, CircleShape)
            ) {
                Image(
                    painter = painterResource(
                        id = user.iconResId
                    ),
                    contentDescription = "User Avatar",
                    modifier = Modifier.size(48.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                )

                if (user.accountType == "student") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary)
                            .border(1.dp, MaterialTheme.colorScheme.background, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Child Account",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )

            Text(
                text =
                    if (user.accountType == "student") {
                        "Criança"
                    } else {
                        "Responsável"
                    }
                ,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}