package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TrackEntity
import com.example.ui.components.CrimsonGlassIconButton
import com.example.ui.components.CrimsonGlassPillButton
import com.example.ui.components.CrimsonPrimaryCtaButton
import com.example.ui.formatDurationMs
import com.example.ui.theme.CrimsonActiveBorder
import com.example.ui.theme.CrimsonActiveIcon
import com.example.ui.theme.CrimsonCardElevated
import com.example.ui.theme.CrimsonCardSurface
import com.example.ui.theme.CrimsonEyebrow
import com.example.ui.theme.CrimsonLineBorder
import com.example.ui.theme.CrimsonNoticeText
import com.example.ui.theme.CrimsonSheetScrim
import com.example.ui.theme.CrimsonSubtitle
import com.example.ui.theme.CrimsonWhite

@Composable
fun LibrarySheet(
    tracks: List<Pair<TrackEntity, Int>>,
    totalTrackCount: Int,
    currentIndex: Int,
    searchQuery: String,
    favoritesOnly: Boolean,
    onClose: () -> Unit,
    onAddFiles: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectFavoritesOnly: (Boolean) -> Unit,
    onSelectTrack: (Int) -> Unit,
    onRemoveTrack: (TrackEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(CrimsonSheetScrim)
            .clickable(enabled = true, onClick = {}) // Prevent touch bleed-through
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.library_eyebrow),
                    style = MaterialTheme.typography.labelSmall,
                    color = CrimsonEyebrow
                )
                CrimsonGlassIconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("library_close_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.cd_close_sheet),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.library_title),
                style = MaterialTheme.typography.headlineLarge,
                color = CrimsonWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.library_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = CrimsonSubtitle
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Add audio files CTA
            CrimsonPrimaryCtaButton(
                onClick = onAddFiles,
                modifier = Modifier.testTag("library_add_files_button")
            ) {
                Text(
                    text = stringResource(R.string.library_add_files),
                    style = MaterialTheme.typography.titleMedium,
                    color = CrimsonWhite
                )
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.library_add_files),
                    tint = CrimsonWhite,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(R.string.library_search_hint),
                        color = CrimsonNoticeText
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = CrimsonSubtitle
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CrimsonCardSurface,
                    unfocusedContainerColor = CrimsonCardSurface,
                    focusedBorderColor = CrimsonActiveBorder,
                    unfocusedBorderColor = CrimsonLineBorder,
                    focusedTextColor = CrimsonWhite,
                    unfocusedTextColor = CrimsonWhite,
                    cursorColor = CrimsonActiveIcon
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CrimsonGlassPillButton(
                    onClick = { onSelectFavoritesOnly(false) },
                    active = !favoritesOnly,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("filter_all_button")
                ) {
                    Text(
                        text = stringResource(R.string.filter_all_tracks),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                CrimsonGlassPillButton(
                    onClick = { onSelectFavoritesOnly(true) },
                    active = favoritesOnly,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("filter_favorites_button")
                ) {
                    Text(
                        text = stringResource(R.string.filter_favorites),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.library_track_count, tracks.size),
                style = MaterialTheme.typography.bodySmall,
                color = CrimsonSubtitle,
                modifier = Modifier.testTag("library_count_text")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Track List or Empty State
            if (tracks.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = CrimsonNoticeText.copy(alpha = 0.7f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (totalTrackCount > 0) {
                            stringResource(R.string.library_no_matches)
                        } else {
                            stringResource(R.string.library_empty)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = CrimsonSubtitle,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("library_song_list")
                ) {
                    items(
                        items = tracks,
                        key = { (track, _) -> track.uriString }
                    ) { (track, originalIndex) ->
                        val isCurrent = originalIndex == currentIndex
                        LibrarySongRow(
                            track = track,
                            isCurrent = isCurrent,
                            onClick = { onSelectTrack(originalIndex) },
                            onRemove = { onRemoveTrack(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibrarySongRow(
    track: TrackEntity,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (isCurrent) CrimsonActiveBorder else CrimsonLineBorder

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CrimsonCardElevated)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 6.dp)
            .testTag("library_song_item")
    ) {
        Icon(
            imageVector = if (isCurrent) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
            contentDescription = null,
            tint = if (isCurrent) CrimsonActiveIcon else CrimsonSubtitle,
            modifier = Modifier.size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isCurrent) CrimsonActiveIcon else CrimsonWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val metaLine = buildString {
                append(track.artist)
                if (track.durationMs > 0L) {
                    append(" • ")
                    append(formatDurationMs(track.durationMs))
                }
            }
            Text(
                text = metaLine,
                style = MaterialTheme.typography.bodySmall,
                color = CrimsonNoticeText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (track.isFavorite) {
            Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = stringResource(R.string.action_favorite),
                tint = CrimsonActiveIcon,
                modifier = Modifier.size(16.dp)
            )
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.testTag("remove_track_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = stringResource(R.string.cd_remove_track),
                tint = CrimsonNoticeText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
