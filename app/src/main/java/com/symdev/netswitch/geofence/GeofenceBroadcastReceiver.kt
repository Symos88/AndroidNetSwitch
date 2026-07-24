package com.symdev.netswitch.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.symdev.netswitch.data.HomeLocationRepository
import com.symdev.netswitch.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        val transition = event.geofenceTransition
        if (transition != Geofence.GEOFENCE_TRANSITION_ENTER &&
            transition != Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = HomeLocationRepository(context.applicationContext)
                val settings = repo.currentSettings()
                when (transition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER ->
                        if (settings.notifyOnArrival) NotificationHelper.showArrivalNotification(context)
                    Geofence.GEOFENCE_TRANSITION_EXIT ->
                        if (settings.notifyOnDeparture) NotificationHelper.showDepartureNotification(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
