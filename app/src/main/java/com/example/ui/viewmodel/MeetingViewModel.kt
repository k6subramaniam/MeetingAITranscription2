package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.RecordingState
import com.example.data.model.ActionItem
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.TranscriptSegment
import com.example.data.repository.MeetingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingViewModel(application: Application) : AndroidViewModel(application) {
    val repository = MeetingRepository(application.applicationContext)

    // Filter & Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _filterFavoritesOnly = MutableStateFlow(false)
    val filterFavoritesOnly: StateFlow<Boolean> = _filterFavoritesOnly.asStateFlow()

    // Meetings Stream
    val allMeetings: StateFlow<List<MeetingEntity>> = combine(
        repository.allMeetings,
        _searchQuery,
        _selectedCategory,
        _filterFavoritesOnly
    ) { list, query, category, favOnly ->
        list.filter { meeting ->
            val matchesQuery = query.isBlank() ||
                    meeting.title.contains(query, ignoreCase = true) ||
                    meeting.rawTranscript.contains(query, ignoreCase = true) ||
                    meeting.executiveSummary.contains(query, ignoreCase = true) ||
                    meeting.tags.any { it.contains(query, ignoreCase = true) }
            
            val matchesCategory = category == null || meeting.category == category
            val matchesFav = !favOnly || meeting.isFavorite

            matchesQuery && matchesCategory && matchesFav
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Meeting Detail
    private val _activeMeetingId = MutableStateFlow<Long?>(null)
    val activeMeetingId: StateFlow<Long?> = _activeMeetingId.asStateFlow()

    val activeMeeting: StateFlow<MeetingEntity?> = _activeMeetingId.flatMapLatest { id ->
        if (id != null) repository.getMeetingById(id) else flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val activeChatMessages: StateFlow<List<ChatMessageEntity>> = _activeMeetingId.flatMapLatest { id ->
        if (id != null) repository.getChatMessages(id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Recording State
    val recordingState: StateFlow<RecordingState> = repository.recorderManager.recordingState
    val recordingElapsedSeconds: StateFlow<Int> = repository.recorderManager.elapsedSeconds
    val recordingAmplitude: StateFlow<Float> = repository.recorderManager.currentAmplitude
    val recordingRecentAmplitudes: StateFlow<List<Float>> = repository.recorderManager.recentAmplitudes

    // Transcription Loading State
    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _transcriptionStatusText = MutableStateFlow("")
    val transcriptionStatusText: StateFlow<String> = _transcriptionStatusText.asStateFlow()

    // Player State
    val isAudioPlaying: StateFlow<Boolean> = repository.playerManager.isPlaying
    val audioCurrentPositionMs: StateFlow<Int> = repository.playerManager.currentPositionMs
    val audioDurationMs: StateFlow<Int> = repository.playerManager.durationMs
    val audioPlaybackSpeed: StateFlow<Float> = repository.playerManager.playbackSpeed

    // Deep Thinking & Whiteboard Loading States
    private val _isThinkingLoading = MutableStateFlow(false)
    val isThinkingLoading: StateFlow<Boolean> = _isThinkingLoading.asStateFlow()

    private val _isWhiteboardLoading = MutableStateFlow(false)
    val isWhiteboardLoading: StateFlow<Boolean> = _isWhiteboardLoading.asStateFlow()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun toggleFavoritesFilter() {
        _filterFavoritesOnly.value = !_filterFavoritesOnly.value
    }

    fun selectMeeting(meetingId: Long?) {
        _activeMeetingId.value = meetingId
    }

    // --- Recording Actions ---

    fun startRecording(): File? {
        return repository.recorderManager.startRecording()
    }

    fun pauseRecording() {
        repository.recorderManager.pauseRecording()
    }

    fun resumeRecording() {
        repository.recorderManager.resumeRecording()
    }

    fun stopRecordingAndTranscribe(
        titleHint: String,
        categoryHint: String,
        onComplete: (Long) -> Unit
    ) {
        val (file, durationSeconds) = repository.recorderManager.stopRecording()
        if (file == null) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isTranscribing.value = true
            _transcriptionStatusText.value = "Analyzing audio with Gemini 3.5 Flash..."
            
            try {
                val analyzedMeeting = repository.geminiRepository.transcribeAndAnalyzeAudio(
                    audioFile = file,
                    mimeType = "audio/mp4",
                    customTitleHint = titleHint,
                    categoryHint = categoryHint,
                    durationSeconds = durationSeconds
                )

                _transcriptionStatusText.value = "Saving transcript and notes..."
                val newMeetingId = repository.saveMeeting(analyzedMeeting)
                
                // Add initial chat intro
                repository.sendChatMessage(
                    analyzedMeeting.copy(id = newMeetingId),
                    "Hello! I am your AI Meeting Assistant for **${analyzedMeeting.title}**. You can ask me anything about the transcript, key decisions, or action items."
                )

                _activeMeetingId.value = newMeetingId
                launch(Dispatchers.Main) {
                    _isTranscribing.value = false
                    _transcriptionStatusText.value = ""
                    onComplete(newMeetingId)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    _isTranscribing.value = false
                    _transcriptionStatusText.value = "Error: ${e.message}"
                }
            }
        }
    }

    fun cancelRecording() {
        repository.recorderManager.cancelRecording()
    }

    fun processTranscriptText(
        transcriptText: String,
        titleHint: String,
        categoryHint: String,
        onComplete: (Long) -> Unit
    ) {
        if (transcriptText.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isTranscribing.value = true
            _transcriptionStatusText.value = "Summarizing audio transcript with Gemini 3.5 Flash..."
            
            try {
                val analyzedMeeting = repository.geminiRepository.summarizeTranscriptText(
                    transcriptText = transcriptText,
                    customTitleHint = titleHint,
                    categoryHint = categoryHint
                )

                _transcriptionStatusText.value = "Saving transcript, bullet points, and action items..."
                val newMeetingId = repository.saveMeeting(analyzedMeeting)
                
                repository.sendChatMessage(
                    analyzedMeeting.copy(id = newMeetingId),
                    "I have processed your audio transcript and generated bullet points, summary takeaways, and action items. How can I assist you further?"
                )

                _activeMeetingId.value = newMeetingId
                launch(Dispatchers.Main) {
                    _isTranscribing.value = false
                    _transcriptionStatusText.value = ""
                    onComplete(newMeetingId)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    _isTranscribing.value = false
                    _transcriptionStatusText.value = "Error: ${e.message}"
                }
            }
        }
    }

    // --- Audio Playback Actions ---

    fun playOrPauseMeetingAudio(filePath: String?) {
        repository.playerManager.playOrPause(filePath)
    }

    fun seekAudio(positionMs: Int) {
        repository.playerManager.seekTo(positionMs)
    }

    fun seekToSegment(segment: TranscriptSegment) {
        repository.playerManager.seekTo(segment.timestampSeconds * 1000)
        if (!isAudioPlaying.value) {
            val path = activeMeeting.value?.audioFilePath
            repository.playerManager.playOrPause(path)
        }
    }

    fun skipAudio(deltaSeconds: Int) {
        repository.playerManager.skipBy(deltaSeconds * 1000)
    }

    fun cyclePlaybackSpeed() {
        repository.playerManager.cyclePlaybackSpeed()
    }

    fun stopAudio() {
        repository.playerManager.stop()
    }

    // --- Meeting Content Modifications ---

    fun toggleActionItem(actionItemId: String) {
        val meetingId = _activeMeetingId.value ?: return
        viewModelScope.launch {
            repository.toggleActionItemStatus(meetingId, actionItemId)
        }
    }

    fun addActionItem(title: String, assignee: String, dueDate: String, priority: String) {
        val meetingId = _activeMeetingId.value ?: return
        viewModelScope.launch {
            val item = ActionItem(
                id = UUID.randomUUID().toString(),
                title = title,
                assignee = assignee.ifBlank { "Unassigned" },
                dueDate = dueDate.ifBlank { null },
                priority = priority,
                isCompleted = false
            )
            repository.addCustomActionItem(meetingId, item)
        }
    }

    fun renameSpeaker(speakerId: String, newName: String) {
        val meetingId = _activeMeetingId.value ?: return
        viewModelScope.launch {
            repository.updateSpeakerName(meetingId, speakerId, newName)
        }
    }

    fun toggleFavorite(meetingId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(meetingId)
        }
    }

    fun deleteMeeting(meeting: MeetingEntity, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteMeeting(meeting)
            if (_activeMeetingId.value == meeting.id) {
                _activeMeetingId.value = null
                repository.playerManager.stop()
            }
            onDeleted()
        }
    }

    fun loadSampleMeeting(sample: MeetingEntity, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.addSampleMeeting(sample)
            _activeMeetingId.value = id
            onComplete(id)
        }
    }

    // --- Gemini Intelligence Actions ---

    fun sendChatMessage(userText: String) {
        val meeting = activeMeeting.value ?: return
        if (userText.isBlank()) return

        viewModelScope.launch {
            _isChatSending.value = true
            try {
                repository.sendChatMessage(meeting, userText)
            } finally {
                _isChatSending.value = false
            }
        }
    }

    fun requestDeepThinkingAnalysis() {
        val meeting = activeMeeting.value ?: return
        viewModelScope.launch {
            _isThinkingLoading.value = true
            try {
                repository.runDeepThinkingAnalysis(meeting)
            } finally {
                _isThinkingLoading.value = false
            }
        }
    }

    fun attachAndAnalyzeWhiteboard(bitmap: Bitmap, imageUriString: String) {
        val meeting = activeMeeting.value ?: return
        viewModelScope.launch {
            _isWhiteboardLoading.value = true
            try {
                repository.analyzeAndAttachWhiteboard(meeting, bitmap, imageUriString)
            } finally {
                _isWhiteboardLoading.value = false
            }
        }
    }

    fun generateFollowUpEmail(meeting: MeetingEntity, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val email = repository.geminiRepository.generateFollowUpEmail(meeting)
            onReady(email)
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.recorderManager.cancelRecording()
        repository.playerManager.stop()
    }
}
