package com.symdev.netswitch

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.symdev.netswitch.data.HomeLocationRepository
import com.symdev.netswitch.data.HomeSettings
import com.symdev.netswitch.geofence.GeofenceManager
import com.symdev.netswitch.ui.HomeScreen
import com.symdev.netswitch.ui.MapPickerScreen
import com.symdev.netswitch.ui.SettingsScreen
import com.symdev.netswitch.ui.theme.NetSwitchTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: HomeLocationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = HomeLocationRepository(applicationContext)

        setContent {
            NetSwitchTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NetSwitchApp(repository = repository)
                }
            }
        }
    }
}

fun hasPermission(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

@Composable
fun NetSwitchApp(repository: HomeLocationRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val settings by repository.settingsFlow.collectAsState(initial = HomeSettings())

    var fineLocationGranted by remember {
        mutableStateOf(hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
    }
    var backgroundLocationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            else true
        )
    }
    var notificationsGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            else true
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                fineLocationGranted = hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) else true
                notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    hasPermission(context, Manifest.permission.POST_NOTIFICATIONS) else true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val foregroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        fineLocationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        backgroundLocationGranted = granted
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted
    }

    fun applyMonitoringState(enabled: Boolean) {
        scope.launch {
            repository.setMonitoringEnabled(enabled)
            val current = repository.currentSettings()
            if (enabled && current.latitude != null && current.longitude != null) {
                GeofenceManager.addHomeGeofence(
                    context, current.latitude, current.longitude, current.radiusMeters
                )
            } else {
                GeofenceManager.removeHomeGeofence(context)
            }
        }
    }

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = currentRoute == "map",
                    onClick = {
                        navController.navigate("map") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Map, contentDescription = "Map") },
                    label = { Text("Location") }
                )
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(
                    settings = settings,
                    fineLocationGranted = fineLocationGranted,
                    backgroundLocationGranted = backgroundLocationGranted,
                    notificationsGranted = notificationsGranted,
                    onRequestForegroundLocation = {
                        foregroundLocationLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onRequestBackgroundLocation = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                    },
                    onRequestNotifications = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onOpenAppSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    onToggleMonitoring = { enabled -> applyMonitoringState(enabled) },
                    onChangeLocation = { navController.navigate("map") }
                )
            }
            composable("map") {
                MapPickerScreen(
                    initialLat = settings.latitude,
                    initialLng = settings.longitude,
                    onLocationSelected = { lat, lng ->
                        scope.launch {
                            repository.setHomeLocation(lat, lng)
                            val current = repository.currentSettings()
                            if (current.monitoringEnabled) {
                                GeofenceManager.addHomeGeofence(context, lat, lng, current.radiusMeters)
                            }
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                )
            }
            composable("settings") {
                SettingsScreen(
                    settings = settings,
                    onRadiusChange = { radius ->
                        scope.launch {
                            repository.setRadius(radius)
                            val current = repository.currentSettings()
                            if (current.monitoringEnabled && current.latitude != null && current.longitude != null) {
                                GeofenceManager.addHomeGeofence(
                                    context, current.latitude, current.longitude, radius
                                )
                            }
                        }
                    },
                    onNotifyArrivalChange = { scope.launch { repository.setNotifyOnArrival(it) } },
                    onNotifyDepartureChange = { scope.launch { repository.setNotifyOnDeparture(it) } }
                )
            }
        }
    }
}
