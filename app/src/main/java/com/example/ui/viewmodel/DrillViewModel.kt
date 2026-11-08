package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FluencyAiService
import com.example.data.local.AppDatabase
import com.example.data.model.DrillFeedback
import com.example.data.model.PronunciationDrill
import com.example.data.model.ScenarioData
import com.example.data.repository.FluencyRepository
import com.example.util.SpeechRecognizerHelper
import com.example.util.SpeechState
import com.example.util.TtsManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DrillUiState {
    data object Idle : DrillUiState
    data object Listening : DrillUiState
    data class Evaluating(val transcript: String) : DrillUiState
    data class Success(val feedback: DrillFeedback) : DrillUiState
    data class Error(val message: String) : DrillUiState
}

class DrillViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "DrillViewModel"

    private val database = AppDatabase.getDatabase(application)
    private val aiService = FluencyAiService()
    val repository = FluencyRepository(database, aiService)

    val speechRecognizerHelper = SpeechRecognizerHelper(application)
    val ttsManager = TtsManager(application)

    val drills = ScenarioData.drills

    private val _selectedDrill = MutableStateFlow(drills.first())
    val selectedDrill: StateFlow<PronunciationDrill> = _selectedDrill.asStateFlow()

    private val _uiState = MutableStateFlow<DrillUiState>(DrillUiState.Idle)
    val uiState: StateFlow<DrillUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            speechRecognizerHelper.speechState.collect { state ->
                when (state) {
                    is SpeechState.Result -> {
                        val text = state.text
                        speechRecognizerHelper.reset()
                        if (text.isNotBlank()) {
                            evaluateAttempt(text)
                        }
                    }
                    is SpeechState.Error -> {
                        _uiState.value = DrillUiState.Error(state.message)
                    }
                    is SpeechState.Listening -> {
                        _uiState.value = DrillUiState.Listening
                    }
                    else -> {}
                }
            }
        }
    }

    fun selectDrill(drill: PronunciationDrill) {
        _selectedDrill.value = drill
        _uiState.value = DrillUiState.Idle
        ttsManager.stop()
    }

    fun playTargetAudio(speed: Float = 0.9f) {
        ttsManager.speak(
            text = _selectedDrill.value.phrase,
            accent = "US",
            speechRate = speed
        )
    }

    fun startListening() {
        ttsManager.stop()
        _uiState.value = DrillUiState.Listening
        speechRecognizerHelper.startListening()
    }

    fun stopListening() {
        speechRecognizerHelper.stopListening()
    }

    fun evaluateAttempt(spokenText: String) {
        val drill = _selectedDrill.value
        viewModelScope.launch {
            _uiState.value = DrillUiState.Evaluating(spokenText)
            try {
                val feedback = repository.evaluateDrill(drill, spokenText)
                _uiState.value = DrillUiState.Success(feedback)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Drill evaluation timed out", e)
                _uiState.value = DrillUiState.Error("Evaluation timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Drill evaluation error", e)
                val rawMsg = e.localizedMessage ?: "Failed to evaluate pronunciation."
                val formatted = if (rawMsg.contains("App Check", ignoreCase = true) || rawMsg.contains("403", ignoreCase = true)) {
                    "App Check authentication error. Please verify network connection or device attestation."
                } else {
                    rawMsg
                }
                _uiState.value = DrillUiState.Error(formatted)
            }
        }
    }

    fun resetDrillState() {
        _uiState.value = DrillUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerHelper.destroy()
        ttsManager.shutdown()
    }
}
