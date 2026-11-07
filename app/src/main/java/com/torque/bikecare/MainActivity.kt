package com.torque.bikecare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.torque.bikecare.ui.navigation.TorqueNavHost
import com.torque.bikecare.ui.theme.TorqueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TorqueTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TorqueNavHost()
                }
            }
        }
    }
}
