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
import com.example.fernfreunde.ui.components.feed.MissionBanner
import com.example.fernfreunde.ui.components.feed.PostCardPlaceholder
import com.example.fernfreunde.ui.components.navigation.TopBar

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    missionText: String = "Today's Mission: Share a photo of your workspace.",

) {
    Scaffold(
        topBar = {
            TopBar("MissionMate")
        },
        bottomBar = {
            BottomBar(current = NavItem.Upload) { /* TODO: später Navigation */ }
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

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MaterialTheme { MainScreen() }
}
