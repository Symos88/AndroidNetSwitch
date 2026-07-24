package com.symdev.netswitch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.symdev.netswitch.data.HomeSettings
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    settings: HomeSettings,
    onRadiusChange: (Float) -> Unit,
    onNotifyArrivalChange: (Boolean) -> Unit,
    onNotifyDepartureChange: (Boolean) -> Unit
) {
    var sliderPosition by remember(settings.radiusMeters) { mutableStateOf(settings.radiusMeters) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Geofence radius", fontWeight = FontWeight.SemiBold)
                Text(
                    "${sliderPosition.roundToInt()} meters",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = { onRadiusChange(sliderPosition) },
                    valueRange = 50f..500f,
                    steps = 17
                )
                Text(
                    "A smaller radius switches networks more precisely but may be less reliable indoors.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Notify when arriving home", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = settings.notifyOnArrival,
                        onCheckedChange = onNotifyArrivalChange
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Notify when leaving home", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = settings.notifyOnDeparture,
                        onCheckedChange = onNotifyDepartureChange
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tip for your Poco (HyperOS)", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "HyperOS aggressively closes background apps. For reliable detection, open " +
                        "Settings > Apps > NetSwitch > Battery saver and pick \"No restrictions\", " +
                        "then enable Autostart for NetSwitch under Security app > Permissions > Autostart.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
