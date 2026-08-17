package com.example.data.model

object SampleMeetings {
    fun getInitialSamples(): List<MeetingEntity> = listOf(
        MeetingEntity(
            id = 1,
            title = "Q3 Product Architecture & Gemini AI Integration",
            category = MeetingCategories.ENGINEERING,
            createdAt = System.currentTimeMillis() - (2 * 3600 * 1000), // 2 hours ago
            durationSeconds = 1450, // ~24 mins
            rawTranscript = """
                [00:00] Alex (Lead Architect): Welcome everyone to our Q3 architectural review. Today our primary focus is finalizing our on-device and cloud Gemini AI pipeline for our mobile applications.
                [00:45] Sarah (Principal Engineer): Thanks Alex. On the mobile side, we tested Gemini 3.5 Flash for audio transcription and diarization. Latency dropped by 42% compared to previous models, and speaker separation is remarkably accurate.
                [02:10] David (Product Manager): That is huge for our enterprise customers. What about memory footprint on Android devices with 4GB RAM?
                [03:25] Sarah (Principal Engineer): The lightweight streaming response architecture consumes under 45MB heap space during continuous transcription.
                [05:10] Elena (Design Lead): From a UX perspective, we want live audio waveform feedback and instant timestamp jumping when users tap any transcript segment.
                [07:30] Alex (Lead Architect): Agreed. Let's make sure the Room database indexes transcript timestamps for sub-50ms search queries.
                [10:15] David (Product Manager): Let's lock in the target beta release date for September 15th.
                [12:00] Alex (Lead Architect): Great. Action items: Sarah will complete the Retrofit streaming integration by next Tuesday; Elena will finalize the M3 tabbed layout by Thursday.
            """.trimIndent(),
            transcriptSegments = listOf(
                TranscriptSegment(
                    speakerId = "Speaker 1",
                    speakerName = "Alex (Lead Architect)",
                    timestampSeconds = 0,
                    formattedTime = "00:00",
                    text = "Welcome everyone to our Q3 architectural review. Today our primary focus is finalizing our on-device and cloud Gemini AI pipeline for our mobile applications.",
                    sentiment = "Neutral"
                ),
                TranscriptSegment(
                    speakerId = "Speaker 2",
                    speakerName = "Sarah (Principal Engineer)",
                    timestampSeconds = 45,
                    formattedTime = "00:45",
                    text = "Thanks Alex. On the mobile side, we tested Gemini 3.5 Flash for audio transcription and diarization. Latency dropped by 42% compared to previous models, and speaker separation is remarkably accurate.",
                    sentiment = "Positive"
                ),
                TranscriptSegment(
                    speakerId = "Speaker 3",
                    speakerName = "David (Product Manager)",
                    timestampSeconds = 130,
                    formattedTime = "02:10",
                    text = "That is huge for our enterprise customers. What about memory footprint on Android devices with 4GB RAM?",
                    sentiment = "Concern"
                ),
                TranscriptSegment(
                    speakerId = "Speaker 2",
                    speakerName = "Sarah (Principal Engineer)",
                    timestampSeconds = 205,
                    formattedTime = "03:25",
                    text = "The lightweight streaming response architecture consumes under 45MB heap space during continuous transcription.",
                    sentiment = "Positive"
                ),
                TranscriptSegment(
                    speakerId = "Speaker 4",
                    speakerName = "Elena (Design Lead)",
                    timestampSeconds = 310,
                    formattedTime = "05:10",
                    text = "From a UX perspective, we want live audio waveform feedback and instant timestamp jumping when users tap any transcript segment.",
                    sentiment = "Decision"
                ),
                TranscriptSegment(
                    speakerId = "Speaker 1",
                    speakerName = "Alex (Lead Architect)",
                    timestampSeconds = 450,
                    formattedTime = "07:30",
                    text = "Agreed. Let's make sure the Room database indexes transcript timestamps for sub-50ms search queries.",
                    sentiment = "Decision"
                ),
                TranscriptSegment(
                    speakerId = "Speaker 3",
                    speakerName = "David (Product Manager)",
                    timestampSeconds = 615,
                    formattedTime = "10:15",
                    text = "Let's lock in the target beta release date for September 15th.",
                    sentiment = "Decision"
                ),
                TranscriptSegment(
                    speakerId = "Speaker 1",
                    speakerName = "Alex (Lead Architect)",
                    timestampSeconds = 720,
                    formattedTime = "12:00",
                    text = "Great. Action items: Sarah will complete the Retrofit streaming integration by next Tuesday; Elena will finalize the M3 tabbed layout by Thursday.",
                    sentiment = "Positive"
                )
            ),
            executiveSummary = "The engineering leadership team reviewed the Q3 mobile AI architecture, confirming Gemini 3.5 Flash delivers a 42% latency reduction with sub-45MB memory overhead. The team approved interactive audio waveform playback with timestamp scrubbing and locked in the beta rollout target for September 15th.",
            keyDecisions = listOf(
                "Adopt Gemini 3.5 Flash for primary mobile audio transcription & speaker diarization.",
                "Implement Room Database indexing on transcript timestamps for ultra-fast local search.",
                "Lock in Beta customer launch date for September 15th.",
                "Mandate Material 3 design system with dynamic waveform visualizers."
            ),
            actionItems = listOf(
                ActionItem(
                    id = "act-1",
                    title = "Complete Retrofit Gemini 3.5 streaming audio integration",
                    assignee = "Sarah (Principal Engineer)",
                    dueDate = "Tuesday",
                    priority = "HIGH",
                    isCompleted = false,
                    timestampSeconds = 45,
                    formattedTime = "00:45"
                ),
                ActionItem(
                    id = "act-2",
                    title = "Finalize Material 3 tabbed workspace UI & waveform scrubber",
                    assignee = "Elena (Design Lead)",
                    dueDate = "Thursday",
                    priority = "HIGH",
                    isCompleted = true,
                    timestampSeconds = 310,
                    formattedTime = "05:10"
                ),
                ActionItem(
                    id = "act-3",
                    title = "Draft Q3 Beta rollout communications for enterprise customers",
                    assignee = "David (Product Manager)",
                    dueDate = "Next Friday",
                    priority = "MEDIUM",
                    isCompleted = false,
                    timestampSeconds = 615,
                    formattedTime = "10:15"
                ),
                ActionItem(
                    id = "act-4",
                    title = "Benchmark offline Room database search query latencies",
                    assignee = "Alex (Lead Architect)",
                    dueDate = "Next Wednesday",
                    priority = "LOW",
                    isCompleted = false,
                    timestampSeconds = 450,
                    formattedTime = "07:30"
                )
            ),
            discussionTopics = listOf(
                DiscussionTopic(
                    id = "top-1",
                    title = "Gemini AI Audio Pipeline & Performance Benchmarks",
                    timeRange = "00:00 - 05:00",
                    summary = "Evaluated Gemini 3.5 Flash performance on real audio samples, showing 42% latency improvement and minimal heap overhead.",
                    keyPoints = listOf("42% lower latency", "<45MB heap footprint", "Accurate multi-speaker diarization")
                ),
                DiscussionTopic(
                    id = "top-2",
                    title = "User Experience & Waveform Interactivity",
                    timeRange = "05:10 - 09:30",
                    summary = "Agreed on interactive timestamp navigation and live audio waveform visualizer components.",
                    keyPoints = listOf("Timestamp seekable transcripts", "Real-time dB audio meter", "Material 3 elevation")
                ),
                DiscussionTopic(
                    id = "top-3",
                    title = "Beta Schedule & Action Item Allocation",
                    timeRange = "09:30 - 12:00",
                    summary = "Set September 15th beta milestone and assigned core engineering deliverables.",
                    keyPoints = listOf("September 15 target", "Sarah leads API integration", "Elena leads UI design")
                )
            ),
            speakerStats = listOf(
                SpeakerStat("Speaker 1", "Alex (Lead Architect)", 420, 0.35f, 540, "Architecture & Database Indexing"),
                SpeakerStat("Speaker 2", "Sarah (Principal Engineer)", 480, 0.40f, 620, "Gemini Flash Benchmarks & Memory"),
                SpeakerStat("Speaker 3", "David (Product Manager)", 290, 0.15f, 280, "Beta Rollout & Timeline"),
                SpeakerStat("Speaker 4", "Elena (Design Lead)", 260, 0.10f, 210, "Waveform UX & Design System")
            ),
            deepThinkingAnalysis = """
                ### 🧠 High-Thinking Strategic Analysis (Gemini 3.1 Pro)
                
                #### 1. Strategic Alignment & Viability
                The decision to standardize on Gemini 3.5 Flash for audio transcription provides a competitive advantage in enterprise voice intelligence. The 42% latency reduction unlocks real-time collaboration scenarios.
                
                #### 2. Critical Blindspots & Risk Matrix
                - **Network Fluctuation Risk**: The team assumed persistent low-latency network connections for streaming AI responses. On mobile networks with jitter, fallback caching strategies must be implemented.
                - **Microphone Hardware Diversity**: Android devices have varying AGC (Auto Gain Control) and noise floor profiles. Audio pre-processing (noise suppression) should be verified.
                
                #### 3. Unspoken Assumptions
                - The beta release deadline of September 15th assumes no major breaking changes in the API response format.
                - Elena's design requires precise timestamp sync between MediaPlayer playback and Compose LazyColumn scroll positions.
                
                #### 4. Actionable Recommendations
                - Create a Mock Audio Stream test harness to validate the player scrubber under 100+ simulated segments.
                - Implement local Room caching of transcribed audio tokens to support instant offline re-openings.
            """.trimIndent(),
            tags = listOf("Gemini AI", "Architecture", "Beta-Release", "Android"),
            isFavorite = true,
            sentiment = "Highly Productive"
        ),
        MeetingEntity(
            id = 2,
            title = "Executive Strategy & Annual Budget Allocation",
            category = MeetingCategories.STRATEGY,
            createdAt = System.currentTimeMillis() - (26 * 3600 * 1000), // 1 day ago
            durationSeconds = 2100, // 35 mins
            rawTranscript = """
                [00:00] Marcus (CEO): Good morning team. Let's address our capital allocation for the coming fiscal year.
                [01:15] Rachel (CFO): Based on current revenue ARR growth at 38%, we have \$4.2M allocated for R&D expansion and \$2.1M for customer acquisition.
                [03:40] Jordan (VP Sales): We are seeing accelerated demand in the healthcare and legal sectors for automated compliance transcription.
                [06:00] Marcus (CEO): Let's reallocate \$500K from general marketing into specialized AI compliance engineering.
                [08:20] Rachel (CFO): Approved. I will update the master pro-forma budget and circulate it by Friday.
            """.trimIndent(),
            transcriptSegments = listOf(
                TranscriptSegment(
                    speakerId = "Speaker 1",
                    speakerName = "Marcus (CEO)",
                    timestampSeconds = 0,
                    formattedTime = "00:00",
                    text = "Good morning team. Let's address our capital allocation for the coming fiscal year.",
                    sentiment = "Neutral"
                ),
                TranscriptSegment(
                    speakerId = "Speaker 2",
                    speakerName = "Rachel (CFO)",
                    timestampSeconds = 75,
                    formattedTime = "01:15",
                    text = "Based on current revenue ARR growth at 38%, we have $4.2M allocated for R&D expansion and $2.1M for customer acquisition.",
                    sentiment = "Positive"
                ),
                TranscriptSegment(
                    speakerId = "Speaker 3",
                    speakerName = "Jordan (VP Sales)",
                    timestampSeconds = 220,
                    formattedTime = "03:40",
                    text = "We are seeing accelerated demand in the healthcare and legal sectors for automated compliance transcription.",
                    sentiment = "Positive"
                ),
                TranscriptSegment(
                    speakerId = "Speaker 1",
                    speakerName = "Marcus (CEO)",
                    timestampSeconds = 360,
                    formattedTime = "06:00",
                    text = "Let's reallocate $500K from general marketing into specialized AI compliance engineering.",
                    sentiment = "Decision"
                ),
                TranscriptSegment(
                    speakerId = "Speaker 2",
                    speakerName = "Rachel (CFO)",
                    timestampSeconds = 500,
                    formattedTime = "08:20",
                    text = "Approved. I will update the master pro-forma budget and circulate it by Friday.",
                    sentiment = "Decision"
                )
            ),
            executiveSummary = "Executive leadership approved the annual capital allocation plan, including $4.2M for R&D expansion. In response to surge demand in healthcare and legal verticals, CEO Marcus reallocated $500K from general marketing toward AI compliance engineering.",
            keyDecisions = listOf(
                "Approve $4.2M R&D budget for fiscal year.",
                "Shift $500K marketing funds to AI compliance engineering.",
                "Target healthcare and legal enterprise verticals for Q4 sales campaign."
            ),
            actionItems = listOf(
                ActionItem(
                    id = "act-201",
                    title = "Circulate revised master pro-forma budget model",
                    assignee = "Rachel (CFO)",
                    dueDate = "Friday 5:00 PM",
                    priority = "HIGH",
                    isCompleted = false
                ),
                ActionItem(
                    id = "act-202",
                    title = "Define compliance certification requirements (HIPAA, SOC-2)",
                    assignee = "Jordan (VP Sales)",
                    dueDate = "Next Month",
                    priority = "MEDIUM",
                    isCompleted = false
                )
            ),
            discussionTopics = listOf(
                DiscussionTopic(
                    id = "top-201",
                    title = "Fiscal Revenue Review & ARR Growth",
                    timeRange = "00:00 - 03:00",
                    summary = "38% ARR growth validated the expansion plan.",
                    keyPoints = listOf("38% ARR growth", "$4.2M R&D pool", "$2.1M CAC pool")
                ),
                DiscussionTopic(
                    id = "top-202",
                    title = "Healthcare & Legal Market Opportunity",
                    timeRange = "03:00 - 08:30",
                    summary = "Identified high-margin compliance transcription market segments.",
                    keyPoints = listOf("HIPAA transcription demand", "$500K budget shift", "Enterprise sales pipeline")
                )
            ),
            speakerStats = listOf(
                SpeakerStat("Speaker 1", "Marcus (CEO)", 720, 0.40f, 610, "Strategic Capital Allocation"),
                SpeakerStat("Speaker 2", "Rachel (CFO)", 680, 0.35f, 540, "Financial Modeling & Budgeting"),
                SpeakerStat("Speaker 3", "Jordan (VP Sales)", 520, 0.25f, 430, "Healthcare & Legal Opportunities")
            ),
            deepThinkingAnalysis = """
                ### 🧠 High-Thinking Strategic Analysis (Gemini 3.1 Pro)
                
                #### 1. Strategic Trade-offs
                Shifting \$500K from top-of-funnel marketing to verticalized compliance engineering increases deal size and contract retention, mitigating churn in enterprise tiers.
                
                #### 2. Regulatory Exposure
                Healthcare (HIPAA) and Legal (CJIS/Bar ethics) require zero-data-retention AI processing agreements. The engineering team must ensure on-device redaction of PII before cloud indexing.
            """.trimIndent(),
            tags = listOf("Budget", "Executive", "Strategy", "Compliance"),
            isFavorite = false,
            sentiment = "Decisive & Confident"
        )
    )
}
