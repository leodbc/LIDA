package org.lida.launcher.activity.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope // Used for coroutine scope in Activity context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.lida.launcher.database.LauncherDatabase // You need to create this for Room
import org.lida.launcher.database.AppUsageDao
import org.lida.launcher.database.AppUsageEntity
import org.lida.launcher.database.AppUsageSummary
import org.lida.launcher.database.UserDao // Import UserDao
import org.lida.launcher.database.UserEntity // Import UserEntity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

val LightBlue = Color(0xFF87CEEB) // Example for "Limite de Tempo"
val LightGreen = Color(0xFF7CFC00) // Example for "Gerir Apps"
val LightPurple = Color(0xFFBF80FF) // Example for "Monitoramento"
val LightYellow = Color(0xFFFDD835) // Example for "Objetivos"

enum class TimePeriod { DAILY, WEEKLY }

private val TAG: String = MonitorDashboard::class.java.simpleName
// --- Activity ---
class MonitorDashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Room Database and DAO
        val appDatabase = LauncherDatabase.getDatabase(applicationContext)
        val appUsageDao = appDatabase.appUsageDao()
        val userDao = appDatabase.userDao() // Get UserDao instance

        setContent {
            MaterialTheme { // Use your app's theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the DAO directly to the composable
                    DashboardScreen(
                        appUsageDao = appUsageDao,
                        userDao = userDao, // Pass UserDao
                        lifecycleScope = lifecycleScope
                    )
                }
            }
        }
    }
}

