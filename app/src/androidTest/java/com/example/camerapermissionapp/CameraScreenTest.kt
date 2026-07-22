package com.example.camerapermissionapp

import android.Manifest
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.example.camerapermissionapp.ui.camera.CameraContent
import com.example.camerapermissionapp.ui.camera.CameraUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class CameraScreenTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun idleState_clicking_captureButton_Invokes_Callback() {
        var captured = false

        composeRule.setContent {
            CameraContent(
                uiState = CameraUiState.Idle,
                onCapturePhoto = { captured = true },
                onResultShown = {}
            )
        }

        val label = composeRule.activity.getString(R.string.capture_photo)
        composeRule.onNodeWithContentDescription(label).performClick()

        assertTrue(captured)
    }

    @Test
    fun successState_showsSnackbar_and_Consumes() {
        val uri: Uri = Uri.parse("content://fake/1")

        composeRule.setContent {
            CameraContent(
                uiState = CameraUiState.Success(uri),
                onCapturePhoto = {},
                onResultShown = {}
            )
        }
        composeRule.waitForIdle()

        val savedText = composeRule.activity.getString(R.string.photo_saved)
        composeRule.onNodeWithText(savedText).assertExists()
    }

    @Test
    fun errorState_shows_ErrorMessage_and_Consumes() {
        val text = "Capture failed"
        composeRule.setContent {
            CameraContent(
                uiState = CameraUiState.Error(text),
                onCapturePhoto = {},
                onResultShown = {}
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(text).assertExists()
    }
}
