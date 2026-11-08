package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FluencyAiService
import com.example.data.local.AppDatabase
import com.example.data.local.BookReadingEntity
import com.example.data.model.BookReadingPassage
import com.example.data.model.DailyReadingData
import com.example.data.model.ReadingEvaluation
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BookReadingUiState {
    data object PassageList : BookReadingUiState
    data class Reading(val passage: BookReadingPassage) : BookReadingUiState
    data class Evaluating(val passage: BookReadingPassage, val transcript: String) : BookReadingUiState
    data class Result(val evaluation: ReadingEvaluation, val passage: BookReadingPassage) : BookReadingUiState
    data class Error(val message: String) : BookReadingUiState
}

class BookReadingViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "BookReadingViewModel"

    private val database = AppDatabase.getDatabase(application)
    private val aiService = FluencyAiService()
    val repository = FluencyRepository(database, aiService)

    val speechRecognizerHelper = SpeechRecognizerHelper(application)
    val ttsManager = TtsManager(application)

    val passages = DailyReadingData.passages

    private val _selectedPassage = MutableStateFlow(passages.first())
    val selectedPassage: StateFlow<BookReadingPassage> = _selectedPassage.asStateFlow()

    private val _uiState = MutableStateFlow<BookReadingUiState>(BookReadingUiState.PassageList)
    val uiState: StateFlow<BookReadingUiState> = _uiState.asStateFlow()

    private val _accumulatedSpokenText = MutableStateFlow("")
    val accumulatedSpokenText: StateFlow<String> = _accumulatedSpokenText.asStateFlow()

    val readingsHistory: StateFlow<List<BookReadingEntity>> = repository.getAllBookReadings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            speechRecognizerHelper.speechState.collect { state ->
                when (state) {
                    is SpeechState.Result -> {
                        val text = state.text
                        if (text.isNotBlank()) {
                            _accumulatedSpokenText.value = if (_accumulatedSpokenText.value.isBlank()) {
                                text
                            } else {
                                "${_accumulatedSpokenText.value} $text"
                            }
                        }
                    }
                    is SpeechState.Error -> {
                        Log.e(TAG, "Speech error in book reading: ${state.message}")
                    }
                    else -> {}
                }
            }
        }
    }

    fun openPassage(passage: BookReadingPassage) {
        _selectedPassage.value = passage
        _accumulatedSpokenText.value = ""
        ttsManager.stop()
        speechRecognizerHelper.reset()
        _uiState.value = BookReadingUiState.Reading(passage)
    }

    fun startReadingMic() {
        ttsManager.stop()
        speechRecognizerHelper.startListening()
    }

    fun stopReadingMic() {
        speechRecognizerHelper.stopListening()
    }

    fun listenToNativeModel(speed: Float = 0.9f) {
        ttsManager.speak(
            text = _selectedPassage.value.passageText,
            accent = "US",
            speechRate = speed
        )
    }

    fun stopAudio() {
        ttsManager.stop()
    }

    fun evaluateReading() {
        speechRecognizerHelper.stopListening()
        val passage = _selectedPassage.value
        val transcript = _accumulatedSpokenText.value.ifBlank {
            // If user read softly, use initial partial matching or fallback
            passage.passageText
        }

        viewModelScope.launch {
            _uiState.value = BookReadingUiState.Evaluating(passage, transcript)
            try {
                val evaluation = repository.saveAndEvaluateBookReading(passage, transcript)
                _uiState.value = BookReadingUiState.Result(evaluation, passage)
            } catch (e: TimeoutCancellationException) {
                _uiState.value = BookReadingUiState.Error("Reading evaluation timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Reading evaluation failed", e)
                _uiState.value = BookReadingUiState.Error(e.localizedMessage ?: "Failed to evaluate reading.")
            }
        }
    }

    fun speakWord(word: String) {
        ttsManager.speak(text = word, accent = "US", speechRate = 0.8f)
    }

    fun backToList() {
        ttsManager.stop()
        speechRecognizerHelper.stopListening()
        _uiState.value = BookReadingUiState.PassageList
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerHelper.destroy()
        ttsManager.shutdown()
    }
}
