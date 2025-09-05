package com.example.fernfreunde.ui.components.feed

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PostCardPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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

            // Bild-Platzhalter (ein Bild)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                tonalElevation = 1.dp
            ) {}

            Spacer(Modifier.height(12.dp))
            Surface(modifier = Modifier.fillMaxWidth().height(10.dp)) {}
        }
    }
}