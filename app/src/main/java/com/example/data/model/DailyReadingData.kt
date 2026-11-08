package com.example.data.model

object DailyReadingData {

    val sampleTopics = listOf(
        "Pitching an AI Startup to Venture Investors",
        "The Future of Renewable Energy & Microgrids",
        "Remote Work Culture & Engineering Agility",
        "The Neuroscience of Habit Formation",
        "Ethical AI, Data Sovereignty & User Privacy"
    )

    val passages = listOf(
        BookReadingPassage(
            id = "reading_steve_jobs",
            bookTitle = "Stanford Commencement Address",
            author = "Steve Jobs",
            category = "Historic Speeches",
            passageText = "Your time is limited, so don't waste it living someone else's life. Don't be trapped by dogma, which is living with the results of other people's thinking. Don't let the noise of others' opinions drown out your own inner voice. And most importantly, have the courage to follow your heart and intuition.",
            contextNote = "Delivered at Stanford University in 2005. Focus on assertive cadence, dynamic pauses, and clear vowel enunciations.",
            keyPhonetics = listOf(
                PronunciationItem("dogma", "/ˈdɒɡmə/", "DOG-ma", "Short open 'o' with a soft schwa ending.", "good"),
                PronunciationItem("intuition", "/ˌɪntjuːˈɪʃən/", "in-tu-I-tion", "Stress is heavily on the third syllable 'I'.", "good"),
                PronunciationItem("courage", "/ˈkʌrɪdʒ/", "COUR-age", "First vowel is /ʌ/ as in 'cup', ending with soft /ɪdʒ/.", "good")
            )
        ),
        BookReadingPassage(
            id = "reading_great_gatsby",
            bookTitle = "The Great Gatsby",
            author = "F. Scott Fitzgerald",
            category = "Classic Literature",
            passageText = "In my younger and more vulnerable years my father gave me some advice that I've been turning over in my mind ever since. Whenever you feel like criticizing anyone, he told me, just remember that all the people in this world haven't had the advantages that you've had.",
            contextNote = "One of the most celebrated opening lines in American literature. Practice lyrical rhythm, liaison between words, and soft 'v' sounds.",
            keyPhonetics = listOf(
                PronunciationItem("vulnerable", "/ˈvʌlnərəbl/", "VUL-ner-a-ble", "Lip-to-teeth contact on /v/. Keep the word crisp in 4 smooth syllables.", "needs_work"),
                PronunciationItem("criticizing", "/ˈkrɪtɪsaɪzɪŋ/", "CRIT-i-ciz-ing", "Flap the medial 't' softly in American English, ending in /ɪŋ/.", "good"),
                PronunciationItem("advantages", "/ədˈvɑːntɪdʒɪz/", "ad-VAN-tag-es", "Stress is on the second syllable.", "good")
            )
        ),
        BookReadingPassage(
            id = "reading_atomic_habits",
            bookTitle = "Atomic Habits",
            author = "James Clear",
            category = "Psychology & Growth",
            passageText = "Habits are the compound interest of self-improvement. The same way that money multiplies through compound interest, the effects of your habits multiply as you repeat them. They seem to make little difference on any given day, and yet the impact they deliver over months and years can be enormous.",
            contextNote = "Modern non-fiction bestseller. Master technical clarity, rhythm, and clear consonant endings.",
            keyPhonetics = listOf(
                PronunciationItem("compound", "/ˈkɒmpaʊnd/", "COM-pound", "As a noun/adjective, accent is on the first syllable.", "good"),
                PronunciationItem("multiplies", "/ˈmʌltɪplaɪz/", "MUL-ti-plies", "Distinct /l/ sound followed by diphthong /aɪ/.", "good"),
                PronunciationItem("enormous", "/ɪˈnɔːrməs/", "e-NOR-mous", "Open back vowel /ɔːr/ with silent 'r' in British or retroflex in US.", "good")
            )
        ),
        BookReadingPassage(
            id = "reading_mockingbird",
            bookTitle = "To Kill a Mockingbird",
            author = "Harper Lee",
            category = "Classic Literature",
            passageText = "You never really understand a person until you consider things from his point of view, until you climb into his skin and walk around in it. Atticus said it was an extraordinary thing to see a man conquer his fear and stand up for righteousness.",
            contextNote = "Pulitzer Prize masterpiece. Emphasize empathy, warm pacing, and the contrast between stressed and unstressed syllables.",
            keyPhonetics = listOf(
                PronunciationItem("extraordinary", "/ɪkˈstrɔːrdəneri/", "ex-TRAOR-di-nary", "Do not pronounce extra-ordinary as two words. Blend into 5 syllables.", "needs_work"),
                PronunciationItem("conquer", "/ˈkɒŋkər/", "CON-quer", "The 'qu' sounds like /k/ here, not /kw/. Rhymes with 'honker'.", "needs_work"),
                PronunciationItem("righteousness", "/ˈraɪtʃəsnəs/", "RIGH-teous-ness", "The middle consonant cluster is /tʃ/ as in 'chair'.", "good")
            )
        ),
        BookReadingPassage(
            id = "reading_hawking",
            bookTitle = "A Brief History of Time",
            author = "Stephen Hawking",
            category = "Science & Philosophy",
            passageText = "Even if it turns out that time travel is impossible, it is important that we understand why it is impossible. We find ourselves in a bewildering world. We want to make sense of what we see around us and to ask: What is the nature of the universe? What is our place in it and where did it and we come from?",
            contextNote = "Seminal cosmology text. Focus on articulate scientific vocabulary and philosophical inquiry pacing.",
            keyPhonetics = listOf(
                PronunciationItem("bewildering", "/bɪˈwɪldərɪŋ/", "be-WIL-der-ing", "Clean /w/ sound with rounded lips, followed by soft unstressed /dər/.", "good"),
                PronunciationItem("nature", "/ˈneɪtʃər/", "NA-ture", "Starts with diphthong /eɪ/ and ends with /tʃər/.", "good"),
                PronunciationItem("universe", "/ˈjuːnɪvɜːrs/", "U-ni-verse", "Starts with palatal /j/ sound as in 'you'.", "good")
            )
        )
    )
}
