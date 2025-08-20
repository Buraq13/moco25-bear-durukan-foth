package com.example.fernfreunde.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class NavItem { Friends, Upload, Profile }

@Composable
fun BottomBar(current: NavItem, onSelect: (NavItem) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = current == NavItem.Friends,
            onClick = { onSelect(NavItem.Friends) },
            icon = { Icon(Icons.Outlined.Group, contentDescription = "Friends") },
            label = { Text("Friends") }
        )
        NavigationBarItem(
            selected = current == NavItem.Upload,
            onClick = { onSelect(NavItem.Upload) },
            icon = { Icon(Icons.Outlined.AddCircle, contentDescription = "Upload") },
            label = { Text("Upload") }
        )
        NavigationBarItem(
            selected = current == NavItem.Profile,
            onClick = { onSelect(NavItem.Profile) },
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
