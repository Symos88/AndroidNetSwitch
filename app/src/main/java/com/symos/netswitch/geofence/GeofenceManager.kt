package com.symos.netswitch.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.symos.netswitch.data.HomeLocation
import kotlinx.coroutines.tasks.await

object GeofenceManager {

    private const val GEOFENCE_ID = "home_geofence"
    private const val REQUEST_CODE = 1001

    fun addGeofences(
        context: Context,
        home: HomeLocation,
        radiusM: Int,
        callback: (Boolean, String?) -> Unit
    ) {
        val client = LocationServices.getGeofencingClient(context)
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(home.latitude, home.longitude, radiusM.toFloat())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            .setAction("com.symos.netswitch.action.GEOFENCE_TRANSITION")
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        try {
            client.addGeofences(request, pendingIntent)
                .addOnSuccessListener { callback(true, null) }
                .addOnFailureListener { callback(false, it.localizedMessage) }
        } catch (e: SecurityException) {
            callback(false, "Location permission missing")
        }
    }

    fun removeGeofences(context: Context) {
        val client = LocationServices.getGeofencingClient(context)
        client.removeGeofences(listOf(GEOFENCE_ID))
    }
}
