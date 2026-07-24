package com.symdev.netswitch.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.symdev.netswitch.data.HomeLocationRepository
import com.symdev.netswitch.geofence.GeofenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = HomeLocationRepository(context.applicationContext)
                val settings = repo.currentSettings()
                val lat = settings.latitude
                val lng = settings.longitude
                if (settings.monitoringEnabled && lat != null && lng != null) {
                    GeofenceManager.addHomeGeofence(
                        context.applicationContext,
                        lat,
                        lng,
                        settings.radiusMeters
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
