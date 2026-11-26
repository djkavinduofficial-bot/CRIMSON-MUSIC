package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.TrackEntity
import com.example.playback.CrimsonPlaybackController
import com.example.playback.PlaybackSnapshot
import com.example.playback.VisualizerMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CrimsonScreen {
    ONBOARDING,
    PLAYER,
    LOCK_MODE
}

enum class ActiveSheet {
    NONE,
    LIBRARY,
    SETTINGS
}

data class CrimsonUiState(
    val screen: CrimsonScreen = CrimsonScreen.ONBOARDING,
    val activeSheet: ActiveSheet = ActiveSheet.NONE,
    val searchQuery: String = "",
    val favoritesOnly: Boolean = false,
    val playback: PlaybackSnapshot = PlaybackSnapshot()
) {
    val filteredTracks: List<Pair<TrackEntity, Int>>
        get() {
            val q = searchQuery.trim().lowercase()
            return playback.tracks.mapIndexed { index, track -> track to index }
                .filter { (track, _) ->
                    (!favoritesOnly || track.isFavorite) &&
                        (q.isEmpty() ||
                            track.title.lowercase().contains(q) ||
                            track.artist.lowercase().contains(q))
                }
        }
}

private data class NavUiState(
    val screen: CrimsonScreen = CrimsonScreen.ONBOARDING,
    val activeSheet: ActiveSheet = ActiveSheet.NONE,
    val searchQuery: String = "",
    val favoritesOnly: Boolean = false
)

class CrimsonViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val controller = CrimsonPlaybackController.getInstance(appContext)
    private val trackRepository = controller.trackRepository
    private val prefsRepository = controller.preferencesRepository

    private val navState = MutableStateFlow(NavUiState())

    val uiState: StateFlow<CrimsonUiState> = combine(
        navState,
        controller.state
    ) { nav, playback ->
        CrimsonUiState(
            screen = nav.screen,
            activeSheet = nav.activeSheet,
            searchQuery = nav.searchQuery,
            favoritesOnly = nav.favoritesOnly,
            playback = playback
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CrimsonUiState()
    )

    init {
        viewModelScope.launch {
            val prefs = prefsRepository.preferencesFlow.first()
            if (prefs.hasCompletedOnboarding) {
                navState.update { it.copy(screen = CrimsonScreen.PLAYER) }
            }
        }
    }

    fun completeOnboarding() {
        navState.update { it.copy(screen = CrimsonScreen.PLAYER, activeSheet = ActiveSheet.NONE) }
        viewModelScope.launch { prefsRepository.setOnboardingCompleted() }
    }

    fun openLockMode() {
        navState.update { it.copy(screen = CrimsonScreen.LOCK_MODE, activeSheet = ActiveSheet.NONE) }
    }

    fun exitLockMode() {
        navState.update { it.copy(screen = CrimsonScreen.PLAYER, activeSheet = ActiveSheet.NONE) }
    }

    fun openLibrary() {
        navState.update { it.copy(activeSheet = ActiveSheet.LIBRARY) }
    }

    fun openSettings() {
        navState.update { it.copy(activeSheet = ActiveSheet.SETTINGS) }
    }

    fun closeSheet() {
        navState.update { it.copy(activeSheet = ActiveSheet.NONE) }
    }

    fun updateSearchQuery(query: String) {
        navState.update { it.copy(searchQuery = query) }
    }

    fun setFavoritesOnly(onlyFavorites: Boolean) {
        navState.update { it.copy(favoritesOnly = onlyFavorites) }
    }

    fun importAudioFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val wasEmpty = controller.state.value.tracks.isEmpty()
            val addedCount = trackRepository.importAudioUris(uris)
            if (addedCount == 0) {
                val total = controller.state.value.tracks.size
                controller.setNotice(
                    if (total == 0) appContext.getString(R.string.notice_no_supported_files)
                    else appContext.getString(R.string.notice_tracks_in_library, total)
                )
            } else {
                val total = controller.state.value.tracks.size + addedCount
                controller.setNotice(appContext.getString(R.string.notice_added_tracks, addedCount))
                if (wasEmpty) {
                    navState.update {
                        it.copy(screen = CrimsonScreen.PLAYER, activeSheet = ActiveSheet.NONE)
                    }
                    // Wait briefly for Room flow to deliver inserted track before starting playback
                    val loadedTracks = trackRepository.allTracks.first { it.isNotEmpty() }
                    if (loadedTracks.isNotEmpty()) {
                        controller.loadTrackAt(0, autoPlay = true)
                    }
                }
            }
        }
    }

    fun playTrackAt(index: Int) {
        navState.update { it.copy(screen = CrimsonScreen.PLAYER, activeSheet = ActiveSheet.NONE) }
        controller.loadTrackAt(index, autoPlay = true)
    }

    /**
     * Returns true if playback toggled, or false if library is empty (caller should open audio picker).
     */
    fun onPlayPauseTapped(): Boolean {
        return controller.togglePlayPause()
    }

    fun nextTrack() = controller.nextTrack()

    fun previousTrack() = controller.previousTrack()

    fun seekToFraction(fraction: Float) = controller.seekToFraction(fraction)

    fun toggleShuffle() = controller.toggleShuffle()

    fun toggleRepeatOne() = controller.toggleRepeatOne()

    fun setVolume(volume: Float) = controller.setVolume(volume)

    fun setVisualizerMode(mode: VisualizerMode) = controller.setVisualizerMode(mode)

    fun cycleVisualizerMode() = controller.cycleVisualizerMode()

    fun toggleCurrentFavorite() {
        val track = controller.state.value.currentTrack
        if (track == null) {
            controller.setNotice(appContext.getString(R.string.notice_add_first))
            return
        }
        viewModelScope.launch {
            trackRepository.toggleFavorite(track)
        }
    }

    fun removeTrack(track: TrackEntity) {
        viewModelScope.launch {
            val current = controller.state.value
            val isRemovingCurrent = current.currentTrack?.uriString == track.uriString
            trackRepository.removeTrack(track.uriString)
            if (isRemovingCurrent) {
                controller.pause()
            }
        }
    }
}

fun formatDurationMs(durationMs: Long): String {
    val totalSeconds = (durationMs.coerceAtLeast(0L)) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
