# Meeting AI Transcriber & Summarizer 🎙️✨

A native Android application built with Kotlin and Jetpack Compose that uses Google Gemini 3.5 Flash for live meeting recording, speech-to-text transcription, speaker diarization, AI summary generation, and action item extraction. All transcriptions and summaries are stored locally using a Room Database for offline access.

---

## 🌟 Key Features

- **🎙️ Live Audio Recording & Transcription**: Record live meetings or discussions with real-time waveform visuals.
- **⚡ Gemini 3.5 Flash SDK Integration**: Generates structured executive summaries, bulleted key decisions, topic breakdowns, and actionable task lists with priority tags.
- **📝 Manual Transcript Input**: Paste spoken audio transcripts directly to generate instant AI bullet points and summaries.
- **💾 Offline Room Database**: Local SQLite storage via Jetpack Room keeps all meeting notes, transcripts, and action items available offline without requiring internet.
- **💬 Interactive AI Meeting Chat**: Ask questions, follow-ups, or request action item updates from Gemini about any past recorded meeting.
- **🎨 Modern Material 3 UI**: Clean, accessible Android interface built with Jetpack Compose and dynamic color themes.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
- **Local Persistence**: Jetpack Room Database with KSP & Moshi TypeConverters
- **AI Engine**: Google Gemini API (`gemini-3.5-flash`)
- **Async Operations**: Kotlin Coroutines & Flow
- **Build System**: Gradle (Kotlin DSL `.gradle.kts`)

---

## 🚀 How to Build & Run in Android Studio

1. **Clone the repository**:
   ```bash
   git clone <YOUR_GITHUB_REPO_URL>
   cd <REPO_DIRECTORY>
   ```

2. **Open in Android Studio**:
   - Launch **Android Studio** (Ladybug or newer recommended).
   - Select **Open** and choose the project root folder.
   - Wait for Gradle sync to complete automatically.

3. **Configure Gemini API Key** (Optional for live AI features):
   - Add your Gemini API Key in `gradle.properties` or environment variables:
     ```properties
     GEMINI_API_KEY=your_gemini_api_key_here
     ```

4. **Run on Device or Emulator**:
   - Connect an Android device or start an Android Emulator (API 26+).
   - Click the **Run ▶** button in Android Studio.
