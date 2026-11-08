package com.example.data.model

object NativeCallData {

    val personas: List<NativeCallPersona> = listOf(
        NativeCallPersona(
            id = "marcus_us",
            name = "Marcus Vance",
            title = "Silicon Valley Executive & Tech Strategist",
            defaultAccent = NativeCallAccent.US,
            avatarEmoji = "💼",
            bio = "Clear, confident American English with natural Silicon Valley business vocabulary and sharp conversational pacing.",
            voiceTone = "Confident, articulate, fast-paced"
        ),
        NativeCallPersona(
            id = "emma_uk",
            name = "Emma Kensington",
            title = "Cambridge Literature Tutor & Media Broadcaster",
            defaultAccent = NativeCallAccent.UK,
            avatarEmoji = "🏛️",
            bio = "Classic British Received Pronunciation (RP) with refined diction, rich idiomatic phrasing, and elegant cadence.",
            voiceTone = "Polished, warm, intellectual"
        ),
        NativeCallPersona(
            id = "liam_aus",
            name = "Liam Gallagher",
            title = "Sydney Creative Director & Surfing Enthusiast",
            defaultAccent = NativeCallAccent.AUS,
            avatarEmoji = "🏄‍♂️",
            bio = "Friendly, authentic Australian English featuring relaxed vowel shifts, modern Aussie banter, and energetic flow.",
            voiceTone = "Upbeat, casual, highly conversational"
        ),
        NativeCallPersona(
            id = "sophia_us",
            name = "Dr. Sophia Martinez",
            title = "New York University Professor & Interview Coach",
            defaultAccent = NativeCallAccent.US,
            avatarEmoji = "🎙️",
            bio = "Encouraging, insightful American speech with emphasis on structure, persuasive rhetoric, and clear phonetics.",
            voiceTone = "Supportive, analytical, engaging"
        ),
        NativeCallPersona(
            id = "oliver_uk",
            name = "Oliver Bennett",
            title = "London International Diplomat & Finance Lead",
            defaultAccent = NativeCallAccent.UK,
            avatarEmoji = "🇬🇧",
            bio = "Contemporary London professional tone with crisp consonants, diplomatic tact, and formal meeting vocabulary.",
            voiceTone = "Professional, composed, precise"
        ),
        NativeCallPersona(
            id = "chloe_aus",
            name = "Chloe Mitchell",
            title = "Melbourne Specialty Barista & Cultural Host",
            defaultAccent = NativeCallAccent.AUS,
            avatarEmoji = "☕",
            bio = "Lively Melbourne conversationalist with modern Australian colloquialisms, warm story-telling, and open questions.",
            voiceTone = "Welcoming, spontaneous, friendly"
        )
    )

