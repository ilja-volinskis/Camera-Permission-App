package com.example.camerapermissionapp

import android.net.Uri
import androidx.camera.view.LifecycleCameraController
import com.example.camerapermissionapp.data.PhotoRepository
import com.example.camerapermissionapp.ui.camera.CameraUiState
import com.example.camerapermissionapp.ui.camera.CameraViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.test.assertEquals
import kotlin.test.assertIs


@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}


class CameraViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: PhotoRepository = mockk()
    private val cameraController: LifecycleCameraController = mockk(relaxed = true)

    @Test
    fun initial_state_is_Idle() {
        val viewModel = CameraViewModel(repository)

        assertEquals(CameraUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun capturePhoto_moves_to_Capturing_then_Success() = runTest {
        val uri: Uri = mockk()
        coEvery {
            repository.capturePhoto(cameraController)
        } returns Result.success(uri)
        val viewModel = CameraViewModel(repository)

        viewModel.capturePhoto(cameraController)
        assertEquals(CameraUiState.Capturing, viewModel.uiState.value)

        testScheduler.advanceUntilIdle()

        assertEquals(CameraUiState.Success(uri), viewModel.uiState.value)
    }

    @Test
    fun capturePhoto_moves_to_Error_on_failure() = runTest {
        coEvery {
            repository.capturePhoto(cameraController)
        } returns Result.failure(RuntimeException("oops"))
        val viewModel = CameraViewModel(repository)

        viewModel.capturePhoto(cameraController)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<CameraUiState.Error>(state)
        assertEquals("oops", state.message)
    }

    @Test
    fun capturePhoto_ignores_repeated_calls_while_already_capturing() = runTest {
        coEvery {
            repository.capturePhoto(cameraController)
        } coAnswers {
            delay(1000)
            Result.success(mockk())
        }
        val viewModel = CameraViewModel(repository)

        viewModel.capturePhoto(cameraController)
        viewModel.capturePhoto(cameraController) // it is noop here

        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.capturePhoto(cameraController) }
    }

    @Test
    fun consumeResult_resets_state_to_Idle() = runTest {
        val uri: Uri = mockk()
        coEvery {
            repository.capturePhoto(cameraController)
        } returns Result.success(uri)

        val viewModel = CameraViewModel(repository)

        viewModel.capturePhoto(cameraController)
        testScheduler.advanceUntilIdle()
        viewModel.consumeResult()

        assertEquals(CameraUiState.Idle, viewModel.uiState.value)
    }
}
