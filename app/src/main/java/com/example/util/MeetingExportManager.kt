package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ActionItem
import com.example.data.model.MeetingEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for exporting meeting summaries, action items, and transcripts
 * to Calendar apps (Google Calendar, Outlook), Task Managers (Asana, Trello),
 * Email clients, and Markdown documents.
 */
object MeetingExportManager {

    /**
     * Creates a Google Calendar / System Calendar event for the meeting recap and follow-up.
     */
    fun exportMeetingToGoogleCalendar(context: Context, meeting: MeetingEntity) {
        try {
            val descriptionBuilder = StringBuilder()
            descriptionBuilder.append("📋 MEETING RECAP: ${meeting.title}\n\n")

            if (meeting.executiveSummary.isNotBlank()) {
                descriptionBuilder.append("🎯 Executive Summary:\n")
                descriptionBuilder.append(meeting.executiveSummary).append("\n\n")
            }

            if (meeting.keyDecisions.isNotEmpty()) {
                descriptionBuilder.append("✅ Key Decisions:\n")
                meeting.keyDecisions.forEach { decision ->
                    descriptionBuilder.append("• ").append(decision).append("\n")
                }
                descriptionBuilder.append("\n")
            }

            if (meeting.actionItems.isNotEmpty()) {
                descriptionBuilder.append("📌 Action Items:\n")
                meeting.actionItems.forEach { item ->
                    val due = if (!item.dueDate.isNullOrBlank()) " (Due: ${item.dueDate})" else ""
                    val time = if (!item.formattedTime.isNullOrBlank()) " [at ${item.formattedTime}]" else ""
                    descriptionBuilder.append("• [${item.priority}] ${item.title} -> ${item.assignee}$due$time\n")
                }
                descriptionBuilder.append("\n")
            }

            val startTime = meeting.createdAt
            val endTime = meeting.createdAt + (meeting.durationSeconds.coerceAtLeast(1800) * 1000L)

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "Follow-up: ${meeting.title}")
                putExtra(CalendarContract.Events.DESCRIPTION, descriptionBuilder.toString())
                putExtra(CalendarContract.Events.EVENT_LOCATION, "Meeting AI Workspace")
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(Intent.createChooser(intent, "Add Meeting to Calendar"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Calendar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Exports an individual action item as a calendar reminder / task event.
     */
    fun exportActionItemToCalendar(context: Context, meetingTitle: String, item: ActionItem) {
        try {
            val startTime = System.currentTimeMillis() + (24 * 3600 * 1000L) // Default tomorrow
            val endTime = startTime + (30 * 60 * 1000L)

            val desc = buildString {
                append("Action Item from meeting: $meetingTitle\n\n")
                append("Task: ${item.title}\n")
                append("Assignee: ${item.assignee}\n")
                append("Priority: ${item.priority}\n")
                if (!item.dueDate.isNullOrBlank()) {
                    append("Due: ${item.dueDate}\n")
                }
                if (!item.formattedTime.isNullOrBlank()) {
                    append("Discussed at: ${item.formattedTime} in transcript\n")
                }
            }

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "[Action Item] ${item.title}")
                putExtra(CalendarContract.Events.DESCRIPTION, desc)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(Intent.createChooser(intent, "Set Action Item Reminder"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not schedule reminder: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generates an iCalendar (.ics) file and opens standard calendar / share intents (Outlook, Apple, etc.).
     */
    fun exportMeetingToICS(context: Context, meeting: MeetingEntity) {
        try {
            val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
            val startTimeStr = sdf.format(Date(meeting.createdAt))
            val endTimeStr = sdf.format(Date(meeting.createdAt + (meeting.durationSeconds * 1000L)))

            val icsContent = buildString {
                append("BEGIN:VCALENDAR\n")
                append("VERSION:2.0\n")
                append("PRODID:-//Meeting AI//Smart Transcriber//EN\n")
                append("CALSCALE:GREGORIAN\n")
                append("BEGIN:VEVENT\n")
                append("UID:${meeting.id}-${System.currentTimeMillis()}@meetingai.local\n")
                append("DTSTAMP:$startTimeStr\n")
                append("DTSTART:$startTimeStr\n")
                append("DTEND:$endTimeStr\n")
                append("SUMMARY:${escapeIcs(meeting.title)}\n")
                append("DESCRIPTION:${escapeIcs(meeting.executiveSummary + "\\n\\nKey Decisions:\\n" + meeting.keyDecisions.joinToString("\\n• "))}\n")
                append("STATUS:CONFIRMED\n")
                append("END:VEVENT\n")
                append("END:VCALENDAR\n")
            }

            val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(cacheDir, "meeting_${meeting.id}.ics")
            FileWriter(file).use { it.write(icsContent) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/calendar"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Calendar Event: ${meeting.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Open with Calendar / Outlook"))
        } catch (e: Exception) {
            Toast.makeText(context, "ICS export failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Exports action items formatted for Asana task creation.
     */
    fun exportToAsana(context: Context, meeting: MeetingEntity) {
        val asanaText = buildString {
            append("📋 [Asana Tasks] ${meeting.title}\n\n")
            append("Meeting Date: ${meeting.formattedDate}\n")
            append("Summary: ${meeting.executiveSummary}\n\n")
            append("TASKS TO IMPORT:\n")
            meeting.actionItems.forEachIndexed { index, item ->
                val due = if (!item.dueDate.isNullOrBlank()) " | Due: ${item.dueDate}" else ""
                val priority = " [${item.priority}]"
                append("${index + 1}. [ ] ${item.title}$priority\n")
                append("   Assignee: @${item.assignee}$due\n")
                if (!item.formattedTime.isNullOrBlank()) {
                    append("   Context: Discussed at ${item.formattedTime}\n")
                }
                append("\n")
            }
        }

        shareTextWithApp(context, asanaText, "Export Action Items to Asana", "com.asana.app")
    }

    /**
     * Exports action items formatted for Trello card & checklist creation.
     */
    fun exportToTrello(context: Context, meeting: MeetingEntity) {
        val trelloText = buildString {
            append("📌 [Trello Card] ${meeting.title}\n\n")
            append("**Meeting Summary:**\n${meeting.executiveSummary}\n\n")
            append("**Checklist (Action Items):**\n")
            meeting.actionItems.forEach { item ->
                val due = if (!item.dueDate.isNullOrBlank()) " (${item.dueDate})" else ""
                append("- [ ] ${item.title} - ${item.assignee}$due [${item.priority}]\n")
            }
            if (meeting.keyDecisions.isNotEmpty()) {
                append("\n**Key Decisions:**\n")
                meeting.keyDecisions.forEach { append("- $it\n") }
            }
        }

        shareTextWithApp(context, trelloText, "Export Checklist to Trello", "com.trello")
    }

    /**
     * Generates a pre-formatted Executive Follow-up Email draft.
     */
    fun composeFollowUpEmail(context: Context, meeting: MeetingEntity) {
        val emailBody = buildString {
            append("Hi team,\n\n")
            append("Here is the executive summary and action item recap from our meeting on ${meeting.formattedDate} regarding \"${meeting.title}\".\n\n")

            if (meeting.executiveSummary.isNotBlank()) {
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                append("🎯 EXECUTIVE SUMMARY\n")
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                append(meeting.executiveSummary).append("\n\n")
            }

            if (meeting.keyDecisions.isNotEmpty()) {
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                append("✅ KEY DECISIONS\n")
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                meeting.keyDecisions.forEach { decision ->
                    append("• ").append(decision).append("\n")
                }
                append("\n")
            }

            if (meeting.actionItems.isNotEmpty()) {
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                append("📌 ACTION ITEMS & ASSIGNMENTS\n")
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                meeting.actionItems.forEach { item ->
                    val status = if (item.isCompleted) "[COMPLETED]" else "[PENDING]"
                    val due = if (!item.dueDate.isNullOrBlank()) " | Due: ${item.dueDate}" else ""
                    val time = if (!item.formattedTime.isNullOrBlank()) " | Audio timestamp: ${item.formattedTime}" else ""
                    append("• $status ${item.title}\n")
                    append("  👤 Assignee: ${item.assignee} (Priority: ${item.priority})$due$time\n\n")
                }
            }

            append("Generated automatically by Meeting AI.")
        }

        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_SUBJECT, "Meeting Recap & Action Items: ${meeting.title}")
                putExtra(Intent.EXTRA_TEXT, emailBody)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(emailIntent, "Send Meeting Follow-up Email"))
        } catch (e: Exception) {
            // Fallback to generic share
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Meeting Recap: ${meeting.title}")
                putExtra(Intent.EXTRA_TEXT, emailBody)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(genericIntent, "Share Meeting Recap"))
        }
    }

    /**
     * Exports full meeting notes, transcript, and action items as a formatted Markdown (.md) file.
     */
    fun exportMeetingToMarkdown(context: Context, meeting: MeetingEntity) {
        val markdown = buildString {
            append("# ${meeting.title}\n\n")
            append("**Date:** ${meeting.formattedDate}  \n")
            append("**Duration:** ${meeting.formattedDuration}  \n")
            append("**Category:** ${meeting.category}  \n")
            append("**Sentiment:** ${meeting.sentiment}  \n\n")
            append("---\n\n")

            append("## 🎯 Executive Summary\n\n")
            append("${meeting.executiveSummary}\n\n")

            if (meeting.keyDecisions.isNotEmpty()) {
                append("## ✅ Key Decisions\n\n")
                meeting.keyDecisions.forEach { decision ->
                    append("- $decision\n")
                }
                append("\n")
            }

            if (meeting.actionItems.isNotEmpty()) {
                append("## 📌 Action Items\n\n")
                append("| Priority | Task | Assignee | Due Date | Timestamp |\n")
                append("|---|---|---|---|---|\n")
                meeting.actionItems.forEach { item ->
                    val time = item.formattedTime ?: "—"
                    val due = item.dueDate ?: "—"
                    val done = if (item.isCompleted) "~~${item.title}~~ (Done)" else item.title
                    append("| ${item.priority} | $done | ${item.assignee} | $due | $time |\n")
                }
                append("\n")
            }

            if (meeting.discussionTopics.isNotEmpty()) {
                append("## 💡 Discussion Topics\n\n")
                meeting.discussionTopics.forEach { topic ->
                    append("### ${topic.title} (${topic.timeRange})\n")
                    append("${topic.summary}\n\n")
                    if (topic.keyPoints.isNotEmpty()) {
                        topic.keyPoints.forEach { point ->
                            append("- $point\n")
                        }
                        append("\n")
                    }
                }
            }

            if (meeting.speakerStats.isNotEmpty()) {
                append("## 👥 Speaker Breakdown\n\n")
                meeting.speakerStats.forEach { stat ->
                    val pct = String.format(Locale.US, "%.1f%%", stat.talkPercentage * 100)
                    append("- **${stat.name}**: $pct of speaking time (${stat.wordCount} words) — *Focus: ${stat.keyTheme}*\n")
                }
                append("\n")
            }

            if (meeting.transcriptSegments.isNotEmpty()) {
                append("## 🎙️ Diarized Transcript\n\n")
                meeting.transcriptSegments.forEach { seg ->
                    append("**[${seg.formattedTime}] ${seg.speakerName}:** ${seg.text}\n\n")
                }
            }
        }

        try {
            val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanTitle = meeting.title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
            val file = File(cacheDir, "${cleanTitle}_notes.md")
            FileWriter(file).use { it.write(markdown) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/markdown"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Markdown Notes: ${meeting.title}")
                putExtra(Intent.EXTRA_TEXT, markdown)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Markdown Document"))
        } catch (e: Exception) {
            Toast.makeText(context, "Markdown export failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies text to system clipboard with a confirmation Toast.
     */
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareTextWithApp(context: Context, text: String, title: String, preferredPackage: String) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val chooser = Intent.createChooser(sendIntent, title)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Sharing failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeIcs(str: String): String {
        return str
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
    }
}
