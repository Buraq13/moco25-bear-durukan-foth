package com.example.fernfreunde.FriendsList

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(
    onFriendsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Friends") }) },
        bottomBar = {
            FriendsBottomNavBar(
                onFriendsClick = onFriendsClick,
                onUploadClick = onUploadClick,
                onProfileClick = onProfileClick
            )
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
                handle = "@user${index + 1}",
                avatarShape = CircleShape
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun FriendItem(
    name: String,
    handle: String,
    avatarShape: Shape,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Surface(
                modifier = Modifier.size(40.dp),
                shape = avatarShape,
                tonalElevation = 2.dp
            ) {}

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Text(text = handle, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedButton(onClick = { /* TODO: action later */ }) {
                Text("Add")
            }
        }
    }
}

@Composable
private fun FriendsBottomNavBar(
    onFriendsClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = true, // Friends ist hier aktiv
            onClick = onFriendsClick,
            icon = { Icon(Icons.Outlined.Group, contentDescription = "Friends") },
            label = { Text("Friends") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onUploadClick,
            icon = { Icon(Icons.Outlined.AddCircle, contentDescription = "Upload") },
            label = { Text("Upload") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendsListPreview() {
    MaterialTheme { FriendsListScreen() }
}
