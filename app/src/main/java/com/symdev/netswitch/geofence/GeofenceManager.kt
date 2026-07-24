package com.symdev.netswitch.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofenceManager {

    private const val HOME_GEOFENCE_ID = "home_geofence"
    const val ACTION_GEOFENCE_EVENT = "com.symdev.netswitch.ACTION_GEOFENCE_EVENT"

    private fun geofencingClient(context: Context): GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun addHomeGeofence(
        context: Context,
        lat: Double,
        lng: Double,
        radiusMeters: Float,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val geofence = Geofence.Builder()
            .setRequestId(HOME_GEOFENCE_ID)
            .setCircularRegion(lat, lng, radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .setLoiteringDelay(30_000)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient(context).addGeofences(request, pendingIntent(context))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun removeHomeGeofence(context: Context) {
        geofencingClient(context).removeGeofences(pendingIntent(context))
    }
}
