package com.symdev.netswitch.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.symdev.netswitch.R
import com.symdev.netswitch.ui.SettingsRedirectActivity

object NotificationHelper {

    private const val CHANNEL_ID = "geofence_alert_channel"
    private const val NOTIF_ID = 2001

    fun showGeofenceNotification(context: Context, transition: Int) {
        createChannel(context)

        val isArrival = transition == Geofence.GEOFENCE_TRANSITION_ENTER
        val wifiIntent = Intent(context, SettingsRedirectActivity::class.java).apply {
            putExtra("target", "wifi")
        }
        val dataIntent = Intent(context, SettingsRedirectActivity::class.java).apply {
            putExtra("target", "data")
        }

        val wifiPending = PendingIntent.getActivity(
            context,
            0,
            wifiIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dataPending = PendingIntent.getActivity(
            context,
            1,
            dataIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isArrival) "Welcome home" else "Leaving home"
        val message = if (isArrival) {
            "Open the Internet panel to switch to Wi-Fi"
        } else {
            "Open the Internet panel to switch to mobile data"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_wifi, "Wi-Fi", wifiPending)
            .addAction(R.drawable.ic_data, "Mobile Data", dataPending)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
