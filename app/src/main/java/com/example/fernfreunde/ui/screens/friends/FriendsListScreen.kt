package com.example.fernfreunde.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fernfreunde.ui.components.navigation.BottomBar
import com.example.fernfreunde.ui.components.navigation.NavItem
import com.example.fernfreunde.ui.components.friends.FriendItem
import com.example.fernfreunde.ui.components.navigation.TopBar

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen() {
    Scaffold(
        topBar = { TopBar("Friends") },
        bottomBar = {
            BottomBar(current = NavItem.Upload) { /* TODO: später Navigation */ }
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(count = 12, key = { it }, contentType = { "friend" }) { index ->
            FriendItem(
                name = "Friend ${index + 1}",
                handle = "@user${index + 1}"
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}


@Preview(showBackground = true)
@Composable
private fun FriendsListPreview() {
    MaterialTheme { FriendsListScreen() }
}
