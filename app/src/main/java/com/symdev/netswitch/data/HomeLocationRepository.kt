package com.symdev.netswitch.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "netswitch_settings")

data class HomeSettings(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Float = 150f,
    val monitoringEnabled: Boolean = false,
    val notifyOnArrival: Boolean = true,
    val notifyOnDeparture: Boolean = true
)

class HomeLocationRepository(private val context: Context) {

    private object Keys {
        val LAT = doublePreferencesKey("home_lat")
        val LNG = doublePreferencesKey("home_lng")
        val RADIUS = floatPreferencesKey("radius_m")
        val ENABLED = booleanPreferencesKey("monitoring_enabled")
        val NOTIFY_ARRIVAL = booleanPreferencesKey("notify_arrival")
        val NOTIFY_DEPARTURE = booleanPreferencesKey("notify_departure")
    }

    val settingsFlow: Flow<HomeSettings> = context.dataStore.data.map { prefs ->
        HomeSettings(
            latitude = prefs[Keys.LAT],
            longitude = prefs[Keys.LNG],
            radiusMeters = prefs[Keys.RADIUS] ?: 150f,
            monitoringEnabled = prefs[Keys.ENABLED] ?: false,
            notifyOnArrival = prefs[Keys.NOTIFY_ARRIVAL] ?: true,
            notifyOnDeparture = prefs[Keys.NOTIFY_DEPARTURE] ?: true
        )
    }

    suspend fun currentSettings(): HomeSettings = settingsFlow.first()

    suspend fun setHomeLocation(lat: Double, lng: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAT] = lat
            prefs[Keys.LNG] = lng
        }
    }

    suspend fun setRadius(radius: Float) {
        context.dataStore.edit { prefs -> prefs[Keys.RADIUS] = radius }
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.ENABLED] = enabled }
    }

    suspend fun setNotifyOnArrival(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.NOTIFY_ARRIVAL] = enabled }
    }

    suspend fun setNotifyOnDeparture(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.NOTIFY_DEPARTURE] = enabled }
    }
}
