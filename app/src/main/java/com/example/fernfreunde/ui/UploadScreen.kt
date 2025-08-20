package com.example.fernfreunde.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fernfreunde.ui.components.navigation.BottomBar
import com.example.fernfreunde.ui.components.navigation.NavItem
import com.example.fernfreunde.ui.components.navigation.TopBar

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onFriendsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    Scaffold(
        topBar = { TopBar("Upload") },
        bottomBar = {
            BottomBar(current = NavItem.Upload) { item ->
                when (item) {
                    NavItem.Friends -> onFriendsClick()
                    NavItem.Upload  -> onUploadClick()
                    NavItem.Profile -> onProfileClick()
                }
            }
        }
    ) { innerPadding ->
        CameraStub(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun CameraStub(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(Color.Black)) {
        // Flash (links oben)
        IconButton(
            onClick = { /* TODO: toggle flash */ },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Icon(Icons.Outlined.FlashOn, contentDescription = "Flash", tint = Color.White)
        }

        // Flip Camera (rechts oben)
        IconButton(
            onClick = { /* TODO: flip camera */ },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Icon(Icons.Outlined.Cached, contentDescription = "Flip camera", tint = Color.White)
        }

        // Preview-Frame (Stub)
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .aspectRatio(3f / 4f),
            color = Color(0xFF111111),
            tonalElevation = 2.dp
        ) {}

        // Shutter
        ShutterButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            onClick = { /* TODO: take photo */ }
        )
    }
}

@Composable
private fun ShutterButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // äußerer Kreis
    Surface(
        modifier = modifier
            .size(76.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(4.dp, Color.White)
    ) {
        // innerer Kreis
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
private fun UploadScreenPreview() {
    MaterialTheme { UploadScreen() }
}
