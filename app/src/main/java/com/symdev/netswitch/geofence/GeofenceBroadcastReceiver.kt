package com.symdev.netswitch.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofencingEvent
import com.symdev.netswitch.notification.NotificationHelper

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        when (event.geofenceTransition) {
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER,
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT ->
                NotificationHelper.showGeofenceNotification(context, event.geofenceTransition)
        }
    }
}
