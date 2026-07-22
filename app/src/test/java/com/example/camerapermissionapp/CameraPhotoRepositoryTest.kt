package com.example.camerapermissionapp

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import com.example.camerapermissionapp.data.CameraPhotoRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class CameraPhotoRepositoryTest {

    private val resources: Resources = mockk()
    private val context: Context = mockk(relaxed = true)
    private val cameraController: LifecycleCameraController = mockk(relaxed = true)
    private lateinit var repository: CameraPhotoRepository

    @Before
    fun setUp() {
        every { context.resources } returns resources
        every { resources.getString(R.string.app_name) } returns "CameraPermissionApp"

        mockkConstructor(ContentValues::class)
        every { anyConstructed<ContentValues>().put(any<String>(), any<String>()) } just Runs

        repository = CameraPhotoRepository(context)
    }


    @Test
    fun capturePhoto_success_with_uri() = runTest {
        val savedUri: Uri = mockk()
        val outputResults: ImageCapture.OutputFileResults = mockk()
        every { outputResults.savedUri } returns savedUri
        every {
            cameraController.takePicture(any<ImageCapture.OutputFileOptions>(), any(), any())
        } answers {
            thirdArg<ImageCapture.OnImageSavedCallback>().onImageSaved(outputResults)
        }

        val result = repository.capturePhoto(cameraController)

        assertTrue(result.isSuccess)
        assertEquals(savedUri, result.getOrNull())
    }

    @Test
    fun capturePhoto_fails_when_uri_is_null() = runTest {
        val outputResults: ImageCapture.OutputFileResults = mockk()
        every { outputResults.savedUri } returns null
        every {
            cameraController.takePicture(any<ImageCapture.OutputFileOptions>(), any(), any())
        } answers {
            thirdArg<ImageCapture.OnImageSavedCallback>().onImageSaved(outputResults)
        }

        val result = repository.capturePhoto(cameraController)

        assertTrue(result.isFailure)
    }

    @Test
    fun capturePhoto_capture_errors_are_failure() = runTest {
        val exception: ImageCaptureException = mockk(relaxed = true)
        every {
            cameraController.takePicture(any<ImageCapture.OutputFileOptions>(), any(), any())
        } answers {
            thirdArg<ImageCapture.OnImageSavedCallback>().onError(exception)
        }

        val result = repository.capturePhoto(cameraController)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

}