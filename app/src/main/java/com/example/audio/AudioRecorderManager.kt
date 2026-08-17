package com.example.audio

import android.content.Context
import android.media.MediaRecorder
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED
}

class AudioRecorderManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    companion object {
        private const val TAG = "AudioRecorderManager"
    }

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var tickerJob: Job? = null

    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _recentAmplitudes = MutableStateFlow<List<Float>>(emptyList())
    val recentAmplitudes: StateFlow<List<Float>> = _recentAmplitudes.asStateFlow()

    fun startRecording(customOutputFile: File? = null): File? {
        try {
            val file = customOutputFile ?: createRecordingFile()
            currentOutputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _recordingState.value = RecordingState.RECORDING
            _elapsedSeconds.value = 0
            _recentAmplitudes.value = emptyList()

            startAmplitudeTicker()
            Log.d(TAG, "Recording started: ${file.absolutePath}")
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording: ${e.message}", e)
            _recordingState.value = RecordingState.IDLE
            return null
        }
    }

    fun pauseRecording() {
        if (_recordingState.value == RecordingState.RECORDING) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.pause()
                    _recordingState.value = RecordingState.PAUSED
                }
            } catch (e: Exception) {
                Log.e(TAG, "Pause failed: ${e.message}", e)
            }
        }
    }

    fun resumeRecording() {
        if (_recordingState.value == RecordingState.PAUSED) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.resume()
                    _recordingState.value = RecordingState.RECORDING
                }
            } catch (e: Exception) {
                Log.e(TAG, "Resume failed: ${e.message}", e)
            }
        }
    }

    fun stopRecording(): Pair<File?, Int> {
        val seconds = _elapsedSeconds.value
        val file = currentOutputFile
        tickerJob?.cancel()
        tickerJob = null

        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recorder: ${e.message}", e)
        } finally {
            mediaRecorder = null
            _recordingState.value = RecordingState.STOPPED
            _currentAmplitude.value = 0f
        }

        return Pair(file, seconds)
    }

    fun cancelRecording() {
        tickerJob?.cancel()
        tickerJob = null
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            // Ignore
        } finally {
            mediaRecorder = null
            _recordingState.value = RecordingState.IDLE
            _currentAmplitude.value = 0f
            currentOutputFile?.delete()
            currentOutputFile = null
        }
    }

    private fun startAmplitudeTicker() {
        tickerJob?.cancel()
        tickerJob = coroutineScope.launch(Dispatchers.Default) {
            var counter = 0
            while (isActive) {
                if (_recordingState.value == RecordingState.RECORDING) {
                    val maxAmp = try {
                        mediaRecorder?.maxAmplitude ?: 0
                    } catch (e: Exception) {
                        0
                    }

                    // Normalize amplitude between 0.05 and 1.0
                    val normalized = if (maxAmp > 0) {
                        val db = 20 * log10(maxAmp.toDouble())
                        ((db - 20) / 70.0).toFloat().coerceIn(0.08f, 1.0f)
                    } else {
                        (0.08f + Math.random().toFloat() * 0.15f)
                    }

                    _currentAmplitude.value = normalized

                    // Update recent amplitudes queue (keep last 35 points)
                    val currentList = _recentAmplitudes.value.toMutableList()
                    currentList.add(normalized)
                    if (currentList.size > 40) {
                        currentList.removeAt(0)
                    }
                    _recentAmplitudes.value = currentList

                    counter++
                    if (counter % 10 == 0) { // roughly every 1 second
                        _elapsedSeconds.value += 1
                    }
                }
                delay(100)
            }
        }
    }

    private fun createRecordingFile(): File {
        val dir = File(context.filesDir, "recordings").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(dir, "REC_${timeStamp}.m4a")
    }
}
