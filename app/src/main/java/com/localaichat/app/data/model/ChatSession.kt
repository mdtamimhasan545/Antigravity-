package com.localaichat.app.data.model

import java.util.UUID

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Conversation",
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val personaId: String = "general",
    val messages: List<ChatMessage> = emptyList()
)
