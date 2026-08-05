package com.symos.netswitch.ui

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.symos.netswitch.data.HomeLocation
import com.symos.netswitch.data.PreferencesManager
import com.symos.netswitch.geofence.GeofenceManager
import com.symos.netswitch.service.LocationMonitorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel(private val application: Application) : AndroidViewModel(application) {

    val prefs = PreferencesManager(application)

    private val _selected = MutableStateFlow<HomeLocation?>(null)
    private val _radius = MutableStateFlow(PreferencesManager.DEFAULT_RADIUS)
    private val _distance = MutableStateFlow<Float?>(null)
    val distance: StateFlow<Float?> = _distance

    data class UiState(
        val home: HomeLocation? = null,
        val radius: Int = PreferencesManager.DEFAULT_RADIUS,
        val monitoring: Boolean = false,
        val selected: HomeLocation? = null
    )

    val uiState: StateFlow<UiState> = combine(
        prefs.homeLocation, prefs.monitoringActive, _radius, _selected
    ) { home, monitoring, radius, selected ->
        UiState(home = home, radius = radius, monitoring = monitoring, selected = selected)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    init {
        viewModelScope.launch { _radius.value = prefs.radius.first() }
    }

    fun selectLocation(lat: Double, lng: Double) {
        _selected.value = HomeLocation(lat, lng)
    }

    fun clearSelection() {
        _selected.value = null
    }

    fun saveSelection(onSaved: () -> Unit) {
        val sel = _selected.value ?: return
        viewModelScope.launch {
            prefs.saveHome(sel)
            _selected.value = null
            if (uiState.value.monitoring) {
                GeofenceManager.addGeofences(application, sel, uiState.value.radius) { _, _ -> }
            }
            refreshDistance()
            onSaved()
        }
    }

    fun onRadiusChanged(value: Int) {
        _radius.value = value.coerceIn(PreferencesManager.MIN_RADIUS, PreferencesManager.MAX_RADIUS)
    }

    fun onRadiusChangeFinished() {
        viewModelScope.launch {
            prefs.saveRadius(_radius.value)
            val home = uiState.value.home
            if (uiState.value.monitoring && home != null) {
                GeofenceManager.addGeofences(application, home, _radius.value) { _, _ -> }
            }
        }
    }

    fun enableMonitoring(onError: (String) -> Unit, onSuccess: () -> Unit) {
        val home = uiState.value.home
        if (home == null) {
            onError("Set your home location first.")
            return
        }
        GeofenceManager.addGeofences(application, home, uiState.value.radius) { ok, msg ->
            if (ok) {
                viewModelScope.launch { prefs.setMonitoring(true) }
                LocationMonitorService.start(application)
                onSuccess()
            } else {
                onError(msg ?: "Could not register geofence.")
            }
        }
    }

    fun disableMonitoring() {
        GeofenceManager.removeGeofences(application)
        LocationMonitorService.stop(application)
        viewModelScope.launch { prefs.setMonitoring(false) }
    }

    fun refreshDistance() {
        viewModelScope.launch {
            val home = uiState.value.home ?: return@launch
            try {
                val loc = LocationServices
                    .getFusedLocationProviderClient(application)
                    .lastLocation.await() ?: return@launch
                val out = FloatArray(1)
                Location.distanceBetween(loc.latitude, loc.longitude, home.latitude, home.longitude, out)
                _distance.value = out[0]
            } catch (e: Exception) {
                // permission missing or location unavailable – ignore
            }
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(app) as T
        }
    }
}
