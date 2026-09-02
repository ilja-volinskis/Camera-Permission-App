package com.example.camerapermissionapp.ui.camera

import android.net.Uri
import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camerapermissionapp.data.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun capturePhoto(cameraController: LifecycleCameraController) {
        if (_uiState.value == CameraUiState.Capturing) return

        _uiState.value = CameraUiState.Capturing
        viewModelScope.launch {
            photoRepository.capturePhoto(cameraController)
                .onSuccess { uri -> _uiState.value = CameraUiState.Success(uri) }
                .onFailure { e -> _uiState.value = CameraUiState.Error(e.message ?: "Capture failed") }
        }
    }

    fun consumeResult() {
        _uiState.value = CameraUiState.Idle
    }
}

sealed interface CameraUiState {
    data object Idle: CameraUiState
    data object Capturing: CameraUiState
    data class Success(val uri: Uri): CameraUiState
    data class Error(val message: String): CameraUiState
}

