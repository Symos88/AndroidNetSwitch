package com.symdev.netswitch.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.symdev.netswitch.data.HomeLocation
import com.symdev.netswitch.ui.components.AppTopBar
import com.symdev.netswitch.ui.components.SectionLabel
import com.symdev.netswitch.ui.components.StatCard
import com.symdev.netswitch.ui.theme.Card
import com.symdev.netswitch.ui.theme.CardHigh
import com.symdev.netswitch.ui.theme.Cyan
import com.symdev.netswitch.ui.theme.Line
import com.symdev.netswitch.ui.theme.Orange
import com.symdev.netswitch.ui.theme.Pink
import com.symdev.netswitch.ui.theme.Teal
import com.symdev.netswitch.ui.theme.TealDark
import com.symdev.netswitch.ui.theme.TextDim
import com.symdev.netswitch.ui.theme.TextMain
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.io.File
import kotlin.math.roundToInt

private fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

@Composable
fun NetSwitchScreenFixed(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val distance by viewModel.distance.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var locationGranted by remember { mutableStateOf(context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) }
    var backgroundGranted by remember { mutableStateOf(Build.VERSION.SDK_INT < 29 || context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) }
    var notificationsGranted by remember { mutableStateOf(Build.VERSION.SDK_INT < 33 || context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) }

    fun refreshPermissions() {
        locationGranted = context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        backgroundGranted = Build.VERSION.SDK_INT < 29 || context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        notificationsGranted = Build.VERSION.SDK_INT < 33 || context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
    }

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        refreshPermissions()
    }

    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        refreshPermissions()
    }

    fun openAppLocationSettings() {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        )
    }

    fun requestLocation() {
        locationLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    fun requestOrOpenBackgroundLocation() {
        if (Build.VERSION.SDK_INT < 29) return
        if (Build.VERSION.SDK_INT >= 30) {
            scope.launch { snackbar.showSnackbar("Open Location → Allow all the time for NetSwitch") }
            openAppLocationSettings()
        } else {
            // Android 10 can present the background-location choice in the permission flow.
            context.requestBackgroundLocationLegacy()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
                viewModel.refreshDistance()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var mapRef by remember { mutableStateOf<MapView?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { AppTopBar() }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = Card, border = BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(18.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            SectionLabel("GEOFENCE RADIUS")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${ui.radius}", style = MaterialTheme.typography.displayLarge, color = Teal)
                                Text(" m", style = MaterialTheme.typography.titleMedium, color = TextDim)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (ui.monitoring) "ACTIVE" else "INACTIVE", color = if (ui.monitoring) Teal else Orange)
                            SectionLabel("MONITORING")
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("HOME", if (ui.home != null) "SET" else "-", ui.home?.let { "%.4f, %.4f".format(it.latitude, it.longitude) } ?: "tap map", if (ui.home != null) Teal else Orange, Modifier.weight(1f))
                StatCard("DISTANCE", distance?.let { if (it < 1000) "${it.roundToInt()} m" else "%.2f km".format(it / 1000f) } ?: "-", "from home", Cyan, Modifier.weight(1f))
                StatCard("STATE", if (ui.monitoring) "ARMED" else "OFF", if (ui.monitoring) "geofence live" else "standby", if (ui.monitoring) Teal else Pink, Modifier.weight(1f))
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Card, border = BorderStroke(1.dp, Line)) {
                Column {
                    Box(Modifier.fillMaxWidth().height(340.dp)) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                createMap(ctx, viewModel, ui.home ?: ui.selected, ui.radius.toDouble()).also { mapRef = it }
                            },
                            update = { map ->
                                updateMap(map, ui.selected ?: ui.home, ui.radius.toDouble())
                            }
                        )
                        if (ui.home == null && ui.selected == null) {
                            Surface(
                                Modifier.align(Alignment.Center),
                                shape = RoundedCornerShape(8.dp),
                                color = CardHigh.copy(alpha = 0.94f)
                            ) {
                                Text("TAP THE MAP TO DROP YOUR HOME MARKER", color = TextMain, modifier = Modifier.padding(12.dp))
                            }
                        }
                        if (ui.selected != null) {
                            Surface(
                                Modifier.align(Alignment.BottomCenter).padding(10.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = CardHigh.copy(alpha = 0.97f),
                                border = BorderStroke(1.dp, Teal)
                            ) {
                                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        SectionLabel("NEW HOME")
                                        Text("%.5f, %.5f".format(ui.selected.latitude, ui.selected.longitude), color = TextMain)
                                    }
                                    IconButton(onClick = viewModel::clearSelection) { Icon(Icons.Rounded.Close, "Cancel") }
                                    Button(onClick = { viewModel.saveSelection { scope.launch { snackbar.showSnackbar("Home saved") } } }, colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = TealDark)) { Text("SAVE") }
                                }
                            }
                        }
                    }
                    Text("© OpenStreetMap contributors", color = TextDim, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Card, border = BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            SectionLabel("MONITORING")
                            Text(if (ui.monitoring) "Watching for your arrival" else "Geofence is disarmed", color = TextDim)
                        }
                        Switch(
                            checked = ui.monitoring,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    viewModel.disableMonitoring()
                                } else if (ui.home == null) {
                                    scope.launch { snackbar.showSnackbar("Set your home location first") }
                                } else if (!locationGranted) {
                                    scope.launch { snackbar.showSnackbar("Location permission is required") }
                                    requestLocation()
                                } else if (!backgroundGranted) {
                                    requestOrOpenBackgroundLocation()
                                } else if (!notificationsGranted && Build.VERSION.SDK_INT >= 33) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.enableMonitoring(
                                        onError = { message -> scope.launch { snackbar.showSnackbar(message) } },
                                        onSuccess = { scope.launch { snackbar.showSnackbar("Monitoring armed") } }
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Teal, checkedThumbColor = TealDark, uncheckedTrackColor = CardHigh)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermissionStatus("LOCATION", locationGranted) { requestLocation() }
                        PermissionStatus("ALL-TIME", backgroundGranted) { requestOrOpenBackgroundLocation() }
                        PermissionStatus("NOTIFICATIONS", notificationsGranted) { if (Build.VERSION.SDK_INT >= 33) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                    }
                    if (!backgroundGranted && Build.VERSION.SDK_INT >= 30) {
                        Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Warning, null, tint = Orange, modifier = Modifier.size(18.dp))
                            Text("For Android 11+, open NetSwitch → Location → Allow all the time.", color = Orange, modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Card, border = BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(16.dp)) {
                    SectionLabel("GEOFENCE RADIUS")
                    Slider(value = ui.radius.toFloat(), onValueChange = { viewModel.onRadiusChanged(it.roundToInt()) }, onValueChangeFinished = viewModel::onRadiusChangeFinished, valueRange = 50f..500f, steps = 8, colors = SliderDefaults.colors(thumbColor = Teal, activeTrackColor = Teal, inactiveTrackColor = CardHigh))
                    Row(Modifier.fillMaxWidth()) { Text("50 m", color = TextDim); Spacer(Modifier.weight(1f)); Text("500 m", color = TextDim) }
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Card, border = BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("HOW IT WORKS")
                    HelpRow(Icons.Rounded.LocationOn, Teal, "A geofence watches your home zone in the background.")
                    HelpRow(Icons.Rounded.NotificationsActive, Orange, "On arrival or departure you get an alert.")
                    HelpRow(Icons.Rounded.Wifi, Pink, "One tap opens the Wi-Fi / mobile-data panel.")
                }
            }
        }
    }

    DisposableEffect(mapRef) {
        mapRef?.onResume()
        onDispose { mapRef?.onPause(); mapRef?.onDetach() }
    }
}

