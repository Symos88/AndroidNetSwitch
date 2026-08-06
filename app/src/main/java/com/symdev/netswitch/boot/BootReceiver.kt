package com.symdev.netswitch.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.symdev.netswitch.data.PreferencesManager
import com.symdev.netswitch.geofence.GeofenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = PreferencesManager(context.applicationContext)
                val home = prefs.homeLocation.first()
                val monitoring = prefs.monitoringActive.first()
                val radius = prefs.radius.first()
                if (monitoring && home != null) {
                    GeofenceManager.addGeofences(context.applicationContext, home, radius)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
