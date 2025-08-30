package com.example.fernfreunde.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fernfreunde.ui.components.navigation.TopBar

@Composable
fun MissionDetailsScreen(
    title: String = "Today's Mission",
    description: String = "Share a photo of your workspace.",
    onBack: () -> Unit = {},
    onStartMission: () -> Unit = {}
) {
    Scaffold(
        topBar = { TopBar(title) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Text(text = description, style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onStartMission,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Start mission") }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Back") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MissionDetailsPreview() {
    MaterialTheme { MissionDetailsScreen() }
}