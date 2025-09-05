package com.example.fernfreunde.feature.permissions


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun PermissionRequester(
    permission: String,
    // Optional: ob beim ersten Compose automatisch geprüft werden soll
    autoCheckOnStart: Boolean = true
): PermissionState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(isGranted(context, permission)) }
    var requestedOnce by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { ok -> granted = ok }
    )

    if (autoCheckOnStart) {
        LaunchedEffect(permission) {
            granted = isGranted(context, permission)
        }
    }

    val request: () -> Unit = {
        requestedOnce = true
        launcher.launch(permission)
    }

    return remember(granted, requestedOnce) {
        PermissionState(
            granted = granted,
            requestedOnce = requestedOnce,
            request = request
        )
    }
}

private fun isGranted(context: android.content.Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        context, permission
    ) == PackageManager.PERMISSION_GRANTED
}

data class PermissionState(
    val granted: Boolean,
    val requestedOnce: Boolean,
    val request: () -> Unit
)

object Permission {
    const val CAMERA = Manifest.permission.CAMERA
    const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO

    const val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES
    const val READ_MEDIA_VIDEO = Manifest.permission.READ_MEDIA_VIDEO
}
