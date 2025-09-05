package com.example.fernfreunde.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fernfreunde.data.viewmodels.CreatePostViewModel
import com.example.fernfreunde.feature.media.MediaType
import com.example.fernfreunde.feature.media.rememberMediaPicker
import com.example.fernfreunde.feature.media.rememberTakePhoto
import com.example.fernfreunde.feature.permissions.Permission
import com.example.fernfreunde.feature.permissions.PermissionRequester
import com.example.fernfreunde.ui.screens.friends.FriendsListScreen
import com.example.fernfreunde.ui.screens.main.MainScreen
import com.example.fernfreunde.ui.screens.mission.MissionDetailsScreen
import com.example.fernfreunde.ui.screens.profile.ProfileScreen
import com.example.fernfreunde.ui.screens.profile.EditProfileScreen
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

            val viewModel: CreatePostViewModel = hiltViewModel()

            // Gallery
            val pickImage =
                if (inPreview) ({})
                else rememberMediaPicker(MediaType.Image) { uri ->
                    uri?.let {
                        viewModel.onImageChosen(it)
                    }
                }

            // Take photo via system intent
            val takePhoto =
                if (inPreview) ({})
                else rememberTakePhoto { uri ->
                    uri?.let {
                        viewModel.onPhotoCaptured(it)
                    }
                }

            // Runtime permission
            val cameraPerm = if (inPreview) null else PermissionRequester(Permission.CAMERA)

            val createdId = viewModel.createdPostId.collectAsState()
            val isPosting = viewModel.isPosting.collectAsState()

            UploadScreen(
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.MAIN) },
                onProfileClick = { nav.go(Routes.PROFILE) },

                onOpenGallery  = pickImage,
                onShutter      = {
                    if (inPreview) {
                        takePhoto()
                    } else {
                        if (cameraPerm?.granted == true) takePhoto() else cameraPerm?.request?.let { it1 -> it1() }
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
                onProfileClick = { nav.go(Routes.MAIN) },
                onEditProfileClick = { nav.navigate(Routes.EDIT_PROFILE) },
                onSettingsClick = { nav.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                onSaveClick = { username, bio, email, imageUri ->
                    // TODO: Daten speichern (z.B. Firebase / lokale DB)
                    nav.popBackStack() // zurück zum Profil
                },
                onCancelClick = {
                    nav.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onFriendsClick = { nav.go(Routes.FRIENDS) },
                onUploadClick  = { nav.go(Routes.UPLOAD)  },
                onProfileClick = { nav.go(Routes.PROFILE) },
                onBackClick = { nav.popBackStack() },
                onSaveClick = { push, requests ->
                    // TODO: später speichern (Firebase / DB)
                    nav.popBackStack() // nach Save zurück zum Profil
                }
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
        restoreState = true
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AppNavHostPreview() {
    FernfreundeTheme { AppNavHost() }
}

