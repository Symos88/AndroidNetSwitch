package com.symdev.netswitch.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.symdev.netswitch.ui.components.AppTopBar
import com.symdev.netswitch.ui.components.PulseIndicator
import com.symdev.netswitch.ui.components.SectionLabel
import com.symdev.netswitch.ui.components.StatCard
import com.symdev.netswitch.ui.components.WaveLine
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

private fun android.content.Context.has(p: String) = ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED

private fun Float.formatDistance(): String = if (this < 1000) "${roundToInt()} m" else String.format("%.2f km", this / 1000)

@Composable
fun NetSwitchScreen(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val distance by viewModel.distance.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var fineGranted by remember { mutableStateOf(context.has(Manifest.permission.ACCESS_FINE_LOCATION)) }
    var bgGranted by remember { mutableStateOf(Build.VERSION.SDK_INT < 29 || context.has(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) }
    var notifGranted by remember { mutableStateOf(Build.VERSION.SDK_INT < 33 || context.has(Manifest.permission.POST_NOTIFICATIONS)) }
    fun refresh() {
        fineGranted = context.has(Manifest.permission.ACCESS_FINE_LOCATION)
        bgGranted = Build.VERSION.SDK_INT < 29 || context.has(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        notifGranted = Build.VERSION.SDK_INT < 33 || context.has(Manifest.permission.POST_NOTIFICATIONS)
    }

    var pendingEnable by remember { mutableStateOf(false) }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        refresh()
        if (pendingEnable) {
            pendingEnable = false
            if (!granted && Build.VERSION.SDK_INT >= 33) {
                scope.launch { snackbar.showSnackbar("Notifications denied - monitoring was not started") }
            } else {
                viewModel.enableMonitoring(
                    onError = { m -> scope.launch { snackbar.showSnackbar(m) } },
                    onSuccess = { scope.launch { snackbar.showSnackbar("Monitoring armed") } }
                )
            }
        }
    }

    fun finishEnable() {
        if (Build.VERSION.SDK_INT >= 33 && !notifGranted) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        pendingEnable = false
        viewModel.enableMonitoring(
            onError = { m -> scope.launch { snackbar.showSnackbar(m) } },
            onSuccess = { scope.launch { snackbar.showSnackbar("Monitoring armed") } }
        )
    }

    fun openBackgroundLocationSettings() {
        scope.launch { snackbar.showSnackbar("Open Location and select \"Allow all the time\"") }
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:${context.packageName}"))
            )
        }
    }

    val bgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        refresh()
        if (pendingEnable) {
            if (!granted) scope.launch { snackbar.showSnackbar("All-time location is required for reliable background alerts") }
            finishEnable()
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        refresh()
        if (pendingEnable) {
            val fineResult = results[Manifest.permission.ACCESS_FINE_LOCATION] == true || context.has(Manifest.permission.ACCESS_FINE_LOCATION)
            if (fineResult) {
                if (Build.VERSION.SDK_INT >= 30 && !context.has(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    openBackgroundLocationSettings()
                    pendingEnable = false
                } else if (Build.VERSION.SDK_INT == 29 && !context.has(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    finishEnable()
                }
            } else {
                pendingEnable = false
                scope.launch { snackbar.showSnackbar("Precise location is required for geofencing") }
            }
        }
    }

    fun beginEnable() {
        pendingEnable = true
        when {
            !fineGranted -> locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            Build.VERSION.SDK_INT >= 30 && !bgGranted -> {
                openBackgroundLocationSettings()
                pendingEnable = false
            }
            Build.VERSION.SDK_INT == 29 && !bgGranted -> bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            else -> finishEnable()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refresh()
                viewModel.refreshDistance()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background, snackbarHost = { SnackbarHost(snackbar) }, topBar = { AppTopBar() }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PulseIndicator(ui.monitoring)
                                Text(if (ui.monitoring) "ACTIVE" else "INACTIVE", style = MaterialTheme.typography.titleMedium, color = if (ui.monitoring) Teal else Orange)
                            }
                            SectionLabel("MONITORING")
                        }
                    }
                    Spacer(Modifier.height(10.dp)); WaveLine()
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StatCard("HOME", if (ui.home != null) "SET" else "-", ui.home?.let { String.format("%.4f, %.4f", it.latitude, it.longitude) } ?: "tap the map below", if (ui.home != null) Teal else Orange, Modifier.weight(1f))
                StatCard("DISTANCE", distance?.formatDistance() ?: "-", "from home", Cyan, Modifier.weight(1f))
                StatCard("STATE", if (ui.monitoring) "ARMED" else "OFF", if (ui.monitoring) "geofence live" else "standby", if (ui.monitoring) Teal else Pink, Modifier.weight(1f))
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Card, border = BorderStroke(1.dp, Line)) {
                Box(Modifier.height(340.dp)) {
                    AndroidView(modifier = Modifier.matchParentSize(), factory = { ctx ->
                        configureOsmdroid(ctx)
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setUseDataConnection(true)
                            setMultiTouchControls(true)
                            val start = (ui.home ?: ui.selected)?.let { GeoPoint(it.latitude, it.longitude) } ?: GeoPoint(47.4979, 19.0402)
                            controller.setZoom(if (ui.home != null) 16.0 else 11.0)
                            controller.setCenter(start)
                            val receiver = object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean { viewModel.selectLocation(p.latitude, p.longitude); return true }
                                override fun longPressHelper(p: GeoPoint): Boolean = false
                            }
                            overlays.add(0, MapEventsOverlay(receiver))
                            mapViewRef = this
                            refreshHomeOverlay(ui.selected ?: ui.home, ui.radius.toDouble())
                            onResume()
                        }
                    }, update = { view ->
                        view.refreshHomeOverlay(ui.selected ?: ui.home, ui.radius.toDouble())
                        view.invalidate()
                    })
                    if (ui.home == null && ui.selected == null) {
                        Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                            Text("TAP THE MAP TO DROP YOUR HOME MARKER", style = MaterialTheme.typography.labelSmall, color = TextMain, modifier = Modifier.background(CardHigh.copy(alpha = 0.9f), RoundedCornerShape(8.dp)).padding(12.dp))
                        }
                    }
                    val sel = ui.selected
                    if (sel != null) {
                        Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
                            Surface(Modifier.padding(10.dp), shape = RoundedCornerShape(12.dp), color = CardHigh.copy(alpha = 0.97f), border = BorderStroke(1.dp, Teal.copy(alpha = 0.6f))) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Rounded.LocationOn, null, tint = Pink, modifier = Modifier.size(20.dp))
                                    Column(Modifier.weight(1f)) {
                                        SectionLabel("NEW HOME")
                                        Text(String.format("%.5f, %.5f", sel.latitude, sel.longitude), style = MaterialTheme.typography.titleMedium, color = TextMain)
                                    }
                                    IconButton(onClick = viewModel::clearSelection) { Icon(Icons.Rounded.Close, "Cancel", tint = TextDim) }
                                    Button(onClick = { viewModel.saveSelection { scope.launch { snackbar.showSnackbar("Home saved") } } }, colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = TealDark)) { Text("SAVE") }
                                }
                            }
                        }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Card, border = BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { SectionLabel("GEOFENCE RADIUS"); Spacer(Modifier.weight(1f)); Text("${ui.radius} m", style = MaterialTheme.typography.titleMedium, color = Teal) }
                    Slider(value = ui.radius.toFloat(), onValueChange = { viewModel.onRadiusChanged(it.roundToInt()) }, onValueChangeFinished = viewModel::onRadiusChangeFinished, valueRange = 50f..500f, steps = 8, colors = SliderDefaults.colors(thumbColor = Teal, activeTrackColor = Teal, inactiveTrackColor = CardHigh))
                    Row(Modifier.fillMaxWidth()) { Text("50 m", style = MaterialTheme.typography.labelSmall, color = TextDim); Spacer(Modifier.weight(1f)); Text("500 m", style = MaterialTheme.typography.labelSmall, color = TextDim) }
                    Text("The circle on the map updates live.", style = MaterialTheme.typography.bodySmall, color = TextDim)
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Card, border = BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { SectionLabel("MONITORING"); Text(if (ui.monitoring) "Watching for your arrival" else "Geofence is disarmed", style = MaterialTheme.typography.bodyMedium, color = TextDim) }
                        Switch(checked = ui.monitoring, onCheckedChange = { enabled ->
                            if (!enabled) { viewModel.disableMonitoring(); scope.launch { snackbar.showSnackbar("Monitoring stopped") } }
                            else if (ui.home == null) scope.launch { snackbar.showSnackbar("Set your home location first") }
                            else beginEnable()
                        }, colors = SwitchDefaults.colors(checkedTrackColor = Teal, checkedThumbColor = TealDark, uncheckedTrackColor = CardHigh))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermissionChip("LOCATION", fineGranted) { locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }
                        PermissionChip("ALL-TIME", bgGranted) { if (Build.VERSION.SDK_INT >= 30) openBackgroundLocationSettings() else if (Build.VERSION.SDK_INT == 29) bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
                        PermissionChip("NOTIFICATIONS", notifGranted) { if (Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("For Android 11+ open Location in App permissions and choose Allow all the time. HyperOS/MIUI: allow Autostart and remove battery limits for NetSwitch so background alerts keep working.", style = MaterialTheme.typography.bodySmall, color = Orange.copy(alpha = 0.85f))
                }
            }

            Surface(shape = RoundedCornerShape(16.dp), color = Card, border = BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("HOW IT WORKS")
                    FlowItem(Icons.Rounded.LocationOn, Teal, "1 - A geofence watches your home zone in the background.")
                    FlowItem(Icons.Rounded.NotificationsActive, Orange, "2 - On arrival or departure you get a high-priority alert.")
                    FlowItem(Icons.Rounded.Wifi, Pink, "3 - One tap opens the Wi-Fi / mobile-data panel.")
                }
            }
            Text("Geofence-based Wi-Fi reminder · all data stays on device", style = MaterialTheme.typography.labelSmall, color = TextDim, modifier = Modifier.padding(vertical = 4.dp))
        }
    }

    DisposableEffect(mapViewRef) {
        mapViewRef?.onResume()
        onDispose { mapViewRef?.onPause(); mapViewRef?.onDetach() }
    }
}

