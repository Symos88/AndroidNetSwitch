package com.symdev.netswitch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.symdev.netswitch.data.HomeSettings
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    settings: HomeSettings,
    fineLocationGranted: Boolean,
    backgroundLocationGranted: Boolean,
    notificationsGranted: Boolean,
    onRequestForegroundLocation: () -> Unit,
    onRequestBackgroundLocation: () -> Unit,
    onRequestNotifications: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onToggleMonitoring: (Boolean) -> Unit,
    onChangeLocation: () -> Unit
) {
    val hasHome = settings.latitude != null && settings.longitude != null
    val allPermissionsGranted = fineLocationGranted && backgroundLocationGranted && notificationsGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("NetSwitch", style = MaterialTheme.typography.titleLarge)
        Text(
            "Automatically switch between Wi-Fi and mobile data based on whether you're home.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Monitoring", fontWeight = FontWeight.SemiBold)
                    Text(
                        if (settings.monitoringEnabled) "Active" else "Paused",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (settings.monitoringEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.monitoringEnabled,
                    onCheckedChange = { onToggleMonitoring(it) },
                    enabled = hasHome && allPermissionsGranted
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Home location", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (hasHome) {
                    Text(
                        "Lat ${roundTo(settings.latitude!!)}, Lng ${roundTo(settings.longitude!!)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Radius: ${settings.radiusMeters.roundToInt()} m",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Not set yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onChangeLocation) {
                    Text(if (hasHome) "Change location" else "Set home location")
                }
            }
        }

        if (!fineLocationGranted) {
            PermissionCard(
                icon = Icons.Filled.LocationOn,
                title = "Location access needed",
                description = "NetSwitch needs your location to know when you're home.",
                buttonText = "Grant access",
                onClick = onRequestForegroundLocation
            )
        } else if (!backgroundLocationGranted) {
            PermissionCard(
                icon = Icons.Filled.LocationOn,
                title = "Background location needed",
                description = "Allow location access \"All the time\" so this works even when the app is closed.",
                buttonText = "Enable",
                onClick = onRequestBackgroundLocation,
                secondaryText = "If nothing happens, open Settings",
                onSecondaryClick = onOpenAppSettings
            )
        }

        if (!notificationsGranted) {
            PermissionCard(
                icon = Icons.Filled.NotificationsActive,
                title = "Notifications needed",
                description = "NetSwitch alerts you with a notification when it's time to switch networks.",
                buttonText = "Allow",
                onClick = onRequestNotifications
            )
        }

        if (hasHome && allPermissionsGranted && !settings.monitoringEnabled) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Wifi, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Everything is set up. Turn on monitoring above to start.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    secondaryText: String? = null,
    onSecondaryClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                Button(onClick = onClick) { Text(buttonText) }
                if (secondaryText != null && onSecondaryClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onSecondaryClick) { Text(secondaryText) }
                }
            }
        }
    }
}

private fun roundTo(value: Double): Double = (value * 10000).roundToInt() / 10000.0
