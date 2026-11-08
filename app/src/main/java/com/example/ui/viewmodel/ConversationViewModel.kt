package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FluencyAiService
import com.example.data.local.AppDatabase
import com.example.data.local.MessageEntity
import com.example.data.model.AiPersona
import com.example.data.model.FeedbackResult
import com.example.data.model.Scenario
import com.example.data.model.ScenarioData
import com.example.data.repository.FluencyRepository
import com.example.util.SpeechRecognizerHelper
import com.example.util.SpeechState
import com.example.util.TtsManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ConversationUiState {
    data object Idle : ConversationUiState
    data object Listening : ConversationUiState
    data class Processing(val userText: String) : ConversationUiState
    data class Error(val message: String) : ConversationUiState
}

class ConversationViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ConversationViewModel"

    private val database = AppDatabase.getDatabase(application)
    private val aiService = FluencyAiService()
    val repository = FluencyRepository(database, aiService)

    val speechRecognizerHelper = SpeechRecognizerHelper(application)
    val ttsManager = TtsManager(application)

    // Current Active Session
    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    private val _activeScenario = MutableStateFlow(ScenarioData.scenarios.first())
    val activeScenario: StateFlow<Scenario> = _activeScenario.asStateFlow()

    private val _activePersona = MutableStateFlow(ScenarioData.personas.first())
    val activePersona: StateFlow<AiPersona> = _activePersona.asStateFlow()

    // UI Status
    private val _uiState = MutableStateFlow<ConversationUiState>(ConversationUiState.Idle)
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private val _latestFeedback = MutableStateFlow<FeedbackResult?>(null)
    val latestFeedback: StateFlow<FeedbackResult?> = _latestFeedback.asStateFlow()

    private val _showFeedbackSheet = MutableStateFlow(false)
    val showFeedbackSheet: StateFlow<Boolean> = _showFeedbackSheet.asStateFlow()

    // Settings
    private val _ttsRate = MutableStateFlow(1.0f)
    val ttsRate: StateFlow<Float> = _ttsRate.asStateFlow()

    private val _autoPlayAiAudio = MutableStateFlow(true)
    val autoPlayAiAudio: StateFlow<Boolean> = _autoPlayAiAudio.asStateFlow()

    // Reactive list of messages for current conversation
    val messages: StateFlow<List<MessageEntity>> = _activeConversationId.flatMapLatest { convId ->
        if (convId != null) {
            repository.getConversationMessages(convId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe speech recognition results
        viewModelScope.launch {
            speechRecognizerHelper.speechState.collect { state ->
                when (state) {
                    is SpeechState.Result -> {
                        val text = state.text
                        speechRecognizerHelper.reset()
                        if (text.isNotBlank()) {
                            submitUserResponse(text)
                        }
                    }
                    is SpeechState.Error -> {
                        _uiState.value = ConversationUiState.Error(state.message)
                    }
                    is SpeechState.Listening -> {
                        _uiState.value = ConversationUiState.Listening
                    }
                    else -> {}
                }
            }
        }
    }

    fun startSession(scenario: Scenario, persona: AiPersona) {
        _activeScenario.value = scenario
        _activePersona.value = persona
        _latestFeedback.value = null
        _showFeedbackSheet.value = false

        viewModelScope.launch {
            try {
                val convId = repository.startNewConversation(scenario, persona)
                _activeConversationId.value = convId
                _uiState.value = ConversationUiState.Idle

                // Speak greeting if auto play enabled
                if (_autoPlayAiAudio.value) {
                    ttsManager.speak(
                        text = scenario.initialAiGreeting,
                        accent = persona.accent,
                        speechRate = _ttsRate.value
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start conversation", e)
                _uiState.value = ConversationUiState.Error("Failed to start session: ${e.localizedMessage}")
            }
        }
    }

    fun resumeSession(conversationId: Long, scenario: Scenario, persona: AiPersona) {
        _activeConversationId.value = conversationId
        _activeScenario.value = scenario
        _activePersona.value = persona
        _uiState.value = ConversationUiState.Idle
    }

    fun startListening() {
        ttsManager.stop()
        _uiState.value = ConversationUiState.Listening
        speechRecognizerHelper.startListening()
    }

    fun stopListening() {
        speechRecognizerHelper.stopListening()
    }

    fun submitUserResponse(userText: String) {
        val convId = _activeConversationId.value ?: return
        val scenario = _activeScenario.value
        val persona = _activePersona.value

        viewModelScope.launch {
            _uiState.value = ConversationUiState.Processing(userText)
            try {
                val feedback = repository.processUserSpokenResponse(
                    conversationId = convId,
                    scenario = scenario,
                    persona = persona,
                    userUtterance = userText
                )
                _latestFeedback.value = feedback
                _showFeedbackSheet.value = true
                _uiState.value = ConversationUiState.Idle

                // Automatically speak the AI response
                if (_autoPlayAiAudio.value) {
                    ttsManager.speak(
                        text = feedback.aiReply,
                        accent = persona.accent,
                        speechRate = _ttsRate.value
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI evaluation timed out", e)
                _uiState.value = ConversationUiState.Error("Feedback generation timed out after 60s. Please check your connection.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI evaluation failed", e)
                val rawMsg = e.localizedMessage ?: "Failed to process pronunciation feedback."
                val formatted = if (rawMsg.contains("App Check", ignoreCase = true) || rawMsg.contains("403", ignoreCase = true)) {
                    "App Check authentication error. Tap the 🛡️ Shield icon at top-right to view App Check status and debug token."
                } else {
                    rawMsg
                }
                _uiState.value = ConversationUiState.Error(formatted)
            }
        }
    }

    fun speakText(text: String, accentOverride: String? = null) {
        ttsManager.speak(
            text = text,
            accent = accentOverride ?: _activePersona.value.accent,
            speechRate = _ttsRate.value
        )
    }

    fun stopSpeech() {
        ttsManager.stop()
    }

    fun setTtsRate(rate: Float) {
        _ttsRate.value = rate
    }

    fun toggleAutoPlayAudio() {
        _autoPlayAiAudio.value = !_autoPlayAiAudio.value
    }

    fun setFeedbackSheetVisible(visible: Boolean) {
        _showFeedbackSheet.value = visible
    }

    fun setLatestFeedback(feedback: FeedbackResult?) {
        _latestFeedback.value = feedback
        if (feedback != null) {
            _showFeedbackSheet.value = true
        }
    }

    fun clearError() {
        _uiState.value = ConversationUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerHelper.destroy()
        ttsManager.shutdown()
    }
}
