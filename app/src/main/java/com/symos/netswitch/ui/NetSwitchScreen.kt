package com.symos.netswitch.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.symos.netswitch.R
import com.symos.netswitch.ui.components.AppTopBar
import com.symos.netswitch.ui.components.PulseIndicator
import com.symos.netswitch.ui.components.SectionLabel
import com.symos.netswitch.ui.components.StatCard
import com.symos.netswitch.ui.components.WaveLine
import com.symos.netswitch.ui.theme.Card
import com.symos.netswitch.ui.theme.CardHigh
import com.symos.netswitch.ui.theme.Cyan
import com.symos.netswitch.ui.theme.Line
import com.symos.netswitch.ui.theme.Orange
import com.symos.netswitch.ui.theme.Pink
import com.symos.netswitch.ui.theme.Teal
import com.symos.netswitch.ui.theme.TealDark
import com.symos.netswitch.ui.theme.TextDim
import com.symos.netswitch.ui.theme.TextMain
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private fun android.content.Context.has(p: String) =
    ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED

private fun Float.formatDistance(): String =
    if (this < 1000) "${roundToInt()} m" else String.format("%.2f km", this / 1000)

@Composable
fun NetSwitchScreen(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val distance by viewModel.distance.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ── Permission state ────────────────────────────────────────────────
    var fineGranted by remember { mutableStateOf(context.has(Manifest.permission.ACCESS_FINE_LOCATION)) }
    var bgGranted by remember { mutableStateOf(context.has(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) }
    var notifGranted by remember {
        mutableStateOf(Build.VERSION.SDK_INT < 33 || context.has(Manifest.permission.POST_NOTIFICATIONS))
    }
    fun refresh() {
        fineGranted = context.has(Manifest.permission.ACCESS_FINE_LOCATION)
        bgGranted = context.has(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        notifGranted = Build.VERSION.SDK_INT < 33 || context.has(Manifest.permission.POST_NOTIFICATIONS)
    }

    var pendingEnable by remember { mutableStateOf(false) }

    // ── Permission launchers (order matters: notif → finish → bg → fine) ─
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refresh() }

    fun finishEnable() {
        if (Build.VERSION.SDK_INT >= 33 && !notifGranted) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        pendingEnable = false
        viewModel.enableMonitoring(
            onError = { m -> scope.launch { snackbar.showSnackbar(m) } },
            onSuccess = { scope.launch { snackbar.showSnackbar("Monitoring armed") } }
        )
    }

    val bgLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        refresh()
        if (pendingEnable) {
            if (!granted) scope.launch {
                snackbar.showSnackbar("All‑time location denied – alerts may be delayed")
            }
            finishEnable()
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refresh()
        if (pendingEnable) {
            if (fineGranted) {
                if (Build.VERSION.SDK_INT >= 29 && !bgGranted) {
                    bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else finishEnable()
            } else {
                pendingEnable = false
                scope.launch { snackbar.showSnackbar("Precise location is required for geofencing") }
            }
        }
    }

    fun beginEnable() {
        pendingEnable = true
        when {
            !fineGranted -> locationLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
            Build.VERSION.SDK_INT >= 29 && !bgGranted ->
                bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            else -> finishEnable()
        }
    }

    // Refresh permissions + distance on every resume
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

    // ── Map state ───────────────────────────────────────────────────────
    val cameraState = rememberCameraPositionState {
        position = CameraPosition(LatLng(47.4979, 19.0402), 11.0f) // Hungary default
    }
    val mapStyle = remember {
        runCatching {
            MapStyleOptions(
                context.resources.openRawResource(R.raw.map_style_dark)
                    .bufferedReader().readText()
            )
        }.getOrNull()
    }
    LaunchedEffect(ui.home) {
        ui.home?.let {
            cameraState.position = CameraPosition(LatLng(it.latitude, it.longitude), 15f)
        }
    }
    LaunchedEffect(ui.selected) {
        ui.selected?.let {
            runCatching {
                cameraState.move(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f))
            }
        }
    }
    val homeIcon = remember { BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN) }
    val pickIcon = remember { BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { AppTopBar() }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Hero status card ─────────────────────────────────────────
            Surface(shape = RoundedCornerShape(16.dp), color = Card,
                border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
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
                                Text(
                                    if (ui.monitoring) "ACTIVE" else "INACTIVE",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (ui.monitoring) Teal else Orange
                                )
                            }
                            SectionLabel("MONITORING")
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    WaveLine()
                }
            }

            // ── Stat cards ───────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StatCard(
                    label = "HOME",
                    value = if (ui.home != null) "SET" else "—",
                    caption = ui.home?.let { String.format("%.4f, %.4f", it.latitude, it.longitude) }
                        ?: "tap the map below",
                    accent = if (ui.home != null) Teal else Orange,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "DISTANCE",
                    value = distance?.formatDistance() ?: "—",
                    caption = "from home",
                    accent = Cyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "STATE",
                    value = if (ui.monitoring) "ARMED" else "OFF",
                    caption = if (ui.monitoring) "geofence live" else "standby",
                    accent = if (ui.monitoring) Teal else Pink,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Map card ─────────────────────────────────────────────────
            Surface(shape = RoundedCornerShape(16.dp), color = Card,
                border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
                Box(Modifier.height(340.dp)) {
                    GoogleMap(
                        modifier = Modifier.matchParentSize(),
                        cameraPositionState = cameraState,
                        properties = MapProperties(
                            mapStyleOptions = mapStyle,
                            isMyLocationEnabled = fineGranted
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = fineGranted,
                            compassEnabled = true,
                            mapToolbarEnabled = false
                        ),
                        onMapClick = { latLng ->
                            viewModel.selectLocation(latLng.latitude, latLng.longitude)
                        }
                    ) {
                        val anchor = ui.selected ?: ui.home
                        anchor?.let {
                            val pos = LatLng(it.latitude, it.longitude)
                            Circle(
                                center = pos,
                                radius = ui.radius.toDouble(),
                                fillColor = Teal.copy(alpha = 0.12f),
                                strokeColor = Teal,
                                strokeWidth = 3f
                            )
                            Marker(
                                position = pos,
                                title = "Home",
                                icon = if (ui.selected != null) pickIcon else homeIcon
                            )
                        }
                    }

                    if (ui.home == null && ui.selected == null) {
                        Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "TAP THE MAP TO DROP YOUR HOME MARKER",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMain,
                                modifier = Modifier
                                    .background(CardHigh.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            )
                        }
                    }

                    // Save bar
                    val sel = ui.selected
                    AnimatedVisibility(
                        visible = sel != null,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Surface(
                            Modifier.padding(10.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = CardHigh.copy(alpha = 0.97f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Teal.copy(alpha = 0.6f))
                        ) {
                            Row(
                                Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.LocationOn, null, tint = Pink, modifier = Modifier.size(20.dp))
                                Column(Modifier.weight(1f)) {
                                    SectionLabel("NEW HOME")
                                    Text(
                                        sel?.let { String.format("%.5f, %.5f", it.latitude, it.longitude) } ?: "",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextMain
                                    )
                                }
                                IconButton(onClick = viewModel::clearSelection) {
                                    Icon(Icons.Rounded.Close, "Cancel", tint = TextDim)
                                }
                                Button(
                                    onClick = {
                                        viewModel.saveSelection {
                                            scope.launch { snackbar.showSnackbar("Home saved") }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Teal, contentColor = TealDark
                                    )
                                ) { Text("SAVE") }
                            }
                        }
                    }
                }
            }

            // ── Radius slider card ───────────────────────────────────────
            Surface(shape = RoundedCornerShape(16.dp), color = Card,
                border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel("GEOFENCE RADIUS")
                        Spacer(Modifier.weight(1f))
                        Text("${ui.radius} m", style = MaterialTheme.typography.titleMedium, color = Teal)
                    }
                    Slider(
                        value = ui.radius.toFloat(),
                        onValueChange = { viewModel.onRadiusChanged(it.roundToInt()) },
                        onValueChangeFinished = viewModel::onRadiusChangeFinished,
                        valueRange = 50f..500f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = Teal,
                            activeTrackColor = Teal,
                            inactiveTrackColor = CardHigh
                        )
                    )
                    Row(Modifier.fillMaxWidth()) {
                        Text("50 m", style = MaterialTheme.typography.labelSmall, color = TextDim)
                        Spacer(Modifier.weight(1f))
                        Text("500 m", style = MaterialTheme.typography.labelSmall, color = TextDim)
                    }
                    Text(
                        "The circle on the map updates live.",
                        style = MaterialTheme.typography.bodySmall, color = TextDim
                    )
                }
            }

            // ── Monitoring card ──────────────────────────────────────────
            Surface(shape = RoundedCornerShape(16.dp), color = Card,
                border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            SectionLabel("MONITORING")
                            Text(
                                if (ui.monitoring) "Watching for your arrival" else "Geofence is disarmed",
                                style = MaterialTheme.typography.bodyMedium, color = TextDim
                            )
                        }
                        Switch(
                            checked = ui.monitoring,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    viewModel.disableMonitoring()
                                    scope.launch { snackbar.showSnackbar("Monitoring stopped") }
                                } else if (ui.home == null) {
                                    scope.launch { snackbar.showSnackbar("Set your home location first") }
                                } else beginEnable()
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Teal,
                                checkedThumbColor = TealDark,
                                uncheckedTrackColor = CardHigh
                            )
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermissionChip("LOCATION", fineGranted) {
                            locationLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }
                        PermissionChip("ALL‑TIME", bgGranted) {
                            if (Build.VERSION.SDK_INT >= 29) {
                                scope.launch {
                                    snackbar.showSnackbar("Choose “Allow all the time”")
                                }
                                context.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.parse("package:${context.packageName}"))
                                )
                            }
                        }
                        PermissionChip("NOTIFICATIONS", notifGranted) {
                            if (Build.VERSION.SDK_INT >= 33) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "HyperOS/MIUI: allow Autostart and remove battery limits for NetSwitch so background alerts keep working.",
                        style = MaterialTheme.typography.bodySmall, color = Orange.copy(alpha = 0.85f)
                    )
                }
            }

            // ── How it works ─────────────────────────────────────────────
            Surface(shape = RoundedCornerShape(16.dp), color = Card,
                border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("HOW IT WORKS")
                    FlowItem(Icons.Rounded.LocationOn, Teal, "1 · A geofence watches your home zone in the background.")
                    FlowItem(Icons.Rounded.NotificationsActive, Orange, "2 · On arrival you get a high‑priority alert.")
                    FlowItem(Icons.Rounded.Wifi, Pink, "3 · One tap opens Wi‑Fi or mobile‑data settings.")
                }
            }

            Text(
                "Geofence‑based Wi‑Fi reminder · all data stays on device",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun FlowItem(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: androidx.compose.ui.graphics.Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextMain)
    }
}

@Composable
private fun PermissionChip(label: String, granted: Boolean, onClick: () -> Unit) {
    Surface(
        Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (granted) TealDark else CardHigh,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, if (granted) Teal.copy(alpha = 0.5f) else Orange.copy(alpha = 0.5f)
        )
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                if (granted) Icons.Rounded.Check else Icons.Rounded.Warning,
                null, Modifier.size(12.dp),
                tint = if (granted) Teal else Orange
            )
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = if (granted) Teal else Orange)
        }
    }
}
