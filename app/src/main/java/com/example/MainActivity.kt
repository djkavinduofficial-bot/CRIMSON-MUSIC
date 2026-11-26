package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ActiveSheet
import com.example.ui.CrimsonScreen
import com.example.ui.CrimsonViewModel
import com.example.ui.screens.LibrarySheet
import com.example.ui.screens.LockModeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SettingsSheet
import com.example.ui.theme.CrimsonMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            CrimsonMusicTheme {
                CrimsonMusicApp()
            }
        }
    }
}

@Composable
fun CrimsonMusicApp(
    viewModel: CrimsonViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var askedNotificationPermission by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Foreground playback still functions regardless of notification permission */ }
    )

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.importAudioFiles(uris)
            }
        }
    )

    val requestNotificationPermissionIfNeeded = {
        if (!askedNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                askedNotificationPermission = true
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val openAudioPicker = {
        audioPickerLauncher.launch(arrayOf("audio/*"))
    }

    val handlePlayPause = {
        requestNotificationPermissionIfNeeded()
        val toggled = viewModel.onPlayPauseTapped()
        if (!toggled) {
            openAudioPicker()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = uiState.screen,
            animationSpec = tween(durationMillis = 260),
            label = "crimson_screen_transition"
        ) { screen ->
            when (screen) {
                CrimsonScreen.ONBOARDING -> {
                    OnboardingScreen(
                        onExploreClick = viewModel::completeOnboarding
                    )
                }

                CrimsonScreen.PLAYER -> {
                    PlayerScreen(
                        playback = uiState.playback,
                        onOpenLibrary = viewModel::openLibrary,
                        onOpenLockMode = viewModel::openLockMode,
                        onOpenSettings = viewModel::openSettings,
                        onAddMusic = openAudioPicker,
                        onPlayPause = handlePlayPause,
                        onPrevious = viewModel::previousTrack,
                        onNext = viewModel::nextTrack,
                        onSeek = viewModel::seekToFraction,
                        onToggleShuffle = viewModel::toggleShuffle,
                        onToggleRepeat = viewModel::toggleRepeatOne,
                        onToggleFavorite = viewModel::toggleCurrentFavorite,
                        onCycleVisualizer = viewModel::cycleVisualizerMode
                    )
                }

                CrimsonScreen.LOCK_MODE -> {
                    LockModeScreen(
                        playback = uiState.playback,
                        onUnlock = viewModel::exitLockMode,
                        onPlayPause = handlePlayPause,
                        onPrevious = viewModel::previousTrack,
                        onNext = viewModel::nextTrack,
                        onSeek = viewModel::seekToFraction
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.activeSheet == ActiveSheet.LIBRARY,
            enter = fadeIn(tween(200)) + slideInVertically(tween(240)) { it / 10 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 10 }
        ) {
            LibrarySheet(
                tracks = uiState.filteredTracks,
                totalTrackCount = uiState.playback.tracks.size,
                currentIndex = uiState.playback.currentIndex,
                searchQuery = uiState.searchQuery,
                favoritesOnly = uiState.favoritesOnly,
                onClose = viewModel::closeSheet,
                onAddFiles = openAudioPicker,
                onSearchChange = viewModel::updateSearchQuery,
                onSelectFavoritesOnly = viewModel::setFavoritesOnly,
                onSelectTrack = { index ->
                    requestNotificationPermissionIfNeeded()
                    viewModel.playTrackAt(index)
                },
                onRemoveTrack = viewModel::removeTrack
            )
        }

        AnimatedVisibility(
            visible = uiState.activeSheet == ActiveSheet.SETTINGS,
            enter = fadeIn(tween(200)) + slideInVertically(tween(240)) { it / 10 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 10 }
        ) {
            SettingsSheet(
                volume = uiState.playback.volume,
                visualizerMode = uiState.playback.visualizerMode,
                onClose = viewModel::closeSheet,
                onVolumeChange = viewModel::setVolume,
                onVisualizerModeSelected = viewModel::setVisualizerMode
            )
        }
    }
}
