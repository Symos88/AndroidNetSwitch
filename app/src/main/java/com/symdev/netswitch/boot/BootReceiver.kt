package com.symdev.netswitch.boot

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
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
                val appContext = context.applicationContext
                val prefs = PreferencesManager(appContext)
                val home = prefs.homeLocation.first()
                val monitoring = prefs.monitoringActive.first()
                val radius = prefs.radius.first()

                if (!monitoring || home == null) return@launch

                val fineGranted = ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val backgroundGranted = ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                // Permissions can be revoked while the app is not running.
                // Never let the reboot receiver call the location API without
                // the required permissions; the UI can recover on next launch.
                if (fineGranted && backgroundGranted) {
                    GeofenceManager.addGeofences(appContext, home, radius)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
