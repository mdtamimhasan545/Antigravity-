package com.localaichat.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.localaichat.app.ui.screens.ChatScreen
import com.localaichat.app.ui.screens.SettingsScreen
import com.localaichat.app.ui.theme.LocalAIChatAppTheme
import com.localaichat.app.ui.theme.Slate950
import com.localaichat.app.viewmodel.ChatViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    // File picker contract for selecting .gguf model files
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val fileName = getFileName(it) ?: "custom_model.gguf"
            viewModel.setModelUri(it, fileName)
            Toast.makeText(this, "Model selected: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    // Speech-to-Text recognition launcher
    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            spokenText?.let {
                viewModel.sendMessage(it)
            }
        }
    }

    // Audio permission request
    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognizer()
        } else {
            Toast.makeText(this, "Microphone permission is required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            LocalAIChatAppTheme(darkTheme = uiState.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Slate950
                ) {
                    AppNavigation()
                }
            }
        }
    }

    @Composable
    private fun AppNavigation() {
        val uiState by viewModel.uiState.collectAsState()
        var currentScreen by remember { mutableStateOf("chat") }

        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                "chat" -> {
                    ChatScreen(
                        uiState = uiState,
                        onSendMessage = { viewModel.sendMessage(it) },
                        onStopGeneration = { viewModel.stopGeneration() },
                        onRegenerateLastMessage = { viewModel.regenerateLastMessage() },
                        onSpeakMessage = { viewModel.toggleSpeakMessage(it) },
                        onStartVoiceInput = { checkVoicePermissionAndStart() },
                        onNewChat = { viewModel.createNewSession() },
                        onSelectSession = { viewModel.selectSession(it) },
                        onTogglePinSession = { viewModel.togglePinSession(it) },
                        onShowRenameDialog = { viewModel.showRenameDialog(it) },
                        onRenameSession = { id, title -> viewModel.renameSession(id, title) },
                        onHideRenameDialog = { viewModel.hideRenameDialog() },
                        onShowDeleteDialog = { viewModel.showDeleteDialog(it) },
                        onConfirmDeleteSession = { viewModel.confirmDeleteSession() },
                        onHideDeleteDialog = { viewModel.hideDeleteDialog() },
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onSelectPersona = { viewModel.selectPersona(it) },
                        onClearChat = { viewModel.clearCurrentChat() },
                        onShareChat = { viewModel.shareChat(this@MainActivity) },
                        onNavigateToSettings = { currentScreen = "settings" }
                    )
                }
                "settings" -> {
                    SettingsScreen(
                        currentConfig = uiState.modelConfig,
                        modelMetadata = uiState.modelMetadata,
                        isDarkTheme = uiState.isDarkTheme,
                        onToggleTheme = { viewModel.toggleTheme() },
                        onSelectModelClick = {
                            openDocumentLauncher.launch(arrayOf("*/*"))
                        },
                        onSaveConfig = { viewModel.updateConfig(it) },
                        onNavigateBack = { currentScreen = "chat" }
                    )
                }
            }
        }
    }

    private fun checkVoicePermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognizer()
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your message...")
        }
        try {
            speechRecognitionLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Speech recognition is not supported on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
