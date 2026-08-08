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
import androidx.compose.animation.AnimatedVisibility
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
import org.osmdroid.views.overlay.TilesOverlay
import java.io.File
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

    // ── Permission launchers ──────────────────────────────────────────────
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
            if (!granted) {
                scope.launch { snackbar.showSnackbar("All-time location denied - alerts may be delayed") }
            }
            finishEnable()
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        refresh()
        if (pendingEnable) {
            val fineResult = results[Manifest.permission.ACCESS_FINE_LOCATION] == true || fineGranted
            if (fineResult) {
                if (Build.VERSION.SDK_INT >= 29 && !bgGranted) {
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
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

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
            Surface(
                shape = RoundedCornerShape(16.dp), color = Card,
                border = BorderStroke(1.dp, Line)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {