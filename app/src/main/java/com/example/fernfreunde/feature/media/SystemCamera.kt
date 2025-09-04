package com.example.fernfreunde.feature.media

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberTakePhoto(
    onResult: (Uri?) -> Unit
): () -> Unit {
    val context = LocalContext.current
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { ok -> onResult(if (ok) pendingUri else null) }
    )

    return {
        val uri = createTempImageUri(context)
        pendingUri = uri
        launcher.launch(uri)
    }
}

private fun createTempImageUri(context: Context): Uri {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= 29) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Fernfreunde")
        }
    }
    return requireNotNull(
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    ) { "MediaStore URI konnte nicht erstellt werden" }
}
