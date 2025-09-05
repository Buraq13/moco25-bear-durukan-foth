package com.example.fernfreunde.feature.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

enum class MediaType { Image, Video }

/**
 * Merkt sich einen Media-Picker und gibt dir eine Launch-Funktion zurück.
 */
@Composable
fun rememberMediaPicker(
    type: MediaType,
    onPicked: (Uri?) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = onPicked
    )
    val request = remember(type) {
        when (type) {
            MediaType.Image -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            MediaType.Video -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        }
    }
    return { launcher.launch(request) }
}
