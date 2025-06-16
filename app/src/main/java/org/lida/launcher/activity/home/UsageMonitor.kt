package org.lida.launcher.activity.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.lida.launcher.R // Assuming you have a drawable for the app icon, e.g., ic_launcher_round
import org.lida.launcher.database.LauncherDatabase // You need to create this for Room
import org.lida.launcher.database.AppUsageDao
import org.lida.launcher.database.AppUsageEntity
import org.lida.launcher.database.AppUsageSummary
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Assume these are placeholder colors or define them in your Theme.kt
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val LightBlue = Color(0xFFADD8E6) // Example for "Limite de Tempo"
val LightGreen = Color(0xFF90EE90) // Example for "Gerir Apps"
val LightPurple = Color(0xFFD8BFD8) // Example for "Monitoramento"
val LightYellow = Color(0xFFFFFFE0) // Example for "Objetivos"


// --- ViewModel ---
class DashboardViewModel(private val appUsageDao: AppUsageDao) : ViewModel() {

    private val _totalUsageToday = MutableStateFlow(0L)
    val totalUsageToday: StateFlow<Long> = _totalUsageToday

    private val _educationalUsageToday = MutableStateFlow(0L) // Placeholder for educational usage
    val educationalUsageToday: StateFlow<Long> = _educationalUsageToday

    private val _goalsMetToday = MutableStateFlow(0) // Placeholder for goals met
    val goalsMetToday: StateFlow<Int> = _goalsMetToday

    private val _recentAppUsage = MutableStateFlow<List<AppUsageSummary>>(emptyList())
    val recentAppUsage: StateFlow<List<AppUsageSummary>> = _recentAppUsage

    init {
        fetchDashboardData()
    }

    private fun fetchDashboardData(who: Int = 1) { // 'who' parameter for selecting child
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTimeToday = calendar.timeInMillis
            val endTimeToday = System.currentTimeMillis()

            // Fetch total usage for today
            val allUsageToday = appUsageDao.getUsageInTimeRange(startTimeToday, endTimeToday)
            _totalUsageToday.value = allUsageToday.sumOf { it.durationMs }

            // Placeholder: For educational usage, you'd need a way to mark apps as educational.
            // For now, let's assume it's a fixed value or based on a separate logic.
            _educationalUsageToday.value = 45 * 60 * 1000L // 45 minutes as per design

            // Placeholder: Goals met
            _goalsMetToday.value = 3 // As per design

            // Fetch recent app usage summary for the selected child
            _recentAppUsage.value = appUsageDao.getAppUsageSummary(who)
        }
    }

    // You might want a function to change the 'who' (child) and refetch data
    fun selectChild(childId: Int) {
        fetchDashboardData(childId)
    }
}

// ViewModel Factory to pass DAO to ViewModel
class DashboardViewModelFactory(private val appUsageDao: AppUsageDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(appUsageDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// --- Activity ---
class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Room Database and DAO
        val appUsageDao = LauncherDatabase.getDatabase(applicationContext).appUsageDao()

        setContent {
            MaterialTheme { // Use your app's theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(appUsageDao = appUsageDao)
                }
            }
        }
    }
}

// --- UI Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(appUsageDao: AppUsageDao) {
    val viewModel: DashboardViewModel = viewModel( // ViewModel initialized here
        factory = DashboardViewModelFactory(appUsageDao)
    )

    val totalUsageToday by viewModel.totalUsageToday.collectAsState()
    val educationalUsageToday by viewModel.educationalUsageToday.collectAsState()
    val goalsMetToday by viewModel.goalsMetToday.collectAsState()
    val recentAppUsage by viewModel.recentAppUsage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("L.I.D.A.", fontWeight = FontWeight.Bold) },
                actions = {
                    // Placeholder for child selection dropdown
                    var expanded by remember { mutableStateOf(false) }
                    val children = listOf("Criança 1", "Criança 2") // Example children
                    var selectedChild by remember { mutableStateOf(children[0]) }

                    TextButton(onClick = { expanded = true }) {
                        Text(selectedChild)
                        Icon(Icons.Default.List, contentDescription = "Select child")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        children.forEachIndexed { index, child ->
                            DropdownMenuItem(
                                text = { Text(child) },
                                onClick = {
                                    selectedChild = child
                                    expanded = false
                                    viewModel.selectChild(index + 1) // Assuming child IDs start from 1
                                }
                            )
                        }
                    }

                    IconButton(onClick = { /* TODO: Navigate to settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
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
                .padding(16.dp)
        ) {
            Text(
                text = "Tela Inicial (Responsável) LIDA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Current date and time
            Text(
                text = SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date()),
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Resumo de Hoje", // ($selectedChild)",
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

            // Placeholder for "Gráfico de uso de Apps"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star, // Placeholder icon
                        contentDescription = "Gráfico de uso de Apps",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Gráfico de uso de Apps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // You can iterate over recentAppUsage to show a list of apps if needed.
            // For now, only the graph placeholder is in the design.
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
            .height(120.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = Color.Black, // Or a contrasting color
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
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