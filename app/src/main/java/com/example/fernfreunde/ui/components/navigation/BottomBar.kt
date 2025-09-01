package com.example.fernfreunde.ui.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fernfreunde.ui.navigation.Routes

enum class NavItem(
    val label: String,
    val route: String,
    val icon: ImageVector = Icons.Outlined.Group // default, wird überschrieben
) {
    Friends("Friends", Routes.FRIENDS, Icons.Outlined.Group),
    Upload ("Upload" , Routes.UPLOAD , Icons.Outlined.AddCircle),
    Profile("Profile", Routes.PROFILE, Icons.Outlined.Person);
}

@Composable
fun BottomBar(
    currentRoute: String?,
    onItemSelected: (NavItem) -> Unit,
) {
    NavigationBar {
        NavItem.values().forEach { item ->
            NavigationBarItem(
                selected = item.route == currentRoute,
                onClick = { onItemSelected(item) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
