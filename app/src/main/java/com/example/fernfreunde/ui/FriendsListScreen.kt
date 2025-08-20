package com.example.fernfreunde.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.example.fernfreunde.ui.components.BottomBar
import com.example.fernfreunde.ui.components.NavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(

) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Friends") }) },
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

@Preview(showBackground = true)
@Composable
private fun FriendsListPreview() {
    MaterialTheme { FriendsListScreen() }
}
