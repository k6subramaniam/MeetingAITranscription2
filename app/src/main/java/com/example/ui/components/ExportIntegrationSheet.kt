package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MeetingEntity
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoBlueLight
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberLight
import com.example.util.MeetingExportManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportIntegrationSheet(
    meeting: MeetingEntity,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onOpenEmailDraft: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = "Export & Integrations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Sync summary, action items & calendar events for \"${meeting.title}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
            )

            // Section 1: Calendar Integrations
            IntegrationSectionHeader(title = "Calendar Apps")
            Spacer(modifier = Modifier.height(8.dp))

            IntegrationOptionCard(
                icon = Icons.Default.CalendarMonth,
                iconTint = PrimaryDark,
                iconBg = MaterialTheme.colorScheme.primaryContainer,
                title = "Google Calendar",
                subtitle = "Create event with summary, decisions & action items",
                tag = "export_google_calendar_button",
                onClick = {
                    MeetingExportManager.exportMeetingToGoogleCalendar(context, meeting)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            IntegrationOptionCard(
                icon = Icons.Default.CalendarToday,
                iconTint = InfoBlue,
                iconBg = InfoBlueLight,
                title = "Outlook / iCalendar (.ics)",
                subtitle = "Export standard .ics file for Outlook, Apple Calendar, etc.",
                tag = "export_outlook_calendar_button",
                onClick = {
                    MeetingExportManager.exportMeetingToICS(context, meeting)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Task Management Tools
            IntegrationSectionHeader(title = "Task Management")
            Spacer(modifier = Modifier.height(8.dp))

            IntegrationOptionCard(
                icon = Icons.Default.FormatListNumbered,
                iconTint = RecordingRed,
                iconBg = RecordingRed.copy(alpha = 0.12f),
                title = "Export to Asana",
                subtitle = "Format ${meeting.actionItems.size} action items with assignees & due dates",
                tag = "export_asana_button",
                onClick = {
                    MeetingExportManager.exportToAsana(context, meeting)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            IntegrationOptionCard(
                icon = Icons.Default.ViewKanban,
                iconTint = InfoBlue,
                iconBg = InfoBlueLight,
                title = "Export to Trello",
                subtitle = "Create card checklist with assigned tasks & decisions",
                tag = "export_trello_button",
                onClick = {
                    MeetingExportManager.exportToTrello(context, meeting)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Summary & Documentation
            IntegrationSectionHeader(title = "Recap & Documents")
            Spacer(modifier = Modifier.height(8.dp))

            IntegrationOptionCard(
                icon = Icons.Default.Email,
                iconTint = WarningAmber,
                iconBg = WarningAmberLight,
                title = "Executive Follow-up Email",
                subtitle = "Draft formatted email recap with assigned next steps",
                tag = "export_email_button",
                onClick = {
                    MeetingExportManager.composeFollowUpEmail(context, meeting)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            IntegrationOptionCard(
                icon = Icons.Default.Description,
                iconTint = SuccessGreen,
                iconBg = SuccessGreen.copy(alpha = 0.15f),
                title = "Markdown Document (.md)",
                subtitle = "Export full meeting notes, transcript table & speaker stats",
                tag = "export_markdown_button",
                onClick = {
                    MeetingExportManager.exportMeetingToMarkdown(context, meeting)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            IntegrationOptionCard(
                icon = Icons.Default.ContentCopy,
                iconTint = MaterialTheme.colorScheme.primary,
                iconBg = MaterialTheme.colorScheme.primaryContainer,
                title = "Copy Executive Summary",
                subtitle = "Copy summary text to clipboard for quick pasting",
                tag = "copy_summary_clipboard_button",
                onClick = {
                    MeetingExportManager.copyToClipboard(
                        context,
                        "Executive Summary",
                        meeting.executiveSummary
                    )
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun IntegrationSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun IntegrationOptionCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.Launch,
                contentDescription = "Export action",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
