package com.example.fernfreunde.ui.screens.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fernfreunde.data.local.entities.User
import com.example.fernfreunde.data.viewmodels.FriendsListViewModel
import com.example.fernfreunde.ui.components.friends.FriendItem
import com.example.fernfreunde.ui.components.friends.FriendItemPlaceholder
import com.example.fernfreunde.ui.components.navigation.BottomBar
import com.example.fernfreunde.ui.components.navigation.NavItem
import com.example.fernfreunde.ui.components.navigation.TopBar
import com.example.fernfreunde.ui.navigation.Routes
import com.example.fernfreunde.ui.theme.FernfreundeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(
    onFriendsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: FriendsListViewModel = hiltViewModel()
) {
    val friends by viewModel.friends.collectAsState()
    val pending by viewModel.pending.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = { TopBar(title = "Friends") },
        bottomBar = {
            BottomBar(currentRoute = Routes.FRIENDS) { item ->
                when (item) {
                    NavItem.Friends -> onFriendsClick()
                    NavItem.Upload  -> onUploadClick()
                    NavItem.Profile -> onProfileClick()
                }
            }
        }
    ) { innerPadding ->
        FriendsList(
            friends = friends,
            pending = pending,
            allUsers = allUsers,
            onAccept = { fromUserId -> viewModel.acceptFriendshipRequest(fromUserId) },
            onAdd = { toUserId -> viewModel.sendFriendshipRequest(toUserId) },
            onRemove = { friendId -> viewModel.removeFriend(friendId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun FriendsList(
    friends: List<User>,
    pending: List<User>,
    allUsers: List<User>,
    onAccept: (String) -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ***** Friendship Requests *****
        if (pending.isNotEmpty()) {
            item {
                Text(
                    text = "Pending Requests",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            items(items = pending, key = { it.userId }) { user ->
                FriendItem(
                    user = user,
                    actionLabel = "Accept",
                    onActionClick = { onAccept(user.userId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ***** Friends *****
        item {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Friends",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
            )
        }
        if (friends.isEmpty()) {
            item {
                Text(text = "You have no friends yet.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(items = friends, key = { it.userId }) { user ->
                FriendItem(
                    user = user,
                    actionLabel = "Remove",
                    onActionClick = { onRemove(user.userId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ***** All Users (people you can add) *****
        item {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "People",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
            )
        }
        if (allUsers.isEmpty()) {
            item {
                Text(text = "No other users available.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(items = allUsers, key = { it.userId }) { user ->
                FriendItem(
                    user = user,
                    actionLabel = "Add",
                    onActionClick = { onAdd(user.userId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendsListPreview() {
    FernfreundeTheme {
        FriendsListScreen()
    }
}
