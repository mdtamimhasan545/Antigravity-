package com.localaichat.app.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localaichat.app.data.audio.SpeechManager
import com.localaichat.app.data.engine.GgufMetadata
import com.localaichat.app.data.engine.LocalInferenceEngine
import com.localaichat.app.data.model.ChatMessage
import com.localaichat.app.data.model.ChatSession
import com.localaichat.app.data.model.DefaultPersonas
import com.localaichat.app.data.model.MessageRole
import com.localaichat.app.data.model.ModelConfig
import com.localaichat.app.data.model.Persona
import com.localaichat.app.data.repository.ChatRepository
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
    val modelMetadata: GgufMetadata? = null,
    val currentlyPlayingAudioId: String? = null,
    val sessionToRename: ChatSession? = null,
    val sessionToDelete: ChatSession? = null,
    val isDarkTheme: Boolean = true,
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

    private val repository = ChatRepository(application.applicationContext)
    private val engine = LocalInferenceEngine(application.applicationContext)
    val speechManager = SpeechManager(application.applicationContext)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null

    init {
        loadPersistedData()

        viewModelScope.launch {
            speechManager.currentlyPlayingId.collect { playingId ->
                _uiState.update { it.copy(currentlyPlayingAudioId = playingId) }
            }
        }
    }

    private fun loadPersistedData() {
        val (savedSessions, activeId) = repository.loadSessions()
        val savedConfig = repository.loadModelConfig()
        val savedPersona = repository.loadSelectedPersona()
        val isDark = repository.loadThemeDark()

        if (savedSessions.isNotEmpty()) {
            val validActiveId = if (savedSessions.any { it.id == activeId }) activeId ?: savedSessions.first().id else savedSessions.first().id
            _uiState.update {
                it.copy(
                    sessions = savedSessions,
                    currentSessionId = validActiveId,
                    modelConfig = savedConfig,
                    selectedPersonaId = savedPersona,
                    isDarkTheme = isDark
                )
            }
        } else {
            createNewSession()
            _uiState.update {
                it.copy(
                    modelConfig = savedConfig,
                    selectedPersonaId = savedPersona,
                    isDarkTheme = isDark
                )
            }
        }
    }

    private fun persistState() {
        repository.saveSessions(_uiState.value.sessions, _uiState.value.currentSessionId)
        repository.saveModelConfig(_uiState.value.modelConfig)
        repository.saveSelectedPersona(_uiState.value.selectedPersonaId)
        repository.saveThemeDark(_uiState.value.isDarkTheme)
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
        persistState()
    }

    fun createNewSession() {
        vibrate(30)
        val newSession = ChatSession(
            title = "New Chat",
            personaId = _uiState.value.selectedPersonaId,
            messages = listOf(
                ChatMessage(
                    role = MessageRole.ASSISTANT,
                    content = "Hello! I am your offline AI Assistant. How can I help you today?"
                )
            )
        )
        _uiState.update { state ->
            state.copy(
                sessions = listOf(newSession) + state.sessions,
                currentSessionId = newSession.id
            )
        }
        persistState()
    }

    fun selectSession(sessionId: String) {
        _uiState.update { it.copy(currentSessionId = sessionId) }
        persistState()
    }

    fun togglePinSession(sessionId: String) {
        vibrate(20)
        _uiState.update { state ->
            val updated = state.sessions.map {
                if (it.id == sessionId) it.copy(isPinned = !it.isPinned) else it
            }
            state.copy(sessions = updated)
        }
        persistState()
    }

    fun showRenameDialog(session: ChatSession) {
        _uiState.update { it.copy(sessionToRename = session) }
    }

    fun hideRenameDialog() {
        _uiState.update { it.copy(sessionToRename = null) }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        _uiState.update { state ->
            val updated = state.sessions.map {
                if (it.id == sessionId) it.copy(title = newTitle.trim()) else it
            }
            state.copy(sessions = updated, sessionToRename = null)
        }
        persistState()
    }

    fun showDeleteDialog(session: ChatSession) {
        _uiState.update { it.copy(sessionToDelete = session) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(sessionToDelete = null) }
    }

    fun confirmDeleteSession() {
        val target = _uiState.value.sessionToDelete ?: return
        val sessionId = target.id
        vibrate(35)
        _uiState.update { state ->
            val remaining = state.sessions.filter { it.id != sessionId }
            val nextActiveId = if (state.currentSessionId == sessionId) {
                remaining.firstOrNull()?.id ?: ""
            } else {
                state.currentSessionId
            }
            state.copy(sessions = remaining, currentSessionId = nextActiveId, sessionToDelete = null)
        }
        if (_uiState.value.sessions.isEmpty()) {
            createNewSession()
        }
        persistState()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectPersona(personaId: String) {
        vibrate(20)
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
        persistState()
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _uiState.value.isGenerating) return

        vibrate(25)
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

        vibrate(30)
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
                responseBuilder.append("\n[Error during generation: ${e.localizedMessage}]")
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
                persistState()
            }
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        vibrate(40)
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
        persistState()
    }

    fun toggleSpeakMessage(message: ChatMessage) {
        vibrate(20)
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
        val (success, metadata) = engine.loadModel(uri, fileName)
        if (success) {
            _uiState.update { state ->
                state.copy(
                    modelConfig = state.modelConfig.copy(
                        modelUri = uri.toString(),
                        modelName = fileName
                    ),
                    modelMetadata = metadata
                )
            }
            persistState()
        }
    }

    fun updateConfig(config: ModelConfig) {
        _uiState.update { it.copy(modelConfig = config) }
        persistState()
    }

    fun clearCurrentChat() {
        vibrate(30)
        val currentId = _uiState.value.currentSessionId
        _uiState.update { state ->
            val updatedSessions = state.sessions.map { session ->
                if (session.id == currentId) {
                    session.copy(
                        messages = listOf(
                            ChatMessage(
                                role = MessageRole.ASSISTANT,
                                content = "Conversation cleared. Ready for your next question!"
                            )
                        )
                    )
                } else session
            }
            state.copy(sessions = updatedSessions)
        }
        persistState()
    }

    private fun vibrate(durationMs: Long) {
        try {
            val app = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }
}
