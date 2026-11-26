package com.example.playback

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.CrimsonDatabase
import com.example.data.PreferencesRepository
import com.example.data.TrackEntity
import com.example.data.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class CrimsonPlaybackController private constructor(
    private val appContext: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val database = CrimsonDatabase.getInstance(appContext)
    val trackRepository = TrackRepository(appContext, database.trackDao())
    val preferencesRepository = PreferencesRepository(appContext)

    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false

    private var currentEnergyProfile: AudioEnergyProfile? = null
    private var analysisJob: Job? = null
    private var artworkJob: Job? = null
    private var tickerJob: Job? = null

    private val _state = MutableStateFlow(PlaybackSnapshot())
    val state: StateFlow<PlaybackSnapshot> = _state.asStateFlow()

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                val vol = (_state.value.volume * 0.25f).coerceIn(0f, 1f)
                mediaPlayer?.setVolume(vol, vol)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                val vol = _state.value.volume
                mediaPlayer?.setVolume(vol, vol)
            }
        }
    }

    init {
        scope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            _state.update {
                it.copy(
                    volume = prefs.volume,
                    visualizerMode = VisualizerMode.fromIndex(prefs.visualizerMode),
                    shuffle = prefs.shuffleEnabled,
                    repeatOne = prefs.repeatOneEnabled
                )
            }

            trackRepository.allTracks.collect { tracks ->
                val currentUri = _state.value.currentTrack?.uriString ?: prefs.lastTrackUri
                val newIndex = if (tracks.isEmpty()) {
                    -1
                } else {
                    tracks.indexOfFirst { it.uriString == currentUri }.let { idx ->
                        if (idx >= 0) idx else if (_state.value.currentIndex in tracks.indices) _state.value.currentIndex else 0
                    }
                }
                val previousUri = _state.value.currentTrack?.uriString
                _state.update {
                    it.copy(
                        tracks = tracks,
                        currentIndex = newIndex,
                        durationMs = if (newIndex >= 0 && !isPrepared) {
                            tracks[newIndex].durationMs
                        } else {
                            it.durationMs
                        }
                    )
                }
                if (newIndex < 0) {
                    stopAndRelease()
                } else if (previousUri == null && !isPrepared) {
                    loadTrackAt(newIndex, autoPlay = false)
                }
            }
        }
        startTicker()
    }

    fun loadTrackAt(index: Int, autoPlay: Boolean = true) {
        val tracks = _state.value.tracks
        if (tracks.isEmpty()) return
        val safeIndex = ((index % tracks.size) + tracks.size) % tracks.size
        val track = tracks[safeIndex]

        releasePlayerOnly()
        isPrepared = false

        _state.update {
            it.copy(
                currentIndex = safeIndex,
                isPlaying = false,
                positionMs = 0L,
                durationMs = track.durationMs,
                noticeMessage = null
            )
        }

        scope.launch { preferencesRepository.setLastTrackUri(track.uriString) }
        loadArtworkAsync(track.uriString)
        analyzeAudioAsync(track.uriString)

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setWakeMode(appContext, PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(appContext, Uri.parse(track.uriString))
                isLooping = _state.value.repeatOne
                val vol = _state.value.volume
                setVolume(vol, vol)
                setOnPreparedListener { player ->
                    isPrepared = true
                    val dur = player.duration.toLong().coerceAtLeast(0L)
                    _state.update { it.copy(durationMs = dur) }
                    scope.launch { trackRepository.updateDuration(track.uriString, dur) }
                    if (autoPlay) {
                        startPlaybackInternal()
                    } else {
                        syncServiceNotification()
                    }
                }
                setOnCompletionListener {
                    if (!_state.value.repeatOne) {
                        nextTrack()
                    }
                }
                setOnErrorListener { _, _, _ ->
                    isPrepared = false
                    _state.update {
                        it.copy(
                            isPlaying = false,
                            noticeMessage = appContext.getString(R.string.notice_playback_error)
                        )
                    }
                    syncServiceNotification()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (_: Exception) {
            _state.update {
                it.copy(
                    isPlaying = false,
                    noticeMessage = appContext.getString(R.string.notice_playback_error)
                )
            }
        }
    }

    fun togglePlayPause(): Boolean {
        val current = _state.value
        if (current.tracks.isEmpty()) return false
        if (current.isPlaying) {
            pause()
        } else {
            if (isPrepared && mediaPlayer != null) {
                startPlaybackInternal()
            } else {
                val targetIndex = if (current.currentIndex >= 0) current.currentIndex else 0
                loadTrackAt(targetIndex, autoPlay = true)
            }
        }
        return true
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: IllegalStateException) {
        }
        _state.update { it.copy(isPlaying = false) }
        syncServiceNotification()
    }

    fun nextTrack() {
        val tracks = _state.value.tracks
        if (tracks.isEmpty()) return
        val currentIdx = _state.value.currentIndex
        val nextIdx = if (_state.value.shuffle && tracks.size > 1) {
            var candidate = currentIdx
            while (candidate == currentIdx) {
                candidate = Random.nextInt(tracks.size)
            }
            candidate
        } else {
            currentIdx + 1
        }
        loadTrackAt(nextIdx, autoPlay = true)
    }

    fun previousTrack() {
        val tracks = _state.value.tracks
        if (tracks.isEmpty()) return
        if (_state.value.positionMs > 3000L && isPrepared) {
            seekToMs(0L)
            return
        }
        loadTrackAt(_state.value.currentIndex - 1, autoPlay = true)
    }

    fun seekToFraction(fraction: Float) {
        val duration = _state.value.durationMs
        if (duration <= 0L) return
        val targetMs = (duration * fraction.coerceIn(0f, 1f)).toLong()
        seekToMs(targetMs)
    }

    fun seekToMs(positionMs: Long) {
        val duration = _state.value.durationMs
        val clamped = positionMs.coerceIn(0L, duration.coerceAtLeast(0L))
        try {
            if (isPrepared) {
                mediaPlayer?.seekTo(clamped.toInt())
            }
        } catch (_: IllegalStateException) {
        }
        _state.update { it.copy(positionMs = clamped) }
    }

    fun toggleShuffle() {
        val nextValue = !_state.value.shuffle
        _state.update { it.copy(shuffle = nextValue) }
        scope.launch { preferencesRepository.setShuffle(nextValue) }
    }

    fun toggleRepeatOne() {
        val nextValue = !_state.value.repeatOne
        try {
            mediaPlayer?.isLooping = nextValue
        } catch (_: IllegalStateException) {
        }
        _state.update { it.copy(repeatOne = nextValue) }
        scope.launch { preferencesRepository.setRepeatOne(nextValue) }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (_: IllegalStateException) {
        }
        _state.update { it.copy(volume = clamped) }
        scope.launch { preferencesRepository.setVolume(clamped) }
    }

    fun setVisualizerMode(mode: VisualizerMode) {
        _state.update { it.copy(visualizerMode = mode) }
        scope.launch { preferencesRepository.setVisualizerMode(mode.index) }
    }

    fun cycleVisualizerMode() {
        setVisualizerMode(_state.value.visualizerMode.next())
    }

    fun setNotice(message: String?) {
        _state.update { it.copy(noticeMessage = message) }
    }

    fun stopAndRelease() {
        releasePlayerOnly()
        isPrepared = false
        abandonAudioFocus()
        currentEnergyProfile = null
        _state.update {
            it.copy(
                isPlaying = false,
                positionMs = 0L,
                durationMs = 0L,
                currentArtwork = null
            )
        }
        try {
            appContext.stopService(Intent(appContext, CrimsonPlaybackService::class.java))
        } catch (_: Exception) {
        }
    }

    private fun startPlaybackInternal() {
        if (!requestAudioFocus()) return
        try {
            mediaPlayer?.start()
            _state.update { it.copy(isPlaying = true, noticeMessage = null) }
            syncServiceNotification()
        } catch (_: Exception) {
            _state.update {
                it.copy(
                    isPlaying = false,
                    noticeMessage = appContext.getString(R.string.notice_playback_error)
                )
            }
        }
    }

    private fun syncServiceNotification() {
        val current = _state.value
        val track = current.currentTrack ?: return
        if (!current.isPlaying && !CrimsonPlaybackService.isRunning) return
        val intent = Intent(appContext, CrimsonPlaybackService::class.java).apply {
            action = CrimsonPlaybackService.ACTION_SYNC
            putExtra(CrimsonPlaybackService.EXTRA_TITLE, track.title)
            putExtra(CrimsonPlaybackService.EXTRA_ARTIST, track.artist)
            putExtra(CrimsonPlaybackService.EXTRA_PLAYING, current.isPlaying)
        }
        try {
            if (current.isPlaying) {
                ContextCompat.startForegroundService(appContext, intent)
            } else {
                appContext.startService(intent)
            }
        } catch (_: Exception) {
            // Foreground service may be restricted if launched from background; audio still plays locally
        }
    }

    private fun loadArtworkAsync(uriString: String) {
        artworkJob?.cancel()
        artworkJob = scope.launch {
            val bitmap = trackRepository.loadArtwork(uriString)
            if (_state.value.currentTrack?.uriString == uriString) {
                _state.update { it.copy(currentArtwork = bitmap) }
            }
        }
    }

    private fun analyzeAudioAsync(uriString: String) {
        if (currentEnergyProfile?.uriString == uriString) return
        analysisJob?.cancel()
        currentEnergyProfile = null
        analysisJob = scope.launch {
            val profile = AudioEnvelopeAnalyzer.analyze(appContext, uriString)
            if (_state.value.currentTrack?.uriString == uriString) {
                currentEnergyProfile = profile
            }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val snapshot = _state.value
                val playing = snapshot.isPlaying && isPrepared
                val posMs = if (playing) {
                    try {
                        mediaPlayer?.currentPosition?.toLong()?.coerceAtLeast(0L) ?: snapshot.positionMs
                    } catch (_: IllegalStateException) {
                        snapshot.positionMs
                    }
                } else {
                    snapshot.positionMs
                }

                val hasResidualEnergy = snapshot.spectrumBins.any { it > 0.01f }
                if (playing || hasResidualEnergy) {
                    val (bins, wave) = AudioEnvelopeAnalyzer.synthesizeSpectrumAndWave(
                        isPlaying = playing,
                        positionMs = posMs,
                        volume = snapshot.volume,
                        profile = currentEnergyProfile,
                        prevBins = snapshot.spectrumBins
                    )
                    _state.update {
                        it.copy(
                            positionMs = posMs,
                            spectrumBins = bins,
                            waveformSamples = wave
                        )
                    }
                }
                delay(if (playing) 33L else 120L)
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            audioFocusRequest = req
            audioManager.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }
    }

    private fun releasePlayerOnly() {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }

    companion object {
        @Volatile
        private var instance: CrimsonPlaybackController? = null

        fun getInstance(context: Context): CrimsonPlaybackController {
            return instance ?: synchronized(this) {
                instance ?: CrimsonPlaybackController(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
