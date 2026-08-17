package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val audioFilePath: String? = null,
    val rawTranscript: String = "",
    val transcriptSegments: List<TranscriptSegment> = emptyList(),
    val executiveSummary: String = "",
    val keyDecisions: List<String> = emptyList(),
    val actionItems: List<ActionItem> = emptyList(),
    val discussionTopics: List<DiscussionTopic> = emptyList(),
    val speakerStats: List<SpeakerStat> = emptyList(),
    val deepThinkingAnalysis: String? = null,
    val whiteboardAnalysis: String? = null,
    val whiteboardImageUri: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val language: String = "en-US",
    val sentiment: String = "Positive & Productive"
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            return sdf.format(Date(createdAt))
        }

    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return if (minutes >= 60) {
                val hours = minutes / 60
                val remMinutes = minutes % 60
                String.format(Locale.getDefault(), "%dh %02dm %02ds", hours, remMinutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }
}

@JsonClass(generateAdapter = true)
data class TranscriptSegment(
    val id: String = UUID.randomUUID().toString(),
    val speakerId: String, // e.g. "Speaker 1"
    val speakerName: String, // e.g. "Sarah Jenkins" or "Speaker 1"
    val timestampSeconds: Int, // e.g. 15
    val formattedTime: String, // e.g. "00:15"
    val text: String,
    val sentiment: String = "Neutral" // "Positive", "Neutral", "Concern", "Decision"
)

@JsonClass(generateAdapter = true)
data class ActionItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val assignee: String,
    val dueDate: String? = null,
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false,
    val timestampSeconds: Int? = null,
    val formattedTime: String? = null
)

@JsonClass(generateAdapter = true)
data class DiscussionTopic(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val timeRange: String, // e.g. "00:00 - 04:30"
    val summary: String,
    val keyPoints: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SpeakerStat(
    val speakerId: String,
    val name: String,
    val talkTimeSeconds: Int,
    val talkPercentage: Float, // 0.0 - 1.0 (e.g. 0.45 for 45%)
    val wordCount: Int,
    val keyTheme: String
)

object MeetingCategories {
    const val STRATEGY = "Strategy & Planning"
    const val ENGINEERING = "Engineering Sprint"
    const val PRODUCT = "Product & Design"
    const val ONE_ON_ONE = "1-on-1 Sync"
    const val CLIENT = "Client Discovery"
    const val ALL_HANDS = "All-Hands / Town Hall"
    const val BRAINSTORMING = "Brainstorming"
    const val INTERVIEW = "Interview / Hiring"

    val all = listOf(
        STRATEGY,
        ENGINEERING,
        PRODUCT,
        ONE_ON_ONE,
        CLIENT,
        ALL_HANDS,
        BRAINSTORMING,
        INTERVIEW
    )
}
