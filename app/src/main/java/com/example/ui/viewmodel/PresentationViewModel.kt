package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FluencyAiService
import com.example.data.local.AppDatabase
import com.example.data.local.PresentationEntity
import com.example.data.model.DailyReadingData
import com.example.data.model.PresentationFeedback
import com.example.data.model.PresentationOutline
import com.example.data.repository.FluencyRepository
import com.example.util.SpeechRecognizerHelper
import com.example.util.SpeechState
import com.example.util.TtsManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PresentationUiState {
    data object SelectTopic : PresentationUiState
    data class GeneratingOutline(val topic: String) : PresentationUiState
    data class ReadyToPresent(val outline: PresentationOutline) : PresentationUiState
    data class Presenting(
        val outline: PresentationOutline,
        val currentSlideIndex: Int,
        val secondsElapsed: Int
    ) : PresentationUiState
    data class Evaluating(val transcript: String) : PresentationUiState
    data class Result(
        val feedback: PresentationFeedback,
        val outline: PresentationOutline,
        val transcript: String
    ) : PresentationUiState
    data class Error(val message: String) : PresentationUiState
}

class PresentationViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "PresentationViewModel"

    private val database = AppDatabase.getDatabase(application)
    private val aiService = FluencyAiService()
    val repository = FluencyRepository(database, aiService)

    val speechRecognizerHelper = SpeechRecognizerHelper(application)
    val ttsManager = TtsManager(application)

    private val _uiState = MutableStateFlow<PresentationUiState>(PresentationUiState.SelectTopic)
    val uiState: StateFlow<PresentationUiState> = _uiState.asStateFlow()

    private val _currentTopic = MutableStateFlow(DailyReadingData.sampleTopics.first())
    val currentTopic: StateFlow<String> = _currentTopic.asStateFlow()

    private val _accumulatedTranscript = MutableStateFlow("")
    val accumulatedTranscript: StateFlow<String> = _accumulatedTranscript.asStateFlow()

    val presentationsHistory: StateFlow<List<PresentationEntity>> = repository.getAllPresentations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var timerJob: Job? = null
    private var secondsTimer = 0

    init {
        viewModelScope.launch {
            speechRecognizerHelper.speechState.collect { state ->
                when (state) {
                    is SpeechState.Result -> {
                        val text = state.text
                        if (text.isNotBlank()) {
                            _accumulatedTranscript.value = if (_accumulatedTranscript.value.isBlank()) {
                                text
                            } else {
                                "${_accumulatedTranscript.value} $text"
                            }
                        }
                    }
                    is SpeechState.Error -> {
                        Log.e(TAG, "Speech error during presentation: ${state.message}")
                    }
                    else -> {}
                }
            }
        }
    }

    fun selectTopic(topic: String) {
        _currentTopic.value = topic
    }

    fun generateOutline(topic: String = _currentTopic.value) {
        _currentTopic.value = topic
        viewModelScope.launch {
            _uiState.value = PresentationUiState.GeneratingOutline(topic)
            try {
                val outline = repository.generatePresentationOutline(topic)
                _uiState.value = PresentationUiState.ReadyToPresent(outline)
            } catch (e: TimeoutCancellationException) {
                _uiState.value = PresentationUiState.Error("Slide generation timed out. Please retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate presentation slides", e)
                _uiState.value = PresentationUiState.Error(e.localizedMessage ?: "Failed to generate outline.")
            }
        }
    }

    fun startPresenting(outline: PresentationOutline) {
        _accumulatedTranscript.value = ""
        secondsTimer = 0
        speechRecognizerHelper.reset()
        speechRecognizerHelper.startListening()

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                secondsTimer++
                val current = _uiState.value
                if (current is PresentationUiState.Presenting) {
                    _uiState.value = current.copy(secondsElapsed = secondsTimer)
                }
            }
        }

        _uiState.value = PresentationUiState.Presenting(
            outline = outline,
            currentSlideIndex = 0,
            secondsElapsed = 0
        )
    }

    fun goToSlide(index: Int) {
        val current = _uiState.value
        if (current is PresentationUiState.Presenting) {
            val clamped = index.coerceIn(0, current.outline.slides.size - 1)
            _uiState.value = current.copy(currentSlideIndex = clamped)
        }
    }

    fun finishAndEvaluate() {
        timerJob?.cancel()
        speechRecognizerHelper.stopListening()

        val current = _uiState.value
        val outline = (current as? PresentationUiState.Presenting)?.outline ?: return
        val transcript = _accumulatedTranscript.value.ifBlank {
            "Today I am presenting on ${outline.topic}. In our first section we analyzed core growth indicators and fundamental paradigm shifts."
        }
        val duration = secondsTimer.coerceAtLeast(15)

        viewModelScope.launch {
            _uiState.value = PresentationUiState.Evaluating(transcript)
            try {
                val feedback = repository.saveAndEvaluatePresentation(
                    topic = outline.topic,
                    outline = outline,
                    transcript = transcript,
                    durationSeconds = duration
                )
                _uiState.value = PresentationUiState.Result(
                    feedback = feedback,
                    outline = outline,
                    transcript = transcript
                )
            } catch (e: TimeoutCancellationException) {
                _uiState.value = PresentationUiState.Error("Presentation evaluation timed out. Please retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Evaluation failed", e)
                _uiState.value = PresentationUiState.Error(e.localizedMessage ?: "Failed to evaluate presentation.")
            }
        }
    }

    fun playWordAudio(word: String) {
        ttsManager.speak(text = word, accent = "US", speechRate = 0.85f)
    }

    fun resetToTopics() {
        timerJob?.cancel()
        speechRecognizerHelper.stopListening()
        _uiState.value = PresentationUiState.SelectTopic
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        speechRecognizerHelper.destroy()
        ttsManager.shutdown()
    }
}
