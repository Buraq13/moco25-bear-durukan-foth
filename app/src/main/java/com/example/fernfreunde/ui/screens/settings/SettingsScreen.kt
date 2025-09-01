package com.example.fernfreunde.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fernfreunde.ui.components.navigation.BottomBar
import com.example.fernfreunde.ui.components.navigation.NavItem
import com.example.fernfreunde.ui.components.navigation.TopBar
import com.example.fernfreunde.ui.navigation.Routes
import com.example.fernfreunde.ui.theme.FernfreundeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onFriendsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    var push by remember { mutableStateOf(true) }
    var requests by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { TopBar(title = "Settings") },
        bottomBar = {
            BottomBar(currentRoute = Routes.SETTINGS) { item ->
                when (item) {
                    NavItem.Friends -> onFriendsClick()
                    NavItem.Upload  -> onUploadClick()
                    NavItem.Profile -> onProfileClick()
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ListItem(
                headlineContent = { Text("Push notifications") },
                trailingContent = {
                    Switch(checked = push, onCheckedChange = { push = it })
                }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Friend request alerts") },
                trailingContent = {
                    Switch(checked = requests, onCheckedChange = { requests = it })
                }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("About") }
            )
            HorizontalDivider()
        }
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    FernfreundeTheme { SettingsScreen() }
}
