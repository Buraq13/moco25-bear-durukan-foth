package com.example.fernfreunde.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fernfreunde.ui.components.navigation.BottomBar
import com.example.fernfreunde.ui.components.navigation.NavItem
import com.example.fernfreunde.ui.components.navigation.TopBar
import com.example.fernfreunde.ui.components.profile.ProfileHeader
import com.example.fernfreunde.ui.components.profile.ProfileMenuItem

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onFriendsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    Scaffold(
        topBar = { TopBar("Profile") },
        bottomBar = {
            BottomBar(current = NavItem.Upload) { /* TODO: später Navigation */ }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProfileHeader(
                    name = "Your Name",
                    handle = "@yourhandle"
                )
            }
            item { Spacer(Modifier.height(8.dp)) }

            item { ProfileMenuItem("Settings", onClick = onSettingsClick) }
            item { ProfileMenuItem("My Friends", onClick = onFriendsClick) }
            item { ProfileMenuItem("Legal", onClick = { /* TODO */ }) }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    MaterialTheme { ProfileScreen() }
}
