package com.example.data.model

enum class NativeCallAccent(val code: String, val label: String, val flag: String, val regionNote: String) {
    US("US", "American", "🇺🇸", "General American (Neutral, Clear Rhotic)"),
    UK("UK", "British", "🇬🇧", "Received Pronunciation (Non-Rhotic, Crisp)"),
    AUS("AU", "Australian", "🇦🇺", "Australian English (Broad Vowels, Uplifting Cadence)")
}

enum class CallStatus {
    IDLE,
    CALLING,
    CONNECTED,
    LISTENING,
    AI_SPEAKING,
    ENDED
}

data class NativeCallPersona(
    val id: String,
    val name: String,
    val title: String,
    val defaultAccent: NativeCallAccent,
    val avatarEmoji: String,
    val bio: String,
    val voiceTone: String
)

data class NativeCallScenario(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val aiRole: String,
    val userRole: String,
    val initialGreeting: String,
    val suggestedTopics: List<String>
)

data class CallMessage(
    val id: String = System.currentTimeMillis().toString(),
    val sender: String, // "You" or persona name
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class CallDebrief(
    val personaName: String,
    val scenarioTitle: String,
    val accentUsed: NativeCallAccent,
    val callDurationSeconds: Int,
    val totalTurnsSpoken: Int,
    val overallFluencyScore: Int,
    val pronunciationClarity: Int,
    val accentAdherenceScore: Int,
    val praiseMessage: String,
    val keyStrengths: List<String>,
    val accentTip: String,
    val recommendedExpressions: List<String>
)
