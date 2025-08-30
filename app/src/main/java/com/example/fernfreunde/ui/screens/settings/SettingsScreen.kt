package com.example.fernfreunde.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fernfreunde.ui.components.navigation.BottomBar
import com.example.fernfreunde.ui.components.navigation.NavItem
import com.example.fernfreunde.ui.components.navigation.TopBar
import com.example.fernfreunde.ui.components.settings.SettingNavItem
import com.example.fernfreunde.ui.components.settings.SettingToggleItem

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLegalClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    var notifications by remember { mutableStateOf(true) }
    var friendRequests by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { TopBar("Settings") },
        bottomBar = {
            BottomBar(current = NavItem.Upload) { /* TODO: später Navigation */ }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                SettingToggleItem(
                    label = "Push notifications",
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )
            }
            item {
                SettingToggleItem(
                    label = "Friend request alerts",
                    checked = friendRequests,
                    onCheckedChange = { friendRequests = it }
                )
            }
            item { SettingNavItem(label = "Legal", onClick = onLegalClick) }
            item { SettingNavItem(label = "About", onClick = onAboutClick) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme { SettingsScreen() }
}