private fun configureOsmdroid(context: android.content.Context) {
    val prefs = context.getSharedPreferences("osmdroid_prefs", android.content.Context.MODE_PRIVATE)
    val config = Configuration.getInstance()
    config.load(context, prefs)
    config.userAgentValue = context.packageName
    val basePath = File(context.filesDir, "osmdroid")
    val tilePath = File(basePath, "tiles")
    basePath.mkdirs()
    tilePath.mkdirs()
    config.osmdroidBasePath = basePath
    config.osmdroidTileCache = tilePath
}

private fun MapView.refreshHomeOverlay(anchor: com.symdev.netswitch.data.HomeLocation?, radiusMeters: Double) {
    while (overlays.size > 1) overlays.removeAt(overlays.size - 1)
    if (anchor != null) {
        val point = GeoPoint(anchor.latitude, anchor.longitude)
        val circle = Polygon(this); circle.points = Polygon.pointsAsCircle(point, radiusMeters); circle.fillColor = 0x263BE8C8; circle.strokeColor = 0xFF3BE8C8.toInt(); circle.strokeWidth = 3f; overlays.add(circle)
        val marker = Marker(this); marker.position = point; marker.title = "Home"; marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); overlays.add(marker)
    }
    invalidate()
}

@Composable
private fun PermissionChip(label: String, granted: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(10.dp), color = if (granted) Teal.copy(alpha = 0.12f) else Orange.copy(alpha = 0.12f), border = BorderStroke(1.dp, if (granted) Teal.copy(alpha = 0.45f) else Orange.copy(alpha = 0.45f))) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(if (granted) Icons.Rounded.Check else Icons.Rounded.Warning, null, tint = if (granted) Teal else Orange, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMain)
        }
    }
}

@Composable
private fun FlowItem(icon: ImageVector, tint: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = TextDim)
    }
}
