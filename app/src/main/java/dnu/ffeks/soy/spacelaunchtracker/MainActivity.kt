package dnu.ffeks.soy.spacelaunchtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dnu.ffeks.soy.spacelaunchtracker.data.settings.SettingsRepository
import dnu.ffeks.soy.spacelaunchtracker.ui.navigation.AppNavigation
import dnu.ffeks.soy.spacelaunchtracker.ui.theme.SpaceLaunchTrackerTheme
import dnu.ffeks.soy.spacelaunchtracker.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsRepository = SettingsRepository(applicationContext)
        val settingsViewModel = SettingsViewModel(settingsRepository)

        setContent {
            val isDarkMode by settingsRepository.isDarkMode.collectAsState(initial = false)
            val useDynamicColors by settingsRepository.useDynamicColors.collectAsState(initial = true)

            SpaceLaunchTrackerTheme(
                darkTheme = isDarkMode,
                dynamicColor = useDynamicColors
            ) {
                AppNavigation(settingsViewModel = settingsViewModel)
            }
        }
    }
}