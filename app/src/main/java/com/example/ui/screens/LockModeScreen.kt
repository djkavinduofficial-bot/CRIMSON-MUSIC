package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.playback.PlaybackSnapshot
import com.example.ui.components.BackdropStyle
import com.example.ui.components.CrimsonBackdrop
import com.example.ui.components.CrimsonGlassIconButton
import com.example.ui.components.CrimsonGlassPillButton
import com.example.ui.components.CrimsonHeroDisc
import com.example.ui.components.CrimsonVisualizerCanvas
import com.example.ui.formatDurationMs
import com.example.ui.theme.CrimsonArcTrack
import com.example.ui.theme.CrimsonArtistText
import com.example.ui.theme.CrimsonEyebrow
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonRoseText
import com.example.ui.theme.CrimsonSoftPink
import com.example.ui.theme.CrimsonSubtitle
import com.example.ui.theme.CrimsonWhite
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LockModeScreen(
    playback: PlaybackSnapshot,
    onUnlock: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onUnlock)

    var clockText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            clockText = timeFmt.format(now)
            dateText = dateFmt.format(now)
            delay(1000L)
        }
    }

    val currentTrack = playback.currentTrack

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isCompactHeight = maxHeight < 740.dp
        val discSize = if (isCompactHeight) 144.dp else 180.dp

        CrimsonBackdrop(style = BackdropStyle.LOCK_MODE)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                CrimsonGlassIconButton(
                    onClick = onUnlock,
                    modifier = Modifier.testTag("lock_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back_to_player),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.lock_mode_eyebrow),
                    style = MaterialTheme.typography.labelSmall,
                    color = CrimsonEyebrow
                )

                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = stringResource(R.string.cd_lock_icon),
                    tint = CrimsonSoftPink,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(if (isCompactHeight) 12.dp else 22.dp))

            // Digital Clock + Date
            Text(
                text = clockText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (isCompactHeight) 56.sp else 72.sp
                ),
                color = CrimsonWhite,
                modifier = Modifier.testTag("lock_clock_text")
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium,
                color = CrimsonRoseText,
                modifier = Modifier.testTag("lock_date_text")
            )

            Spacer(modifier = Modifier.height(if (isCompactHeight) 14.dp else 22.dp))

            // Spinning Disc
            CrimsonHeroDisc(
                isPlaying = playback.isPlaying,
                artwork = playback.currentArtwork,
                discContainerSize = discSize
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Track Info
            Text(
                text = currentTrack?.title ?: stringResource(R.string.empty_track_title),
                style = MaterialTheme.typography.headlineMedium,
                color = CrimsonWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentTrack?.artist ?: stringResource(R.string.empty_track_artist),
                style = MaterialTheme.typography.bodyMedium,
                color = CrimsonArtistText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Visualizer
            CrimsonVisualizerCanvas(
                mode = playback.visualizerMode,
                spectrumBins = playback.spectrumBins,
                waveformSamples = playback.waveformSamples,
                modifier = Modifier.height(58.dp)
            )

            // Linear Seek Slider
            Slider(
                value = playback.progressFraction,
                onValueChange = onSeek,
                colors = SliderDefaults.colors(
                    thumbColor = CrimsonGlow,
                    activeTrackColor = CrimsonGlow,
                    inactiveTrackColor = CrimsonArcTrack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lock_seek_slider")
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatDurationMs(playback.positionMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = CrimsonSubtitle
                )
                Text(
                    text = formatDurationMs(playback.durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = CrimsonSubtitle
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prev / Play / Next controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 330.dp)
            ) {
                CrimsonGlassPillButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("lock_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = stringResource(R.string.cd_previous),
                        modifier = Modifier.size(26.dp)
                    )
                }

                CrimsonPlayButton(
                    isPlaying = playback.isPlaying,
                    size = 80.dp,
                    onClick = onPlayPause,
                    modifier = Modifier.testTag("lock_play_button")
                )

                CrimsonGlassPillButton(
                    onClick = onNext,
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("lock_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = stringResource(R.string.cd_next),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Back to Player unlock button
            CrimsonGlassPillButton(
                onClick = onUnlock,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.testTag("lock_unlock_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.LockOpen,
                    contentDescription = stringResource(R.string.action_back_to_player),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.action_back_to_player),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
