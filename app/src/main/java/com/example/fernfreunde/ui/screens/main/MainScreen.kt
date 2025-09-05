package com.example.fernfreunde.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fernfreunde.data.local.entities.DailyChallenge
import com.example.fernfreunde.data.viewmodels.MainScreenViewModel
import com.example.fernfreunde.ui.components.feed.PostCard
import com.example.fernfreunde.ui.components.feed.PostCardPlaceholder
import com.example.fernfreunde.ui.components.feed.PostDisplay
import com.example.fernfreunde.ui.components.navigation.BottomBar
import com.example.fernfreunde.ui.components.navigation.NavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    // missionText: String = "Today's Mission: Share a photo of your workspace.",
    currentRoute: String? = null,
    onFriendsClick: () -> Unit = {},
    onUploadClick:  () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMissionClick: () -> Unit = {},
    viewModel: MainScreenViewModel = hiltViewModel()
) {
     LaunchedEffect(Unit) {
         viewModel.startObservingFeedForCurrentUser()
     }

    val feed by viewModel.feed.collectAsState()
    val currentMission by viewModel.currentMission.collectAsState()

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
    ) { inner ->
        FeedContent(
            mission = currentMission,
            feed = feed,
            modifier = Modifier.fillMaxSize().padding(inner),
            onMissionClick = onMissionClick
        )
    }
}

@Composable
private fun FeedContent(
    mission: DailyChallenge?,
    feed: List<PostDisplay>,
    modifier: Modifier = Modifier,
    onMissionClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { MissionBanner(mission = mission, onClick = onMissionClick) }

        if (feed.isEmpty()) {
            // Placeholder, wenn Feed Empty ist
            items(count = 6, key = { it }, contentType = { "post" }) { PostCardPlaceholder() }
        } else {
            // echte Posts anzeigen
            items(items = feed, key = {it.postId}) { post ->
                PostCard(
                    post = post
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun MissionBanner(mission: DailyChallenge?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = "Today's Mission", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))

            if (mission != null) {
                Text(
                    text = mission.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "Currently no mission available",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onClick) { Text("View mission") }   // <--------
        }
    }
}
