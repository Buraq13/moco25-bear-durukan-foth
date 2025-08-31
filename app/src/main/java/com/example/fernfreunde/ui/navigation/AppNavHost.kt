package com.example.fernfreunde.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fernfreunde.ui.screens.main.MainScreen
import com.example.fernfreunde.ui.screens.friends.FriendsListScreen
import com.example.fernfreunde.ui.screens.upload.UploadScreen
import com.example.fernfreunde.ui.screens.profile.ProfileScreen
import com.example.fernfreunde.ui.screens.settings.SettingsScreen
import com.example.fernfreunde.ui.screens.mission.MissionDetailsScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val backstackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backstackEntry?.destination?.route

    NavHost(
        navController = nav,
        startDestination = Routes.MAIN
    ) {
        composable(Routes.MAIN) {
            MainScreen(
                currentRoute = currentRoute,
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.UPLOAD)  },
                onProfileClick = { nav.go(Routes.PROFILE) },
                onMissionClick = { nav.go(Routes.MISSION) }    // <— neu
            )
        }
        composable(Routes.FRIENDS) { FriendsListScreen() }
        composable(Routes.UPLOAD)  { UploadScreen() }
        composable(Routes.PROFILE) { ProfileScreen() }
        composable(Routes.SETTINGS){ SettingsScreen() }
        composable(Routes.MISSION) { MissionDetailsScreen() }
    }
}

/** Smartes, wiederverwendbares Navigieren (Tabs verhalten sich wie erwartet) */
private fun NavHostController.go(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
