package com.example.camerapermissionapp.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.example.camerapermissionapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.Executor

fun capturePhoto(
    context: Context,
    cameraController: LifecycleCameraController,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    cameraController.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val bitmap: Bitmap = image
                .toBitmap().rotateDegrees(image.imageInfo.rotationDegrees)

            onPhotoCaptured(bitmap)
            image.close()
        }
    })
}

suspend fun savePhotoToGallery(
    context: Context,
    photo: Bitmap
): Result<Uri> = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver

    val timestamp = System.currentTimeMillis()
    val name = "IMG_${timestamp}.jpg"
    val appName = context.resources.getString(R.string.app_name)

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.DATE_TAKEN, timestamp)
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${appName}")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        ?: return@withContext Result.failure(IOException("Failed to create MediaStore entry"))

    val stream = resolver.openOutputStream(uri)
        ?: return@withContext Result.failure(IOException("Failed to open output stream"))

    val saved = stream.use{ photo.compress(Bitmap.CompressFormat.JPEG, 100, it) }
    if(!saved) {
        resolver.delete(uri, null, null)
        return@withContext Result.failure(IOException("Failed to write bitmap"))
    }

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
    }

    Result.success(uri)
}

fun Bitmap.rotateDegrees(degrees: Int): Bitmap {
    val matrix = Matrix().apply {
        postRotate(-degrees.toFloat())
        postScale(-1f, -1f)
    }

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}