package com.symdev.netswitch.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.symdev.netswitch.data.HomeLocation

object GeofenceManager {

    const val MIN_RADIUS = 50
    const val MAX_RADIUS = 500
    const val DEFAULT_RADIUS = 150

    private const val HOME_GEOFENCE_ID = "home_geofence"
    const val ACTION_GEOFENCE_EVENT = "com.symdev.netswitch.ACTION_GEOFENCE_EVENT"

    fun normalizeRadius(radiusMeters: Int): Int =
        radiusMeters.coerceIn(MIN_RADIUS, MAX_RADIUS)

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun addGeofences(
        context: Context,
        home: HomeLocation,
        radiusMeters: Int,
        callback: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val safeRadius = normalizeRadius(radiusMeters)
        val geofence = Geofence.Builder()
            .setRequestId(HOME_GEOFENCE_ID)
            .setCircularRegion(home.latitude, home.longitude, safeRadius.toFloat())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .setNotificationResponsiveness(30_000)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofence(geofence)
            .build()

        LocationServices.getGeofencingClient(context)
            .addGeofences(request, pendingIntent(context))
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { callback(false, it.message) }
    }

    fun removeGeofences(context: Context) {
        LocationServices.getGeofencingClient(context).removeGeofences(pendingIntent(context))
    }
}
