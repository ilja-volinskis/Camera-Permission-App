package com.example.camerapermissionapp.data

import android.net.Uri
import androidx.camera.view.LifecycleCameraController

interface PhotoRepository {
    suspend fun capturePhoto(cameraController: LifecycleCameraController): Result<Uri>
}