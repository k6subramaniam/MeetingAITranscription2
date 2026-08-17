package com.example.util

import com.example.data.model.ActionItem
import com.example.data.model.TranscriptSegment
import java.util.UUID

/**
 * Utility for parsing action items, task assignments, deadlines,
 * and speaker attribution directly from diarized transcript segments.
 */
object ActionItemExtractor {

    private val ACTION_VERBS = listOf(
        "will complete", "will finalize", "will draft", "will send",
        "will update", "will create", "will test", "will benchmark",
        "will build", "will review", "will lead", "will follow up",
        "action item", "todo", "need to", "needs to", "responsible for",
        "take ownership of", "assigned to", "please ensure", "make sure"
    )

    private val DUE_DATE_REGEX = Regex(
        "(by|due|before|next|this)\\s+(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|EOD|end of week|tomorrow|next week|Q[1-4]|September|October|November|December|January|February|March|April|May|June|July|August|\\d{1,2}(st|nd|rd|th)?)",
        RegexOption.IGNORE_CASE
    )

    /**
     * Scans transcript segments to extract action items with recognized speakers and timestamps.
     */
    fun extractFromSegments(segments: List<TranscriptSegment>): List<ActionItem> {
        val extractedItems = mutableListOf<ActionItem>()

        for (segment in segments) {
            val text = segment.text
            val lower = text.lowercase()

            val hasActionTrigger = ACTION_VERBS.any { lower.contains(it) }

            if (hasActionTrigger) {
                val sentences = text.split(Regex("[.!?]\\s+"))
                for (sentence in sentences) {
                    val sentLower = sentence.lowercase()
                    if (ACTION_VERBS.any { sentLower.contains(it) } && sentence.trim().length > 10) {
                        // Determine Priority
                        val priority = when {
                            sentLower.contains("urgent") || sentLower.contains("critical") || sentLower.contains("high priority") || sentLower.contains("asap") -> "HIGH"
                            sentLower.contains("low priority") || sentLower.contains("when possible") || sentLower.contains("eventually") -> "LOW"
                            else -> "MEDIUM"
                        }

                        // Determine Due Date
                        val dueMatch = DUE_DATE_REGEX.find(sentence)
                        val dueDate = dueMatch?.value?.trim()

                        // Determine Assignee (default to speaker, or check for named participant in sentence)
                        var assignee = segment.speakerName
                        for (otherSeg in segments) {
                            val candidate = otherSeg.speakerName.split(" ").firstOrNull() ?: ""
                            if (candidate.length > 2 && sentence.contains(candidate, ignoreCase = true)) {
                                assignee = otherSeg.speakerName
                                break
                            }
                        }

                        // Clean title
                        var cleanTitle = sentence.trim()
                            .replace(Regex("^(Action items?:?|Todo:?|Note:?)\\s*", RegexOption.IGNORE_CASE), "")
                            .trim()

                        if (cleanTitle.endsWith(".")) {
                            cleanTitle = cleanTitle.substring(0, cleanTitle.length - 1)
                        }

                        if (cleanTitle.isNotBlank() && extractedItems.none { it.title.equals(cleanTitle, ignoreCase = true) }) {
                            extractedItems.add(
                                ActionItem(
                                    id = UUID.randomUUID().toString(),
                                    title = cleanTitle,
                                    assignee = assignee,
                                    dueDate = dueDate,
                                    priority = priority,
                                    isCompleted = false,
                                    timestampSeconds = segment.timestampSeconds,
                                    formattedTime = segment.formattedTime
                                )
                            )
                        }
                    }
                }
            }
        }

        return extractedItems
    }
}
