package com.example.fernfreunde.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fernfreunde.ui.components.BottomBar
import com.example.fernfreunde.ui.components.NavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    missionText: String = "Today's Mission: Share a photo of your workspace.",

) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("MissionMate") })
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

@Composable
private fun MissionBanner(missionText: String) {
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
            // Header placeholder (avatar + two lines)
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

            // Image block placeholder (MVP: ein Bild statt zwei)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp), // oder: .aspectRatio(1f) für quadratisch
                tonalElevation = 1.dp
            ) {}

            Spacer(Modifier.height(12.dp))
            Surface(modifier = Modifier.fillMaxWidth().height(10.dp)) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MaterialTheme { MainScreen() }
}
