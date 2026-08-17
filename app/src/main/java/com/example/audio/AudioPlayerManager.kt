package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    companion object {
        private const val TAG = "AudioPlayerManager"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var progressTickerJob: Job? = null
    private var currentPlayingPath: String? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0)
    val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun playOrPause(filePath: String?) {
        if (filePath == null) return
        
        val file = File(filePath)
        if (!file.exists()) {
            Log.w(TAG, "Audio file does not exist: $filePath. Simulating playback progression.")
            toggleSimulatedPlayback()
            return
        }

        if (currentPlayingPath == filePath && mediaPlayer != null) {
            if (_isPlaying.value) {
                pause()
            } else {
                resume()
            }
            return
        }

        // Start new audio playback
        stop()
        try {
            val player = MediaPlayer()
            player.setDataSource(filePath)
            player.prepare()
            
            _durationMs.value = player.duration
            _currentPositionMs.value = 0
            currentPlayingPath = filePath

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val params = PlaybackParams()
                params.speed = _playbackSpeed.value
                player.playbackParams = params
            }

            player.setOnCompletionListener {
                _isPlaying.value = false
                _currentPositionMs.value = _durationMs.value
                stopProgressTicker()
            }

            player.start()
            mediaPlayer = player
            _isPlaying.value = true
            startProgressTicker()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio file: ${e.message}", e)
            toggleSimulatedPlayback()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
            stopProgressTicker()
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing audio: ${e.message}", e)
        }
    }

    fun resume() {
        try {
            mediaPlayer?.start()
            _isPlaying.value = true
            startProgressTicker()
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming audio: ${e.message}", e)
        }
    }

    fun seekTo(positionMs: Int) {
        val target = positionMs.coerceIn(0, _durationMs.value.coerceAtLeast(1))
        _currentPositionMs.value = target
        try {
            mediaPlayer?.seekTo(target)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking audio: ${e.message}", e)
        }
    }

    fun skipBy(deltaMs: Int) {
        val newPos = (_currentPositionMs.value + deltaMs).coerceIn(0, _durationMs.value.coerceAtLeast(1))
        seekTo(newPos)
    }

    fun cyclePlaybackSpeed(): Float {
        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val currentIndex = speeds.indexOfFirst { it == _playbackSpeed.value }
        val nextIndex = if (currentIndex == -1 || currentIndex == speeds.size - 1) 0 else currentIndex + 1
        val newSpeed = speeds[nextIndex]
        _playbackSpeed.value = newSpeed

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mediaPlayer != null) {
            try {
                val params = PlaybackParams()
                params.speed = newSpeed
                mediaPlayer?.playbackParams = params
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update speed: ${e.message}", e)
            }
        }
        return newSpeed
    }

    fun stop() {
        stopProgressTicker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            mediaPlayer = null
            _isPlaying.value = false
            currentPlayingPath = null
        }
    }

    private fun startProgressTicker() {
        stopProgressTicker()
        progressTickerJob = coroutineScope.launch(Dispatchers.Default) {
            while (isActive && _isPlaying.value) {
                try {
                    val pos = mediaPlayer?.currentPosition ?: _currentPositionMs.value
                    _currentPositionMs.value = pos
                } catch (e: Exception) {
                    // Ignore
                }
                delay(200)
            }
        }
    }

    private fun stopProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = null
    }

    private var simJob: Job? = null
    private fun toggleSimulatedPlayback() {
        if (_isPlaying.value) {
            _isPlaying.value = false
            simJob?.cancel()
            simJob = null
        } else {
            _isPlaying.value = true
            if (_durationMs.value <= 0) {
                _durationMs.value = 180000 // 3 minutes default simulated duration
            }
            simJob?.cancel()
            simJob = coroutineScope.launch(Dispatchers.Default) {
                while (isActive && _isPlaying.value) {
                    delay(500)
                    val next = _currentPositionMs.value + (500 * _playbackSpeed.value).toInt()
                    if (next >= _durationMs.value) {
                        _currentPositionMs.value = _durationMs.value
                        _isPlaying.value = false
                        break
                    } else {
                        _currentPositionMs.value = next
                    }
                }
            }
        }
    }
}
