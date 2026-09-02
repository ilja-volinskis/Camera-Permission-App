package com.example.camerapermissionapp.ui

import android.Manifest
import androidx.compose.runtime.Composable
import com.example.camerapermissionapp.ui.camera.CameraScreen
import com.example.camerapermissionapp.ui.permission.GetPermissionScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val cameraPermissionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val hasPermission = cameraPermissionState.status.isGranted

    if(hasPermission) {
        CameraScreen()
    }else {
        GetPermissionScreen(cameraPermissionState::launchPermissionRequest)
    }
}