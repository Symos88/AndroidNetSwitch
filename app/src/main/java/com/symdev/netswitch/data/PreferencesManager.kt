package com.symdev.netswitch.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "netswitch_settings")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val LAT = doublePreferencesKey("home_lat")
        val LNG = doublePreferencesKey("home_lng")
        val RADIUS = intPreferencesKey("radius_m")
        val MONITORING = booleanPreferencesKey("monitoring_enabled")
    }

    val homeLocation: Flow<HomeLocation?> = context.dataStore.data.map { prefs ->
        val lat = prefs[Keys.LAT]
        val lng = prefs[Keys.LNG]
        if (lat != null && lng != null) HomeLocation(lat, lng) else null
    }

    val radius: Flow<Int> = context.dataStore.data.map { prefs -> prefs[Keys.RADIUS] ?: DEFAULT_RADIUS }

    val monitoringActive: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.MONITORING] ?: false }

    suspend fun saveHome(home: HomeLocation) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAT] = home.latitude
            prefs[Keys.LNG] = home.longitude
        }
    }

    suspend fun saveRadius(radius: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.RADIUS] = radius }
    }

    suspend fun setMonitoring(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.MONITORING] = enabled }
    }

    companion object {
        const val DEFAULT_RADIUS = 150
        const val MIN_RADIUS = 50
        const val MAX_RADIUS = 500
    }
}
