package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DiscussionTopic
import com.example.data.model.MeetingEntity
import com.example.data.model.SpeakerStat
import com.example.data.model.TranscriptSegment
import com.example.ui.components.ActionItemCard
import com.example.ui.components.AddActionItemDialog
import com.example.ui.components.AudioPlayerBar
import com.example.ui.components.ExportIntegrationSheet
import com.example.ui.components.RenameSpeakerDialog
import com.example.ui.components.SpeakerAvatar
import com.example.ui.components.SpeakerChip
import com.example.ui.components.getSpeakerColor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.filled.FileUpload
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.SecondaryDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MeetingViewModel
import com.example.util.MeetingExportManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingDetailScreen(
    meetingId: Long,
    initialSeekTimestamp: Int = -1,
    viewModel: MeetingViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(meetingId, initialSeekTimestamp) {
        viewModel.selectMeeting(meetingId)
        if (initialSeekTimestamp >= 0) {
            viewModel.seekAudio(initialSeekTimestamp * 1000)
        }
    }

    val activeMeeting by viewModel.activeMeeting.collectAsStateWithLifecycle()
    val chatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()

    // Player State
    val isPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.audioCurrentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.audioDurationMs.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.audioPlaybackSpeed.collectAsStateWithLifecycle()

    // Loading States
    val isThinkingLoading by viewModel.isThinkingLoading.collectAsStateWithLifecycle()
    val isWhiteboardLoading by viewModel.isWhiteboardLoading.collectAsStateWithLifecycle()
    val isChatSending by viewModel.isChatSending.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Transcript", "Notes & Tasks", "Speaker Stats", "Deep Thinking", "Whiteboard", "Ask AI")

    // Dialog & Sheet States
    var showRenameDialogForSpeaker by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showAddActionDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf<String?>(null) }
    var showExportSheet by remember { mutableStateOf(false) }
    val exportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Image Picker Launcher for Whiteboard analysis
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && activeMeeting != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.attachAndAnalyzeWhiteboard(bitmap, uri.toString())
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val meeting = activeMeeting
    if (meeting == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = meeting.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${meeting.formattedDate} • ${meeting.formattedDuration}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("meeting_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Export & Integrations Sheet Button
                    IconButton(
                        onClick = { showExportSheet = true },
                        modifier = Modifier.testTag("open_export_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Export and Integrations",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Export Follow-up Email Button
                    IconButton(
                        onClick = {
                            viewModel.generateFollowUpEmail(meeting) { emailText ->
                                showEmailDialog = emailText
                            }
                        },
                        modifier = Modifier.testTag("export_email_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Generate Follow-up Email",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Toggle Favorite Button
                    IconButton(
                        onClick = { viewModel.toggleFavorite(meeting.id) },
                        modifier = Modifier.testTag("toggle_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (meeting.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (meeting.isFavorite) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Pinned Audio Player Dock
            AudioPlayerBar(
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = if (durationMs > 0) durationMs else meeting.durationSeconds * 1000,
                playbackSpeed = playbackSpeed,
                onPlayPause = { viewModel.playOrPauseMeetingAudio(meeting.audioFilePath) },
                onSeek = { viewModel.seekAudio(it) },
                onSkip = { viewModel.skipAudio(it) },
                onCycleSpeed = { viewModel.cyclePlaybackSpeed() },
                modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Tab Header
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (index) {
                                    0 -> Icon(Icons.Default.TextFields, null, modifier = Modifier.size(16.dp))
                                    1 -> Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(16.dp))
                                    2 -> Icon(Icons.Default.PieChart, null, modifier = Modifier.size(16.dp))
                                    3 -> Icon(Icons.Default.Psychology, null, modifier = Modifier.size(16.dp))
                                    4 -> Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(16.dp))
                                    5 -> Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        modifier = Modifier.testTag("detail_tab_$index")
                    )
                }
            }

            // Tab Contents
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTabIndex) {
                    0 -> TranscriptTabContent(
                        segments = meeting.transcriptSegments,
                        currentPositionMs = currentPositionMs,
                        onSeekToSegment = { seg -> viewModel.seekToSegment(seg) },
                        onRenameSpeaker = { id, name -> showRenameDialogForSpeaker = Pair(id, name) }
                    )
                    1 -> NotesAndTasksTabContent(
                        meeting = meeting,
                        onToggleAction = { actionId -> viewModel.toggleActionItem(actionId) },
                        onAddActionClick = { showAddActionDialog = true },
                        onOpenExportSheet = { showExportSheet = true },
                        onSeekToTimestamp = { sec -> viewModel.seekAudio(sec * 1000) }
                    )
                    2 -> SpeakerStatsTabContent(
                        speakerStats = meeting.speakerStats,
                        totalDurationSec = meeting.durationSeconds,
                        onRenameSpeaker = { id, name -> showRenameDialogForSpeaker = Pair(id, name) }
                    )
                    3 -> DeepThinkingTabContent(
                        deepAnalysis = meeting.deepThinkingAnalysis,
                        isLoading = isThinkingLoading,
                        onRequestAnalysis = { viewModel.requestDeepThinkingAnalysis() }
                    )
                    4 -> WhiteboardTabContent(
                        imageUri = meeting.whiteboardImageUri,
                        analysis = meeting.whiteboardAnalysis,
                        isLoading = isWhiteboardLoading,
                        onUploadClick = { photoPickerLauncher.launch("image/*") }
                    )
                    5 -> ChatTabContent(
                        messages = chatMessages,
                        isSending = isChatSending,
                        onSendMessage = { viewModel.sendChatMessage(it) }
                    )
                }
            }
        }
    }

    // Rename Speaker Dialog
    showRenameDialogForSpeaker?.let { (speakerId, currentName) ->
        RenameSpeakerDialog(
            currentSpeakerId = speakerId,
            currentSpeakerName = currentName,
            onDismiss = { showRenameDialogForSpeaker = null },
            onConfirm = { newName ->
                viewModel.renameSpeaker(speakerId, newName)
                showRenameDialogForSpeaker = null
            }
        )
    }

    // Add Action Item Dialog
    if (showAddActionDialog) {
        AddActionItemDialog(
            onDismiss = { showAddActionDialog = false },
            onConfirm = { title, assignee, dueDate, priority ->
                viewModel.addActionItem(title, assignee, dueDate, priority)
                showAddActionDialog = false
            }
        )
    }

    // Follow-up Email Dialog
    showEmailDialog?.let { emailText ->
        AlertDialog(
            onDismissRequest = { showEmailDialog = null },
            title = { Text("Executive Follow-up Email") },
            text = {
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    item {
                        Text(
                            text = emailText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Meeting Recap", emailText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Email copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showEmailDialog = null
                    },
                    modifier = Modifier.testTag("copy_email_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Email")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, emailText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Follow-up"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }
        )
    }

    // Export & Integrations Bottom Sheet
    if (showExportSheet) {
        ExportIntegrationSheet(
            meeting = meeting,
            sheetState = exportSheetState,
            onDismiss = { showExportSheet = false }
        )
    }
}

// -------------------------------------------------------------------------
// Tab 1: Transcript Tab with Diarization & Audio Timestamp Seeking
// -------------------------------------------------------------------------
@Composable
fun TranscriptTabContent(
    segments: List<TranscriptSegment>,
    currentPositionMs: Int,
    onSeekToSegment: (TranscriptSegment) -> Unit,
    onRenameSpeaker: (speakerId: String, currentName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var transcriptSearch by remember { mutableStateOf("") }
    val currentSec = currentPositionMs / 1000

    val filteredSegments = segments.filter {
        transcriptSearch.isBlank() ||
                it.text.contains(transcriptSearch, ignoreCase = true) ||
                it.speakerName.contains(transcriptSearch, ignoreCase = true)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar for transcript
        OutlinedTextField(
            value = transcriptSearch,
            onValueChange = { transcriptSearch = it },
            placeholder = { Text("Search transcript keywords...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("transcript_search_input")
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = filteredSegments, key = { it.id }) { segment ->
                val isCurrentSegment = currentSec >= segment.timestampSeconds &&
                        currentSec < (segment.timestampSeconds + 20)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentSegment) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentSegment) 3.dp else 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSeekToSegment(segment) }
                        .testTag("transcript_segment_${segment.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SpeakerChip(
                                speakerName = segment.speakerName,
                                speakerId = segment.speakerId,
                                onRenameClick = { onRenameSpeaker(segment.speakerId, segment.speakerName) }
                            )

                            // Timestamp Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.clickable { onSeekToSegment(segment) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Seek to time",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = segment.formattedTime,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = segment.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Tab 2: Notes, Executive Summary, Decisions & Action Items Checklist
// -------------------------------------------------------------------------
@Composable
fun NotesAndTasksTabContent(
    meeting: MeetingEntity,
    onToggleAction: (String) -> Unit,
    onAddActionClick: () -> Unit,
    onOpenExportSheet: () -> Unit = {},
    onSeekToTimestamp: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Export & Integrations Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Export & Sync Meeting",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Google Calendar, Outlook, Asana, Trello & Email",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onOpenExportSheet,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("notes_export_sync_button")
                    ) {
                        Text("Export")
                    }
                }
            }
        }

        // Executive Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GEMINI SUMMARY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Executive Overview",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                MeetingExportManager.copyToClipboard(
                                    context,
                                    "Executive Summary",
                                    meeting.executiveSummary
                                )
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Summary",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = meeting.executiveSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Key Decisions
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = WarningAmber.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Key Decisions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (meeting.keyDecisions.isEmpty()) {
                        Text("No explicit decisions recorded.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        meeting.keyDecisions.forEach { decision ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = decision,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Items Section Header with Add Task Button
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Action Items (${meeting.actionItems.count { it.isCompleted }}/${meeting.actionItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onAddActionClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("add_action_item_button")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task")
                }
            }
        }

        // Action Items List
        items(items = meeting.actionItems, key = { it.id }) { actionItem ->
            ActionItemCard(
                item = actionItem,
                onToggleCompleted = { onToggleAction(actionItem.id) },
                onSeekToTimestamp = onSeekToTimestamp,
                onExportToCalendar = {
                    MeetingExportManager.exportActionItemToCalendar(
                        context,
                        meeting.title,
                        actionItem
                    )
                }
            )
        }

        // Discussion Topics Breakdown
        if (meeting.discussionTopics.isNotEmpty()) {
            item {
                Text(
                    text = "Discussion Topics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(items = meeting.discussionTopics, key = { it.id }) { topic ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = topic.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = topic.timeRange,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (topic.summary.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = topic.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Tab 3: Speaker Diarization Analytics & Talk Time Percentage
// -------------------------------------------------------------------------
@Composable
fun SpeakerStatsTabContent(
    speakerStats: List<SpeakerStat>,
    totalDurationSec: Int,
    onRenameSpeaker: (speakerId: String, currentName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Speaker Participation Analytics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Diarization metrics calculated by Gemini speech models.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(items = speakerStats, key = { it.speakerId }) { stat ->
            val color = getSpeakerColor(stat.speakerId)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("speaker_stat_${stat.speakerId}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SpeakerAvatar(speakerName = stat.name, speakerId = stat.speakerId, size = 38)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${stat.wordCount} words spoken",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        TextButton(onClick = { onRenameSpeaker(stat.speakerId, stat.name) }) {
                            Text("Rename")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar for Talk Time %
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Talk Time Share",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(stat.talkPercentage * 100).toInt()}% (${stat.talkTimeSeconds / 60}m)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { stat.talkPercentage.coerceIn(0f, 1f) },
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    if (stat.keyTheme.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "💡 Main Focus: ${stat.keyTheme}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Tab 4: High Thinking Mode (Gemini 3.1 Pro with thinkingLevel = HIGH)
// -------------------------------------------------------------------------
@Composable
fun DeepThinkingTabContent(
    deepAnalysis: String?,
    isLoading: Boolean,
    onRequestAnalysis: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Gemini 3.1 Pro Deep Thinking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Strategic blindspot & risk matrix analysis",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "High Thinking mode performs an exhaustive cognitive audit of the meeting to expose unstated assumptions, dependency bottlenecks, team power dynamics, and execution risks.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onRequestAnalysis,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_deep_thinking_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reasoning with Gemini 3.1 Pro...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (deepAnalysis == null) "Run High Thinking Analysis" else "Regenerate Deep Analysis")
                        }
                    }
                }
            }
        }

        if (deepAnalysis != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = deepAnalysis,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Tab 5: Whiteboard & Photos Understanding (Gemini 3.1 Pro Vision)
// -------------------------------------------------------------------------
@Composable
fun WhiteboardTabContent(
    imageUri: String?,
    analysis: String?,
    isLoading: Boolean,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Whiteboard & Slide Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Upload diagrams, handwritten notes, or whiteboard sketches. Gemini 3.1 Pro will extract the content directly into your meeting notes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onUploadClick,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_whiteboard_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Whiteboard with Gemini 3.1 Pro...")
                        } else {
                            Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (imageUri == null) "Select Whiteboard Photo" else "Upload New Image")
                        }
                    }
                }
            }
        }

        if (imageUri != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = Uri.parse(imageUri)),
                        contentDescription = "Whiteboard Image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (analysis != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AI Extracted Insights",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = analysis,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Tab 6: Ask AI Multi-Turn Meeting Chat (Gemini 3.5 Flash)
// -------------------------------------------------------------------------
@Composable
fun ChatTabContent(
    messages: List<ChatMessageEntity>,
    isSending: Boolean,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val quickPrompts = listOf(
        "Draft a follow-up email",
        "What are the high-priority action items?",
        "List all key decisions made",
        "Who talked the most during this meeting?"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Quick Action Prompt Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            quickPrompts.take(2).forEach { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { onSendMessage(prompt) },
                    label = { Text(prompt, maxLines = 1) },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(12.dp)) },
                    modifier = Modifier.testTag("quick_prompt_chip")
                )
            }
        }

        // Messages Thread
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(items = messages, key = { it.id }) { msg ->
                val isUser = msg.sender == "user"

                Row(
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isUser) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.Top)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(0.82f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.formattedTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }

            if (isSending) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 36.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini is typing...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Input Field Dock
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Gemini about this meeting...") },
                    maxLines = 3,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val text = inputText.trim()
                            inputText = ""
                            onSendMessage(text)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
