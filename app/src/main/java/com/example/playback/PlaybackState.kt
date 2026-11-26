package com.example.playback

import android.graphics.Bitmap
import com.example.data.TrackEntity

enum class VisualizerMode(val index: Int) {
    EDM_SPECTRUM(0),
    TRANCE_RING(1),
    NEON_WAVE(2);

    fun next(): VisualizerMode = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromIndex(index: Int): VisualizerMode =
            entries.firstOrNull { it.index == index } ?: EDM_SPECTRUM
    }
}

data class PlaybackSnapshot(
    val tracks: List<TrackEntity> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatOne: Boolean = false,
    val volume: Float = 0.85f,
    val visualizerMode: VisualizerMode = VisualizerMode.EDM_SPECTRUM,
    val currentArtwork: Bitmap? = null,
    val spectrumBins: FloatArray = FloatArray(64),
    val waveformSamples: FloatArray = FloatArray(128),
    val noticeMessage: String? = null
) {
    val currentTrack: TrackEntity?
        get() = tracks.getOrNull(currentIndex)

    val progressFraction: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlaybackSnapshot) return false
        return tracks == other.tracks &&
            currentIndex == other.currentIndex &&
            isPlaying == other.isPlaying &&
            positionMs == other.positionMs &&
            durationMs == other.durationMs &&
            shuffle == other.shuffle &&
            repeatOne == other.repeatOne &&
            volume == other.volume &&
            visualizerMode == other.visualizerMode &&
            currentArtwork === other.currentArtwork &&
            spectrumBins.contentEquals(other.spectrumBins) &&
            waveformSamples.contentEquals(other.waveformSamples) &&
            noticeMessage == other.noticeMessage
    }

    override fun hashCode(): Int {
        var result = tracks.hashCode()
        result = 31 * result + currentIndex
        result = 31 * result + isPlaying.hashCode()
        result = 31 * result + positionMs.hashCode()
        result = 31 * result + durationMs.hashCode()
        result = 31 * result + shuffle.hashCode()
        result = 31 * result + repeatOne.hashCode()
        result = 31 * result + volume.hashCode()
        result = 31 * result + visualizerMode.hashCode()
        result = 31 * result + (currentArtwork?.hashCode() ?: 0)
        result = 31 * result + spectrumBins.contentHashCode()
        result = 31 * result + waveformSamples.contentHashCode()
        result = 31 * result + (noticeMessage?.hashCode() ?: 0)
        return result
    }
}
