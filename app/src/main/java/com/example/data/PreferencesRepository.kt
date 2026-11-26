package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.crimsonDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "crimson_preferences"
)

data class CrimsonPreferences(
    val volume: Float = 0.85f,
    val visualizerMode: Int = 0,
    val shuffleEnabled: Boolean = false,
    val repeatOneEnabled: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val lastTrackUri: String? = null
)

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val VOLUME = floatPreferencesKey("volume")
        val VISUALIZER_MODE = intPreferencesKey("visualizer_mode")
        val SHUFFLE = booleanPreferencesKey("shuffle_enabled")
        val REPEAT_ONE = booleanPreferencesKey("repeat_one_enabled")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val LAST_TRACK_URI = stringPreferencesKey("last_track_uri")
    }

    val preferencesFlow: Flow<CrimsonPreferences> = context.crimsonDataStore.data.map { prefs ->
        CrimsonPreferences(
            volume = (prefs[Keys.VOLUME] ?: 0.85f).coerceIn(0f, 1f),
            visualizerMode = (prefs[Keys.VISUALIZER_MODE] ?: 0).coerceIn(0, 2),
            shuffleEnabled = prefs[Keys.SHUFFLE] ?: false,
            repeatOneEnabled = prefs[Keys.REPEAT_ONE] ?: false,
            hasCompletedOnboarding = prefs[Keys.ONBOARDING_DONE] ?: false,
            lastTrackUri = prefs[Keys.LAST_TRACK_URI]
        )
    }

    suspend fun setVolume(volume: Float) {
        context.crimsonDataStore.edit { it[Keys.VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setVisualizerMode(mode: Int) {
        context.crimsonDataStore.edit { it[Keys.VISUALIZER_MODE] = mode.coerceIn(0, 2) }
    }

    suspend fun setShuffle(enabled: Boolean) {
        context.crimsonDataStore.edit { it[Keys.SHUFFLE] = enabled }
    }

    suspend fun setRepeatOne(enabled: Boolean) {
        context.crimsonDataStore.edit { it[Keys.REPEAT_ONE] = enabled }
    }

    suspend fun setOnboardingCompleted() {
        context.crimsonDataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    suspend fun setLastTrackUri(uriString: String?) {
        context.crimsonDataStore.edit {
            if (uriString == null) it.remove(Keys.LAST_TRACK_URI)
            else it[Keys.LAST_TRACK_URI] = uriString
        }
    }
}
