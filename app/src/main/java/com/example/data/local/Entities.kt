package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: String,
    val scenarioTitle: String,
    val personaId: String,
    val personaName: String,
    val startedAt: Long = System.currentTimeMillis(),
    val totalTurns: Int = 0,
    val avgFluencyScore: Int = 0,
    val avgPronunciationScore: Int = 0
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    // Evaluation fields (populated for user messages)
    val fluencyScore: Int = 0,
    val pronunciationScore: Int = 0,
    val grammarScore: Int = 0,
    val feedbackSummary: String = "",
    val pronunciationJson: String = "", // serialized items
    val grammarOriginal: String = "",
    val grammarBetter: String = "",
    val grammarExplanation: String = "",
    val vocabularyBooster: String = "",
    val suggestedRepliesJson: String = ""
)

@Entity(tableName = "saved_words")
data class SavedWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val phoneticIpa: String,
    val syllableStress: String,
    val tip: String,
    val timesPracticed: Int = 1,
    val lastScore: Int = 0,
    val isMastered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "presentations")
data class PresentationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val slidesJson: String,
    val transcript: String,
    val scoreOutOfTen: Float, // e.g. 8.7
    val pronunciationScore: Int, // 0 - 100
    val grammarScore: Int, // 0 - 100
    val pacingScore: Int, // 0 - 100
    val wordsPerMinute: Int,
    val fillerWordCount: Int,
    val fillerWordsSummary: String,
    val feedbackSummary: String,
    val strengths: String,
    val improvements: String,
    val slideNotesJson: String,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "book_readings")
data class BookReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookTitle: String,
    val author: String,
    val excerptText: String,
    val spokenTranscript: String,
    val accuracyScore: Int, // 0 - 100
    val wordsPerMinute: Int,
    val mispronouncedWordsJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
