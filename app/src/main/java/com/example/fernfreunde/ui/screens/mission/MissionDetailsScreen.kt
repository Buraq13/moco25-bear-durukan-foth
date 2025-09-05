package com.example.fernfreunde.ui.screens.mission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fernfreunde.ui.components.navigation.TopBar
import com.example.fernfreunde.ui.theme.FernfreundeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionDetailsScreen(
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = { TopBar(title = "Mission", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Mission details placeholder", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Text("Explain the mission, rules, time left, etc.")
        }
    }
}

@Preview
@Composable
private fun MissionDetailsPreview() {
    FernfreundeTheme { MissionDetailsScreen() }
}
