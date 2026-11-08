package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) {

    private val TAG = "TtsManager"
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                textToSpeech?.language = Locale.US
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        _currentUtteranceId.value = utteranceId
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentUtteranceId.value = null
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentUtteranceId.value = null
                        Log.e(TAG, "TTS playback error for utterance: $utteranceId")
                    }
                })
                Log.d(TAG, "TTS successfully initialized")
            } else {
                Log.e(TAG, "TTS initialization failed with status $status")
            }
        }
    }

    fun speak(
        text: String,
        accent: String = "US",
        speechRate: Float = 1.0f,
        utteranceId: String = System.currentTimeMillis().toString()
    ) {
        if (!isInitialized || textToSpeech == null) {
            Log.w(TAG, "TTS not ready yet")
            return
        }

        try {
            val locale = when (accent.uppercase()) {
                "UK", "GB" -> Locale.UK
                "AU" -> Locale("en", "AU")
                "CA" -> Locale.CANADA
                else -> Locale.US
            }

            textToSpeech?.language = locale
            textToSpeech?.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            textToSpeech?.setPitch(1.0f)

            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to speak text", e)
        }
    }

    fun stop() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop TTS", e)
        }
        _isSpeaking.value = false
        _currentUtteranceId.value = null
    }

    fun shutdown() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to shutdown TTS", e)
        }
        textToSpeech = null
        isInitialized = false
    }
}
