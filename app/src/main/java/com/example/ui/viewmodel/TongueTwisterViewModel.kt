package com.example.ui.viewmodel

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.TongueTwisterData
import com.example.data.model.TongueTwisterDifficulty
import com.example.data.model.TongueTwisterEvaluation
import com.example.data.model.TongueTwisterItem
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

class TongueTwisterViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "TongueTwisterVM"

    val speechRecognizerHelper = SpeechRecognizerHelper(application)
    val ttsManager = TtsManager(application)

    private val _selectedDifficulty = MutableStateFlow<TongueTwisterDifficulty?>(null)
    val selectedDifficulty: StateFlow<TongueTwisterDifficulty?> = _selectedDifficulty.asStateFlow()

    private val _currentTwister = MutableStateFlow(TongueTwisterData.twisters.first())
    val currentTwister: StateFlow<TongueTwisterItem> = _currentTwister.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0f)
    val elapsedSeconds: StateFlow<Float> = _elapsedSeconds.asStateFlow()

    private val _liveSpokenText = MutableStateFlow("")
    val liveSpokenText: StateFlow<String> = _liveSpokenText.asStateFlow()

    // Map of word index in current twister -> whether recognized correctly so far
    private val _matchedWordsIndex = MutableStateFlow<Set<Int>>(emptySet())
    val matchedWordsIndex: StateFlow<Set<Int>> = _matchedWordsIndex.asStateFlow()

    private val _evaluation = MutableStateFlow<TongueTwisterEvaluation?>(null)
    val evaluation: StateFlow<TongueTwisterEvaluation?> = _evaluation.asStateFlow()

    private val _ttsRate = MutableStateFlow(1.0f)
    val ttsRate: StateFlow<Float> = _ttsRate.asStateFlow()

    private var timerJob: Job? = null
    private var recordingStartTime = 0L

    init {
        // Collect speech recognizer state
        viewModelScope.launch {
            speechRecognizerHelper.speechState.collect { state ->
                when (state) {
                    is SpeechState.Partial -> {
                        _liveSpokenText.value = state.text
                        updateRealtimeWordMatch(state.text)
                    }
                    is SpeechState.Result -> {
                        _liveSpokenText.value = state.text
                        updateRealtimeWordMatch(state.text)
                        stopAndEvaluate(state.text)
                    }
                    is SpeechState.Error -> {
                        Log.e(TAG, "Speech recognition error: ${state.message}")
                        if (_isRecording.value) {
                            stopAndEvaluate(_liveSpokenText.value)
                        }
                    }
                    SpeechState.Idle -> {}
                    SpeechState.Listening -> {}
                }
            }
        }
    }

    fun setDifficulty(diff: TongueTwisterDifficulty?) {
        _selectedDifficulty.value = diff
        val filtered = getFilteredTwisters(diff)
        if (filtered.isNotEmpty() && _currentTwister.value !in filtered) {
            selectTwister(filtered.first())
        }
    }

    fun getFilteredTwisters(diff: TongueTwisterDifficulty? = _selectedDifficulty.value): List<TongueTwisterItem> {
        return if (diff == null) {
            TongueTwisterData.twisters
        } else {
            TongueTwisterData.twisters.filter { it.difficulty == diff }
        }
    }

    fun selectTwister(item: TongueTwisterItem) {
        ttsManager.stop()
        speechRecognizerHelper.stopListening()
        timerJob?.cancel()
        _isRecording.value = false
        _currentTwister.value = item
        _liveSpokenText.value = ""
        _matchedWordsIndex.value = emptySet()
        _elapsedSeconds.value = 0f
        _evaluation.value = null
    }

    fun startListening() {
        ttsManager.stop()
        _evaluation.value = null
        _liveSpokenText.value = ""
        _matchedWordsIndex.value = emptySet()
        _elapsedSeconds.value = 0f
        _isRecording.value = true

        recordingStartTime = SystemClock.elapsedRealtime()
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _isRecording.value) {
                val elapsed = (SystemClock.elapsedRealtime() - recordingStartTime) / 1000f
                _elapsedSeconds.value = elapsed
                delay(50)
            }
        }

        speechRecognizerHelper.startListening("en-US")
    }

    fun stopListeningManually() {
        if (!_isRecording.value) return
        speechRecognizerHelper.stopListening()
        stopAndEvaluate(_liveSpokenText.value)
    }

    private fun stopAndEvaluate(spokenTranscript: String) {
        _isRecording.value = false
        timerJob?.cancel()

        val duration = (_elapsedSeconds.value).coerceAtLeast(0.5f)
        val twister = _currentTwister.value
        val targetTokens = tokenize(twister.text)
        val spokenTokens = tokenize(spokenTranscript)

        var matchedCount = 0
        val mispronounced = mutableListOf<String>()

        val spokenSet = spokenTokens.map { it.lowercase() }.toSet()
        val matchedIndices = mutableSetOf<Int>()

        targetTokens.forEachIndexed { index, token ->
            val clean = token.lowercase()
            if (clean in spokenSet || isFuzzyMatch(clean, spokenTokens)) {
                matchedCount++
                matchedIndices.add(index)
            } else {
                if (clean.length > 2) {
                    mispronounced.add(token)
                }
            }
        }

        _matchedWordsIndex.value = matchedIndices

        val accuracyScore = ((matchedCount.toFloat() / targetTokens.size.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)
        val minutes = duration / 60.0f
        val wpm = if (minutes > 0f) ((targetTokens.size) / minutes).toInt().coerceIn(0, 300) else 0
        val beatTarget = wpm >= twister.targetWpm && accuracyScore >= 80

        val praise = when {
            accuracyScore >= 90 && beatTarget -> "Incredible Speed & Precision! You beat the ${twister.targetWpm} WPM challenge!"
            accuracyScore >= 80 -> "Great articulation! Clear consonant delivery."
            accuracyScore >= 60 -> "Good speed, but watch out for sound blending on tricky phonemes."
            else -> "Keep warming up! Take it slowly before speeding up."
        }

        _evaluation.value = TongueTwisterEvaluation(
            accuracyScore = accuracyScore,
            wordsPerMinute = wpm,
            elapsedSeconds = duration,
            totalWords = targetTokens.size,
            correctWordsCount = matchedCount,
            isTargetWpmBeaten = beatTarget,
            mispronouncedWords = mispronounced.distinct().take(3),
            feedbackPraise = praise,
            coachingTip = twister.tip
        )
    }

    private fun updateRealtimeWordMatch(spokenTranscript: String) {
        val targetTokens = tokenize(_currentTwister.value.text)
        val spokenTokens = tokenize(spokenTranscript).map { it.lowercase() }.toSet()

        val matched = mutableSetOf<Int>()
        targetTokens.forEachIndexed { index, token ->
            if (token.lowercase() in spokenTokens) {
                matched.add(index)
            }
        }
        _matchedWordsIndex.value = matched
    }

    private fun tokenize(text: String): List<String> {
        return text.split("\\s+".toRegex())
            .map { it.replace("[^A-Za-z0-9']".toRegex(), "").trim() }
            .filter { it.isNotBlank() }
    }

    private fun isFuzzyMatch(target: String, spokenList: List<String>): Boolean {
        return spokenList.any { s ->
            s.startsWith(target.take(3)) || target.startsWith(s.take(3))
        }
    }

    fun playModelAudio(slowMotion: Boolean = false) {
        val rate = if (slowMotion) 0.75f else 1.0f
        _ttsRate.value = rate
        ttsManager.speak(
            text = _currentTwister.value.text,
            accent = "US",
            speechRate = rate
        )
    }

    fun stopModelAudio() {
        ttsManager.stop()
    }

    fun resetDrill() {
        ttsManager.stop()
        speechRecognizerHelper.reset()
        timerJob?.cancel()
        _isRecording.value = false
        _liveSpokenText.value = ""
        _matchedWordsIndex.value = emptySet()
        _elapsedSeconds.value = 0f
        _evaluation.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        speechRecognizerHelper.destroy()
        ttsManager.shutdown()
    }
}
