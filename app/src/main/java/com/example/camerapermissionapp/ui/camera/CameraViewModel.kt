package com.example.camerapermissionapp.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camerapermissionapp.data.capturePhoto
import com.example.camerapermissionapp.data.savePhotoToGallery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel(

): ViewModel() {

    private val _state = MutableStateFlow(CameraState())
    val state = _state.asStateFlow()

    fun capturePhotoAndStoreInGallery(
        context: Context,
        cameraController: LifecycleCameraController
    ) {
        capturePhoto(
            context,
            cameraController,
            onPhotoCaptured = {
                _state.value = _state.value.copy(lastImage = it)
                storePhotoInGallery(context, it)
            }
        )
    }

    fun storePhotoInGallery(
        context: Context,
        photo: Bitmap
    ) {
        viewModelScope.launch {
            savePhotoToGallery(context, photo)
                .onSuccess { uri -> Log.i("PHOTO", "Photo saved successfully at ${uri.path}") }
                .onFailure { e -> Log.e("PHOTO", e.message.orEmpty()) }
            photo.recycle()
        }
    }
}

data class CameraState(
    val lastImage: Bitmap? = null
)