package com.symos.netswitch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.symos.netswitch.ui.HomeViewModel
import com.symos.netswitch.ui.NetSwitchScreen
import com.symos.netswitch.ui.theme.NetSwitchTheme

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
