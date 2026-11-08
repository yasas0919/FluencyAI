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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.DailyReadingData
import com.example.data.model.PresentationFeedback
import com.example.data.model.PresentationOutline
import com.example.data.model.PresentationSlide
import com.example.data.model.PronunciationItem
import com.example.ui.components.AudioWaveform
import com.example.ui.components.PresentationProgressGraph
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
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.VioletNeon
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.PresentationUiState
import com.example.ui.viewmodel.PresentationViewModel
import com.example.util.SpeechState
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresentationScreen(
    viewModel: PresentationViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentTopic by viewModel.currentTopic.collectAsState()
    val transcript by viewModel.accumulatedTranscript.collectAsState()
    val history by viewModel.presentationsHistory.collectAsState()

    val speechState by viewModel.speechRecognizerHelper.speechState.collectAsState()
    val soundLevel by viewModel.speechRecognizerHelper.soundLevel.collectAsState()

    var customTopicInput by remember { mutableStateOf("") }

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
            // Header Bar
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
                            .background(VioletPrimary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CoPresent,
                            contentDescription = "Presentation Mode",
                            tint = CyanNeon,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Presentation Studio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Generate outline • Present live • AI Score / 10",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }

                if (uiState !is PresentationUiState.SelectTopic) {
                    Text(
                        text = "Change Topic",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.resetToTopics() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }

        when (val state = uiState) {
            is PresentationUiState.SelectTopic -> {
                // Topic selection UI
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("presentation_topic_selector_card"),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(18.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardSurfaceBorder))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "1. CHOOSE PRESENTATION TOPIC",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DailyReadingData.sampleTopics.forEach { topic ->
                                    val isSelected = topic == currentTopic
                                    SuggestionChip(
                                        onClick = {
                                            viewModel.selectTopic(topic)
                                            customTopicInput = topic
                                        },
                                        label = {
                                            Text(
                                                text = topic,
                                                color = if (isSelected) Slate950 else Slate200,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (isSelected) CyanNeon else Slate800
                                        ),
                                        border = SuggestionChipDefaults.suggestionChipBorder(
                                            enabled = true,
                                            borderColor = if (isSelected) CyanNeon else Slate700
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = customTopicInput,
                                onValueChange = {
                                    customTopicInput = it
                                    viewModel.selectTopic(it)
                                },
                                placeholder = { Text("Or enter any custom topic...", color = Slate400) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_topic_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate850,
                                    unfocusedContainerColor = Slate850,
                                    focusedBorderColor = CyanNeon,
                                    unfocusedBorderColor = CardSurfaceBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val finalTopic = customTopicInput.ifBlank { currentTopic }
                                    viewModel.generateOutline(finalTopic)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("generate_slides_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Slate950,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generate AI Slides & Outlines",
                                    color = Slate950,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Interactive Progress Graph
                item {
                    PresentationProgressGraph(
                        presentations = history,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            is PresentationUiState.GeneratingOutline -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
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
                                color = CyanNeon,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Creating Presentation Slides...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Crafting executive talking points for \"${state.topic}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            is PresentationUiState.ReadyToPresent -> {
                item {
                    Text(
                        text = "PREVIEW SLIDES & REHEARSE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                }

                items(state.outline.slides, key = { it.slideNumber }) { slide ->
                    SlidePreviewCard(slide = slide)
                }

                item {
                    Button(
                        onClick = {
                            if (hasMicPermission) {
                                viewModel.startPresenting(state.outline)
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_presenting_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Slate950,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Live Presentation",
                            color = Slate950,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            is PresentationUiState.Presenting -> {
                val currentSlide = state.outline.slides.getOrNull(state.currentSlideIndex) ?: state.outline.slides.first()

                // Timer & Slide Index banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardSurfaceBorder))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = CyanNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val mins = state.secondsElapsed / 60
                                val secs = state.secondsElapsed % 60
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d", mins, secs),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = "Slide ${state.currentSlideIndex + 1} of ${state.outline.slides.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = VioletNeon
                            )
                        }
                    }
                }

                // Active Slide Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_presentation_slide_card"),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        shape = RoundedCornerShape(18.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyanNeon.copy(alpha = 0.5f)))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = currentSlide.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            currentSlide.bulletPoints.forEach { bp ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(text = "• ", color = CyanNeon, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(
                                        text = bp,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Slate200,
                                        lineHeight = 24.sp
                                    )
                                }
                            }

                            if (currentSlide.keyVocabulary.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Key Vocabulary: ${currentSlide.keyVocabulary.joinToString("  •  ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AmberWarning
                                )
                            }

                            if (currentSlide.speakerNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = Slate850,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "💡 Speaker Note:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate400
                                        )
                                        Text(
                                            text = currentSlide.speakerNotes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate200,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Navigation controls between slides
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.goToSlide(state.currentSlideIndex - 1) },
                            enabled = state.currentSlideIndex > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous")
                        }

                        Button(
                            onClick = { viewModel.goToSlide(state.currentSlideIndex + 1) },
                            enabled = state.currentSlideIndex < state.outline.slides.size - 1,
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Audio Waveform
                item {
                    AudioWaveform(
                        isListening = true,
                        soundLevel = soundLevel,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Finish and Evaluate Button
                item {
                    Button(
                        onClick = { viewModel.finishAndEvaluate() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("finish_presentation_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Slate950)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Complete Presentation & Get AI Score",
                            color = Slate950,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            is PresentationUiState.Evaluating -> {
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
                                color = CyanNeon,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Evaluating Your Presentation...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Analyzing delivery, pronunciation, pacing & rhetorical structure with Gemini AI.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            is PresentationUiState.Result -> {
                val feedback = state.feedback

                // Score Out of 10 Card
                item {
                    PresentationScoreHeroCard(feedback = feedback)
                }

                // Detailed Pillar Breakdown Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricPillarCard("Pronunciation", "${feedback.pronunciationScore}%", CyanNeon, Modifier.weight(1f))
                        MetricPillarCard("Grammar", "${feedback.grammarScore}%", VioletNeon, Modifier.weight(1f))
                        MetricPillarCard("Pacing", "${feedback.pacingScore}%", EmeraldGreen, Modifier.weight(1f))
                        MetricPillarCard("Speed", "${feedback.wordsPerMinute} WPM", AmberWarning, Modifier.weight(1f))
                    }
                }

                // Executive Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "EXECUTIVE SUMMARY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = feedback.feedbackSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate200,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Strengths & Areas to Improve
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Strengths
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldGreen.copy(alpha = 0.4f)))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🏆 Strengths",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                feedback.strengths.forEach { s ->
                                    Text(
                                        text = "• $s",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate200,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Improvements
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmberWarning.copy(alpha = 0.4f)))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🎯 Growth Tips",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberWarning
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                feedback.improvements.forEach { imp ->
                                    Text(
                                        text = "• $imp",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate200,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Slide Notes Breakdown
                if (feedback.slideNotes.isNotEmpty()) {
                    item {
                        Text(
                            text = "SLIDE-BY-SLIDE DELIVERY NOTES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 1.sp
                        )
                    }

                    items(feedback.slideNotes) { sn ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Slide ${sn.slideNumber}: ${sn.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sn.remark,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate200,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // Pronunciation Diction Tips
                if (feedback.pronunciationTips.isNotEmpty()) {
                    item {
                        Text(
                            text = "KEY VOCABULARY & PRONUNCIATION TIPS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                    }

                    items(feedback.pronunciationTips) { item ->
                        PresentationPronunciationCard(
                            item = item,
                            onPlay = { viewModel.playWordAudio(item.word) }
                        )
                    }
                }

                // Interactive Progress Graph updated with this new presentation!
                item {
                    PresentationProgressGraph(
                        presentations = history,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Start New Presentation Button
                item {
                    Button(
                        onClick = { viewModel.resetToTopics() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Slate950)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Practice Another Presentation", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                }
            }

            is PresentationUiState.Error -> {
                item {
                    Surface(
                        color = RoseError.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Presentation Error", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(text = state.message, style = MaterialTheme.typography.bodySmall, color = Slate200, modifier = Modifier.padding(vertical = 4.dp))
                            Button(
                                onClick = { viewModel.resetToTopics() },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                            ) {
                                Text("Back to Topics", color = Color.White)
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
private fun SlidePreviewCard(slide: PresentationSlide) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardSurfaceBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Slide ${slide.slideNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = VioletNeon
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = slide.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            slide.bulletPoints.forEach { bp ->
                Text(
                    text = "• $bp",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate200,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PresentationScoreHeroCard(feedback: PresentationFeedback) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldGreen.copy(alpha = 0.5f)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Surface(
                    color = EmeraldGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = feedback.ratingBadge.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Overall Presentation Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Filler words detected: ${feedback.fillerWordCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.US, "%.1f", feedback.scoreOutOfTen),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldGreen
                )
                Text(
                    text = "out of 10.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun MetricPillarCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun PresentationPronunciationCard(item: PronunciationItem, onPlay: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.word, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = item.phoneticIpa, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = CyanAccent)
                }
                if (item.tip.isNotBlank()) {
                    Text(text = item.tip, style = MaterialTheme.typography.bodySmall, color = Slate400, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }

            FilledIconButton(
                onClick = onPlay,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Slate800, contentColor = CyanNeon)
            ) {
                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Play", modifier = Modifier.size(18.dp))
            }
        }
    }
}
