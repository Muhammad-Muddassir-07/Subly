package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.SublyViewModel
import com.example.ui.screens.ChannelScreen
import com.example.ui.screens.ConnectScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.theme.SublyRedPrimary
import kotlinx.coroutines.flow.collectLatest

enum class ScreenDestination {
    SPLASH,
    CONNECT,
    HOME,
    SUBSCRIPTIONS,
    SEARCH,
    LIBRARY,
    PROFILE,
    PLAYER,
    CHANNEL
}

data class NavigationTabItem(
    val destination: ScreenDestination,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun SublyAppShell(viewModel: SublyViewModel) {
    val preferences by viewModel.preferences.collectAsState()
    var currentScreen by remember { mutableStateOf(ScreenDestination.SPLASH) }
    var previousScreen by remember { mutableStateOf(ScreenDestination.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen to user messages from ViewModel
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val bottomNavItems = listOf(
        NavigationTabItem(
            destination = ScreenDestination.HOME,
            titleRes = R.string.nav_home,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            testTag = "nav_home"
        ),
        NavigationTabItem(
            destination = ScreenDestination.SUBSCRIPTIONS,
            titleRes = R.string.nav_subscriptions,
            selectedIcon = Icons.Filled.Subscriptions,
            unselectedIcon = Icons.Outlined.Subscriptions,
            testTag = "nav_subscriptions"
        ),
        NavigationTabItem(
            destination = ScreenDestination.SEARCH,
            titleRes = R.string.nav_search,
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
            testTag = "nav_search"
        ),
        NavigationTabItem(
            destination = ScreenDestination.LIBRARY,
            titleRes = R.string.nav_library,
            selectedIcon = Icons.Filled.VideoLibrary,
            unselectedIcon = Icons.Outlined.VideoLibrary,
            testTag = "nav_library"
        )
    )

    val showBottomNav = currentScreen in listOf(
        ScreenDestination.HOME,
        ScreenDestination.SUBSCRIPTIONS,
        ScreenDestination.SEARCH,
        ScreenDestination.LIBRARY
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentScreen == item.destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                previousScreen = currentScreen
                                currentScreen = item.destination
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.titleRes)
                                )
                            },
                            label = { Text(stringResource(item.titleRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SublyRedPrimary,
                                selectedTextColor = SublyRedPrimary,
                                indicatorColor = SublyRedPrimary.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentScreen) {
            ScreenDestination.SPLASH -> SplashScreen(
                onSplashFinished = {
                    if (viewModel.currentUser != null || preferences.isConnected) {
                        currentScreen = ScreenDestination.HOME
                    } else {
                        currentScreen = ScreenDestination.CONNECT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            ScreenDestination.CONNECT -> ConnectScreen(
                viewModel = viewModel,
                onConnectedSuccess = {
                    currentScreen = ScreenDestination.HOME
                },
                modifier = Modifier.fillMaxSize()
            )

            ScreenDestination.HOME -> HomeScreen(
                viewModel = viewModel,
                onVideoSelected = { video ->
                    viewModel.openVideo(video)
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.PLAYER
                },
                onChannelSelected = { channelId ->
                    viewModel.selectChannel(channelId)
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.CHANNEL
                },
                onNavigateToSearch = {
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.SEARCH
                },
                onNavigateToProfile = {
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.PROFILE
                },
                modifier = screenModifier
            )

            ScreenDestination.SUBSCRIPTIONS -> SubscriptionsScreen(
                viewModel = viewModel,
                onVideoSelected = { video ->
                    viewModel.openVideo(video)
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.PLAYER
                },
                onChannelSelected = { channelId ->
                    viewModel.selectChannel(channelId)
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.CHANNEL
                },
                modifier = screenModifier
            )

            ScreenDestination.SEARCH -> SearchScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = previousScreen },
                onVideoSelected = { video ->
                    viewModel.openVideo(video)
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.PLAYER
                },
                onChannelSelected = { channelId ->
                    viewModel.selectChannel(channelId)
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.CHANNEL
                },
                modifier = screenModifier
            )

            ScreenDestination.LIBRARY -> LibraryScreen(
                viewModel = viewModel,
                onVideoSelected = { video ->
                    viewModel.openVideo(video)
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.PLAYER
                },
                onChannelSelected = { channelId ->
                    viewModel.selectChannel(channelId)
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.CHANNEL
                },
                modifier = screenModifier
            )

            ScreenDestination.PLAYER -> PlayerScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = previousScreen },
                onChannelClick = { channelId ->
                    viewModel.selectChannel(channelId)
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.CHANNEL
                },
                modifier = screenModifier
            )

            ScreenDestination.CHANNEL -> ChannelScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = previousScreen },
                onVideoSelected = { video ->
                    viewModel.openVideo(video)
                    previousScreen = currentScreen
                    currentScreen = ScreenDestination.PLAYER
                },
                modifier = screenModifier
            )

            ScreenDestination.PROFILE -> ProfileScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = previousScreen },
                onDisconnect = { currentScreen = ScreenDestination.CONNECT },
                modifier = screenModifier
            )
        }
    }
}
