package dnu.ffeks.soy.spacelaunchtracker.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Upcoming : Screen("upcoming")
    object Past : Screen("past")
    object Followed : Screen("followed")
    object Settings : Screen("settings")
    object LaunchDetails : Screen("launch_details/{launchId}") {
        fun createRoute(launchId: String) = "launch_details/$launchId"
    }
}