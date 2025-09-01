package com.example.fernfreunde.ui.screens.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fernfreunde.ui.components.friends.FriendItem
import com.example.fernfreunde.ui.components.navigation.BottomBar
import com.example.fernfreunde.ui.components.navigation.NavItem
import com.example.fernfreunde.ui.components.navigation.TopBar
import com.example.fernfreunde.ui.navigation.Routes
import com.example.fernfreunde.ui.theme.FernfreundeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(
    onFriendsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    Scaffold(
        topBar = { TopBar(title = "Friends") },
        bottomBar = {
            BottomBar(currentRoute = Routes.FRIENDS) { item ->
                when (item) {
                    NavItem.Friends -> onFriendsClick()
                    NavItem.Upload  -> onUploadClick()
                    NavItem.Profile -> onProfileClick()
                }
            }
        }
    ) { innerPadding ->
        FriendsList(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun FriendsList(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(count = 12, key = { it }, contentType = { "friend" }) { index ->
            FriendItem(
                name = "Friend ${index + 1}",
                handle = "@user${index + 1}",
                // kein avatarShape mehr im aktuellen FriendItem
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendsListPreview() {
    FernfreundeTheme {
        FriendsListScreen()
    }
}
