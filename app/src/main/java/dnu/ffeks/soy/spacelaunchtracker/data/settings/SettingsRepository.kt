package dnu.ffeks.soy.spacelaunchtracker.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val DYNAMIC_COLORS_KEY = booleanPreferencesKey("dynamic_colors")

    private val REMINDER_24H_KEY = booleanPreferencesKey("reminder_24h_enabled")
    private val REMINDER_1H_KEY = booleanPreferencesKey("reminder_1h_enabled")

    val isDarkMode = context.dataStore.data.map { it[DARK_MODE_KEY] ?: false }
    val useDynamicColors = context.dataStore.data.map { it[DYNAMIC_COLORS_KEY] ?: true }

    val is24hReminderEnabled = context.dataStore.data.map { it[REMINDER_24H_KEY] ?: true }
    val is1hReminderEnabled = context.dataStore.data.map { it[REMINDER_1H_KEY] ?: true }

    suspend fun setDarkMode(enabled: Boolean) { context.dataStore.edit { it[DARK_MODE_KEY] = enabled } }
    suspend fun setDynamicColors(enabled: Boolean) { context.dataStore.edit { it[DYNAMIC_COLORS_KEY] = enabled } }

    suspend fun set24hReminderEnabled(enabled: Boolean) { context.dataStore.edit { it[REMINDER_24H_KEY] = enabled } }
    suspend fun set1hReminderEnabled(enabled: Boolean) { context.dataStore.edit { it[REMINDER_1H_KEY] = enabled } }
}