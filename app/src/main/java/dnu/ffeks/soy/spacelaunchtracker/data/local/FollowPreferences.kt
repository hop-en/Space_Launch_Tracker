package dnu.ffeks.soy.spacelaunchtracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "launch_preferences")

class FollowPreferences(private val context: Context) {
    private val FOLLOWED_IDS_KEY = stringSetPreferencesKey("followed_launch_ids")

    val followedIds: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[FOLLOWED_IDS_KEY] ?: emptySet()
    }

    suspend fun toggleFollow(launchId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[FOLLOWED_IDS_KEY] ?: emptySet()
            if (current.contains(launchId)) {
                prefs[FOLLOWED_IDS_KEY] = current - launchId
            } else {
                prefs[FOLLOWED_IDS_KEY] = current + launchId
            }
        }
    }
}