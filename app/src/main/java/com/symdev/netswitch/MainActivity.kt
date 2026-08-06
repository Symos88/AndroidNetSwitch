package com.symdev.netswitch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.symdev.netswitch.ui.HomeViewModel
import com.symdev.netswitch.ui.NetSwitchScreen
import com.symdev.netswitch.ui.theme.NetSwitchTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            NetSwitchTheme {
                NetSwitchScreen(viewModel)
            }
        }
    }
}
