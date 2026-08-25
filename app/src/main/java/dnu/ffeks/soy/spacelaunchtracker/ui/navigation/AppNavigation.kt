package dnu.ffeks.soy.spacelaunchtracker.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import dnu.ffeks.soy.spacelaunchtracker.R
import dnu.ffeks.soy.spacelaunchtracker.ui.screens.LaunchDetailsScreen
import dnu.ffeks.soy.spacelaunchtracker.ui.screens.LaunchListScreen
import dnu.ffeks.soy.spacelaunchtracker.ui.screens.SettingsScreen
import dnu.ffeks.soy.spacelaunchtracker.ui.screens.home.HomeScreen
import dnu.ffeks.soy.spacelaunchtracker.ui.viewmodel.SettingsViewModel
import dnu.ffeks.soy.spacelaunchtracker.ui.viewmodel.SpaceViewModel
import kotlinx.coroutines.launch
import androidx.navigation.compose.currentBackStackEntryAsState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val viewModel: SpaceViewModel = viewModel()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(id = R.string.app_name),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.nav_home)) },
                    selected = false,
                    onClick = {
                        viewModel.resetFilters()
                        navController.navigate(Screen.Home.route)
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.nav_upcoming)) },
                    selected = false,
                    onClick = {
                        viewModel.resetFilters()
                        navController.navigate(Screen.Upcoming.route)
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.nav_past)) },
                    selected = false,
                    onClick = {
                        viewModel.resetFilters()
                        navController.navigate(Screen.Past.route)
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.nav_followed)) },
                    selected = false,
                    onClick = {
                        viewModel.resetFilters()
                        navController.navigate(Screen.Followed.route)
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.nav_settings)) },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Settings.route)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute != Screen.LaunchDetails.route) {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.app_name)) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Text("☰", modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->

            val upcoming by viewModel.upcomingLaunches.collectAsState()
            val past by viewModel.pastLaunches.collectAsState()
            val followedIds by viewModel.followedIds.collectAsState()

            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
            ) {
                composable(Screen.Home.route) {
                    val top5Followed = upcoming
                        .filter { followedIds.contains(it.id) }
                        .take(5)

                    Box(modifier = Modifier.padding(paddingValues)) {
                        HomeScreen(
                            nextLaunch = upcoming.firstOrNull(),
                            viewModel = viewModel,
                            followedLaunches = top5Followed,
                            onNavigateToDetails = { launchId ->
                                navController.navigate(Screen.LaunchDetails.createRoute(launchId))
                            }
                        )
                    }
                }

                composable(Screen.Upcoming.route) {
                    Box(modifier = Modifier.padding(paddingValues)) {
                        LaunchListScreen(
                            viewModel = viewModel,
                            launches = upcoming,
                            followedIds = followedIds,
                            onToggleFollow = { viewModel.toggleFollow(it) },
                            onNavigateToDetails = { navController.navigate(Screen.LaunchDetails.createRoute(it)) }
                        )
                    }
                }

                composable(Screen.Past.route) {
                    Box(modifier = Modifier.padding(paddingValues)) {
                        LaunchListScreen(
                            viewModel = viewModel,
                            launches = past,
                            followedIds = followedIds,
                            onToggleFollow = { viewModel.toggleFollow(it) },
                            onNavigateToDetails = { navController.navigate(Screen.LaunchDetails.createRoute(it)) }
                        )
                    }
                }

                composable(Screen.Followed.route) {
                    val allFollowed = (upcoming + past).filter { followedIds.contains(it.id) }
                    Box(modifier = Modifier.padding(paddingValues)) {
                        LaunchListScreen(
                            viewModel = viewModel,
                            launches = allFollowed,
                            followedIds = followedIds,
                            onToggleFollow = { viewModel.toggleFollow(it) },
                            onNavigateToDetails = { navController.navigate(Screen.LaunchDetails.createRoute(it)) }
                        )
                    }
                }

                composable(Screen.Settings.route) {
                    Box(modifier = Modifier.padding(paddingValues)) {
                        SettingsScreen(viewModel = settingsViewModel)
                    }
                }

                composable(Screen.LaunchDetails.route) { backStackEntry ->
                    val launchId = backStackEntry.arguments?.getString("launchId") ?: return@composable

                    LaunchedEffect(launchId) {
                        viewModel.loadLaunchDetails(launchId)
                    }

                    val details by viewModel.launchDetails.collectAsState()
                    val isFollowed = followedIds.contains(launchId)

                    LaunchDetailsScreen(
                        launch = details,
                        isFollowed = isFollowed,
                        onToggleFollow = { viewModel.toggleFollow(launchId) },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}