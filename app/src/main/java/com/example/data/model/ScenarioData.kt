package com.example.data.model

object ScenarioData {

    val personas = listOf(
        AiPersona(
            id = "emma",
            name = "Emma",
            title = "Friendly English Coach",
            description = "Warm, patient, and speaks with clear Standard American pronunciation. Ideal for building confidence.",
            accent = "US",
            avatarEmoji = "👩‍🏫"
        ),
        AiPersona(
            id = "david",
            name = "David",
            title = "Executive & Business Mentor",
            description = "Articulate, professional with a modern British accent. Expert in professional vocabulary and concise phrasing.",
            accent = "UK",
            avatarEmoji = "👨‍💼"
        ),
        AiPersona(
            id = "alex",
            name = "Alex",
            title = "Native Friend & Conversationalist",
            description = "Casual, fun, uses natural idioms, slang, and phrasal verbs. Perfect for everyday chit-chat.",
            accent = "US",
            avatarEmoji = "🧑‍🎤"
        ),
        AiPersona(
            id = "dr_vance",
            name = "Dr. Vance",
            title = "IELTS/TOEFL Senior Examiner",
            description = "Structured, evaluative, focusing on grammatical range, lexical resource, and phonetic fluency.",
            accent = "UK",
            avatarEmoji = "🧐"
        )
    )

    val scenarios = listOf(
        Scenario(
            id = "job_interview",
            title = "Tech Job Interview",
            category = "Career",
            description = "Practice answering behavioral and technical questions using the STAR method with an experienced hiring manager.",
            roleA = "Hiring Manager",
            roleB = "Job Candidate",
            initialAiGreeting = "Welcome! Thank you for joining us today. To start off, could you tell me a little about yourself and what attracted you to this role?",
            promptContext = "You are a professional hiring manager conducting a friendly yet thorough interview. Evaluate clarity, tone, and professional English diction.",
            level = "Intermediate / Advanced",
            iconName = "Work"
        ),
        Scenario(
            id = "cafe_order",
            title = "Coffee Shop & Casual Talk",
            category = "Daily Life",
            description = "Order your favorite beverage, ask for customizations, and make pleasant small talk with the friendly barista.",
            roleA = "Barista at Artisan Coffee",
            roleB = "Customer",
            initialAiGreeting = "Hey there! Good morning! What can I get started for you today?",
            promptContext = "You are a friendly, energetic barista at a hip coffee shop. Respond quickly with natural colloquial spoken English and casual transitions.",
            level = "Beginner / Intermediate",
            iconName = "Coffee"
        ),
        Scenario(
            id = "ielts_exam",
            title = "IELTS Speaking Part 2 & 3",
            category = "Exam Prep",
            description = "Simulate an official IELTS speaking examination with academic prompts, follow-ups, and band-score feedback.",
            roleA = "IELTS Official Examiner",
            roleB = "Candidate",
            initialAiGreeting = "Good afternoon. My name is Dr. Vance. In this part of the test, I would like you to speak about a memorable journey you took. What was the journey, and why was it significant?",
            promptContext = "You are an official IELTS examiner. Maintain a formal yet courteous demeanor. Ask probing follow-up questions to assess fluency and coherence.",
            level = "Advanced",
            iconName = "School"
        ),
        Scenario(
            id = "airport_travel",
            title = "Airport Customs & Travel",
            category = "Travel",
            description = "Navigate passport control, answer customs questions, and ask airport staff for directions.",
            roleA = "Border Control Officer",
            roleB = "International Traveler",
            initialAiGreeting = "Next, please. Passports out. Good day, what is the primary purpose of your visit to the country today?",
            promptContext = "You are a courteous but precise border customs officer. Ask standard travel, duration, and accommodation questions.",
            level = "Beginner / Intermediate",
            iconName = "Flight"
        ),
        Scenario(
            id = "hotel_service",
            title = "Hotel Check-In & Requests",
            category = "Travel",
            description = "Check into a luxury hotel, request room adjustments or high-floor views, and inquire about local recommendations.",
            roleA = "Concierge / Front Desk",
            roleB = "Guest",
            initialAiGreeting = "Good evening and welcome to the Grand Hyatt! Do you have a reservation with us tonight?",
            promptContext = "You are an attentive hotel front desk specialist. Answer gracefully and offer helpful services.",
            level = "Intermediate",
            iconName = "Hotel"
        ),
        Scenario(
            id = "casual_party",
            title = "Networking & Party Chit-Chat",
            category = "Social",
            description = "Break the ice at a mixer or casual party, discuss hobbies, movies, work, and build natural rapport.",
            roleA = "Guest at Tech Mixer",
            roleB = "Attendee",
            initialAiGreeting = "Hey! Great turnout tonight, right? I love this rooftop venue. Are you local to the city or visiting for the event?",
            promptContext = "You are a lively, witty attendee at an evening networking mixer. Keep conversation light, natural, and friendly.",
            level = "Beginner / Intermediate",
            iconName = "Groups"
        ),
        Scenario(
            id = "salary_negotiation",
            title = "Salary & Promotion Pitch",
            category = "Career",
            description = "Negotiate compensation or pitch for a promotion using confident, tactful, and persuasive spoken English.",
            roleA = "Department Director",
            roleB = "Team Lead",
            initialAiGreeting = "Thanks for setting up this 1-on-1 discussion. I reviewed your recent performance appraisal. What did you want to discuss specifically regarding your compensation?",
            promptContext = "You are an open-minded department director. Evaluate candidate's persuasion, assertiveness, and professional diplomacy.",
            level = "Advanced",
            iconName = "TrendingUp"
        ),
        Scenario(
            id = "doctor_clinic",
            title = "Medical Clinic Consultation",
            category = "Health",
            description = "Describe physical symptoms, understand medical instructions, and ask about dosage and recovery.",
            roleA = "Attending Physician",
            roleB = "Patient",
            initialAiGreeting = "Hello, please take a seat. I see you're not feeling at your best. Can you tell me what symptoms you've been experiencing?",
            promptContext = "You are an empathetic, clear doctor. Help patient describe symptoms accurately and provide caring guidance.",
            level = "Intermediate",
            iconName = "MedicalServices"
        )
    )

