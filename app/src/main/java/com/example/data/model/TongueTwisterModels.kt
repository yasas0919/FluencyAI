package com.example.data.model

enum class TongueTwisterDifficulty(val label: String, val badgeColorHex: Long) {
    WARM_UP("Warm-Up", 0xFF06B6D4),        // Cyan
    AGILITY("Agility", 0xFF10B981),        // Emerald
    MASTER("Master", 0xFF8B5CF6),          // Violet
    SPEED_DEMON("Speed Demon", 0xFFF59E0B) // Amber
}

data class TongueTwisterItem(
    val id: String,
    val title: String,
    val text: String,
    val phoneticFocus: String,
    val difficulty: TongueTwisterDifficulty,
    val targetWpm: Int,
    val tip: String,
    val highlightWords: List<String> = emptyList()
)

data class TongueTwisterEvaluation(
    val accuracyScore: Int, // 0 - 100%
    val wordsPerMinute: Int,
    val elapsedSeconds: Float,
    val totalWords: Int,
    val correctWordsCount: Int,
    val isTargetWpmBeaten: Boolean,
    val mispronouncedWords: List<String>,
    val feedbackPraise: String,
    val coachingTip: String
)