    val scenarios: List<NativeCallScenario> = listOf(
        NativeCallScenario(
            id = "call_coffee_catchup",
            title = "Casual Catch-Up Over Coffee",
            category = "Social",
            description = "Ring up your native English friend to catch up on recent travels, weekend hobbies, and upcoming plans.",
            aiRole = "Close Native Friend",
            userRole = "Friend catching up",
            initialGreeting = "Hey there! It's so great to hear from you. How has your week been going so far?",
            suggestedTopics = listOf("Weekend road trip", "A great new podcast or book", "Upcoming holiday plans")
        ),
        NativeCallScenario(
            id = "call_job_interview",
            title = "Job Interview Phone Screen",
            category = "Career",
            description = "A preliminary phone screening with a senior hiring manager. Highlight your strengths, past projects, and answer behavioral questions.",
            aiRole = "Lead Hiring Manager",
            userRole = "Senior Candidate",
            initialGreeting = "Hello! Thanks for taking the time to speak with me today. To kick things off, could you give me a brief overview of your background and experience?",
            suggestedTopics = listOf("Previous technical experience", "Handling a tight project deadline", "Long-term career aspirations")
        ),
        NativeCallScenario(
            id = "call_immigration_travel",
            title = "Travelling & Airport Customs",
            category = "Travel",
            description = "Answer direct questions from a border officer regarding your travel itinerary, accommodation, and purpose of visit.",
            aiRole = "Border Protection Officer",
            userRole = "Incoming International Traveler",
            initialGreeting = "Good morning. May I inspect your passport, please? What is the primary purpose of your trip, and how long do you plan to stay?",
            suggestedTopics = listOf("Hotel reservation and itinerary", "Conference attendance details", "Return flight confirmation")
        ),
        NativeCallScenario(
            id = "call_medical_clinic",
            title = "Medical Clinic Consultation",
            category = "Health",
            description = "Consult with an attentive doctor about your physical symptoms, medical history, and treatment recovery plan.",
            aiRole = "Attending Physician",
            userRole = "Patient",
            initialGreeting = "Hello, thanks for calling in to the clinic. I see you're not feeling at your best today. Can you walk me through the symptoms you've been experiencing?",
            suggestedTopics = listOf("Onset and severity of symptoms", "Prescription and allergy history", "Recovery and dosage guidelines")
        ),
        NativeCallScenario(
            id = "call_networking",
            title = "Professional Networking Mixer",
            category = "Networking",
            description = "Connect with an industry peer at a networking reception. Introduce your background, discuss trends, and exchange ideas.",
            aiRole = "Senior Industry Peer",
            userRole = "Networking Attendee",
            initialGreeting = "Hey there! Really great turnout at the mixer tonight, isn't it? What kind of work or projects are you currently focused on?",
            suggestedTopics = listOf("Current industry projects", "Exciting tech developments", "Collaborative partnerships")
        ),
        NativeCallScenario(
            id = "call_coffee_catchup",
            title = "Coffee Shop & Casual Talk",
            category = "Social",
            description = "Ring up your native English friend to catch up on recent travels, weekend hobbies, and upcoming plans.",
            aiRole = "Close Native Friend",
            userRole = "Friend catching up",
            initialGreeting = "Hey there! It's so great to hear from you. How has your week been going so far?",
            suggestedTopics = listOf("Weekend road trip", "A great new podcast or book", "Upcoming holiday plans")
        ),
        NativeCallScenario(
            id = "call_startup_pitch",
            title = "Venture Pitch & Product Strategy",
            category = "Business",
            description = "Discuss your new app concept with an angel investor. Pitch the core value proposition and market traction.",
            aiRole = "Angel Investor",
            userRole = "Startup Founder",
            initialGreeting = "Hi, I've had a quick look at your one-pager. What is the single biggest problem your product solves for users right now?",
            suggestedTopics = listOf("Unique value proposition", "User retention strategy", "Competitive landscape")
        ),
        NativeCallScenario(
            id = "call_local_banter",
            title = "London Pub or Aussie BBQ Banter",
            category = "Culture & Slang",
            description = "Practice natural cultural idioms, banter, and small talk in a relaxed social setting.",
            aiRole = "Local Resident & Host",
            userRole = "Newcomer to the city",
            initialGreeting = "Hey mate! Glad you could make it. Have you had a chance to explore around the neighborhood yet?",
            suggestedTopics = listOf("Local food recommendations", "Weekend sports matches", "Weather surprises")
        ),
        NativeCallScenario(
            id = "call_restaurant_order",
            title = "Boutique Restaurant & Concierge",
            category = "Daily Life",
            description = "Call to reserve a table for an anniversary dinner, request dietary accommodations, and ask for recommendations.",
            aiRole = "Restaurant Concierge",
            userRole = "Customer making reservation",
            initialGreeting = "Good afternoon, thank you for calling The Carlton. How may I assist you with your booking today?",
            suggestedTopics = listOf("Table for four with quiet seating", "Gluten-free dietary options", "Special anniversary dessert")
        )
    )
}
