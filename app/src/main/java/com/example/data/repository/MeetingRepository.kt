package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.audio.AudioPlayerManager
import com.example.audio.AudioRecorderManager
import com.example.data.local.AppDatabase
import com.example.data.local.ChatDao
import com.example.data.local.MeetingDao
import com.example.data.model.ActionItem
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.SampleMeetings
import com.example.data.remote.GeminiRepository
import kotlinx.coroutines.flow.Flow
import java.io.File

class MeetingRepository(
    private val context: Context,
    private val meetingDao: MeetingDao = AppDatabase.getDatabase(context).meetingDao(),
    private val chatDao: ChatDao = AppDatabase.getDatabase(context).chatDao(),
    val geminiRepository: GeminiRepository = GeminiRepository(),
    val recorderManager: AudioRecorderManager = AudioRecorderManager(context),
    val playerManager: AudioPlayerManager = AudioPlayerManager(context)
) {
    val allMeetings: Flow<List<MeetingEntity>> = meetingDao.getAllMeetings()
    val favoriteMeetings: Flow<List<MeetingEntity>> = meetingDao.getFavoriteMeetings()

    fun getMeetingById(id: Long): Flow<MeetingEntity?> = meetingDao.getMeetingById(id)

    fun searchMeetings(query: String): Flow<List<MeetingEntity>> = meetingDao.searchMeetings(query)

    fun getMeetingsByCategory(category: String): Flow<List<MeetingEntity>> = meetingDao.getMeetingsByCategory(category)

    fun getChatMessages(meetingId: Long): Flow<List<ChatMessageEntity>> = chatDao.getMessagesForMeeting(meetingId)

    suspend fun checkAndSeedInitialData() {
        if (meetingDao.getMeetingCount() == 0) {
            val samples = SampleMeetings.getInitialSamples()
            meetingDao.insertMeetings(samples)
            
            // Seed initial welcome message for chat
            samples.forEach { sample ->
                chatDao.insertMessage(
                    ChatMessageEntity(
                        meetingId = sample.id,
                        sender = "gemini",
                        content = "Hello! I am your AI Meeting Assistant for **${sample.title}**. You can ask me anything about the discussion, decisions, action items, or have me draft a follow-up email."
                    )
                )
            }
        }
    }

    suspend fun saveMeeting(meeting: MeetingEntity): Long {
        return meetingDao.insertMeeting(meeting)
    }

    suspend fun updateMeeting(meeting: MeetingEntity) {
        meetingDao.updateMeeting(meeting)
    }

    suspend fun deleteMeeting(meeting: MeetingEntity) {
        if (!meeting.audioFilePath.isNullOrEmpty()) {
            try {
                File(meeting.audioFilePath).delete()
            } catch (e: Exception) {
                // Ignore
            }
        }
        chatDao.deleteMessagesForMeeting(meeting.id)
        meetingDao.deleteMeeting(meeting)
    }

    suspend fun toggleFavorite(id: Long) {
        meetingDao.toggleFavorite(id)
    }

    suspend fun toggleActionItemStatus(meetingId: Long, actionItemId: String) {
        val meeting = meetingDao.getMeetingByIdDirect(meetingId) ?: return
        val updatedActions = meeting.actionItems.map { item ->
            if (item.id == actionItemId) {
                item.copy(isCompleted = !item.isCompleted)
            } else {
                item
            }
        }
        meetingDao.updateMeeting(meeting.copy(actionItems = updatedActions))
    }

    suspend fun addCustomActionItem(meetingId: Long, actionItem: ActionItem) {
        val meeting = meetingDao.getMeetingByIdDirect(meetingId) ?: return
        val updatedActions = meeting.actionItems + actionItem
        meetingDao.updateMeeting(meeting.copy(actionItems = updatedActions))
    }

    suspend fun updateSpeakerName(meetingId: Long, speakerId: String, newName: String) {
        val meeting = meetingDao.getMeetingByIdDirect(meetingId) ?: return
        val updatedSegments = meeting.transcriptSegments.map { seg ->
            if (seg.speakerId == speakerId || seg.speakerName == speakerId) {
                seg.copy(speakerName = newName)
            } else {
                seg
            }
        }
        val updatedStats = meeting.speakerStats.map { stat ->
            if (stat.speakerId == speakerId || stat.name == speakerId) {
                stat.copy(name = newName)
            } else {
                stat
            }
        }
        val updatedRaw = updatedSegments.joinToString("\n\n") { "[${it.formattedTime}] ${it.speakerName}: ${it.text}" }
        meetingDao.updateMeeting(
            meeting.copy(
                transcriptSegments = updatedSegments,
                speakerStats = updatedStats,
                rawTranscript = updatedRaw
            )
        )
    }

    suspend fun sendChatMessage(meeting: MeetingEntity, userText: String): String {
        // Save user message
        chatDao.insertMessage(
            ChatMessageEntity(
                meetingId = meeting.id,
                sender = "user",
                content = userText
            )
        )

        // Get past messages for context
        val history = mutableListOf<ChatMessageEntity>()
        // Generate AI response
        val aiResponse = geminiRepository.chatWithMeeting(meeting, history, userText)

        // Save AI message
        chatDao.insertMessage(
            ChatMessageEntity(
                meetingId = meeting.id,
                sender = "gemini",
                content = aiResponse
            )
        )

        return aiResponse
    }

    suspend fun runDeepThinkingAnalysis(meeting: MeetingEntity): String {
        val analysis = geminiRepository.generateDeepThinkingAnalysis(meeting)
        meetingDao.updateMeeting(meeting.copy(deepThinkingAnalysis = analysis))
        return analysis
    }

    suspend fun analyzeAndAttachWhiteboard(meeting: MeetingEntity, bitmap: Bitmap, imageUri: String): String {
        val analysis = geminiRepository.analyzeWhiteboardPhoto(
            bitmap,
            "Meeting Title: ${meeting.title}, Category: ${meeting.category}, Summary: ${meeting.executiveSummary}"
        )
        meetingDao.updateMeeting(
            meeting.copy(
                whiteboardAnalysis = analysis,
                whiteboardImageUri = imageUri
            )
        )
        return analysis
    }

    suspend fun addSampleMeeting(sample: MeetingEntity): Long {
        val newId = meetingDao.insertMeeting(sample.copy(id = 0, createdAt = System.currentTimeMillis()))
        chatDao.insertMessage(
            ChatMessageEntity(
                meetingId = newId,
                sender = "gemini",
                content = "I've loaded the transcript and notes for **${sample.title}**. Feel free to ask any questions!"
            )
        )
        return newId
    }
}
