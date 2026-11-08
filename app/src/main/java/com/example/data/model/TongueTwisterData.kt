package com.example.data.model

object TongueTwisterData {

    val twisters: List<TongueTwisterItem> = listOf(
        // WARM-UP (BEGINNER)
        TongueTwisterItem(
            id = "warmup_red_lorry",
            title = "Red Lorry, Yellow Lorry",
            text = "Red lorry, yellow lorry, red lorry, yellow lorry, red lorry, yellow lorry.",
            phoneticFocus = "/r/ vs /l/ Liquid Distinction",
            difficulty = TongueTwisterDifficulty.WARM_UP,
            targetWpm = 110,
            tip = "Keep your tongue curled back for /r/ and flick the tip behind upper front teeth for /l/.",
            highlightWords = listOf("red", "lorry", "yellow")
        ),
        TongueTwisterItem(
            id = "warmup_unique_new_york",
            title = "Unique New York",
            text = "You know you need unique New York, you know you need unique New York.",
            phoneticFocus = "/juː/ Palatal Glide & /k/ Velar Stop",
            difficulty = TongueTwisterDifficulty.WARM_UP,
            targetWpm = 115,
            tip = "Round your lips firmly on 'unique' and crisply release the final /k/ consonant.",
            highlightWords = listOf("unique", "New", "York", "need")
        ),
        TongueTwisterItem(
            id = "warmup_she_sees_cheese",
            title = "She Sees Cheese",
            text = "She sees cheese, and the cheese she sees is cheap Swiss cheese.",
            phoneticFocus = "/ʃ/ vs /s/ vs /tʃ/ Fricative Control",
            difficulty = TongueTwisterDifficulty.WARM_UP,
            targetWpm = 120,
            tip = "Protrude your lips slightly for /ʃ/ ('she') and pull lips into a smile for /s/ ('sees').",
            highlightWords = listOf("She", "sees", "cheese", "Swiss")
        ),
        TongueTwisterItem(
            id = "warmup_ice_cream",
            title = "Ice Cream Scream",
            text = "I scream, you scream, we all scream for ice cream!",
            phoneticFocus = "/skr/ Initial Consonant Cluster",
            difficulty = TongueTwisterDifficulty.WARM_UP,
            targetWpm = 130,
            tip = "Blend /s/, /k/, and /r/ smoothly without inserting an extra vowel sound between consonants.",
            highlightWords = listOf("scream", "ice", "cream")
        ),

        // AGILITY (INTERMEDIATE)
        TongueTwisterItem(
            id = "agility_peter_piper",
            title = "Peter Piper",
            text = "Peter Piper picked a peck of pickled peppers. A peck of pickled peppers Peter Piper picked.",
            phoneticFocus = "/p/ Bilabial Aspirated Plosives",
            difficulty = TongueTwisterDifficulty.AGILITY,
            targetWpm = 140,
            tip = "Feel the energetic puff of air on each /p/ sound without losing the crisp /k/ in 'pickled'.",
            highlightWords = listOf("Peter", "Piper", "picked", "peck", "pickled", "peppers")
        ),
        TongueTwisterItem(
            id = "agility_woodchuck",
            title = "The Woodchuck",
            text = "How much wood would a woodchuck chuck if a woodchuck could chuck wood?",
            phoneticFocus = "/w/ Labio-Velar & /tʃ/ Affricate",
            difficulty = TongueTwisterDifficulty.AGILITY,
            targetWpm = 145,
            tip = "Round your lips tightly into a small 'O' for /w/ then release explosively into /tʃ/ on 'chuck'.",
            highlightWords = listOf("wood", "would", "woodchuck", "chuck", "could")
        ),
        TongueTwisterItem(
            id = "agility_fuzzy_wuzzy",
            title = "Fuzzy Wuzzy",
            text = "Fuzzy Wuzzy was a bear. Fuzzy Wuzzy had no hair. Fuzzy Wuzzy wasn't fuzzy, was he?",
            phoneticFocus = "/f/ Labiodental & /z/ Voiced Sibilant",
            difficulty = TongueTwisterDifficulty.AGILITY,
            targetWpm = 150,
            tip = "Vibrate your vocal cords continuously on /z/ while lightly touching lower lip to upper teeth for /f/.",
            highlightWords = listOf("Fuzzy", "Wuzzy", "was", "wasn't")
        ),
        TongueTwisterItem(
            id = "agility_french_fritters",
            title = "French Fried Fritters",
            text = "Fresh French fried fly fritters from fabulous foreign fishmongers.",
            phoneticFocus = "/fr/ & /fl/ Consonant Blends",
            difficulty = TongueTwisterDifficulty.AGILITY,
            targetWpm = 155,
            tip = "Alternate quickly between the /fr/ liquid curl and the /fl/ lateral touch.",
            highlightWords = listOf("Fresh", "French", "fried", "fritters", "fabulous")
        ),

        // MASTER (ADVANCED)
        TongueTwisterItem(
            id = "master_seashells",
            title = "Seashells on the Seashore",
            text = "She sells seashells by the seashore. The shells she sells are seashells, I'm sure.",
            phoneticFocus = "Alternating /s/ & /ʃ/ Sibilant Precision",
            difficulty = TongueTwisterDifficulty.MASTER,
            targetWpm = 160,
            tip = "Do not let /s/ slip into /ʃ/. Shift tongue posture rapidly between teeth and hard palate.",
            highlightWords = listOf("She", "sells", "seashells", "seashore", "shells", "sure")
        ),
        TongueTwisterItem(
            id = "master_thieves",
            title = "The Thirty-Three Thieves",
            text = "The thirty-three thieves thought that they thrilled the throne throughout Thursday.",
            phoneticFocus = "/θ/ Voiceless & /ð/ Voiced Dental Fricatives",
            difficulty = TongueTwisterDifficulty.MASTER,
            targetWpm = 165,
            tip = "Gently place your tongue tip between your upper and lower incisors for every 'th' word.",
            highlightWords = listOf("thirty-three", "thieves", "thought", "thrilled", "throne", "throughout", "Thursday")
        ),
        TongueTwisterItem(
            id = "master_betty_botter",
            title = "Betty Botter's Butter",
            text = "Betty Botter bought some butter, but she said the butter's bitter. If I put it in my batter, it will make my batter bitter.",
            phoneticFocus = "/b/ Plosives & Alveolar Flap [ɾ]",
            difficulty = TongueTwisterDifficulty.MASTER,
            targetWpm = 170,
            tip = "Use a quick tongue tap against the alveolar ridge for the double 'tt' in American English.",
            highlightWords = listOf("Betty", "Botter", "bought", "butter", "bitter", "batter")
        ),
        TongueTwisterItem(
            id = "master_snails",
            title = "Six Slippery Snails",
            text = "Six slippery snails slid slowly seaward, sliding silently past seaside settlements.",
            phoneticFocus = "/sl/ Lateral Blends & /s/ Duration",
            difficulty = TongueTwisterDifficulty.MASTER,
            targetWpm = 170,
            tip = "Keep airflow tight and high-pitched on /s/ before transitioning into the smooth lateral /l/.",
            highlightWords = listOf("Six", "slippery", "snails", "slid", "slowly", "seaward", "sliding")
        ),

        // SPEED DEMON (EXTREME)
        TongueTwisterItem(
            id = "speed_pad_kid",
            title = "Pad Kid Poured Curd",
            text = "Pad kid poured curd pulled cod. Pad kid poured curd pulled cod. Pad kid poured curd pulled cod.",
            phoneticFocus = "Vowel Reversal & Plosive Alternation (MIT Study)",
            difficulty = TongueTwisterDifficulty.SPEED_DEMON,
            targetWpm = 180,
            tip = "Psychologists at MIT ranked this the hardest twister: maintain distinct vowel heights between /æ/, /ɪ/, /ɔː/, /ɜː/, /ʊ/, and /ɒ/.",
            highlightWords = listOf("Pad", "kid", "poured", "curd", "pulled", "cod")
        ),
        TongueTwisterItem(
            id = "speed_sixth_sheik",
            title = "The Sixth Sick Sheik",
            text = "The sixth sick sheik's sixth sheep's sick.",
            phoneticFocus = "/ksθ/ Quadruple Consonant Coda Cluster",
            difficulty = TongueTwisterDifficulty.SPEED_DEMON,
            targetWpm = 185,
            tip = "The cluster /ksθ/ in 'sixth' demands three micro-movements: velar stop /k/, sibilant /s/, and interdental /θ/.",
            highlightWords = listOf("sixth", "sick", "sheik's", "sheep's")
        ),
        TongueTwisterItem(
            id = "speed_irish_wristwatch",
            title = "Irish Wristwatch",
            text = "I wish to wash my Irish wristwatch! Which Swiss wristwatch is a rich Swiss wristwatch?",
            phoneticFocus = "/ʃ/, /tʃ/, and /st/ Rapid Coda Transitions",
            difficulty = TongueTwisterDifficulty.SPEED_DEMON,
            targetWpm = 190,
            tip = "Isolate the /ʃ/ in 'Irish' from the /tʃ/ in 'wristwatch'. Do not blend them together.",
            highlightWords = listOf("wish", "wash", "Irish", "wristwatch", "Swiss", "which", "rich")
        ),
        TongueTwisterItem(
            id = "speed_cheese_trees",
            title = "Through Three Cheese Trees",
            text = "Through three cheese trees three free fleas flew. While these fleas flew, freezy breeze blew.",
            phoneticFocus = "/θr/ vs /fr/ vs /fl/ vs /br/ Complex Blends",
            difficulty = TongueTwisterDifficulty.SPEED_DEMON,
            targetWpm = 200,
            tip = "A supreme tongue workout. Focus on tongue placement before accelerating your speed.",
            highlightWords = listOf("Through", "three", "cheese", "trees", "free", "fleas", "flew", "freezy", "breeze", "blew")
        )
    )
}
