package com.symdev.netswitch.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.symdev.netswitch.NetSwitchApp
import com.symdev.netswitch.R

object NotificationHelper {

    private const val ARRIVAL_NOTIFICATION_ID = 1001
    private const val DEPARTURE_NOTIFICATION_ID = 1002

    private fun internetPanelPendingIntent(context: Context): PendingIntent {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        } else {
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showArrivalNotification(context: Context) {
        show(
            context = context,
            id = ARRIVAL_NOTIFICATION_ID,
            title = "You're home",
            text = "Tap to switch to Wi-Fi and turn off mobile data."
        )
    }

    fun showDepartureNotification(context: Context) {
        show(
            context = context,
            id = DEPARTURE_NOTIFICATION_ID,
            title = "Leaving home",
            text = "Tap to switch back to mobile data."
        )
    }

    @SuppressLint("MissingPermission")
    private fun show(context: Context, id: Int, title: String, text: String) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return

        val notification = NotificationCompat.Builder(context, NetSwitchApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(internetPanelPendingIntent(context))
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
