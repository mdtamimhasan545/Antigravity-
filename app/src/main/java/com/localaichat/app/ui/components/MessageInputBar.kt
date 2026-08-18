package com.localaichat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localaichat.app.ui.theme.Blue600
import com.localaichat.app.ui.theme.Rose600
import com.localaichat.app.ui.theme.Slate400
import com.localaichat.app.ui.theme.Slate50
import com.localaichat.app.ui.theme.Slate700
import com.localaichat.app.ui.theme.Slate800
import com.localaichat.app.ui.theme.Slate900
import com.localaichat.app.ui.theme.Slate950

@Composable
fun MessageInputBar(
    isGenerating: Boolean,
    onSendMessage: (String) -> Unit,
    onStopGeneration: () -> Unit,
    onStartVoiceInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Slate950)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Input (Mic) Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Slate900),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onStartVoiceInput) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input Field
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 120.dp),
                placeholder = {
                    Text(
                        text = "Message AI...",
                        color = Slate400,
                        fontSize = 14.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900,
                    focusedTextColor = Slate50,
                    unfocusedTextColor = Slate50,
                    focusedBorderColor = Slate700,
                    unfocusedBorderColor = Slate800
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button (Send / Stop)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isGenerating) Rose600 else if (text.isNotBlank()) Blue600 else Slate800),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        if (isGenerating) {
                            onStopGeneration()
                        } else if (text.isNotBlank()) {
                            onSendMessage(text)
                            text = ""
                        }
                    },
                    enabled = isGenerating || text.isNotBlank()
                ) {
                    Icon(
                        imageVector = if (isGenerating) Icons.Default.Stop else Icons.Default.Send,
                        contentDescription = if (isGenerating) "Stop" else "Send",
                        tint = Slate50,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
