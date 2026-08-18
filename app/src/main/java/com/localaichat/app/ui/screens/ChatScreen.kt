package com.localaichat.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localaichat.app.data.model.ChatMessage
import com.localaichat.app.data.model.MessageRole
import com.localaichat.app.ui.components.ChatBubble
import com.localaichat.app.ui.components.DrawerContent
import com.localaichat.app.ui.components.MessageInputBar
import com.localaichat.app.ui.theme.Blue600
import com.localaichat.app.ui.theme.Emerald500
import com.localaichat.app.ui.theme.Slate400
import com.localaichat.app.ui.theme.Slate50
import com.localaichat.app.ui.theme.Slate800
import com.localaichat.app.ui.theme.Slate900
import com.localaichat.app.ui.theme.Slate950
import com.localaichat.app.viewmodel.ChatUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onSendMessage: (String) -> Unit,
    onStopGeneration: () -> Unit,
    onRegenerateLastMessage: () -> Unit,
    onSpeakMessage: (ChatMessage) -> Unit,
    onStartVoiceInput: () -> Unit,
    onNewChat: () -> Unit,
    onSelectSession: (String) -> Unit,
    onTogglePinSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectPersona: (String) -> Unit,
    onClearChat: () -> Unit,
    onShareChat: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val currentMessages = uiState.currentSession?.messages ?: emptyList()

    LaunchedEffect(currentMessages.size, currentMessages.lastOrNull()?.content?.length) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Slate950
            ) {
                DrawerContent(
                    sessions = uiState.filteredSessions,
                    activeSessionId = uiState.currentSessionId,
                    searchQuery = uiState.searchQuery,
                    selectedPersonaId = uiState.selectedPersonaId,
                    onSearchChange = onSearchChange,
                    onSelectPersona = onSelectPersona,
                    onNewChat = {
                        onNewChat()
                        scope.launch { drawerState.close() }
                    },
                    onSelectSession = { id ->
                        onSelectSession(id)
                        scope.launch { drawerState.close() }
                    },
                    onTogglePinSession = onTogglePinSession,
                    onDeleteSession = onDeleteSession,
                    onOpenSettings = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        // Model and Persona status pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Slate900)
                                .clickable { onNavigateToSettings() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Emerald500)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${uiState.currentPersona.emoji} ${uiState.currentPersona.name}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate50,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Slate50
                            )
                        }
                    },
                    actions = {
                        // Share Button
                        IconButton(onClick = onShareChat) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Clear Chat Button
                        IconButton(onClick = onClearChat) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Chat",
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Settings Button
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Slate950
                    )
                )
            },
            bottomBar = {
                MessageInputBar(
                    isGenerating = uiState.isGenerating,
                    onSendMessage = onSendMessage,
                    onStopGeneration = onStopGeneration,
                    onStartVoiceInput = onStartVoiceInput,
                    modifier = Modifier.imePadding()
                )
            },
            containerColor = Slate950,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (currentMessages.size <= 1) {
                    // Empty / Welcome state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Slate900),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Blue600,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = uiState.currentPersona.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = Slate50,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.currentPersona.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )

                        // Suggestion prompt chips
                        val suggestions = when (uiState.selectedPersonaId) {
                            "coder" -> listOf(
                                "🐍 Write a Python script with async/await",
                                "⚙️ Explain Binary Search Tree with code",
                                "🚀 Optimize this function for memory"
                            )
                            "bangla" -> listOf(
                                "🇧🇩 বাংলায় একটি অনুপ্রেরণামূলক কবিতা লেখো",
                                "📖 কৃত্রিম বুদ্ধিমত্তা কীভাবে কাজ করে বাংলায় বোঝাও",
                                "📝 একটি অফিশিয়াল ছুটির আবেদনের ফরম্যাট দাও"
                            )
                            "concise" -> listOf(
                                "⚡ Top 5 Linux commands in Termux",
                                "⚡ Compare SQL vs NoSQL in 3 points",
                                "⚡ Summary of Quantum Computing"
                            )
                            else -> listOf(
                                "💡 Explain how GGUF Quantization works on mobile",
                                "💻 Write a clean Python web scraper",
                                "🌍 What are the top advancements in Edge AI?"
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            suggestions.forEach { prompt ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Slate900)
                                        .clickable { onSendMessage(prompt) }
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = prompt,
                                        color = Slate50,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Chat Message List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        itemsIndexed(currentMessages, key = { _, msg -> msg.id }) { index, message ->
                            val isLatest = index == currentMessages.lastIndex && message.role == MessageRole.ASSISTANT
                            val isPlaying = uiState.currentlyPlayingAudioId == message.id

                            ChatBubble(
                                message = message,
                                isLatestAssistant = isLatest,
                                isPlayingAudio = isPlaying,
                                onSpeakClick = { onSpeakMessage(message) },
                                onRegenerateClick = onRegenerateLastMessage
                            )
                        }
                    }
                }
            }
        }
    }
}
