package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FluencyAiService
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.SavedWordEntity
import com.example.data.repository.FluencyRepository
import com.example.util.TtsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OverallStats(
    val totalSessions: Int = 0,
    val avgFluency: Int = 0,
    val avgPronunciation: Int = 0,
    val totalWordsMastered: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val aiService = FluencyAiService()
    val repository = FluencyRepository(database, aiService)

    val conversations: StateFlow<List<ConversationEntity>> = repository.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedWords: StateFlow<List<SavedWordEntity>> = repository.getAllSavedWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<OverallStats> = repository.getAllConversations().map { list ->
        val totalTurns = list.sumOf { it.totalTurns }
        val avgFluency = if (list.isNotEmpty()) list.map { it.avgFluencyScore }.filter { it > 0 }.let {
            if (it.isNotEmpty()) it.average().toInt() else 0
        } else 0
        val avgPron = if (list.isNotEmpty()) list.map { it.avgPronunciationScore }.filter { it > 0 }.let {
            if (it.isNotEmpty()) it.average().toInt() else 0
        } else 0

        OverallStats(
            totalSessions = list.size,
            avgFluency = avgFluency,
            avgPronunciation = avgPron,
            totalWordsMastered = 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStats())
}

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val aiService = FluencyAiService()
    val repository = FluencyRepository(database, aiService)
    val ttsManager = TtsManager(application)

    val savedWords: StateFlow<List<SavedWordEntity>> = repository.getAllSavedWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleMastered(word: SavedWordEntity) {
        viewModelScope.launch {
            repository.toggleWordMastered(word)
        }
    }

    fun deleteWord(wordId: Long) {
        viewModelScope.launch {
            repository.deleteSavedWord(wordId)
        }
    }

    fun speakWord(word: String, speed: Float = 0.85f) {
        ttsManager.speak(text = word, accent = "US", speechRate = speed)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
