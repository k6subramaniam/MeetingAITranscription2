package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Professional Polish - Primary Palette (M3 Amethyst & Royal Indigo)
val PrimaryLight = Color(0xFF6750A4) // Royal Amethyst
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFF3EDF7) // Soft Lavender Container
val OnPrimaryContainerLight = Color(0xFF21005D) // Deep Violet
val PrimaryDark = Color(0xFFD0BCFF)
val OnPrimaryDark = Color(0xFF381E72)
val PrimaryContainerDark = Color(0xFF4F378B)
val OnPrimaryContainerDark = Color(0xFFEADDFF)

// Secondary Palette (M3 Mauve / Slate Purple)
val SecondaryLight = Color(0xFF625B71)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE8DEF8)
val OnSecondaryContainerLight = Color(0xFF1D192B)
val SecondaryDark = Color(0xFFCCC2DC)
val OnSecondaryDark = Color(0xFF332D41)
val SecondaryContainerDark = Color(0xFF4A4458)
val OnSecondaryContainerDark = Color(0xFFE8DEF8)

// Tertiary Palette (Warm Rose / Blush)
val TertiaryLight = Color(0xFF7D5260)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFD8E4)
val OnTertiaryContainerLight = Color(0xFF31111D)
val TertiaryDark = Color(0xFFEFB8C8)
val OnTertiaryDark = Color(0xFF492532)
val TertiaryContainerDark = Color(0xFF633B48)
val OnTertiaryContainerDark = Color(0xFFFFD8E4)

// Background & Surface (Warm Creamy Off-White & Crisp Cards)
val BackgroundLight = Color(0xFFFDF8F6) // Warm sophisticated cream
val SurfaceLight = Color(0xFFFFFFFF) // Crisp white card
val SurfaceVariantLight = Color(0xFFF3EDF7) // Soft lavender-tinted surface
val SurfaceCardLight = Color(0xFFFFFFFF)
val SurfaceContainerHighLight = Color(0xFFECE6F0)

val BackgroundDark = Color(0xFF141218)
val SurfaceDark = Color(0xFF1D1B20)
val SurfaceVariantDark = Color(0xFF49454F)
val SurfaceCardDark = Color(0xFF211F26)
val SurfaceContainerHighDark = Color(0xFF2B2930)

// Text Colors
val TextPrimaryLight = Color(0xFF1D1B20)
val TextSecondaryLight = Color(0xFF49454F)
val TextPrimaryDark = Color(0xFFE6E0E9)
val TextSecondaryDark = Color(0xFFCAC4D0)

// Outline & Borders
val OutlineLight = Color(0xFFCAC4D0)
val OutlineVariantLight = Color(0xFFEADDFF)
val OutlineDark = Color(0xFF938F99)
val OutlineVariantDark = Color(0xFF49454F)

// Semantic Accents
val RecordingRed = Color(0xFFB3261E) // Crisp Cardinal Red for Recording
val RecordingRedLight = Color(0xFFF9DEDC)
val RecordingPulse = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF2E7D32)
val SuccessGreenLight = Color(0xFFC4EED0)
val WarningAmber = Color(0xFFB25E02)
val WarningAmberLight = Color(0xFFFFDBCD)
val InfoBlue = Color(0xFF00639B)
val InfoBlueLight = Color(0xFFD3E4FF)

// Professional Polish Speaker Colors (Pair of Background + Text)
data class SpeakerTheme(val bg: Color, val text: Color)

val ProfessionalSpeakerThemes = listOf(
    SpeakerTheme(Color(0xFFFFD8E4), Color(0xFF31111D)), // Soft Rose
    SpeakerTheme(Color(0xFFD3E4FF), Color(0xFF001D36)), // Soft Cornflower Blue
    SpeakerTheme(Color(0xFFE8DEF8), Color(0xFF21005D)), // Soft Lavender
    SpeakerTheme(Color(0xFFC4EED0), Color(0xFF00210E)), // Soft Mint
    SpeakerTheme(Color(0xFFFFDBCD), Color(0xFF360F00)), // Soft Peach/Amber
    SpeakerTheme(Color(0xFFE2E8F0), Color(0xFF0F172A))  // Soft Slate
)

val SpeakerColors = ProfessionalSpeakerThemes.map { it.text }