private fun Context.requestBackgroundLocationLegacy() {
    // API 29 exposes the background-location option through the system permission flow.
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:$packageName")
    }
    startActivity(intent)
}

private fun createMap(context: Context, viewModel: HomeViewModel, anchor: HomeLocation?, radius: Double): MapView {
    configureOsmdroid(context)
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setUseDataConnection(true)
        setMultiTouchControls(true)
        setTilesScaledToDpi(false)
        overlayManager.tilesOverlay.setLoadingBackgroundColor(AndroidColor.rgb(15, 25, 27))
        val start = anchor?.let { GeoPoint(it.latitude, it.longitude) } ?: GeoPoint(47.4979, 19.0402)
        controller.setZoom(if (anchor != null) 16.0 else 11.0)
        controller.setCenter(start)
        overlays.add(0, MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                viewModel.selectLocation(p.latitude, p.longitude)
                return true
            }
            override fun longPressHelper(p: GeoPoint): Boolean = false
        }))
        updateMap(this, anchor, radius)
    }
}

private fun updateMap(map: MapView, anchor: HomeLocation?, radius: Double) {
    map.overlays.removeAll { it is Marker || it is Polygon }
    if (anchor != null) {
        val point = GeoPoint(anchor.latitude, anchor.longitude)
        val marker = Marker(map).apply {
            position = point
            title = "Home"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        map.overlays.add(marker)
        val circle = Polygon(map).apply {
            points = Polygon.pointsAsCircle(point, radius)
            fillPaint.color = AndroidColor.argb(45, 0, 220, 180)
            outlinePaint.color = AndroidColor.argb(220, 0, 220, 180)
            outlinePaint.strokeWidth = 3f
        }
        map.overlays.add(circle)
    }
    map.invalidate()
}

private fun configureOsmdroid(context: Context) {
    val prefs = context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
    val config = Configuration.getInstance()
    config.load(context, prefs)
    config.userAgentValue = "NetSwitch/2.0 (${context.packageName})"
    val basePath = File(context.filesDir, "osmdroid")
    val tilePath = File(basePath, "tiles")
    basePath.mkdirs()
    tilePath.mkdirs()
    config.osmdroidBasePath = basePath
    config.osmdroidTileCache = tilePath
}

@Composable
private fun PermissionStatus(label: String, granted: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (granted) Teal.copy(alpha = 0.12f) else Orange.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, if (granted) Teal else Orange),
        onClick = onClick
    ) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (granted) "✓" else "!", color = if (granted) Teal else Orange)
            Text(label, color = if (granted) Teal else Orange, modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
private fun HelpRow(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Text(text, color = TextDim, modifier = Modifier.padding(start = 10.dp))
    }
}
