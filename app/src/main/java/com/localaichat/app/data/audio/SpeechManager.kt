package com.localaichat.app.data.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized: Boolean = false

    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _currentlyPlayingId.value = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    _currentlyPlayingId.value = null
                }

                override fun onError(utteranceId: String?) {
                    _currentlyPlayingId.value = null
                }
            })
            isInitialized = true
        }
    }

    fun speak(messageId: String, text: String, speed: Float = 1.0f) {
        if (!isInitialized || tts == null) return

        if (_currentlyPlayingId.value == messageId) {
            stop()
            return
        }

        stop()
        tts?.setSpeechRate(speed)
        // Clean markdown code blocks from speech text
        val cleanText = text.replace(Regex("```[\\s\\S]*?```"), "Code snippet omitted.")
            .replace(Regex("[#*_`\\[\\]]"), "")

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, messageId)
        _currentlyPlayingId.value = messageId
    }

    fun stop() {
        tts?.stop()
        _currentlyPlayingId.value = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
