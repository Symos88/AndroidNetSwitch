package com.symos.netswitch.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.symos.netswitch.R
import com.symos.netswitch.ui.SettingsRedirectActivity

object NotificationHelper {

    private const val CHANNEL_ID = "geofence_alert_channel"
    private const val NOTIF_ID = 2001

    fun showArrivalNotification(context: Context) {
        createChannel(context)

        val wifiIntent = Intent(context, SettingsRedirectActivity::class.java).apply {
            putExtra("target", "wifi")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val dataIntent = Intent(context, SettingsRedirectActivity::class.java).apply {
            putExtra("target", "data")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val wifiPending = PendingIntent.getActivity(
            context, 0, wifiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dataPending = PendingIntent.getActivity(
            context, 1, dataIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
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
