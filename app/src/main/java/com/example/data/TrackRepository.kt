package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class TrackRepository(
    private val context: Context,
    private val trackDao: TrackDao
) {
    val allTracks: Flow<List<TrackEntity>> = trackDao.observeAllTracks()

    private val artworkCache = ConcurrentHashMap<String, Bitmap?>()

    suspend fun importAudioUris(uris: List<Uri>): Int = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val parsedTracks = mutableListOf<TrackEntity>()
        var timestamp = System.currentTimeMillis()

        for (uri in uris) {
            try {
                resolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers do not support persistable URI permissions; keep session read access.
            }

            val displayName = queryDisplayName(uri) ?: uri.lastPathSegment ?: "Untitled Track"
            val mimeType = resolver.getType(uri).orEmpty()
            val extension = displayName.substringAfterLast('.', "").uppercase()
            val isSupportedAudio = mimeType.startsWith("audio/") ||
                extension in SUPPORTED_EXTENSIONS

            if (!isSupportedAudio) continue

            val fallbackTitle = displayName
                .substringBeforeLast('.')
                .replace('_', ' ')
                .trim()
                .ifEmpty { displayName }

            var title = fallbackTitle
            var artist = ""
            var durationMs = 0L

            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { title = it }

                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { artist = it }

                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()
                    ?.takeIf { it > 0L }
                    ?.let { durationMs = it }
            } catch (_: Exception) {
                // Use filename fallback metadata
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {
                }
            }

            val formatLabel = extension.ifEmpty {
                mimeType.substringAfter('/', "AUDIO").uppercase()
            }

            parsedTracks.add(
                TrackEntity(
                    uriString = uri.toString(),
                    title = title,
                    artist = artist.ifEmpty { "Local music • $formatLabel" },
                    format = formatLabel,
                    durationMs = durationMs,
                    isFavorite = false,
                    addedAt = timestamp++
                )
            )
        }

        if (parsedTracks.isEmpty()) return@withContext 0
        val results = trackDao.insertTracks(parsedTracks)
        results.count { it != -1L }
    }

    suspend fun toggleFavorite(track: TrackEntity) {
        trackDao.setFavorite(track.uriString, !track.isFavorite)
    }

    suspend fun updateDuration(uriString: String, durationMs: Long) {
        if (durationMs > 0L) {
            trackDao.updateDuration(uriString, durationMs)
        }
    }

    suspend fun removeTrack(uriString: String) {
        artworkCache.remove(uriString)
        trackDao.deleteTrack(uriString)
    }

    suspend fun loadArtwork(uriString: String): Bitmap? = withContext(Dispatchers.IO) {
        if (artworkCache.containsKey(uriString)) {
            return@withContext artworkCache[uriString]
        }

        val retriever = MediaMetadataRetriever()
        val decoded: Bitmap? = try {
            retriever.setDataSource(context, Uri.parse(uriString))
            val bytes = retriever.embeddedPicture
            if (bytes != null && bytes.isNotEmpty()) {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                val maxDim = maxOf(options.outWidth, options.outHeight)
                val sample = if (maxDim > 512) (maxDim / 512).coerceAtLeast(1) else 1
                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = sample
                }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }

        if (decoded != null) {
            artworkCache[uriString] = decoded
        }
        decoded
    }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index) else null
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private val SUPPORTED_EXTENSIONS = setOf(
            "MP3", "M4A", "WAV", "OGG", "FLAC", "AAC", "OPUS", "WMA", "3GP", "AMR"
        )
    }
}
