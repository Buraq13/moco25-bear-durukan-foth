package com.example.fernfreunde.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Screens importieren – Pfade bitte exakt so lassen, sonst anpassen falls deine package-Namen abweichen
import com.example.fernfreunde.ui.screens.main.MainScreen
import com.example.fernfreunde.ui.screens.friends.FriendsListScreen
import com.example.fernfreunde.ui.screens.upload.UploadScreen
import com.example.fernfreunde.ui.screens.profile.ProfileScreen
import com.example.fernfreunde.ui.screens.settings.SettingsScreen
import com.example.fernfreunde.ui.screens.mission.MissionDetailsScreen
import androidx.compose.ui.tooling.preview.Preview
import com.example.fernfreunde.ui.theme.FernfreundeTheme


@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    // Helfer für BottomBar-Navigation (singleTop + State)
    fun go(route: String) {
        nav.navigate(route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = nav,
        startDestination = Routes.MAIN
    ) {
        composable(Routes.MAIN) {
            MainScreen(
                onFriendsClick = { go(Routes.FRIENDS) },
                onUploadClick  = { go(Routes.UPLOAD) },
                onProfileClick = { go(Routes.PROFILE) }
            )
        }
        composable(Routes.FRIENDS) { FriendsListScreen() }
        composable(Routes.UPLOAD)  { UploadScreen() }
        composable(Routes.PROFILE) { ProfileScreen() }
        composable(Routes.SETTINGS){ SettingsScreen() }
        composable(Routes.MISSION) { MissionDetailsScreen() }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppNavHostPreview() {
    FernfreundeTheme {
        AppNavHost()
    }
}
