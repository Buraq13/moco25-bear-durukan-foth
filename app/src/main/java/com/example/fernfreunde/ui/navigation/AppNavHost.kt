package com.example.fernfreunde.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fernfreunde.feature.media.MediaType
import com.example.fernfreunde.feature.media.rememberMediaPicker
import com.example.fernfreunde.feature.media.rememberTakePhoto
import com.example.fernfreunde.feature.permissions.Permission
import com.example.fernfreunde.feature.permissions.PermissionRequester
import com.example.fernfreunde.ui.screens.friends.FriendsListScreen
import com.example.fernfreunde.ui.screens.main.MainScreen
import com.example.fernfreunde.ui.screens.mission.MissionDetailsScreen
import com.example.fernfreunde.ui.screens.profile.ProfileScreen
import com.example.fernfreunde.ui.screens.settings.SettingsScreen
import com.example.fernfreunde.ui.screens.upload.UploadScreen
import com.example.fernfreunde.ui.theme.FernfreundeTheme

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
            val inPreview = LocalInspectionMode.current

            // Gallery
            val pickImage =
                if (inPreview) ({})
                else rememberMediaPicker(MediaType.Image) { /* TODO: viewModel.onImageChosen(it) */ }

            // Take photo via system intent
            val takePhoto =
                if (inPreview) ({})
                else rememberTakePhoto { /* TODO: viewModel.onPhotoCaptured(it) */ }

            // Runtime permission (nur außerhalb der Preview initialisieren)
            val cameraPerm = if (inPreview) null else PermissionRequester(Permission.CAMERA)

            UploadScreen(
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.MAIN) },   // temporär: zurück zu MAIN
                onProfileClick = { nav.go(Routes.PROFILE) },

                onOpenGallery  = pickImage,
                onShutter      = {
                    if (inPreview) {
                        takePhoto()
                    } else {
                        if (cameraPerm?.granted == true) takePhoto() else cameraPerm?.request()
                    }
                },
                onSwitchCamera = { /* später CameraX */ },
                onFlashModeChange = { /* später CameraX */ }
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
                onBack = { nav.popBackStack() }
            )
        }
    }
}

private fun NavHostController.go(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AppNavHostPreview() {
    FernfreundeTheme { AppNavHost() }
}
