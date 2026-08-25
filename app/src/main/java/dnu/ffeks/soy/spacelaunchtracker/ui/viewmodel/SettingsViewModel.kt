package dnu.ffeks.soy.spacelaunchtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dnu.ffeks.soy.spacelaunchtracker.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val isDarkMode = repository.isDarkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val useDynamicColors = repository.useDynamicColors.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val is24hReminderEnabled = repository.is24hReminderEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val is1hReminderEnabled = repository.is1hReminderEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun toggleDarkMode(enabled: Boolean) { viewModelScope.launch { repository.setDarkMode(enabled) } }
    fun toggleDynamicColors(enabled: Boolean) { viewModelScope.launch { repository.setDynamicColors(enabled) } }

    fun toggle24hReminder(enabled: Boolean) { viewModelScope.launch { repository.set24hReminderEnabled(enabled) } }
    fun toggle1hReminder(enabled: Boolean) { viewModelScope.launch { repository.set1hReminderEnabled(enabled) } }
}