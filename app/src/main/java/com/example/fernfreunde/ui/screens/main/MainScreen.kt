package com.example.fernfreunde.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fernfreunde.ui.components.feed.PostCardPlaceholder
import com.example.fernfreunde.ui.components.navigation.BottomBar
import com.example.fernfreunde.ui.components.navigation.NavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    missionText: String = "Today's Mission: Share a photo of your workspace.",
    currentRoute: String? = null,
    onFriendsClick: () -> Unit = {},
    onUploadClick:  () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMissionClick: () -> Unit = {},       // <— neu
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("MissionMate") }) },
        bottomBar = {
            BottomBar(currentRoute = currentRoute) { item ->
                when (item) {
                    NavItem.Friends -> onFriendsClick()
                    NavItem.Upload  -> onUploadClick()
                    NavItem.Profile -> onProfileClick()
                }
            }
        }
    ) { innerPadding ->
        FeedContent(
            missionText = missionText,
            onMissionClick = onMissionClick,                 // <— weiterreichen
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun FeedContent(
    missionText: String,
    onMissionClick: () -> Unit,                              // <— neu
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { MissionBanner(missionText = missionText, onClick = onMissionClick) } // <—
        items(count = 6, key = { it }, contentType = { "post" }) { PostCardPlaceholder() }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun MissionBanner(missionText: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = "Today's Mission", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = missionText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onClick) { Text("View mission") }   // <—
        }
    }
}
