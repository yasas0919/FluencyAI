package com.example.data.repository

import com.example.data.ai.FluencyAiService
import com.example.data.local.AppDatabase
import com.example.data.local.BookReadingEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PresentationEntity
import com.example.data.local.SavedWordEntity
import com.example.data.model.AiPersona
import com.example.data.model.BookReadingPassage
import com.example.data.model.DrillFeedback
import com.example.data.model.FeedbackResult
import com.example.data.model.GrammarCorrection
import com.example.data.model.PresentationFeedback
import com.example.data.model.PresentationOutline
import com.example.data.model.PronunciationDrill
import com.example.data.model.PronunciationItem
import com.example.data.model.ReadingEvaluation
import com.example.data.model.Scenario
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class FluencyRepository(
    private val database: AppDatabase,
    private val aiService: FluencyAiService
) {
    private val dao = database.fluencyDao()

    suspend fun startNewConversation(scenario: Scenario, persona: AiPersona): Long {
        val conversation = ConversationEntity(
            scenarioId = scenario.id,
            scenarioTitle = scenario.title,
            personaId = persona.id,
            personaName = persona.name,
            startedAt = System.currentTimeMillis()
        )
        val conversationId = dao.insertConversation(conversation)

        val initialMessage = MessageEntity(
            conversationId = conversationId,
            sender = "ai",
            text = scenario.initialAiGreeting,
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(initialMessage)

        return conversationId
    }

    fun getConversationMessages(conversationId: Long): Flow<List<MessageEntity>> {
        return dao.getMessagesForConversation(conversationId)
    }

    fun getAllConversations(): Flow<List<ConversationEntity>> {
        return dao.getAllConversations()
    }

    suspend fun getConversation(conversationId: Long): ConversationEntity? {
        return dao.getConversationById(conversationId)
    }

    suspend fun processUserSpokenResponse(
        conversationId: Long,
        scenario: Scenario,
        persona: AiPersona,
        userUtterance: String
    ): FeedbackResult {
        val previousMessages = dao.getMessagesListForConversation(conversationId)
        val historyPairs = previousMessages.map { it.sender to it.text }

        val feedback = aiService.evaluateUserTurn(
            scenarioTitle = scenario.title,
            scenarioRoleA = scenario.roleA,
            scenarioRoleB = scenario.roleB,
            personaName = persona.name,
            personaAccent = persona.accent,
            conversationHistory = historyPairs,
            userUtterance = userUtterance
        )

        val pronJson = JSONArray().apply {
            feedback.pronunciationItems.forEach { item ->
                put(JSONObject().apply {
                    put("word", item.word)
                    put("phoneticIpa", item.phoneticIpa)
                    put("syllableStress", item.syllableStress)
                    put("tip", item.tip)
                    put("severity", item.severity)
                })
            }
        }.toString()

        val repliesJson = JSONArray().apply {
            feedback.suggestedReplies.forEach { put(it) }
        }.toString()

        val userMessage = MessageEntity(
            conversationId = conversationId,
            sender = "user",
            text = userUtterance,
            timestamp = System.currentTimeMillis(),
            fluencyScore = feedback.fluencyScore,
            pronunciationScore = feedback.pronunciationScore,
            grammarScore = feedback.grammarScore,
            feedbackSummary = feedback.feedbackSummary,
            pronunciationJson = pronJson,
            grammarOriginal = feedback.grammarCorrection?.originalSegment.orEmpty(),
            grammarBetter = feedback.grammarCorrection?.betterAlternative.orEmpty(),
            grammarExplanation = feedback.grammarCorrection?.explanation.orEmpty(),
            vocabularyBooster = feedback.vocabularyBooster.orEmpty(),
            suggestedRepliesJson = repliesJson
        )
        dao.insertMessage(userMessage)

        val aiMessage = MessageEntity(
            conversationId = conversationId,
            sender = "ai",
            text = feedback.aiReply,
            timestamp = System.currentTimeMillis() + 10
        )
        dao.insertMessage(aiMessage)

        val allUpdated = dao.getMessagesListForConversation(conversationId)
        val userTurns = allUpdated.filter { it.sender == "user" }
        val avgFluency = if (userTurns.isNotEmpty()) userTurns.map { it.fluencyScore }.average().toInt() else 0
        val avgPron = if (userTurns.isNotEmpty()) userTurns.map { it.pronunciationScore }.average().toInt() else 0

        val existingConv = dao.getConversationById(conversationId)
        if (existingConv != null) {
            dao.updateConversation(
                existingConv.copy(
                    totalTurns = userTurns.size,
                    avgFluencyScore = avgFluency,
                    avgPronunciationScore = avgPron
                )
            )
        }

        feedback.pronunciationItems.filter { it.severity == "needs_work" }.forEach { item ->
            saveOrUpdateWord(item)
        }

        return feedback
    }

    suspend fun saveOrUpdateWord(item: PronunciationItem) {
        if (item.word.isBlank()) return
        val existing = dao.findSavedWord(item.word.trim())
        if (existing == null) {
            dao.insertSavedWord(
                SavedWordEntity(
                    word = item.word.trim(),
                    phoneticIpa = item.phoneticIpa,
                    syllableStress = item.syllableStress,
                    tip = item.tip,
                    timesPracticed = 1,
                    lastScore = 70
                )
            )
        } else {
            dao.updateSavedWord(
                existing.copy(
                    timesPracticed = existing.timesPracticed + 1,
                    tip = if (item.tip.isNotBlank()) item.tip else existing.tip
                )
            )
        }
    }

    suspend fun evaluateDrill(drill: PronunciationDrill, spokenTranscript: String): DrillFeedback {
        return aiService.evaluatePronunciationDrill(
            targetPhrase = drill.phrase,
            targetPhoneticIpa = drill.phoneticIpa,
            targetSound = drill.targetSound,
            soundInstruction = drill.instruction,
            userSpokenTranscript = spokenTranscript
        )
    }

    // AI Presentation Mode
    suspend fun generatePresentationOutline(topic: String): PresentationOutline {
        return aiService.generatePresentationOutline(topic)
    }

    suspend fun saveAndEvaluatePresentation(
        topic: String,
        outline: PresentationOutline,
        transcript: String,
        durationSeconds: Int
    ): PresentationFeedback {
        val outlineJson = JSONArray().apply {
            outline.slides.forEach { s ->
                put(JSONObject().apply {
                    put("slideNumber", s.slideNumber)
                    put("title", s.title)
                    put("bulletPoints", JSONArray(s.bulletPoints))
                    put("keyVocabulary", JSONArray(s.keyVocabulary))
                    put("speakerNotes", s.speakerNotes)
                })
            }
        }.toString()

        val feedback = aiService.evaluatePresentation(
            topic = topic,
            slidesOutlineText = outlineJson,
            spokenTranscript = transcript,
            durationSeconds = durationSeconds
        )

        val slideNotesJson = JSONArray().apply {
            feedback.slideNotes.forEach { sn ->
                put(JSONObject().apply {
                    put("slideNumber", sn.slideNumber)
                    put("title", sn.title)
                    put("remark", sn.remark)
                })
            }
        }.toString()

        dao.insertPresentation(
            PresentationEntity(
                topic = topic,
                slidesJson = outlineJson,
                transcript = transcript,
                scoreOutOfTen = feedback.scoreOutOfTen,
                pronunciationScore = feedback.pronunciationScore,
                grammarScore = feedback.grammarScore,
                pacingScore = feedback.pacingScore,
                wordsPerMinute = feedback.wordsPerMinute,
                fillerWordCount = feedback.fillerWordCount,
                fillerWordsSummary = feedback.fillerWordsList.joinToString(", "),
                feedbackSummary = feedback.feedbackSummary,
                strengths = feedback.strengths.joinToString("\n• ", prefix = "• "),
                improvements = feedback.improvements.joinToString("\n• ", prefix = "• "),
                slideNotesJson = slideNotesJson,
                durationSeconds = durationSeconds,
                timestamp = System.currentTimeMillis()
            )
        )

        // Save flagged technical/stressed words
        feedback.pronunciationTips.forEach { item ->
            saveOrUpdateWord(item)
        }

        return feedback
    }

    fun getAllPresentations(): Flow<List<PresentationEntity>> = dao.getAllPresentations()
    fun getRecentPresentations(): Flow<List<PresentationEntity>> = dao.getRecentPresentations()
    fun getAveragePresentationScore(): Flow<Double?> = dao.getAveragePresentationScore()
    fun getBestPresentationScore(): Flow<Float?> = dao.getBestPresentationScore()

    // Daily Book Reading Mode
    suspend fun saveAndEvaluateBookReading(
        passage: BookReadingPassage,
        spokenTranscript: String
    ): ReadingEvaluation {
        val evaluation = aiService.evaluateBookReading(
            passageText = passage.passageText,
            spokenTranscript = spokenTranscript
        )

        val misJson = JSONArray().apply {
            evaluation.mispronouncedWords.forEach { item ->
                put(JSONObject().apply {
                    put("word", item.word)
                    put("phoneticIpa", item.phoneticIpa)
                    put("syllableStress", item.syllableStress)
                    put("tip", item.tip)
                })
            }
        }.toString()

        dao.insertBookReading(
            BookReadingEntity(
                bookTitle = passage.bookTitle,
                author = passage.author,
                excerptText = passage.passageText,
                spokenTranscript = spokenTranscript,
                accuracyScore = evaluation.accuracyScore,
                wordsPerMinute = evaluation.wordsPerMinute,
                mispronouncedWordsJson = misJson,
                timestamp = System.currentTimeMillis()
            )
        )

        // Save mispronounced words to user's notebook
        evaluation.mispronouncedWords.forEach { item ->
            saveOrUpdateWord(item)
        }

        return evaluation
    }

    fun getAllBookReadings(): Flow<List<BookReadingEntity>> = dao.getAllBookReadings()
    fun getAverageReadingAccuracy(): Flow<Double?> = dao.getAverageReadingAccuracy()

    // Word Bank & Stats
    fun getAllSavedWords(): Flow<List<SavedWordEntity>> = dao.getAllSavedWords()
    suspend fun toggleWordMastered(word: SavedWordEntity) {
        dao.updateSavedWord(word.copy(isMastered = !word.isMastered))
    }
    suspend fun deleteSavedWord(id: Long) {
        dao.deleteSavedWord(id)
    }
    fun getOverallAverageFluency(): Flow<Double?> = dao.getOverallAverageFluency()
    fun getOverallAveragePronunciation(): Flow<Double?> = dao.getOverallAveragePronunciation()
    fun getTotalSessionsCount(): Flow<Int> = dao.getTotalSessionsCount()
}
