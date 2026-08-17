package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ProfessionalSpeakerThemes
import com.example.ui.theme.SpeakerTheme

@Composable
fun getSpeakerTheme(speakerId: String): SpeakerTheme {
    val hash = speakerId.hashCode()
    val index = Math.abs(hash) % ProfessionalSpeakerThemes.size
    return ProfessionalSpeakerThemes[index]
}

@Composable
fun getSpeakerColor(speakerId: String): Color {
    return getSpeakerTheme(speakerId).text
}

@Composable
fun SpeakerAvatar(
    speakerName: String,
    speakerId: String = speakerName,
    size: Int = 32,
    modifier: Modifier = Modifier
) {
    val theme = getSpeakerTheme(speakerId)
    val initials = speakerName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifEmpty { "S" }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(theme.bg)
    ) {
        Text(
            text = initials,
            color = theme.text,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.42f).sp
        )
    }
}

@Composable
fun SpeakerChip(
    speakerName: String,
    speakerId: String,
    onRenameClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = getSpeakerTheme(speakerId)
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(theme.bg)
            .then(
                if (onRenameClick != null) {
                    Modifier.clickable { onRenameClick() }
                } else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("speaker_chip_${speakerId}")
    ) {
        SpeakerAvatar(speakerName = speakerName, speakerId = speakerId, size = 20)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = speakerName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = theme.text
        )
        if (onRenameClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Rename speaker",
                tint = theme.text.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

