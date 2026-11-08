package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.BookReadingPassage
import com.example.data.model.PronunciationItem
import com.example.ui.components.AudioWaveform
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardSurfaceBorder
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.VioletNeon
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.BookReadingUiState
import com.example.ui.viewmodel.BookReadingViewModel
import com.example.util.SpeechState

@Composable
fun DailyReadingScreen(
    viewModel: BookReadingViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val passages = viewModel.passages
    val selectedPassage by viewModel.selectedPassage.collectAsState()
    val spokenText by viewModel.accumulatedSpokenText.collectAsState()
    val history by viewModel.readingsHistory.collectAsState()

    val speechState by viewModel.speechRecognizerHelper.speechState.collectAsState()
    val soundLevel by viewModel.speechRecognizerHelper.soundLevel.collectAsState()
    val isSpeakingAudio by viewModel.ttsManager.isSpeaking.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Daily Book Reading",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Book Reading",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Read aloud • Real-time tracking • AI phonetic correction",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }

                if (uiState !is BookReadingUiState.PassageList) {
                    Text(
                        text = "All Excerpts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.backToList() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }

        when (val state = uiState) {
            is BookReadingUiState.PassageList -> {
                item {
                    Text(
                        text = "TODAY'S CURATED READINGS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                }

                items(passages, key = { it.id }) { passage ->
                    PassageCard(
                        passage = passage,
                        onSelect = { viewModel.openPassage(passage) }
                    )
                }

                if (history.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "RECENT ORAL READINGS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 1.sp
                        )
                    }

                    items(history.take(3), key = { "history_${it.id}" }) { h ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = h.bookTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = "by ${h.author} • ${h.wordsPerMinute} WPM", style = MaterialTheme.typography.bodySmall, color = Slate400, fontSize = 11.sp)
                                }
                                Surface(
                                    color = EmeraldGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${h.accuracyScore}%",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is BookReadingUiState.Reading -> {
                val passage = state.passage
                val isListening = speechState is SpeechState.Listening || speechState is SpeechState.Partial

                // Passage Header Info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardSurfaceBorder))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = VioletPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = passage.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = VioletNeon,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = "by ${passage.author}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = passage.bookTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Interactive Book Passage Reader with Real-Time Word Highlighting!
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("book_reading_passage_card"),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        shape = RoundedCornerShape(18.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardSurfaceBorder))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Compute highlighted text based on spoken words
                            val spokenWordsClean = spokenText
                                .lowercase()
                                .replace("[^a-z0-9 ]".toRegex(), "")
                                .split("\\s+".toRegex())
                                .filter { it.isNotBlank() }
                                .toSet()

                            val passageWords = passage.passageText.split(" ")
                            val annotatedString = buildAnnotatedString {
                                passageWords.forEachIndexed { index, rawWord ->
                                    val cleanWord = rawWord.lowercase().replace("[^a-z0-9]".toRegex(), "")
                                    val isRead = spokenWordsClean.contains(cleanWord)

                                    if (isRead) {
                                        withStyle(style = SpanStyle(color = EmeraldGreen, fontWeight = FontWeight.Bold, background = EmeraldGreen.copy(alpha = 0.15f))) {
                                            append(rawWord)
                                        }
                                    } else {
                                        withStyle(style = SpanStyle(color = Color.White)) {
                                            append(rawWord)
                                        }
                                    }
                                    if (index < passageWords.size - 1) {
                                        append(" ")
                                    }
                                }
                            }

                            Text(
                                text = annotatedString,
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 19.sp,
                                lineHeight = 30.sp,
                                fontFamily = FontFamily.Serif
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "💡 Note: ${passage.contextNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Audio Controls (Listen to Native Model)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isSpeakingAudio) viewModel.stopAudio() else viewModel.listenToNativeModel(1.0f)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("play_model_reading_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeakingAudio) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSpeakingAudio) "Stop Audio" else "Listen Native", color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.listenToNativeModel(0.8f) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🐢 0.8x Slow Model", color = Color.White)
                        }
                    }
                }

                // Waveform
                item {
                    AudioWaveform(
                        isListening = isListening,
                        soundLevel = soundLevel,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Microphone & Reading Actions
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FilledIconButton(
                            onClick = {
                                if (isListening) {
                                    viewModel.stopReadingMic()
                                } else {
                                    if (hasMicPermission) {
                                        viewModel.startReadingMic()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .testTag("reading_mic_button"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isListening) RoseError else EmeraldGreen,
                                contentColor = if (isListening) Color.White else Slate950
                            )
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop Mic" else "Read Aloud",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isListening) "Listening... read the passage aloud now!" else "Tap microphone to read aloud",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isListening) EmeraldGreen else Slate400
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.evaluateReading() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("evaluate_reading_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Slate950)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Complete & Evaluate Pronunciation",
                                color = Slate950,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            is BookReadingUiState.Evaluating -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = EmeraldGreen,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Analyzing Oral Reading & Articulation...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Detecting phonetic accuracy, word pauses, and cadence with Gemini AI.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            is BookReadingUiState.Result -> {
                val eval = state.evaluation

                // Hero Reading Score Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reading_score_card"),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        shape = RoundedCornerShape(18.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldGreen.copy(alpha = 0.5f)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Surface(
                                    color = EmeraldGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (eval.accuracyScore >= 85) "FLUENT ARTICULATION" else "KEEP PRACTICING",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Reading Accuracy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Speed: ${eval.wordsPerMinute} Words / Min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }

                            Text(
                                text = "${eval.accuracyScore}%",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (eval.accuracyScore >= 80) EmeraldGreen else AmberWarning
                            )
                        }
                    }
                }

                // Coaching Feedback
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "READING CADENCE & PHONETIC FEEDBACK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = eval.feedbackMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate200,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Mispronounced / Skipped Words Breakdown
                if (eval.mispronouncedWords.isNotEmpty()) {
                    item {
                        Text(
                            text = "WORDS TO MASTER (WITH IPA & MOUTH TIPS)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning,
                            letterSpacing = 1.sp
                        )
                    }

                    items(eval.mispronouncedWords) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = item.word, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(color = Slate800, shape = RoundedCornerShape(6.dp)) {
                                            Text(text = item.phoneticIpa, fontFamily = FontFamily.Monospace, color = CyanAccent, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 12.sp)
                                        }
                                    }
                                    if (item.syllableStress.isNotBlank()) {
                                        Text(text = "Stress: ${item.syllableStress}", style = MaterialTheme.typography.bodySmall, color = Slate400, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                    }
                                    if (item.tip.isNotBlank()) {
                                        Text(text = "👅 Tip: ${item.tip}", style = MaterialTheme.typography.bodySmall, color = Slate200, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                    }
                                }

                                FilledIconButton(
                                    onClick = { viewModel.speakWord(item.word) },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Slate800, contentColor = CyanNeon)
                                ) {
                                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Listen", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // Action buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.openPassage(state.passage) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Read Again")
                        }

                        Button(
                            onClick = { viewModel.backToList() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Next Excerpt", color = Slate950, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is BookReadingUiState.Error -> {
                item {
                    Surface(
                        color = RoseError.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Reading Error", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(text = state.message, style = MaterialTheme.typography.bodySmall, color = Slate200, modifier = Modifier.padding(vertical = 4.dp))
                            Button(
                                onClick = { viewModel.backToList() },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                            ) {
                                Text("Back to Excerpts", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PassageCard(
    passage: BookReadingPassage,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("passage_card_${passage.id}"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardSurfaceBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = EmeraldGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = passage.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "by ${passage.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = passage.bookTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "\"${passage.passageText.take(120)}...\"",
                style = MaterialTheme.typography.bodySmall,
                color = Slate200,
                lineHeight = 18.sp,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Read Aloud", color = Slate950, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
