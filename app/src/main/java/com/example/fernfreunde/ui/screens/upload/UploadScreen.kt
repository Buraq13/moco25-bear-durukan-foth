package com.example.fernfreunde.ui.screens.upload

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.FlashAuto
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fernfreunde.ui.components.navigation.BottomBar
import com.example.fernfreunde.ui.components.navigation.NavItem
import com.example.fernfreunde.ui.components.navigation.TopBar
import com.example.fernfreunde.ui.navigation.Routes
import com.example.fernfreunde.ui.theme.FernfreundeTheme

private enum class FlashMode { Auto, On, Off }

@Composable
fun UploadScreen(
    // Nav-Callbacks (wie gehabt)
    onFriendsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},

    // Kamera-/Medien-Callbacks (werden später verdrahtet)
    onShutter: () -> Unit = {},
    onSwitchCamera: () -> Unit = {},
    onOpenGallery: () -> Unit = {},
    onFlashModeChange: (String) -> Unit = {} // "auto" | "on" | "off"
) {
    var flash by remember { mutableStateOf(FlashMode.Auto) }

    fun cycleFlash() {
        flash = when (flash) {
            FlashMode.Auto -> FlashMode.Off
            FlashMode.Off  -> FlashMode.On
            FlashMode.On   -> FlashMode.Auto
        }
        onFlashModeChange(
            when (flash) {
                FlashMode.Auto -> "auto"
                FlashMode.On   -> "on"
                FlashMode.Off  -> "off"
            }
        )
    }

    Scaffold(
        topBar = { TopBar(title = "Upload") },
        bottomBar = {
            BottomBar(currentRoute = Routes.UPLOAD) { item ->
                when (item) {
                    NavItem.Friends -> onFriendsClick()
                    NavItem.Upload  -> onUploadClick()
                    NavItem.Profile -> onProfileClick()
                }
            }
        }
    ) { innerPadding ->
        // Schwarzer „Preview“-Bereich (hier kommt später CameraX/PreviewView rein)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            // Flash (oben links)
            IconButton(
                onClick = { cycleFlash() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                val icon = when (flash) {
                    FlashMode.Auto -> Icons.Outlined.FlashAuto
                    FlashMode.On   -> Icons.Outlined.FlashOn
                    FlashMode.Off  -> Icons.Outlined.FlashOff
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Flash mode",
                    tint = Color.White
                )
            }

            // Kamera wechseln (oben rechts)
            IconButton(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cached, // „flip“-Icon
                    contentDescription = "Switch camera",
                    tint = Color.White
                )
            }

            // Galerie öffnen (unten links)
            IconButton(
                onClick = onOpenGallery,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = "Open gallery",
                    tint = Color.White
                )
            }

            // Shutter (unten Mitte)
            ShutterButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp),
                onClick = onShutter
            )
        }
    }
}

@Composable
private fun ShutterButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // Äußerer Ring
    Surface(
        modifier = modifier
            .size(76.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(width = 4.dp, color = Color.White)
    ) {
        // Zentrierter innerer Kreis
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = Color.White
            ) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UploadPreview() {
    FernfreundeTheme {
        UploadScreen()
    }
}

@Composable
fun UploadScreenRoute(
    onFriendsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val pickImage = com.example.fernfreunde.feature.media.rememberMediaPicker(
        type = com.example.fernfreunde.feature.media.MediaType.Image
    ) { uri ->
        // TODO: VM: onImageChosen(uri)
    }

    val cameraPerm = com.example.fernfreunde.feature.permissions.PermissionRequester(
        com.example.fernfreunde.feature.permissions.Permission.CAMERA
    )

    val takePhoto = com.example.fernfreunde.feature.media.rememberTakePhoto { uri ->
        // TODO: VM: onPhotoCaptured(uri)
    }

    UploadScreen(
        onFriendsClick = onFriendsClick,
        onUploadClick = onUploadClick,
        onProfileClick = onProfileClick,
        onOpenGallery = { pickImage() },
        onShutter = { if (cameraPerm.granted) takePhoto() else cameraPerm.request() },
        onSwitchCamera = { /* später mit CameraX */ },
        onFlashModeChange = { /* später mit CameraX */ }
    )
}
