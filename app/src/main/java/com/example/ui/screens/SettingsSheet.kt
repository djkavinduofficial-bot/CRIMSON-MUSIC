package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.playback.VisualizerMode
import com.example.ui.components.CrimsonGlassIconButton
import com.example.ui.components.CrimsonGlassPillButton
import com.example.ui.theme.CrimsonArcTrack
import com.example.ui.theme.CrimsonEyebrow
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonNoticeText
import com.example.ui.theme.CrimsonSheetScrim
import com.example.ui.theme.CrimsonSoftPink
import com.example.ui.theme.CrimsonSubtitle
import com.example.ui.theme.CrimsonWhite
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    volume: Float,
    visualizerMode: VisualizerMode,
    onClose: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onVisualizerModeSelected: (VisualizerMode) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(CrimsonSheetScrim)
            .clickable(enabled = true, onClick = {})
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.settings_eyebrow),
                    style = MaterialTheme.typography.labelSmall,
                    color = CrimsonEyebrow
                )
                CrimsonGlassIconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("settings_close_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.cd_close_sheet),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                color = CrimsonWhite
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Volume Control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = CrimsonSoftPink,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.settings_volume),
                        style = MaterialTheme.typography.titleMedium,
                        color = CrimsonSubtitle
                    )
                }
                Text(
                    text = "${(volume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = CrimsonSoftPink
                )
            }

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = CrimsonGlow,
                    activeTrackColor = CrimsonGlow,
                    inactiveTrackColor = CrimsonArcTrack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_volume_slider")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Visualizer Style Selector
            Text(
                text = stringResource(R.string.settings_visualizer_style),
                style = MaterialTheme.typography.titleMedium,
                color = CrimsonSubtitle
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                VisualizerMode.entries.forEach { mode ->
                    CrimsonGlassPillButton(
                        onClick = { onVisualizerModeSelected(mode) },
                        active = visualizerMode == mode,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("visualizer_mode_${mode.index}")
                    ) {
                        Text(
                            text = visualizerModeLabel(mode),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_lock_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = CrimsonNoticeText
            )
        }
    }
}
