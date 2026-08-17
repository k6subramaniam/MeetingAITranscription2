package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MeetingEntity
import com.example.ui.components.SpeakerAvatar
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MeetingViewModel

enum class SearchCategoryFilter {
    ALL, TRANSCRIPTS, ACTION_ITEMS, KEY_DECISIONS, TOPICS
}

data class SearchMatchResult(
    val meetingId: Long,
    val meetingTitle: String,
    val meetingCategory: String,
    val meetingDate: String,
    val resultType: SearchCategoryFilter,
    val speakerName: String? = null,
    val timestampSeconds: Int = 0,
    val formattedTime: String? = null,
    val matchedSnippet: String,
    val secondarySnippet: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMeetingsScreen(
    viewModel: MeetingViewModel,
    initialQuery: String = "",
    onNavigateBack: () -> Unit,
    onNavigateToMeeting: (meetingId: Long, timestampSeconds: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedFilter by remember { mutableStateOf(SearchCategoryFilter.ALL) }
    val allMeetings by viewModel.allMeetings.collectAsStateWithLifecycle()

    // Process Search Results Across All Meetings
    val results = remember(searchQuery, selectedFilter, allMeetings) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) return@remember emptyList<SearchMatchResult>()

        val matches = mutableListOf<SearchMatchResult>()

        for (meeting in allMeetings) {
            // 1. Search Diarized Transcript Segments
            if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.TRANSCRIPTS) {
                meeting.transcriptSegments.forEach { seg ->
                    if (seg.text.lowercase().contains(query) || seg.speakerName.lowercase().contains(query)) {
                        matches.add(
                            SearchMatchResult(
                                meetingId = meeting.id,
                                meetingTitle = meeting.title,
                                meetingCategory = meeting.category,
                                meetingDate = meeting.formattedDate,
                                resultType = SearchCategoryFilter.TRANSCRIPTS,
                                speakerName = seg.speakerName,
                                timestampSeconds = seg.timestampSeconds,
                                formattedTime = seg.formattedTime,
                                matchedSnippet = seg.text,
                                secondarySnippet = "Spoken by ${seg.speakerName}"
                            )
                        )
                    }
                }
            }

            // 2. Search Action Items
            if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.ACTION_ITEMS) {
                meeting.actionItems.forEach { item ->
                    if (item.title.lowercase().contains(query) || item.assignee.lowercase().contains(query)) {
                        matches.add(
                            SearchMatchResult(
                                meetingId = meeting.id,
                                meetingTitle = meeting.title,
                                meetingCategory = meeting.category,
                                meetingDate = meeting.formattedDate,
                                resultType = SearchCategoryFilter.ACTION_ITEMS,
                                speakerName = item.assignee,
                                timestampSeconds = item.timestampSeconds ?: 0,
                                formattedTime = item.formattedTime,
                                matchedSnippet = item.title,
                                secondarySnippet = "Assigned to ${item.assignee} • Priority: ${item.priority}"
                            )
                        )
                    }
                }
            }

            // 3. Search Key Decisions
            if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.KEY_DECISIONS) {
                meeting.keyDecisions.forEach { decision ->
                    if (decision.lowercase().contains(query)) {
                        matches.add(
                            SearchMatchResult(
                                meetingId = meeting.id,
                                meetingTitle = meeting.title,
                                meetingCategory = meeting.category,
                                meetingDate = meeting.formattedDate,
                                resultType = SearchCategoryFilter.KEY_DECISIONS,
                                timestampSeconds = 0,
                                matchedSnippet = decision,
                                secondarySnippet = "Key Agreed Decision"
                            )
                        )
                    }
                }
            }

            // 4. Search Discussion Topics
            if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.TOPICS) {
                meeting.discussionTopics.forEach { topic ->
                    if (topic.title.lowercase().contains(query) || topic.summary.lowercase().contains(query)) {
                        matches.add(
                            SearchMatchResult(
                                meetingId = meeting.id,
                                meetingTitle = meeting.title,
                                meetingCategory = meeting.category,
                                meetingDate = meeting.formattedDate,
                                resultType = SearchCategoryFilter.TOPICS,
                                timestampSeconds = 0,
                                formattedTime = topic.timeRange,
                                matchedSnippet = topic.title,
                                secondarySnippet = topic.summary
                            )
                        )
                    }
                }
            }

            // 5. Search Executive Summary
            if (selectedFilter == SearchCategoryFilter.ALL) {
                if (meeting.executiveSummary.lowercase().contains(query)) {
                    matches.add(
                        SearchMatchResult(
                            meetingId = meeting.id,
                            meetingTitle = meeting.title,
                            meetingCategory = meeting.category,
                            meetingDate = meeting.formattedDate,
                            resultType = SearchCategoryFilter.ALL,
                            timestampSeconds = 0,
                            matchedSnippet = meeting.executiveSummary,
                            secondarySnippet = "Executive Summary"
                        )
                    )
                }
            }
        }

        matches
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search All Meetings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("search_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search keywords, speakers, action items...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("universal_search_field")
                )
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchFilterChip(
                    label = "All Results",
                    selected = selectedFilter == SearchCategoryFilter.ALL,
                    onClick = { selectedFilter = SearchCategoryFilter.ALL }
                )
                SearchFilterChip(
                    label = "Transcripts",
                    selected = selectedFilter == SearchCategoryFilter.TRANSCRIPTS,
                    onClick = { selectedFilter = SearchCategoryFilter.TRANSCRIPTS }
                )
                SearchFilterChip(
                    label = "Action Items",
                    selected = selectedFilter == SearchCategoryFilter.ACTION_ITEMS,
                    onClick = { selectedFilter = SearchCategoryFilter.ACTION_ITEMS }
                )
                SearchFilterChip(
                    label = "Key Decisions",
                    selected = selectedFilter == SearchCategoryFilter.KEY_DECISIONS,
                    onClick = { selectedFilter = SearchCategoryFilter.KEY_DECISIONS }
                )
                SearchFilterChip(
                    label = "Topics",
                    selected = selectedFilter == SearchCategoryFilter.TOPICS,
                    onClick = { selectedFilter = SearchCategoryFilter.TOPICS }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            if (searchQuery.isBlank()) {
                // Empty state with search prompts
                SearchEmptyState(
                    onSelectSuggestedQuery = { suggested ->
                        searchQuery = suggested
                    }
                )
            } else if (results.isEmpty()) {
                // No results state
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No matches found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No transcript segments, action items, or decisions matched \"$searchQuery\". Try checking the spelling or broader search terms.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                // Results List
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${results.size} MATCH${if (results.size > 1) "ES" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(results) { item ->
                        SearchResultCard(
                            result = item,
                            searchQuery = searchQuery,
                            onClick = {
                                onNavigateToMeeting(item.meetingId, item.timestampSeconds)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun SearchResultCard(
    result: SearchMatchResult,
    searchQuery: String,
    onClick: () -> Unit
) {
    val (typeLabel, typeColor, typeIcon) = when (result.resultType) {
        SearchCategoryFilter.TRANSCRIPTS -> Triple("TRANSCRIPT", PrimaryDark, Icons.Default.RecordVoiceOver)
        SearchCategoryFilter.ACTION_ITEMS -> Triple("ACTION ITEM", RecordingRed, Icons.Default.FormatListNumbered)
        SearchCategoryFilter.KEY_DECISIONS -> Triple("DECISION", SuccessGreen, Icons.Default.EventNote)
        SearchCategoryFilter.TOPICS -> Triple("TOPIC", WarningAmber, Icons.Default.Topic)
        SearchCategoryFilter.ALL -> Triple("SUMMARY", InfoBlue, Icons.Default.Lightbulb)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("search_result_item_${result.meetingId}_${result.timestampSeconds}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Type Badge + Meeting Category + Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = typeLabel,
                                color = typeColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Timestamp Jump Pill if applicable
                    if (!result.formattedTime.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.clickable { onClick() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Jump",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = result.formattedTime,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Text(
                    text = result.meetingDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Meeting Title
            Text(
                text = result.meetingTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Highlighted Snippet with Speaker if present
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!result.speakerName.isNullOrBlank()) {
                    SpeakerAvatar(
                        speakerName = result.speakerName,
                        speakerId = result.speakerName,
                        size = 28,
                        modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildHighlightedText(result.matchedSnippet, searchQuery),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )

                    if (!result.secondarySnippet.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.secondarySnippet,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyState(onSelectSuggestedQuery: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Suggested Searches",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Quickly search topics, technical terms, or action items across all transcripts",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        val suggestions = listOf(
            "Gemini 3.5 Flash",
            "Latency",
            "Action items",
            "Release date",
            "Architecture review",
            "Room database",
            "Sarah",
            "Alex",
            "High priority"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.take(5).forEach { query ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onSelectSuggestedQuery(query) }
                ) {
                    Text(
                        text = query,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.drop(5).forEach { query ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onSelectSuggestedQuery(query) }
                ) {
                    Text(
                        text = query,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Builds annotated text highlighting all matches of the search query in yellow/primary tint.
 */
private fun buildHighlightedText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)

    val cleanQuery = query.trim()
    val pattern = Regex(Regex.escape(cleanQuery), RegexOption.IGNORE_CASE)
    val matches = pattern.findAll(text)

    return buildAnnotatedString {
        var lastIndex = 0
        for (match in matches) {
            val range = match.range
            if (range.first > lastIndex) {
                append(text.substring(lastIndex, range.first))
            }
            withStyle(
                style = SpanStyle(
                    background = Color(0xFFFFEB3B).copy(alpha = 0.65f),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )
            ) {
                append(text.substring(range.first, range.last + 1))
            }
            lastIndex = range.last + 1
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}
