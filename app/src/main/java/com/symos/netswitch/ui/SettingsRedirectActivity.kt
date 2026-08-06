package com.symos.netswitch.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity

class SettingsRedirectActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent.getStringExtra("target")) {
            "wifi" -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            "data" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    startActivity(Intent(Settings.ACTION_DATA_USAGE_SETTINGS))
                } else {
                    startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                }
            }
        }
        finish()
    }
}