    val drills = listOf(
        PronunciationDrill(
            id = "drill_th_unvoiced",
            phrase = "I think thirty-three thousand people thought about theatre.",
            phoneticIpa = "/aɪ θɪŋk ˈθɜːrti θriː ˈθaʊzənd ˈpiːpəl θɔːt əˈbaʊt ˈθɪətər/",
            targetSound = "/θ/ Voiceless TH",
            soundCategory = "Consonants",
            instruction = "Place tongue tip gently between top and bottom front teeth. Blow air softly without vibrating vocal cords.",
            difficulty = "Medium"
        ),
        PronunciationDrill(
            id = "drill_v_w",
            phrase = "Victor was very wary of the wet winter wind.",
            phoneticIpa = "/ˈvɪktər wəz ˈvɛri ˈwɛəri əv ðə wɛt ˈwɪntər wɪnd/",
            targetSound = "/v/ vs /w/",
            soundCategory = "Consonants",
            instruction = "For /v/: upper teeth touch lower lip with vibration. For /w/: round your lips into a tight circle like whistling without teeth touching.",
            difficulty = "Hard"
        ),
        PronunciationDrill(
            id = "drill_r_l",
            phrase = "Red lorry, yellow lorry rolls really rapidly along the road.",
            phoneticIpa = "/rɛd ˈlɔːri, ˈjɛloʊ ˈlɔːri roʊlz ˈrɪəli ˈræpɪdli əˈlɔːŋ ðə roʊd/",
            targetSound = "/r/ vs /l/",
            soundCategory = "Consonants",
            instruction = "For /l/: tongue touches roof of mouth behind upper teeth. For /r/: curl tongue back slightly without touching the roof.",
            difficulty = "Hard"
        ),
        PronunciationDrill(
            id = "drill_ed_endings",
            phrase = "He walked, laughed, asked, and decided to finish what he started.",
            phoneticIpa = "/hi wɔːkt, læft, æskt, ænd dɪˈsaɪdɪd tu ˈfɪnɪʃ wʌt hi ˈstɑːrtɪd/",
            targetSound = "-ed endings (/t/, /d/, /ɪd/)",
            soundCategory = "Endings",
            instruction = "Voiceless stops (k, p, f, s) end in /t/ ('walked' = walk-t). 'd' and 't' end in /ɪd/ ('decided').",
            difficulty = "Medium"
        ),
        PronunciationDrill(
            id = "drill_tricky_words",
            phrase = "The entrepreneur found a comfortable recipe on Wednesday.",
            phoneticIpa = "/ði ˌɒntrəprəˈnɜːr faʊnd ə ˈkʌmftəbl ˈrɛsəpi ɒn ˈwɛnzdeɪ/",
            targetSound = "Silent letters & Stress",
            soundCategory = "Tricky Words",
            instruction = "'Comfortable' is 3 syllables: /ˈkʌmf-tə-bl/. 'Wednesday' drops 'd': /ˈwɛnz-deɪ/. 'Recipe' ends in /pi/.",
            difficulty = "Hard"
        ),
        PronunciationDrill(
            id = "drill_vowel_distinction",
            phrase = "Please sit on the seat and leave after you live here.",
            phoneticIpa = "/pliːz sɪt ɒn ðə siːt ænd liːv ˈæftər ju lɪv hɪər/",
            targetSound = "/ɪ/ vs /iː/ (Short vs Long E)",
            soundCategory = "Vowels",
            instruction = "/ɪ/ in 'sit' and 'live' is relaxed and short. /iː/ in 'seat' and 'leave' has lips spread wide like smiling.",
            difficulty = "Medium"
        )
    )
}
