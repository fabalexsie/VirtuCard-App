package de.siebes.fabian.virtucard.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

// ~ https://github.com/android/codelab-android-datastore/blob/preferences_datastore/app/src/main/java/com/codelab/android/datastore/data/UserPreferencesRepository.kt
data class UserPreferences(
    val shareUrl: String,
)

/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private val TAG: String = "UserPreferencesRepo"

    private object PreferencesKeys {
        val SHARE_URL = stringPreferencesKey("share_url")
    }

    /**
     * Get the user preferences flow.
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapUserPreferences(preferences)
        }


    suspend fun updateShareUrl(shareUrl: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHARE_URL] = shareUrl
        }
    }

    suspend fun fetchInitialPreferences() =
        mapUserPreferences(dataStore.data.first().toPreferences())


    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        // Get our show completed value, defaulting to "" if not set:
        val shareUrl = preferences[PreferencesKeys.SHARE_URL] ?: ""
        return UserPreferences(shareUrl)
    }
}