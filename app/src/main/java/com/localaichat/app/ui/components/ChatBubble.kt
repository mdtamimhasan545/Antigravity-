package com.localaichat.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoRenew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localaichat.app.data.model.ChatMessage
import com.localaichat.app.data.model.MessageRole
import com.localaichat.app.ui.theme.AssistantBubbleBg
import com.localaichat.app.ui.theme.Blue500
import com.localaichat.app.ui.theme.Blue600
import com.localaichat.app.ui.theme.CodeBlockBg
import com.localaichat.app.ui.theme.Emerald500
import com.localaichat.app.ui.theme.Slate400
import com.localaichat.app.ui.theme.Slate50
import com.localaichat.app.ui.theme.Slate700
import com.localaichat.app.ui.theme.Slate800
import com.localaichat.app.ui.theme.UserBubbleBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubble(
    message: ChatMessage,
    isLatestAssistant: Boolean = false,
    isPlayingAudio: Boolean = false,
    onSpeakClick: () -> Unit = {},
    onRegenerateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Blue600),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI Avatar",
                    tint = Slate50,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) UserBubbleBg else AssistantBubbleBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column {
                    FormattedMessageContent(
                        content = message.content + if (message.isStreaming) " ▍" else "",
                        textColor = Slate50
                    )

                    // Action bar for Assistant messages
                    if (!isUser && message.content.isNotEmpty() && !message.isStreaming) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left side: stats or latency badge
                            if (message.stats != null) {
                                Text(
                                    text = message.stats,
                                    fontSize = 11.sp,
                                    color = Emerald500,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            // Right side: Audio TTS, Copy, Regenerate actions
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Text-to-Speech Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isPlayingAudio) Blue600 else Slate800)
                                        .clickable { onSpeakClick() }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isPlayingAudio) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                            contentDescription = "TTS Voice",
                                            tint = if (isPlayingAudio) Slate50 else Slate400,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isPlayingAudio) "Stop" else "Listen",
                                            fontSize = 11.sp,
                                            color = if (isPlayingAudio) Slate50 else Slate400
                                        )
                                    }
                                }

                                // Copy Message Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Slate800)
                                        .clickable { copyToClipboard(context, message.content) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = Slate400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // Regenerate Button (if latest)
                                if (isLatestAssistant) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Slate800)
                                            .clickable { onRegenerateClick() }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoRenew,
                                            contentDescription = "Regenerate",
                                            tint = Slate400,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Timestamp
            Text(
                text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Slate700),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    tint = Slate50,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FormattedMessageContent(
    content: String,
    textColor: Color
) {
    val context = LocalContext.current

    if (content.contains("```")) {
        val parts = content.split("```")
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            parts.forEachIndexed { index, part ->
                if (index % 2 == 1) { // Code block
                    val lines = part.trim().lines()
                    val language = if (lines.firstOrNull()?.matches(Regex("^[a-zA-Z0-9_#-]+$")) == true) lines.first() else "code"
                    val codeContent = if (language != "code") lines.drop(1).joinToString("\n") else part.trim()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CodeBlockBg)
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = language.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Blue500
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { copyToClipboard(context, codeContent) }
                                        .padding(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy code",
                                            tint = Slate400,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Copy", fontSize = 11.sp, color = Slate400)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = codeContent,
                                color = Slate50,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    if (part.isNotEmpty()) {
                        Text(
                            text = part.trim(),
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    } else {
        Text(
            text = content,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 22.sp
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("AI Message", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
}
