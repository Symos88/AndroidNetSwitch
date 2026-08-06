package com.symos.netswitch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(context: Context) {

    companion object {
        const val DEFAULT_RADIUS = 150
        const val MIN_RADIUS = 50
        const val MAX_RADIUS = 500

        private val KEY_HOME_LAT = doublePreferencesKey("home_lat")
        private val KEY_HOME_LNG = doublePreferencesKey("home_lng")
        private val KEY_RADIUS = intPreferencesKey("radius")
        private val KEY_MONITORING = booleanPreferencesKey("monitoring")
    }

    private val ds = context.dataStore

    val homeLocation: Flow<HomeLocation?> = ds.data.map { prefs ->
        val lat = prefs[KEY_HOME_LAT]
        val lng = prefs[KEY_HOME_LNG]
        if (lat != null && lng != null) HomeLocation(lat, lng) else null
    }

    val radius: Flow<Int> = ds.data.map { it[KEY_RADIUS] ?: DEFAULT_RADIUS }

    val monitoringActive: Flow<Boolean> = ds.data.map { it[KEY_MONITORING] ?: false }

    suspend fun saveHome(location: HomeLocation) {
        ds.edit {
            it[KEY_HOME_LAT] = location.latitude
            it[KEY_HOME_LNG] = location.longitude
        }
    }

    suspend fun saveRadius(value: Int) {
        ds.edit { it[KEY_RADIUS] = value.coerceIn(MIN_RADIUS, MAX_RADIUS) }
    }

    suspend fun setMonitoring(active: Boolean) {
        ds.edit { it[KEY_MONITORING] = active }
    }
}
