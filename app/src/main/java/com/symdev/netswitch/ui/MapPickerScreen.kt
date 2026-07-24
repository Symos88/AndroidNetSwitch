package com.symdev.netswitch.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File

@SuppressLint("MissingPermission")
@Composable
fun MapPickerScreen(
    initialLat: Double?,
    initialLng: Double?,
    onLocationSelected: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(Unit) {
        configureOsmdroid(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.0)
                    val start = GeoPoint(
                        initialLat ?: 47.4979,
                        initialLng ?: 19.0402
                    )
                    controller.setCenter(start)
                    mapViewRef = this
                }
            }
        )

        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Home marker",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 32.dp)
                .size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Pan the map so the pin sits on your home, then confirm.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedButton(
                onClick = {
                    val hasFine = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasFine) {
                        LocationServices.getFusedLocationProviderClient(context).lastLocation
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    mapViewRef?.controller?.setCenter(
                                        GeoPoint(location.latitude, location.longitude)
                                    )
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use my current location")
            }
            Button(
                onClick = {
                    mapViewRef?.mapCenter?.let { center ->
                        onLocationSelected(center.latitude, center.longitude)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set as home")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef?.onPause()
        }
    }
}

private fun configureOsmdroid(context: Context) {
    val prefs = context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
    val config = Configuration.getInstance()
    config.load(context, prefs)
    config.userAgentValue = context.packageName
    val basePath = File(context.filesDir, "osmdroid")
    config.osmdroidBasePath = basePath
    config.osmdroidTileCache = File(basePath, "tiles")
}
