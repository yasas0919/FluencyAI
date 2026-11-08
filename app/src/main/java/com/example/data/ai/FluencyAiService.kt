package com.example.data.ai

import android.util.Log
import com.example.data.model.DrillFeedback
import com.example.data.model.FeedbackResult
import com.example.data.model.GrammarCorrection
import com.example.data.model.PresentationFeedback
import com.example.data.model.PresentationOutline
import com.example.data.model.PresentationSlide
import com.example.data.model.PronunciationItem
import com.example.data.model.ReadingEvaluation
import com.example.data.model.SlideFeedbackNote
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

class FluencyAiService {

    private val TAG = "FluencyAiService"

    // Schema for Conversation Evaluation + AI Turn
    private val conversationEvaluationSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "aiReply" to Schema.string(description = "The AI conversational response continuing the dialogue in character"),
                "fluencyScore" to Schema.integer(description = "Spoken fluency score from 0 to 100"),
                "pronunciationScore" to Schema.integer(description = "Estimated pronunciation accuracy score from 0 to 100"),
                "grammarScore" to Schema.integer(description = "Grammar and phrasing score from 0 to 100"),
                "feedbackSummary" to Schema.string(description = "1-2 sentence friendly, actionable feedback summary"),
                "pronunciationItems" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "word" to Schema.string(description = "The word to analyze"),
                            "phoneticIpa" to Schema.string(description = "IPA phonetic transcription"),
                            "syllableStress" to Schema.string(description = "Syllable breakdown with capitalized stress, e.g. COM-for-ta-ble"),
                            "tip" to Schema.string(description = "Specific mouth and tongue placement guidance"),
                            "severity" to Schema.string(description = "good, needs_work, or minor")
                        )
                    ),
                    description = "Key words from user response with phonetic guidance"
                ),
                "hasGrammarCorrection" to Schema.boolean(description = "True if grammar or natural phrasing can be improved"),
                "grammarOriginal" to Schema.string(description = "What user said"),
                "grammarAlternative" to Schema.string(description = "Natural native speaker phrasing"),
                "grammarExplanation" to Schema.string(description = "Brief explanation of rule or nuance"),
                "vocabularyBooster" to Schema.string(description = "A sophisticated vocabulary alternative or idiom"),
                "suggestedReplies" to Schema.array(
                    items = Schema.string(),
                    description = "2-3 short sample phrases the user could say next"
                )
            ),
            optionalProperties = listOf("grammarOriginal", "grammarAlternative", "grammarExplanation", "vocabularyBooster")
        )
    }

    private val conversationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = conversationEvaluationSchema
                temperature = 0.7f
            }
        )
    }

    // Schema for Pronunciation Drill Evaluation
    private val drillEvaluationSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "score" to Schema.integer(description = "Pronunciation accuracy score 0 to 100"),
                "accuracyLabel" to Schema.string(description = "Native-like, Excellent, Clear, or Needs Work"),
                "phoneticComparison" to Schema.string(description = "Breakdown of how target phonemes were produced"),
                "anatomicalTip" to Schema.string(description = "Physical mouth, lips, tongue placement instruction"),
                "commonMistake" to Schema.string(description = "What sound was substituted and how to fix it"),
                "isSuccess" to Schema.boolean(description = "True if score is 75 or above")
            )
        )
    }

    private val drillModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = drillEvaluationSchema
                temperature = 0.3f
            }
        )
    }

    // Schema for Presentation Slide Generation
    private val presentationOutlineSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "topic" to Schema.string(description = "Presentation topic"),
                "slides" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "slideNumber" to Schema.integer(description = "Slide index 1 to 4"),
                            "title" to Schema.string(description = "Slide title"),
                            "bulletPoints" to Schema.array(items = Schema.string(), description = "Key bullet points"),
                            "keyVocabulary" to Schema.array(items = Schema.string(), description = "Key executive/technical terms"),
                            "speakerNotes" to Schema.string(description = "Recommended speaking prompts and transitions")
                        )
                    ),
                    description = "List of presentation slides"
                )
            )
        )
    }

    private val presentationOutlineModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = presentationOutlineSchema
                temperature = 0.7f
            }
        )
    }

    // Schema for Presentation Delivery Evaluation
    private val presentationEvaluationSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "scoreOutOfTen" to Schema.double(description = "Score out of 10, e.g. 8.8"),
                "ratingBadge" to Schema.string(description = "Executive Ready, Strong Delivery, Clear Communicator, or Needs Polish"),
                "pronunciationScore" to Schema.integer(description = "Pronunciation accuracy score 0-100"),
                "grammarScore" to Schema.integer(description = "Professional grammar and phrasing score 0-100"),
                "pacingScore" to Schema.integer(description = "Cadence, rhythm, and pacing score 0-100"),
                "wordsPerMinute" to Schema.integer(description = "Estimated words per minute"),
                "fillerWordCount" to Schema.integer(description = "Total count of filler words"),
                "fillerWordsList" to Schema.array(items = Schema.string(), description = "Examples of filler words used"),
                "feedbackSummary" to Schema.string(description = "Executive evaluation summary"),
                "strengths" to Schema.array(items = Schema.string(), description = "Key strengths in delivery"),
                "improvements" to Schema.array(items = Schema.string(), description = "Key areas to improve"),
                "slideNotes" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "slideNumber" to Schema.integer(description = "Slide index"),
                            "title" to Schema.string(description = "Slide title"),
                            "remark" to Schema.string(description = "Critique for this slide")
                        )
                    ),
                    description = "Slide specific delivery feedback"
                ),
                "pronunciationTips" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "word" to Schema.string(description = "Word needing polish"),
                            "phoneticIpa" to Schema.string(description = "Standard IPA transcription"),
                            "syllableStress" to Schema.string(description = "Capitalized syllable stress"),
                            "tip" to Schema.string(description = "Anatomical mouth/tongue guidance"),
                            "severity" to Schema.string(description = "needs_work or minor")
                        )
                    ),
                    description = "Pronunciation tips on technical or stressed words"
                )
            )
        )
    }

    private val presentationEvaluationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = presentationEvaluationSchema
                temperature = 0.4f
            }
        )
    }

    // Schema for Book Reading Evaluation
    private val readingEvaluationSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "accuracyScore" to Schema.integer(description = "Reading accuracy score from 0 to 100"),
                "wordsPerMinute" to Schema.integer(description = "Estimated reading speed in words per minute"),
                "feedbackMessage" to Schema.string(description = "Encouraging coaching feedback on reading rhythm and clarity"),
                "mispronouncedWords" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "word" to Schema.string(description = "Word with pronunciation or stress flaw"),
                            "phoneticIpa" to Schema.string(description = "IPA transcription"),
                            "syllableStress" to Schema.string(description = "Syllable breakdown with capitalized stress"),
                            "tip" to Schema.string(description = "Anatomical mouth and tongue guidance"),
                            "severity" to Schema.string(description = "needs_work or minor")
                        )
                    ),
                    description = "Mispronounced or skipped words"
                )
            )
        )
    }

    private val readingEvaluationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = readingEvaluationSchema
                temperature = 0.3f
            }
        )
    }

    suspend fun evaluateUserTurn(
        scenarioTitle: String,
        scenarioRoleA: String,
        scenarioRoleB: String,
        personaName: String,
        personaAccent: String,
        conversationHistory: List<Pair<String, String>>,
        userUtterance: String
    ): FeedbackResult = withContext(Dispatchers.IO) {
        val historyPrompt = conversationHistory.takeLast(6).joinToString("\n") { (sender, text) ->
            "$sender: $text"
        }

        val prompt = """
            You are an elite AI Spoken English Coach & Conversation Partner named $personaName ($personaAccent accent).
            Scenario: $scenarioTitle
            AI Role: $scenarioRoleA
            User Role: $scenarioRoleB
            
            Conversation so far:
            $historyPrompt
            
            User just spoke: "$userUtterance"
            
            TASK:
            1. Respond naturally as $scenarioRoleA, keeping the conversation fluid, authentic, and engaging.
            2. Analyze what the user said for:
               - Spoken fluency (pacing, coherence, naturalness: 0-100)
               - Pronunciation accuracy (phonetic correctness of key words, syllable stress, sound clarity: 0-100)
               - Grammar & Idiomatic correctness (0-100)
            3. Highlight 1 to 3 words from what they said with precise IPA phonetics, syllable stress (e.g. COM-for-ta-ble), and concrete anatomical mouth/tongue tips.
            4. If there is unnatural grammar or word choice, provide a natural native alternative and clear explanation.
            5. Suggest 1 vocabulary booster and 2-3 quick suggested replies they could speak next.
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                conversationModel.generateContent(prompt)
            }

            val jsonText = response.text ?: throw IllegalStateException("AI returned an empty response")
            val json = JSONObject(jsonText)
            val aiReply = json.optString("aiReply", "That's very interesting, tell me more!")
            val fluency = json.optInt("fluencyScore", 82).coerceIn(0, 100)
            val pronunciation = json.optInt("pronunciationScore", 80).coerceIn(0, 100)
            val grammar = json.optInt("grammarScore", 85).coerceIn(0, 100)
            val summary = json.optString("feedbackSummary", "Good communication flow and clear expression.")

            val pronArray = json.optJSONArray("pronunciationItems")
            val pronList = mutableListOf<PronunciationItem>()
            if (pronArray != null) {
                for (i in 0 until pronArray.length()) {
                    val item = pronArray.getJSONObject(i)
                    pronList.add(
                        PronunciationItem(
                            word = item.optString("word", ""),
                            phoneticIpa = item.optString("phoneticIpa", ""),
                            syllableStress = item.optString("syllableStress", ""),
                            tip = item.optString("tip", ""),
                            severity = item.optString("severity", "needs_work")
                        )
                    )
                }
            }

            val hasGrammar = json.optBoolean("hasGrammarCorrection", false)
            val grammarCorrection = if (hasGrammar && json.has("grammarOriginal") && json.has("grammarAlternative")) {
                GrammarCorrection(
                    originalSegment = json.optString("grammarOriginal"),
                    betterAlternative = json.optString("grammarAlternative"),
                    explanation = json.optString("grammarExplanation")
                )
            } else null

            val vocabBooster = json.optString("vocabularyBooster").takeIf { it.isNotBlank() }

            val suggestedArray = json.optJSONArray("suggestedReplies")
            val suggestions = mutableListOf<String>()
            if (suggestedArray != null) {
                for (i in 0 until suggestedArray.length()) {
                    suggestions.add(suggestedArray.getString(i))
                }
            }
            if (suggestions.isEmpty()) {
                suggestions.add("I understand your point, let me elaborate.")
                suggestions.add("Could you clarify what you mean by that?")
            }

            FeedbackResult(
                aiReply = aiReply,
                fluencyScore = fluency,
                pronunciationScore = pronunciation,
                grammarScore = grammar,
                feedbackSummary = summary,
                pronunciationItems = pronList,
                grammarCorrection = grammarCorrection,
                vocabularyBooster = vocabBooster,
                suggestedReplies = suggestions
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to evaluate user turn with Firebase AI", e)
            throw e
        }
    }

    suspend fun evaluatePronunciationDrill(
        targetPhrase: String,
        targetPhoneticIpa: String,
        targetSound: String,
        soundInstruction: String,
        userSpokenTranscript: String
    ): DrillFeedback = withContext(Dispatchers.IO) {
        val prompt = """
            You are a master English Phonetics and Pronunciation Coach.
            Target Phrase to pronounce: "$targetPhrase"
            Target Phonetic IPA: $targetPhoneticIpa
            Focus Sound: $targetSound
            Articulatory Guideline: $soundInstruction
            
            The user attempted to pronounce it and the speech recognizer transcribed: "$userSpokenTranscript"
            
            Evaluate how accurately they pronounced the target sound and phrase.
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                drillModel.generateContent(prompt)
            }

            val jsonText = response.text ?: throw IllegalStateException("Empty drill evaluation response")
            val json = JSONObject(jsonText)
            val score = json.optInt("score", 75).coerceIn(0, 100)
            val label = json.optString("accuracyLabel", if (score >= 80) "Great Job" else "Keep Practicing")
            val comparison = json.optString("phoneticComparison", "Target: $targetPhoneticIpa")
            val tip = json.optString("anatomicalTip", soundInstruction)
            val mistake = json.optString("commonMistake", "Sound substitution or reduced vowel")
            val isSuccess = json.optBoolean("isSuccess", score >= 75)

            DrillFeedback(
                score = score,
                accuracyLabel = label,
                phoneticComparison = comparison,
                anatomicalTip = tip,
                commonMistake = mistake,
                isSuccess = isSuccess
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed drill evaluation", e)
            throw e
        }
    }

    suspend fun generatePresentationOutline(topic: String): PresentationOutline = withContext(Dispatchers.IO) {
        val prompt = """
            You are a premier executive communication coach and presentation designer.
            Create an impactful 3-to-4 slide presentation outline on the topic: "$topic".
            For each slide provide:
            - slideNumber (1, 2, 3...)
            - title (Engaging, punchy slide title)
            - bulletPoints (3 concise, high-value bullet points)
            - keyVocabulary (2 advanced or technical English vocabulary terms to practice)
            - speakerNotes (Script prompts and smooth transition phrases for the speaker)
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                presentationOutlineModel.generateContent(prompt)
            }

            val jsonText = response.text ?: throw IllegalStateException("Empty presentation outline")
            val json = JSONObject(jsonText)
            val slidesArray = json.getJSONArray("slides")
            val slides = mutableListOf<PresentationSlide>()

            for (i in 0 until slidesArray.length()) {
                val sObj = slidesArray.getJSONObject(i)
                val bpArray = sObj.getJSONArray("bulletPoints")
                val bpList = (0 until bpArray.length()).map { bpArray.getString(it) }

                val vocabArray = sObj.optJSONArray("keyVocabulary")
                val vocabList = if (vocabArray != null) {
                    (0 until vocabArray.length()).map { vocabArray.getString(it) }
                } else emptyList()

                slides.add(
                    PresentationSlide(
                        slideNumber = sObj.optInt("slideNumber", i + 1),
                        title = sObj.getString("title"),
                        bulletPoints = bpList,
                        keyVocabulary = vocabList,
                        speakerNotes = sObj.optString("speakerNotes", "")
                    )
                )
            }

            PresentationOutline(
                topic = json.optString("topic", topic),
                slides = slides
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate presentation outline", e)
            throw e
        }
    }

    suspend fun evaluatePresentation(
        topic: String,
        slidesOutlineText: String,
        spokenTranscript: String,
        durationSeconds: Int
    ): PresentationFeedback = withContext(Dispatchers.IO) {
        val wordCount = spokenTranscript.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val minutes = (durationSeconds / 60.0).coerceAtLeast(0.2)
        val calculatedWpm = (wordCount / minutes).toInt()

        val prompt = """
            You are a world-class speech and executive communication evaluator.
            Presentation Topic: "$topic"
            Slide Outline:
            $slidesOutlineText
            
            Speaker Duration: $durationSeconds seconds ($calculatedWpm approximate words per minute)
            Speaker Spoken Transcript:
            "$spokenTranscript"
            
            Evaluate the speaker's presentation thoroughly:
            1. Overall score out of 10 (e.g. 8.7)
            2. Rating badge (Executive Ready, Strong Delivery, Clear Communicator, or Needs Polish)
            3. Pronunciation accuracy (0-100)
            4. Professional grammar and eloquence (0-100)
            5. Pacing, rhythm and cadence (0-100)
            6. Filler words count and list (e.g. "um", "uh", "like", "you know")
            7. Executive feedback summary
            8. 2-3 key strengths in delivery
            9. 2-3 specific actionable improvements
            10. Slide-by-slide remarks
            11. 2-4 pronunciation tips on mispronounced or tricky words
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                presentationEvaluationModel.generateContent(prompt)
            }

            val jsonText = response.text ?: throw IllegalStateException("Empty presentation evaluation")
            val json = JSONObject(jsonText)

            val score = json.optDouble("scoreOutOfTen", 8.2).toFloat().coerceIn(1.0f, 10.0f)
            val badge = json.optString("ratingBadge", if (score >= 8.5f) "Executive Ready" else "Strong Delivery")
            val pronScore = json.optInt("pronunciationScore", 80).coerceIn(0, 100)
            val grammarScore = json.optInt("grammarScore", 85).coerceIn(0, 100)
            val pacingScore = json.optInt("pacingScore", 82).coerceIn(0, 100)
            val wpm = json.optInt("wordsPerMinute", calculatedWpm)
            val fillerCount = json.optInt("fillerWordCount", 2)

            val fillerArray = json.optJSONArray("fillerWordsList")
            val fillers = if (fillerArray != null) {
                (0 until fillerArray.length()).map { fillerArray.getString(it) }
            } else emptyList()

            val summary = json.optString("feedbackSummary", "Well-structured presentation delivery with clear arguments.")

            val strengthsArray = json.optJSONArray("strengths")
            val strengths = if (strengthsArray != null) {
                (0 until strengthsArray.length()).map { strengthsArray.getString(it) }
            } else listOf("Confident opening", "Clear thematic flow")

            val improvementsArray = json.optJSONArray("improvements")
            val improvements = if (improvementsArray != null) {
                (0 until improvementsArray.length()).map { improvementsArray.getString(it) }
            } else listOf("Reduce verbal pauses", "Vary vocal pitch for emphasis")

            val slideNotesArray = json.optJSONArray("slideNotes")
            val slideNotes = mutableListOf<SlideFeedbackNote>()
            if (slideNotesArray != null) {
                for (i in 0 until slideNotesArray.length()) {
                    val sObj = slideNotesArray.getJSONObject(i)
                    slideNotes.add(
                        SlideFeedbackNote(
                            slideNumber = sObj.optInt("slideNumber", i + 1),
                            title = sObj.optString("title", "Slide ${i + 1}"),
                            remark = sObj.optString("remark", "Solid delivery on this section.")
                        )
                    )
                }
            }

            val pronArray = json.optJSONArray("pronunciationTips")
            val pronTips = mutableListOf<PronunciationItem>()
            if (pronArray != null) {
                for (i in 0 until pronArray.length()) {
                    val pObj = pronArray.getJSONObject(i)
                    pronTips.add(
                        PronunciationItem(
                            word = pObj.optString("word", ""),
                            phoneticIpa = pObj.optString("phoneticIpa", ""),
                            syllableStress = pObj.optString("syllableStress", ""),
                            tip = pObj.optString("tip", ""),
                            severity = pObj.optString("severity", "needs_work")
                        )
                    )
                }
            }

            PresentationFeedback(
                scoreOutOfTen = score,
                ratingBadge = badge,
                pronunciationScore = pronScore,
                grammarScore = grammarScore,
                pacingScore = pacingScore,
                wordsPerMinute = wpm,
                fillerWordCount = fillerCount,
                fillerWordsList = fillers,
                feedbackSummary = summary,
                strengths = strengths,
                improvements = improvements,
                slideNotes = slideNotes,
                pronunciationTips = pronTips
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to evaluate presentation", e)
            throw e
        }
    }

    suspend fun evaluateBookReading(
        passageText: String,
        spokenTranscript: String
    ): ReadingEvaluation = withContext(Dispatchers.IO) {
        val wordCount = spokenTranscript.split("\\s+".toRegex()).filter { it.isNotBlank() }.size

        val prompt = """
            You are a master English Phonetics & Oral Reading Coach.
            The user read this excerpt aloud:
            Target Text:
            "$passageText"
            
            Speech Recognizer Transcribed:
            "$spokenTranscript"
            
            Analyze the reading accuracy and articulation:
            1. accuracyScore (0 to 100 percentage of words read correctly and clearly)
            2. wordsPerMinute (estimated oral reading speed)
            3. feedbackMessage (1-2 sentence coaching advice on phrasing and articulation)
            4. mispronouncedWords (array of words from the target text that were mispronounced, skipped, or slurred, with IPA, syllable stress, and mouth/tongue tips)
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                readingEvaluationModel.generateContent(prompt)
            }

            val jsonText = response.text ?: throw IllegalStateException("Empty reading evaluation response")
            val json = JSONObject(jsonText)

            val accuracy = json.optInt("accuracyScore", 88).coerceIn(0, 100)
            val wpm = json.optInt("wordsPerMinute", (wordCount * 2).coerceIn(90, 180))
            val msg = json.optString("feedbackMessage", "Great expression and rhythm in reading this passage!")

            val misArray = json.optJSONArray("mispronouncedWords")
            val misList = mutableListOf<PronunciationItem>()
            if (misArray != null) {
                for (i in 0 until misArray.length()) {
                    val mObj = misArray.getJSONObject(i)
                    misList.add(
                        PronunciationItem(
                            word = mObj.optString("word", ""),
                            phoneticIpa = mObj.optString("phoneticIpa", ""),
                            syllableStress = mObj.optString("syllableStress", ""),
                            tip = mObj.optString("tip", ""),
                            severity = mObj.optString("severity", "needs_work")
                        )
                    )
                }
            }

            ReadingEvaluation(
                accuracyScore = accuracy,
                wordsPerMinute = wpm,
                mispronouncedWords = misList,
                feedbackMessage = msg
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to evaluate reading passage", e)
            throw e
        }
    }
}
