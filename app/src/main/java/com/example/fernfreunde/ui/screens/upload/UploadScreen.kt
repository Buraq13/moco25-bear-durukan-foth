package com.example.fernfreunde.ui.screens.upload

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onFriendsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF111111)),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera stub", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview
@Composable
private fun UploadPreview() {
    FernfreundeTheme { UploadScreen() }
}
