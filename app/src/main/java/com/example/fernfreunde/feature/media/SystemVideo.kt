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
fun rememberRecordVideo(
    onResult: (Uri?) -> Unit
): () -> Unit {
    val context = LocalContext.current
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { ok -> onResult(if (ok) pendingUri else null) }
    )

    return {
        val uri = createTempVideoUri(context)
        pendingUri = uri
        launcher.launch(uri)
    }
}

private fun createTempVideoUri(context: Context): Uri {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "VID_${System.currentTimeMillis()}.mp4")
        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        if (Build.VERSION.SDK_INT >= 29) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Fernfreunde")
        }
    }
    return requireNotNull(
        context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    ) { "MediaStore URI (video) konnte nicht erstellt werden" }
}
