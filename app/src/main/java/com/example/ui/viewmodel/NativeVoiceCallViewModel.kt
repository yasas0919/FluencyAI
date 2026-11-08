package com.example.ui.viewmodel

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FluencyAiService
import com.example.data.model.CallDebrief
import com.example.data.model.CallMessage
import com.example.data.model.CallStatus
import com.example.data.model.NativeCallAccent
import com.example.data.model.NativeCallData
import com.example.data.model.NativeCallPersona
import com.example.data.model.NativeCallScenario
import com.example.util.SpeechRecognizerHelper
import com.example.util.SpeechState
import com.example.util.TtsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NativeVoiceCallViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "NativeVoiceCallVM"

    val speechRecognizerHelper = SpeechRecognizerHelper(application)
    val ttsManager = TtsManager(application)
    private val aiService = FluencyAiService()

    private val _persona = MutableStateFlow(NativeCallData.personas.first())
    val persona: StateFlow<NativeCallPersona> = _persona.asStateFlow()

    private val _scenario = MutableStateFlow(NativeCallData.scenarios.first())
    val scenario: StateFlow<NativeCallScenario> = _scenario.asStateFlow()

    private val _accent = MutableStateFlow(NativeCallAccent.US)
    val accent: StateFlow<NativeCallAccent> = _accent.asStateFlow()

    private val _callStatus = MutableStateFlow(CallStatus.IDLE)
    val callStatus: StateFlow<CallStatus> = _callStatus.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _showCaptions = MutableStateFlow(true)
    val showCaptions: StateFlow<Boolean> = _showCaptions.asStateFlow()

    private val _messages = MutableStateFlow<List<CallMessage>>(emptyList())
    val messages: StateFlow<List<CallMessage>> = _messages.asStateFlow()

    private val _currentSpokenTranscript = MutableStateFlow("")
    val currentSpokenTranscript: StateFlow<String> = _currentSpokenTranscript.asStateFlow()

    private val _callDebrief = MutableStateFlow<CallDebrief?>(null)
    val callDebrief: StateFlow<CallDebrief?> = _callDebrief.asStateFlow()

    private var callTimerJob: Job? = null
    private var callStartTime = 0L

    init {
        // Speech Recognizer listener
        viewModelScope.launch {
            speechRecognizerHelper.speechState.collect { state ->
                when (state) {
                    is SpeechState.Partial -> {
                        if (_callStatus.value == CallStatus.LISTENING && !_isMuted.value) {
                            _currentSpokenTranscript.value = state.text
                        }
                    }
                    is SpeechState.Result -> {
                        if (_callStatus.value == CallStatus.LISTENING && !_isMuted.value) {
                            _currentSpokenTranscript.value = state.text
                            processUserVoiceTurn(state.text)
                        }
                    }
                    is SpeechState.Error -> {
                        Log.w(TAG, "Speech recognizer error during call: ${state.message}")
                        if (_callStatus.value == CallStatus.LISTENING && !_isMuted.value) {
                            // Automatically restart listening if still in call
                            delay(400)
                            if (_callStatus.value == CallStatus.LISTENING && !_isMuted.value) {
                                startUserListening()
                            }
                        }
                    }
                    SpeechState.Idle -> {}
                    SpeechState.Listening -> {}
                }
            }
        }

        // Monitor TTS completion to return to listening state
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                if (!speaking && _callStatus.value == CallStatus.AI_SPEAKING) {
                    delay(300)
                    if (_callStatus.value == CallStatus.AI_SPEAKING) {
                        _callStatus.value = CallStatus.LISTENING
                        if (!_isMuted.value) {
                            startUserListening()
                        }
                    }
                }
            }
        }
    }

    fun selectPersona(p: NativeCallPersona) {
        _persona.value = p
        _accent.value = p.defaultAccent
    }

    fun selectScenario(s: NativeCallScenario) {
        _scenario.value = s
    }

    fun setAccent(a: NativeCallAccent) {
        _accent.value = a
    }

    fun toggleMute() {
        val next = !_isMuted.value
        _isMuted.value = next
        if (next) {
            speechRecognizerHelper.stopListening()
        } else {
            if (_callStatus.value == CallStatus.LISTENING) {
                startUserListening()
            }
        }
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
    }

    fun toggleCaptions() {
        _showCaptions.value = !_showCaptions.value
    }

    fun startCall() {
        ttsManager.stop()
        speechRecognizerHelper.stopListening()
        _messages.value = emptyList()
        _callDebrief.value = null
        _currentSpokenTranscript.value = ""
        _callDurationSeconds.value = 0
        _isMuted.value = false

        _callStatus.value = CallStatus.CALLING

        viewModelScope.launch {
            // Simulated connection ring
            delay(1500)
            _callStatus.value = CallStatus.CONNECTED

            callStartTime = SystemClock.elapsedRealtime()
            callTimerJob?.cancel()
            callTimerJob = viewModelScope.launch {
                while (isActive && _callStatus.value != CallStatus.ENDED && _callStatus.value != CallStatus.IDLE) {
                    val sec = ((SystemClock.elapsedRealtime() - callStartTime) / 1000).toInt()
                    _callDurationSeconds.value = sec
                    delay(1000)
                }
            }

            // Speak initial scenario greeting
            val greeting = _scenario.value.initialGreeting
            addMessage(_persona.value.name, greeting, isUser = false)
            speakAiTurn(greeting)
        }
    }

    private fun speakAiTurn(text: String) {
        _callStatus.value = CallStatus.AI_SPEAKING
        speechRecognizerHelper.stopListening()
        ttsManager.speak(
            text = text,
            accent = _accent.value.code,
            speechRate = 1.0f
        )
    }

    private fun startUserListening() {
        if (_isMuted.value || _callStatus.value != CallStatus.LISTENING) return
        val lang = when (_accent.value) {
            NativeCallAccent.UK -> "en-GB"
            NativeCallAccent.AUS -> "en-AU"
            NativeCallAccent.US -> "en-US"
        }
        speechRecognizerHelper.startListening(lang)
    }

    private fun processUserVoiceTurn(userText: String) {
        if (userText.isBlank()) return
        // Pass full conversation history so Gemini remembers all previous turns and context
        val fullHistory = _messages.value.map { it.sender to it.text }
        addMessage("You", userText, isUser = true)
        _currentSpokenTranscript.value = ""

        viewModelScope.launch {
            _callStatus.value = CallStatus.AI_SPEAKING
            speechRecognizerHelper.stopListening()

            try {
                val aiReply = aiService.generateNativeVoiceCallTurn(
                    scenarioTitle = _scenario.value.title,
                    scenarioCategory = _scenario.value.category,
                    scenarioDescription = _scenario.value.description,
                    aiRole = _scenario.value.aiRole,
                    userRole = _scenario.value.userRole,
                    suggestedTopics = _scenario.value.suggestedTopics,
                    personaName = _persona.value.name,
                    personaAccentLabel = _accent.value.label,
                    personaAccentRegion = _accent.value.regionNote,
                    personaVoiceTone = _persona.value.voiceTone,
                    conversationHistory = fullHistory,
                    latestUserUtterance = userText
                )

                addMessage(_persona.value.name, aiReply, isUser = false)
                speakAiTurn(aiReply)
            } catch (e: Exception) {
                Log.e(TAG, "Call turn processing error: ${e.message}")
                val fallbackReply = aiService.buildDynamicCallFallback(
                    personaName = _persona.value.name,
                    aiRole = _scenario.value.aiRole,
                    scenarioTitle = _scenario.value.title,
                    scenarioCategory = _scenario.value.category,
                    conversationHistory = fullHistory,
                    latestUtterance = userText
                )
                addMessage(_persona.value.name, fallbackReply, isUser = false)
                speakAiTurn(fallbackReply)
            }
        }
    }

    private fun addMessage(sender: String, text: String, isUser: Boolean) {
        val msg = CallMessage(
            sender = sender,
            text = text,
            isUser = isUser
        )
        _messages.value = _messages.value + msg
    }

    fun endCall() {
        ttsManager.stop()
        speechRecognizerHelper.stopListening()
        callTimerJob?.cancel()

        _callStatus.value = CallStatus.ENDED

        val duration = _callDurationSeconds.value
        val userMessages = _messages.value.filter { it.isUser }
        val turns = userMessages.size

        val totalWords = userMessages.sumOf { it.text.split("\\s+".toRegex()).size }
        val fluencyScore = if (turns > 0) (82 + (totalWords / 10).coerceAtMost(14)).coerceIn(75, 96) else 80
        val clarityScore = (84 + (turns * 2)).coerceIn(78, 95)
        val accentScore = when (_accent.value) {
            NativeCallAccent.US -> 88
            NativeCallAccent.UK -> 86
            NativeCallAccent.AUS -> 85
        }

        val accentTip = when (_accent.value) {
            NativeCallAccent.US -> "Maintain clear rhotic /r/ coloring in syllable codas ('hard', 'car') and use the alveolar flap [ɾ] for intervocalic 't' ('water')."
            NativeCallAccent.UK -> "Practice non-rhotic vowel elongation and crisp, non-flapped /t/ consonants ('better', 'city')."
            NativeCallAccent.AUS -> "Relax diphthongs /aɪ/ and /eɪ/ into broader Australian vowels and maintain warm rising vocal inflection."
        }

        _callDebrief.value = CallDebrief(
            personaName = _persona.value.name,
            scenarioTitle = _scenario.value.title,
            accentUsed = _accent.value,
            callDurationSeconds = duration,
            totalTurnsSpoken = turns,
            overallFluencyScore = fluencyScore,
            pronunciationClarity = clarityScore,
            accentAdherenceScore = accentScore,
            praiseMessage = if (turns >= 2) "Dynamic, confident conversational exchange! You kept the dialogue natural and responsive." else "Good call initiation! Try speaking longer turns next time.",
            keyStrengths = listOf(
                "Immediate spontaneous reply cadence",
                "Context-appropriate vocabulary in roleplay"
            ),
            accentTip = accentTip,
            recommendedExpressions = listOf(
                "To put it in perspective...",
                "From my standpoint...",
                "Couldn't agree more, and furthermore..."
            )
        )
    }

    fun resetToDialer() {
        ttsManager.stop()
        speechRecognizerHelper.reset()
        callTimerJob?.cancel()
        _callStatus.value = CallStatus.IDLE
        _messages.value = emptyList()
        _callDebrief.value = null
        _currentSpokenTranscript.value = ""
        _callDurationSeconds.value = 0
        _isMuted.value = false
    }

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
        speechRecognizerHelper.destroy()
        ttsManager.shutdown()
    }
}
