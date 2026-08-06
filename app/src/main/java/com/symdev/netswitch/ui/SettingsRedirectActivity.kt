package com.symdev.netswitch.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity

class SettingsRedirectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val target = intent.getStringExtra("target")
        val action = when (target) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "data" -> Settings.ACTION_DATA_ROAMING_SETTINGS
            else -> Settings.ACTION_WIRELESS_SETTINGS
        }
        startActivity(Intent(action))
        finish()
    }
}
