package com.example.camerapermissionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.camerapermissionapp.ui.MainScreen
import com.example.camerapermissionapp.ui.theme.CameraPermissionAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CameraPermissionAppTheme {
                MainScreen()
            }
        }
    }
}
