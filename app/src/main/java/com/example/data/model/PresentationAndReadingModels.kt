package com.example.data.model

data class PresentationSlide(
    val slideNumber: Int,
    val title: String,
    val bulletPoints: List<String>,
    val keyVocabulary: List<String>,
    val speakerNotes: String
)

data class PresentationOutline(
    val topic: String,
    val slides: List<PresentationSlide>
)

data class SlideFeedbackNote(
    val slideNumber: Int,
    val title: String,
    val remark: String
)

data class PresentationFeedback(
    val scoreOutOfTen: Float, // e.g. 8.7
    val ratingBadge: String, // "Exceptional", "Strong Delivery", "Clear & Engaging", "Needs Polish"
    val pronunciationScore: Int, // 0-100
    val grammarScore: Int, // 0-100
    val pacingScore: Int, // 0-100
    val wordsPerMinute: Int,
    val fillerWordCount: Int,
    val fillerWordsList: List<String>,
    val feedbackSummary: String,
    val strengths: List<String>,
    val improvements: List<String>,
    val slideNotes: List<SlideFeedbackNote>,
    val pronunciationTips: List<PronunciationItem>
)

data class BookReadingPassage(
    val id: String,
    val bookTitle: String,
    val author: String,
    val category: String,
    val passageText: String,
    val contextNote: String,
    val keyPhonetics: List<PronunciationItem>
)

data class ReadingEvaluation(
    val accuracyScore: Int, // 0-100
    val wordsPerMinute: Int,
    val mispronouncedWords: List<PronunciationItem>,
    val feedbackMessage: String
)