// --- UI Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(appUsageDao: AppUsageDao, userDao: UserDao, lifecycleScope: CoroutineScope) {
    // State variables to hold the data, now directly in the composable
    var totalUsageToday by remember { mutableLongStateOf(0L) }
    var educationalUsageToday by remember { mutableLongStateOf(0L) }
    var goalsMetToday by remember { mutableIntStateOf(0) }

    var childrenUsers by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var selectedChild by remember { mutableStateOf<UserEntity?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) } // State for dropdown menu

    var selectedTimePeriod by remember { mutableStateOf(TimePeriod.DAILY) }
    var top5AppsUsage by remember { mutableStateOf<List<AppUsageSummary>>(emptyList()) }

    val scrollState = rememberScrollState()

    val showContent = selectedChild != null

    // LaunchedEffect to fetch all student users initially
    LaunchedEffect(userDao) {
        lifecycleScope.launch {
            val allUsers = userDao.getAllUsers()
            childrenUsers = allUsers.filter { it.accountType == "student" }
            if (childrenUsers.isNotEmpty()) {
                selectedChild = childrenUsers.first() // Select the first child by default
            }
        }
    }

    // LaunchedEffect to fetch usage data whenever the selectedChild changes
    LaunchedEffect(selectedChild) {
        selectedChild?.let { child ->
            val who = child.userId.toInt() // Use the userId of the selected child

            val calendar = Calendar.getInstance()
            val endTime = System.currentTimeMillis()

            lifecycleScope.launch {
                // Fetch total usage for today (always daily for the summary cards)
                val dailyCalendar = Calendar.getInstance()
                dailyCalendar.set(Calendar.HOUR_OF_DAY, 0)
                dailyCalendar.set(Calendar.MINUTE, 0)
                dailyCalendar.set(Calendar.SECOND, 0)
                dailyCalendar.set(Calendar.MILLISECOND, 0)
                val dailyStartTime = dailyCalendar.timeInMillis
                val allDailyUsage = appUsageDao.getUsageInTimeRange(dailyStartTime, endTime)
                totalUsageToday = allDailyUsage.filter { it.who == who }.sumOf { it.durationMs }

                // Placeholder: For educational usage, you'd need a way to mark apps as educational.
                // For now, let's assume it's a fixed value or based on a separate logic.
                educationalUsageToday = totalUsageToday/2

                // Placeholder: Goals met
                goalsMetToday = 3 // As per design

                // Fetch top 5 app usage based on selected time period
                top5AppsUsage = appUsageDao.getAppUsageSummary(who)
                    .take(5) // Take top 5
            }
        } ?: run {
            // Reset data if no child is selected
            totalUsageToday = 0L
            educationalUsageToday = 0L
            goalsMetToday = 0
            top5AppsUsage = emptyList() // Clear top apps list
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("L.I.D.A.", fontWeight = FontWeight.Bold) },
                actions = {
                    // Child selection dropdown
                    TextButton(onClick = { dropdownExpanded = true }) {
                        Text(selectedChild?.username ?: "Selecione Criança")
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select child")
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        childrenUsers.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user.username) },
                                onClick = {
                                    selectedChild = user
                                    dropdownExpanded = false
                                }
                            )
                        }
                        if (childrenUsers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Nenhuma criança encontrada") },
                                onClick = { dropdownExpanded = false },
                                enabled = false
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "Monitoramento de uso - ${selectedChild?.username ?: "..."}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (showContent) {
                Text(
                    text = "Resumo de Hoje", // Display selected child's name
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SummaryCard(
                        title = "Tempo Total",
                        value = formatMillisToHoursMinutes(totalUsageToday),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SummaryCard(
                        title = "Educativos",
                        value = formatMillisToHoursMinutes(educationalUsageToday),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SummaryCard(
                        title = "Objetivos",
                        value = "$goalsMetToday 🎯",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Acesso Rápido",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                QuickAccessGrid()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Uso Recente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SegmentedButton(
                        selected = selectedTimePeriod == TimePeriod.DAILY,
                        onClick = { selectedTimePeriod = TimePeriod.DAILY },
                        label = { Text("Diário") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SegmentedButton(
                        selected = selectedTimePeriod == TimePeriod.WEEKLY,
                        onClick = { selectedTimePeriod = TimePeriod.WEEKLY },
                        label = { Text("Semanal") }
                    )
                }

                // List of Top 5 Apps
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(), // Use wrapContentHeight for the list
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Top 5 Apps",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (top5AppsUsage.isNotEmpty()) {
                            top5AppsUsage.forEachIndexed { index, app ->
                                AppUsageItem(
                                    appName = app.packageName.substringAfterLast("."), // Extract simple app name
                                    duration = formatMillisToHoursMinutes(app.totalDuration),
                                    index = index + 1
                                )
                                if (index < top5AppsUsage.size - 1) {
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        } else {
                            Text(
                                text = "Nenhum dado de uso encontrado para o período.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(vertical = 16.dp)
                            )
                        }
                    }
                }

            }
            else{
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nenhuma criança foi selecionada para monitoramento.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "Por favor, selecione uma criança no menu superior.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun QuickAccessGrid() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QuickAccessButton(
                icon = Icons.Default.Star, // Replace with appropriate icon
                label = "Limite de Tempo",
                backgroundColor = LightBlue,
                onClick = { /* TODO: Navigate to Limit Time screen */ },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            QuickAccessButton(
                icon = Icons.Default.List, // Replace with appropriate icon
                label = "Gerir Apps",
                backgroundColor = LightGreen,
                onClick = { /* TODO: Navigate to Manage Apps screen */ },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QuickAccessButton(
                icon = Icons.Default.Star, // Replace with appropriate icon
                label = "Monitoramento",
                backgroundColor = LightPurple,
                onClick = { /* TODO: Navigate to Monitoring screen */ },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            QuickAccessButton(
                icon = Icons.Default.Star, // Replace with appropriate icon
                label = "Objetivos",
                backgroundColor = LightYellow,
                onClick = { /* TODO: Navigate to Goals screen */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickAccessButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(48.dp),
                tint = Color.White // Or a contrasting color
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = label,
                color = Color.Black, // Or a contrasting color
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AppUsageItem(appName: String, duration: String, index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$index.", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = appName, fontSize = 16.sp, maxLines = 1)
        }
        Text(text = duration, fontWeight = FontWeight.Medium, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButton(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else Color.DarkGray
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.wrapContentWidth()
    ) {
        label()
    }
}

// --- Helper Functions ---
fun formatMillisToHoursMinutes(milliseconds: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}