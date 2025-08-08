package com.example.fernfreunde.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    missionText: String = "Today's Mission: Share a photo of your workspace.",
    onFriendsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            // NOTE: BeReal-like, minimal. No settings/profile icons per your spec.
            CenterAlignedTopAppBar(
                title = { Text("MissionMate") }
            )
        },
        bottomBar = {
            BottomNavBar(
                onFriendsClick = onFriendsClick,
                onUploadClick = onUploadClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        FeedContent(
            missionText = missionText,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun FeedContent(
    missionText: String,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { MissionBanner(missionText = missionText) }
        items(
            count = 6,
            key = { it },
            contentType = { "post" }
        ) {
            PostCardPlaceholder()
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun MissionBanner(missionText: String) {
    // Simple, readable banner at top of feed; easy to restyle later
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today's Mission",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = missionText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { /* TODO: navigate to mission details / accept */ }) {
                Text("View mission")
            }
        }
    }
}

@Composable
private fun PostCardPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 2.dp
                ) {}
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Surface(modifier = Modifier.height(12.dp).fillMaxWidth(0.35f)) {}
                    Spacer(Modifier.height(6.dp))
                    Surface(modifier = Modifier.height(10.dp).fillMaxWidth(0.25f)) {}
                }
            }
            Spacer(Modifier.height(12.dp))
            // Image block placeholder (BeReal dual-cam feel simulated by two boxes)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    tonalElevation = 1.dp
                ) {}
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    tonalElevation = 1.dp
                ) {}
            }
            Spacer(Modifier.height(12.dp))
            Surface(modifier = Modifier.fillMaxWidth().height(10.dp)) {}
        }
    }
}

@Composable
private fun BottomNavBar(
    onFriendsClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = onFriendsClick,
            icon = { Icon(Icons.Outlined.Group, contentDescription = "Friends") },
            label = { Text("Friends") }
        )
        NavigationBarItem(
            selected = true, // middle action feels primary; not switching content for now
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

// --- Preview ---
@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MaterialTheme { MainScreen() }
}
