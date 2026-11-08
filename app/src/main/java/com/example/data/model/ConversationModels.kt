package com.example.data.model

data class Scenario(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val roleA: String, // AI role
    val roleB: String, // User role
    val initialAiGreeting: String,
    val promptContext: String,
    val level: String, // "Beginner", "Intermediate", "Advanced"
    val iconName: String
)

data class AiPersona(
    val id: String,
    val name: String,
    val title: String,
    val description: String,
    val accent: String, // "US", "UK", "AU"
    val avatarEmoji: String
)

data class PronunciationItem(
    val word: String,
    val phoneticIpa: String,
    val syllableStress: String,
    val tip: String,
    val severity: String // "good", "needs_work", "minor"
)

data class GrammarCorrection(
    val originalSegment: String,
    val betterAlternative: String,
    val explanation: String
)

data class FeedbackResult(
    val aiReply: String,
    val fluencyScore: Int,
    val pronunciationScore: Int,
    val grammarScore: Int,
    val feedbackSummary: String,
    val pronunciationItems: List<PronunciationItem>,
    val grammarCorrection: GrammarCorrection?,
    val vocabularyBooster: String?,
    val suggestedReplies: List<String>
)

data class PronunciationDrill(
    val id: String,
    val phrase: String,
    val phoneticIpa: String,
    val targetSound: String,
    val soundCategory: String, // "Vowels", "Consonants", "Ending -ed", "Word Stress", "Tongue Twister"
    val instruction: String,
    val difficulty: String
)

data class DrillFeedback(
    val score: Int,
    val accuracyLabel: String,
    val phoneticComparison: String,
    val anatomicalTip: String,
    val commonMistake: String,
    val isSuccess: Boolean
)
