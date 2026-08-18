package com.localaichat.app.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localaichat.app.data.audio.SpeechManager
import com.localaichat.app.data.engine.LocalInferenceEngine
import com.localaichat.app.data.model.ChatMessage
import com.localaichat.app.data.model.ChatSession
import com.localaichat.app.data.model.DefaultPersonas
import com.localaichat.app.data.model.MessageRole
import com.localaichat.app.data.model.ModelConfig
import com.localaichat.app.data.model.Persona
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val sessions: List<ChatSession> = emptyList(),
    val currentSessionId: String = "",
    val searchQuery: String = "",
    val isGenerating: Boolean = false,
    val selectedPersonaId: String = "general",
    val modelConfig: ModelConfig = ModelConfig(),
    val currentlyPlayingAudioId: String? = null,
    val errorMessage: String? = null
) {
    val currentSession: ChatSession?
        get() = sessions.find { it.id == currentSessionId }

    val filteredSessions: List<ChatSession>
        get() {
            val list = if (searchQuery.isBlank()) sessions else {
                sessions.filter { it.title.contains(searchQuery, ignoreCase = true) ||
                        it.messages.any { m -> m.content.contains(searchQuery, ignoreCase = true) } }
            }
            return list.sortedByDescending { it.isPinned }
        }

    val currentPersona: Persona
        get() = DefaultPersonas.getById(selectedPersonaId)
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = LocalInferenceEngine(application.applicationContext)
    val speechManager = SpeechManager(application.applicationContext)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null

    init {
        createNewSession()

        // Observe TTS audio playing state
        viewModelScope.launch {
            speechManager.currentlyPlayingId.collect { playingId ->
                _uiState.update { it.copy(currentlyPlayingAudioId = playingId) }
            }
        }
    }

    fun createNewSession() {
        val newSession = ChatSession(
            title = "New Chat",
            personaId = _uiState.value.selectedPersonaId,
            messages = listOf(
                ChatMessage(
                    role = MessageRole.ASSISTANT,
                    content = "Hello! I am your offline AI Assistant. Choose a model or persona to begin."
                )
            )
        )
        _uiState.update { state ->
            state.copy(
                sessions = listOf(newSession) + state.sessions,
                currentSessionId = newSession.id
            )
        }
    }

    fun selectSession(sessionId: String) {
        _uiState.update { it.copy(currentSessionId = sessionId) }
    }

    fun togglePinSession(sessionId: String) {
        _uiState.update { state ->
            val updated = state.sessions.map {
                if (it.id == sessionId) it.copy(isPinned = !it.isPinned) else it
            }
            state.copy(sessions = updated)
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        _uiState.update { state ->
            val updated = state.sessions.map {
                if (it.id == sessionId) it.copy(title = newTitle.trim()) else it
            }
            state.copy(sessions = updated)
        }
    }

    fun deleteSession(sessionId: String) {
        _uiState.update { state ->
            val remaining = state.sessions.filter { it.id != sessionId }
            val nextActiveId = if (state.currentSessionId == sessionId) {
                remaining.firstOrNull()?.id ?: ""
            } else {
                state.currentSessionId
            }
            state.copy(sessions = remaining, currentSessionId = nextActiveId)
        }
        if (_uiState.value.sessions.isEmpty()) {
            createNewSession()
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectPersona(personaId: String) {
        val persona = DefaultPersonas.getById(personaId)
        _uiState.update { state ->
            val updatedConfig = state.modelConfig.copy(
                temperature = persona.defaultTemperature,
                systemPrompt = persona.systemPrompt
            )
            state.copy(
                selectedPersonaId = personaId,
                modelConfig = updatedConfig
            )
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _uiState.value.isGenerating) return

        val userMessage = ChatMessage(role = MessageRole.USER, content = trimmed)
        val assistantMessage = ChatMessage(
            role = MessageRole.ASSISTANT,
            content = "",
            isStreaming = true
        )

        val currentId = _uiState.value.currentSessionId

        _uiState.update { state ->
            val updatedSessions = state.sessions.map { session ->
                if (session.id == currentId) {
                    val newTitle = if (session.messages.size <= 1) {
                        if (trimmed.length > 25) trimmed.take(25) + "..." else trimmed
                    } else session.title

                    session.copy(
                        title = newTitle,
                        messages = session.messages + userMessage + assistantMessage
                    )
                } else session
            }
            state.copy(sessions = updatedSessions, isGenerating = true)
        }

        executeInference(currentId, assistantMessage.id)
    }

    fun regenerateLastMessage() {
        val currentSession = _uiState.value.currentSession ?: return
        if (_uiState.value.isGenerating || currentSession.messages.size < 2) return

        val lastMessage = currentSession.messages.last()
        if (lastMessage.role != MessageRole.ASSISTANT) return

        val updatedMessages = currentSession.messages.dropLast(1)
        val newAssistantPlaceholder = ChatMessage(
            role = MessageRole.ASSISTANT,
            content = "",
            isStreaming = true
        )

        val currentId = _uiState.value.currentSessionId

        _uiState.update { state ->
            val updatedSessions = state.sessions.map { session ->
                if (session.id == currentId) {
                    session.copy(messages = updatedMessages + newAssistantPlaceholder)
                } else session
            }
            state.copy(sessions = updatedSessions, isGenerating = true)
        }

        executeInference(currentId, newAssistantPlaceholder.id)
    }

    private fun executeInference(sessionId: String, assistantMessageId: String) {
        generationJob = viewModelScope.launch {
            val currentSession = _uiState.value.sessions.find { it.id == sessionId } ?: return@launch
            val history = currentSession.messages.filter { it.id != assistantMessageId }

            val responseBuilder = StringBuilder()
            var finalStats: String? = null

            try {
                engine.generateStreamingResponse(
                    history = history,
                    config = _uiState.value.modelConfig,
                    personaId = _uiState.value.selectedPersonaId
                ).collect { result ->
                    if (result.isFinished) {
                        finalStats = result.stats
                    } else {
                        responseBuilder.append(result.token)
                    }

                    val currentText = responseBuilder.toString()

                    _uiState.update { state ->
                        val updatedSessions = state.sessions.map { session ->
                            if (session.id == sessionId) {
                                val updatedMessages = session.messages.map { msg ->
                                    if (msg.id == assistantMessageId) {
                                        msg.copy(
                                            content = currentText,
                                            isStreaming = !result.isFinished,
                                            stats = finalStats
                                        )
                                    } else msg
                                }
                                session.copy(messages = updatedMessages)
                            } else session
                        }
                        state.copy(sessions = updatedSessions)
                    }
                }
            } catch (e: Exception) {
                responseBuilder.append("\n[Generation interrupted: ${e.localizedMessage}]")
            } finally {
                _uiState.update { state ->
                    val updatedSessions = state.sessions.map { session ->
                        if (session.id == sessionId) {
                            val updatedMessages = session.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(
                                        content = responseBuilder.toString(),
                                        isStreaming = false,
                                        stats = finalStats
                                    )
                                } else msg
                            }
                            session.copy(messages = updatedMessages)
                        } else session
                    }
                    state.copy(sessions = updatedSessions, isGenerating = false)
                }
            }
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        _uiState.update { state ->
            val currentId = state.currentSessionId
            val updatedSessions = state.sessions.map { session ->
                if (session.id == currentId) {
                    val updatedMessages = session.messages.map { msg ->
                        if (msg.isStreaming) msg.copy(isStreaming = false) else msg
                    }
                    session.copy(messages = updatedMessages)
                } else session
            }
            state.copy(sessions = updatedSessions, isGenerating = false)
        }
    }

    fun toggleSpeakMessage(message: ChatMessage) {
        speechManager.speak(message.id, message.content)
    }

    fun shareChat(context: Context) {
        val currentSession = _uiState.value.currentSession ?: return
        val sb = StringBuilder()
        sb.append("# ${currentSession.title}\n\n")
        currentSession.messages.forEach { msg ->
            val roleName = if (msg.role == MessageRole.USER) "👤 User" else "🤖 Assistant"
            sb.append("**$roleName:**\n${msg.content}\n\n---\n\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Conversation")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun setModelUri(uri: Uri, fileName: String) {
        val success = engine.loadModel(uri, fileName)
        if (success) {
            _uiState.update { state ->
                state.copy(
                    modelConfig = state.modelConfig.copy(
                        modelUri = uri.toString(),
                        modelName = fileName
                    )
                )
            }
        }
    }

    fun updateConfig(config: ModelConfig) {
        _uiState.update { it.copy(modelConfig = config) }
    }

    fun clearCurrentChat() {
        val currentId = _uiState.value.currentSessionId
        _uiState.update { state ->
            val updatedSessions = state.sessions.map { session ->
                if (session.id == currentId) {
                    session.copy(
                        messages = listOf(
                            ChatMessage(
                                role = MessageRole.ASSISTANT,
                                content = "Conversation cleared. Ready for your next query!"
                            )
                        )
                    )
                } else session
            }
            state.copy(sessions = updatedSessions)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }
}
