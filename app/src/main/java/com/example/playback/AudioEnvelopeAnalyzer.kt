package com.example.playback

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Decodes real PCM energy envelopes from local audio tracks without requiring
 * RECORD_AUDIO permission, allowing accurate beat-reactive visualizers offline.
 */
data class AudioEnergyProfile(
    val uriString: String,
    val stepMs: Int,
    val bass: FloatArray,
    val mids: FloatArray,
    val highs: FloatArray
) {
    fun sampleAt(positionMs: Long): Triple<Float, Float, Float> {
        if (bass.isEmpty()) return Triple(0f, 0f, 0f)
        val idx = (positionMs / stepMs).toInt().coerceIn(0, bass.lastIndex)
        return Triple(bass[idx], mids[idx], highs[idx])
    }
}

object AudioEnvelopeAnalyzer {

    private const val STEP_MS = 100
    private const val MAX_SECONDS = 480

    suspend fun analyze(context: Context, uriString: String): AudioEnergyProfile? =
        withContext(Dispatchers.IO) {
            val extractor = MediaExtractor()
            var codec: MediaCodec? = null
            try {
                extractor.setDataSource(context, Uri.parse(uriString), null)
                var audioTrackIndex = -1
                var audioFormat: MediaFormat? = null
                for (i in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME).orEmpty()
                    if (mime.startsWith("audio/")) {
                        audioTrackIndex = i
                        audioFormat = format
                        break
                    }
                }
                if (audioTrackIndex < 0 || audioFormat == null) return@withContext null

                val mime = audioFormat.getString(MediaFormat.KEY_MIME) ?: return@withContext null
                val sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE).coerceAtLeast(8000)
                val channelCount = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT).coerceAtLeast(1)
                val durationUs = if (audioFormat.containsKey(MediaFormat.KEY_DURATION)) {
                    audioFormat.getLong(MediaFormat.KEY_DURATION)
                } else {
                    180_000_000L
                }
                val totalBuckets = ((durationUs / 1000L) / STEP_MS).toInt()
                    .coerceIn(10, MAX_SECONDS * (1000 / STEP_MS))

                val bassArr = FloatArray(totalBuckets)
                val midsArr = FloatArray(totalBuckets)
                val highsArr = FloatArray(totalBuckets)
                val countArr = IntArray(totalBuckets)

                extractor.selectTrack(audioTrackIndex)
                codec = MediaCodec.createDecoderByType(mime)
                codec.configure(audioFormat, null, null, 0)
                codec.start()

                val bufferInfo = MediaCodec.BufferInfo()
                var sawInputEos = false
                var sawOutputEos = false
                var lowPassState = 0f
                var prevSample = 0f
                var decodedFrames = 0

                while (!sawOutputEos && isActive && decodedFrames < 18000) {
                    if (!sawInputEos) {
                        val inIdx = codec.dequeueInputBuffer(4000L)
                        if (inIdx >= 0) {
                            val inBuf = codec.getInputBuffer(inIdx)
                            if (inBuf != null) {
                                val size = extractor.readSampleData(inBuf, 0)
                                if (size < 0) {
                                    codec.queueInputBuffer(
                                        inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                    )
                                    sawInputEos = true
                                } else {
                                    val pts = extractor.sampleTime.coerceAtLeast(0L)
                                    codec.queueInputBuffer(inIdx, 0, size, pts, 0)
                                    extractor.advance()
                                }
                            }
                        }
                    }

                    val outIdx = codec.dequeueOutputBuffer(bufferInfo, 4000L)
                    if (outIdx >= 0) {
                        val outBuf = codec.getOutputBuffer(outIdx)
                        if (outBuf != null && bufferInfo.size > 0) {
                            val bucket = ((bufferInfo.presentationTimeUs / 1000L) / STEP_MS).toInt()
                            if (bucket in 0 until totalBuckets) {
                                outBuf.position(bufferInfo.offset)
                                outBuf.limit(bufferInfo.offset + bufferInfo.size)
                                val shortBuf = outBuf.slice().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                                val stride = (channelCount * 6).coerceAtLeast(channelCount)
                                val limit = shortBuf.remaining()
                                var bassSum = 0f
                                var midSum = 0f
                                var highSum = 0f
                                var samples = 0
                                var i = 0
                                while (i < limit) {
                                    val s = shortBuf.get(i) / 32768f
                                    lowPassState += 0.14f * (s - lowPassState)
                                    val high = s - prevSample
                                    val mid = s - lowPassState
                                    prevSample = s
                                    bassSum += lowPassState * lowPassState
                                    midSum += mid * mid
                                    highSum += high * high
                                    samples++
                                    i += stride
                                }
                                if (samples > 0) {
                                    bassArr[bucket] += sqrt(bassSum / samples)
                                    midsArr[bucket] += sqrt(midSum / samples)
                                    highsArr[bucket] += sqrt(highSum / samples)
                                    countArr[bucket]++
                                }
                            }
                        }
                        codec.releaseOutputBuffer(outIdx, false)
                        decodedFrames++
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            sawOutputEos = true
                        }
                    } else if (outIdx == MediaCodec.INFO_TRY_AGAIN_LATER && sawInputEos) {
                        break
                    }
                }

                var maxB = 0.001f
                var maxM = 0.001f
                var maxH = 0.001f
                for (i in 0 until totalBuckets) {
                    if (countArr[i] > 0) {
                        bassArr[i] /= countArr[i]
                        midsArr[i] /= countArr[i]
                        highsArr[i] /= countArr[i]
                        if (bassArr[i] > maxB) maxB = bassArr[i]
                        if (midsArr[i] > maxM) maxM = midsArr[i]
                        if (highsArr[i] > maxH) maxH = highsArr[i]
                    }
                }
                for (i in 0 until totalBuckets) {
                    bassArr[i] = (bassArr[i] / maxB).coerceIn(0f, 1f)
                    midsArr[i] = (midsArr[i] / maxM).coerceIn(0f, 1f)
                    highsArr[i] = (highsArr[i] / maxH).coerceIn(0f, 1f)
                }

                AudioEnergyProfile(
                    uriString = uriString,
                    stepMs = STEP_MS,
                    bass = bassArr,
                    mids = midsArr,
                    highs = highsArr
                )
            } catch (_: Exception) {
                null
            } finally {
                try {
                    codec?.stop()
                    codec?.release()
                } catch (_: Exception) {
                }
                try {
                    extractor.release()
                } catch (_: Exception) {
                }
            }
        }

    fun synthesizeSpectrumAndWave(
        isPlaying: Boolean,
        positionMs: Long,
        volume: Float,
        profile: AudioEnergyProfile?,
        prevBins: FloatArray,
        binCount: Int = 64,
        waveCount: Int = 128
    ): Pair<FloatArray, FloatArray> {
        val bins = FloatArray(binCount)
        val wave = FloatArray(waveCount)
        if (!isPlaying) {
            for (i in 0 until binCount) {
                bins[i] = (prevBins.getOrElse(i) { 0f } * 0.78f).let { if (it < 0.01f) 0f else it }
            }
            return bins to wave
        }

        val t = positionMs / 1000.0
        val gain = (0.35f + volume * 0.65f).coerceIn(0.2f, 1f)
        val (bass, mid, high) = if (profile != null) {
            profile.sampleAt(positionMs)
        } else {
            val beat = (0.55 + 0.45 * abs(sin(t * PI * 2.15))).toFloat()
            val midPulse = (0.45 + 0.45 * abs(sin(t * PI * 3.4 + 1.1))).toFloat()
            val highPulse = (0.35 + 0.45 * abs(cos(t * PI * 5.2 + 0.4))).toFloat()
            Triple(beat, midPulse, highPulse)
        }

        for (i in 0 until binCount) {
            val norm = i.toFloat() / (binCount - 1).coerceAtLeast(1)
            val bandEnergy = when {
                norm < 0.30f -> bass
                norm < 0.70f -> mid
                else -> high
            }
            val harmonic = 0.62f + 0.38f * abs(
                sin(t * (3.2 + i * 0.27) + i * 0.45).toFloat()
            )
            val target = (bandEnergy * harmonic * gain).coerceIn(0.04f, 1f)
            val prev = prevBins.getOrElse(i) { 0f }
            bins[i] = prev * 0.45f + target * 0.55f
        }

        for (i in 0 until waveCount) {
            val x = i.toDouble() / (waveCount - 1)
            val env = sin(x * PI)
            val s1 = sin(x * PI * 6.0 + t * 14.0) * bass
            val s2 = sin(x * PI * 14.0 - t * 21.0) * mid * 0.65
            val s3 = cos(x * PI * 26.0 + t * 31.0) * high * 0.35
            wave[i] = ((s1 + s2 + s3) * env * 0.55 * gain).toFloat().coerceIn(-1f, 1f)
        }

        return bins to wave
    }
}
