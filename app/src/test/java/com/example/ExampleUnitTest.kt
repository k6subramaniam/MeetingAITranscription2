package com.example

import com.example.data.local.Converters
import com.example.data.model.ActionItem
import com.example.data.model.DiscussionTopic
import com.example.data.model.SampleMeetings
import com.example.data.model.SpeakerStat
import com.example.data.model.TranscriptSegment
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSampleMeetings_areValid() {
        val samples = SampleMeetings.getInitialSamples()
        assertTrue(samples.isNotEmpty())
        val first = samples.first()
        assertTrue(first.title.isNotEmpty())
        assertTrue(first.transcriptSegments.isNotEmpty())
        assertTrue(first.actionItems.isNotEmpty())
        assertTrue(first.speakerStats.isNotEmpty())
    }

    @Test
    fun testRoomConverters_serialization() {
        val converters = Converters()

        val segments = listOf(
            TranscriptSegment(
                id = "seg-1",
                speakerId = "Speaker 1",
                speakerName = "Alex",
                timestampSeconds = 15,
                formattedTime = "00:15",
                text = "Testing audio transcription segment.",
                sentiment = "Positive"
            )
        )
        val segJson = converters.fromTranscriptSegments(segments)
        val parsedSegs = converters.toTranscriptSegments(segJson)
        assertEquals(1, parsedSegs.size)
        assertEquals("seg-1", parsedSegs[0].id)
        assertEquals("Alex", parsedSegs[0].speakerName)

        val actions = listOf(
            ActionItem(
                id = "act-1",
                title = "Deploy AI backend",
                assignee = "Sarah",
                dueDate = "Friday",
                priority = "HIGH",
                isCompleted = false
            )
        )
        val actJson = converters.fromActionItems(actions)
        val parsedActions = converters.toActionItems(actJson)
        assertEquals(1, parsedActions.size)
        assertEquals("Deploy AI backend", parsedActions[0].title)
    }
}

