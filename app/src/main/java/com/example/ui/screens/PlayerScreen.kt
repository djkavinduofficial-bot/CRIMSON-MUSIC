package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.playback.PlaybackSnapshot
import com.example.playback.VisualizerMode
import com.example.ui.components.BackdropStyle
import com.example.ui.components.CrimsonArcSeekBar
import com.example.ui.components.CrimsonBackdrop
import com.example.ui.components.CrimsonGlassIconButton
import com.example.ui.components.CrimsonGlassPillButton
import com.example.ui.components.CrimsonHeroDisc
import com.example.ui.components.CrimsonVisualizerCanvas
import com.example.ui.theme.CrimsonActiveIcon
import com.example.ui.theme.CrimsonArtistText
import com.example.ui.theme.CrimsonChipBg
import com.example.ui.theme.CrimsonEyebrow
import com.example.ui.theme.CrimsonInactiveIcon
import com.example.ui.theme.CrimsonLineBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.CrimsonNoticeText
import com.example.ui.theme.CrimsonPlayCore
import com.example.ui.theme.CrimsonPlayEdge
import com.example.ui.theme.CrimsonPlayIcon
import com.example.ui.theme.CrimsonPlayMid
import com.example.ui.theme.CrimsonPlayOuter
import com.example.ui.theme.CrimsonRoseText
import com.example.ui.theme.CrimsonSoftPink
import com.example.ui.theme.CrimsonWhite

@Composable
fun PlayerScreen(
    playback: PlaybackSnapshot,
    onOpenLibrary: () -> Unit,
    onOpenLockMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddMusic: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCycleVisualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTrack = playback.currentTrack
    val isFavorite = currentTrack?.isFavorite == true
    val modeLabel = visualizerModeLabel(playback.visualizerMode).uppercase()

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isCompactHeight = maxHeight < 740.dp
        val discSize = if (isCompactHeight) 184.dp else 224.dp
        val vizHeight = if (isCompactHeight) 52.dp else 68.dp
        val playButtonSize = if (isCompactHeight) 78.dp else 90.dp

        CrimsonBackdrop(style = BackdropStyle.PLAYER)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CrimsonGlassIconButton(
                    onClick = onOpenLibrary,
                    modifier = Modifier.testTag("top_library_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = stringResource(R.string.cd_open_library),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.now_playing_eyebrow),
                        style = MaterialTheme.typography.labelSmall,
                        color = CrimsonEyebrow
                    )
                    Text(
                        text = stringResource(R.string.brand_wordmark),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = CrimsonWhite
                    )
                }

                CrimsonGlassIconButton(
                    onClick = onOpenLockMode,
                    modifier = Modifier.testTag("top_lock_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = stringResource(R.string.cd_open_lock_mode),
                        modifier = Modifier.size(20.dp)
                    )
                }

                CrimsonGlassIconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = stringResource(R.string.cd_open_settings),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompactHeight) 14.dp else 22.dp))

            // Hero Disc
            CrimsonHeroDisc(
                isPlaying = playback.isPlaying,
                artwork = playback.currentArtwork,
                discContainerSize = discSize
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CrimsonStatusChip(text = stringResource(R.string.chip_offline_audio))
                CrimsonStatusChip(text = modeLabel)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Track Title & Artist
            Text(
                text = currentTrack?.title ?: stringResource(R.string.empty_track_title),
                style = MaterialTheme.typography.headlineLarge,
                color = CrimsonWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_song_title")
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentTrack?.artist ?: stringResource(R.string.empty_track_artist),
                style = MaterialTheme.typography.bodyMedium,
                color = CrimsonArtistText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_song_artist")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Real-time Visualizer
            CrimsonVisualizerCanvas(
                mode = playback.visualizerMode,
                spectrumBins = playback.spectrumBins,
                waveformSamples = playback.waveformSamples,
                modifier = Modifier.height(vizHeight)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Curved Arc Seek Bar
            CrimsonArcSeekBar(
                progress = playback.progressFraction,
                positionMs = playback.positionMs,
                durationMs = playback.durationMs,
                onSeek = onSeek
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Main 5-button Transport Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = stringResource(R.string.cd_shuffle),
                        tint = if (playback.shuffle) CrimsonActiveIcon else CrimsonInactiveIcon,
                        modifier = Modifier.size(22.dp)
                    )
                }

                CrimsonGlassPillButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("previous_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = stringResource(R.string.cd_previous),
                        modifier = Modifier.size(26.dp)
                    )
                }

                CrimsonPlayButton(
                    isPlaying = playback.isPlaying,
                    size = playButtonSize,
                    onClick = onPlayPause,
                    modifier = Modifier.testTag("play_pause_button")
                )

                CrimsonGlassPillButton(
                    onClick = onNext,
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("next_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = stringResource(R.string.cd_next),
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("repeat_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RepeatOne,
                        contentDescription = stringResource(R.string.cd_repeat),
                        tint = if (playback.repeatOne) CrimsonActiveIcon else CrimsonInactiveIcon,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Extras row: Favorite, Visualizer, Lock screen
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CrimsonGlassPillButton(
                    onClick = onToggleFavorite,
                    active = isFavorite,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("favorite_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.cd_favorite),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.action_favorite),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                CrimsonGlassPillButton(
                    onClick = onCycleVisualizer,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("visualizer_cycle_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = stringResource(R.string.action_visualizer),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.action_visualizer),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                CrimsonGlassPillButton(
                    onClick = onOpenLockMode,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lock_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = stringResource(R.string.cd_open_lock_mode),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.action_lock_screen),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Action Row: Add music + Library with track count
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CrimsonGlassPillButton(
                    onClick = onAddMusic,
                    accentFill = true,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_music_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.action_add_music),
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = stringResource(R.string.action_add_music),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }

                CrimsonGlassPillButton(
                    onClick = onOpenLibrary,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottom_library_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = stringResource(R.string.action_library),
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = "${stringResource(R.string.action_library)} ${playback.tracks.size}",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = playback.noticeMessage ?: stringResource(R.string.notice_offline_ready),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = CrimsonNoticeText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_notice_text")
            )
        }
    }
}

@Composable
fun CrimsonPlayButton(
    isPlaying: Boolean,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size + 12.dp)
            .clip(CircleShape)
            .background(CrimsonPlayOuter.copy(alpha = 0.28f))
            .padding(6.dp)
            .shadow(20.dp, CircleShape, ambientColor = CrimsonNeon, spotColor = CrimsonNeon)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    0.0f to CrimsonPlayCore,
                    0.42f to CrimsonPlayMid,
                    0.72f to CrimsonPlayOuter,
                    1.0f to CrimsonPlayEdge
                )
            )
            .border(2.dp, CrimsonSoftPink.copy(alpha = 0.85f), CircleShape)
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = stringResource(if (isPlaying) R.string.cd_pause else R.string.cd_play),
            tint = CrimsonPlayIcon,
            modifier = Modifier.size(size * 0.42f)
        )
    }
}

@Composable
private fun CrimsonStatusChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(CrimsonChipBg)
            .border(1.dp, CrimsonLineBorder, RoundedCornerShape(30.dp))
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = CrimsonRoseText
        )
    }
}

@Composable
fun visualizerModeLabel(mode: VisualizerMode): String {
    return when (mode) {
        VisualizerMode.EDM_SPECTRUM -> stringResource(R.string.mode_edm_spectrum)
        VisualizerMode.TRANCE_RING -> stringResource(R.string.mode_trance_ring)
        VisualizerMode.NEON_WAVE -> stringResource(R.string.mode_neon_wave)
    }
}
