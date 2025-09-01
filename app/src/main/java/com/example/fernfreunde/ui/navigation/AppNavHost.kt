package com.example.fernfreunde.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fernfreunde.ui.screens.friends.FriendsListScreen
import com.example.fernfreunde.ui.screens.main.MainScreen
import com.example.fernfreunde.ui.screens.mission.MissionDetailsScreen
import com.example.fernfreunde.ui.screens.profile.ProfileScreen
import com.example.fernfreunde.ui.screens.settings.SettingsScreen
import com.example.fernfreunde.ui.screens.upload.UploadScreen
import com.example.fernfreunde.ui.theme.FernfreundeTheme

/**
 * Zentrales NavHost der App.
 * Alle BottomBar-Buttons sind hier mit nav.go(...) verdrahtet.
 */
@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.MAIN
    ) {
        composable(Routes.MAIN) {
            MainScreen(
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.UPLOAD)  },
                onProfileClick = { nav.go(Routes.PROFILE) },
                onMissionClick = { nav.go(Routes.MISSION) }
            )
        }

        composable(Routes.FRIENDS) {
            FriendsListScreen(
                onFriendsClick = { nav.go(Routes.MAIN) },
                onUploadClick  = { nav.go(Routes.UPLOAD)  },
                onProfileClick = { nav.go(Routes.PROFILE) }
            )
        }

        composable(Routes.UPLOAD) {
            UploadScreen(
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.MAIN)  },
                onProfileClick = { nav.go(Routes.PROFILE) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.UPLOAD)  },
                onProfileClick = { nav.go(Routes.MAIN) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.UPLOAD)  },
                onProfileClick = { nav.go(Routes.PROFILE) }
            )
        }

        composable(Routes.MISSION) {
            MissionDetailsScreen(
                onBack = { nav.popBackStack() } // Back-Arrow geht zurück
            )
        }
    }
}

/**
 * Kleine Helfer-Extension:
 * - popUpTo(StartDestination) mit saveState: Zurückspring-Anker ist immer MAIN
 * - launchSingleTop: keine doppelten Einträge
 * - restoreState: Zustand eines Tabs bleibt erhalten
 */
private fun NavHostController.go(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Preview mit echtem NavHost – in Android Studio auf „Interactive“ stellen und rumklicken. */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AppNavHostPreview() {
    FernfreundeTheme {
        AppNavHost()
    }
}
