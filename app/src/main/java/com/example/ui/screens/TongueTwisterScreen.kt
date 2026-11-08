package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.TongueTwisterDifficulty
import com.example.data.model.TongueTwisterEvaluation
import com.example.data.model.TongueTwisterItem
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
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.VioletNeon
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.TongueTwisterViewModel
import java.util.Locale

@Composable
fun TongueTwisterScreen(
    viewModel: TongueTwisterViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentTwister by viewModel.currentTwister.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val liveSpokenText by viewModel.liveSpokenText.collectAsState()
    val matchedWordsIndex by viewModel.matchedWordsIndex.collectAsState()
    val evaluation by viewModel.evaluation.collectAsState()
    val soundLevel by viewModel.speechRecognizerHelper.soundLevel.collectAsState()
    val isSpeakingModel by viewModel.ttsManager.isSpeaking.collectAsState()

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

    val filteredList = viewModel.getFilteredTwisters(selectedDifficulty)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Top Navigation & Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("twister_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate200
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tongue Twisters",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = AmberWarning.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "AGILITY LAB",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Real-time accuracy & speech speed analysis",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }
        }

        // Difficulty Selector Chips
        item {
            Column {
                Text(
                    text = "SELECT DIFFICULTY LEVEL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedDifficulty == null,
                            onClick = { viewModel.setDifficulty(null) },
                            label = { Text("All (${com.example.data.model.TongueTwisterData.twisters.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon,
                                selectedLabelColor = Slate950,
                                containerColor = Slate850,
                                labelColor = Slate200
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedDifficulty == null,
                                borderColor = if (selectedDifficulty == null) CyanNeon else Slate700
                            )
                        )
                    }

                    items(TongueTwisterDifficulty.values()) { diff ->
                        val isSelected = selectedDifficulty == diff
                        val count = com.example.data.model.TongueTwisterData.twisters.count { it.difficulty == diff }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDifficulty(diff) },
                            label = { Text("${diff.label} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(diff.badgeColorHex),
                                selectedLabelColor = Slate950,
                                containerColor = Slate850,
                                labelColor = Slate200
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(diff.badgeColorHex) else Slate700
                            )
                        )
                    }
                }
            }
        }

        // Horizontal Twister Selector Cards
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { twister ->
                    val isSelected = twister.id == currentTwister.id
                    val badgeColor = Color(twister.difficulty.badgeColorHex)

                    Surface(
                        modifier = Modifier
                            .width(220.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.selectTwister(twister) }
                            .testTag("twister_card_${twister.id}"),
                        color = if (isSelected) Slate800 else Slate900,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) badgeColor else CardSurfaceBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = badgeColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = twister.difficulty.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "${twister.targetWpm} WPM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = twister.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = twister.phoneticFocus,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Main Twister Exercise Card with Real-Time Word Highlighting
        item {
            val badgeColor = Color(currentTwister.difficulty.badgeColorHex)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_twister_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isRecording) EmeraldGreen else CardSurfaceBorder)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header metadata
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = badgeColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${currentTwister.difficulty.label.uppercase()} CHALLENGE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Target: ${currentTwister.targetWpm} WPM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentTwister.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Focus: ${currentTwister.phoneticFocus}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanAccent,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Twister Text with Live Word Highlighting
                    val words = currentTwister.text.split("\\s+".toRegex())
                    val annotatedText = buildAnnotatedString {
                        words.forEachIndexed { index, word ->
                            val isMatched = matchedWordsIndex.contains(index)
                            if (isMatched) {
                                pushStyle(
                                    SpanStyle(
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.ExtraBold,
                                        background = EmeraldGreen.copy(alpha = 0.15f)
                                    )
                                )
                            } else {
                                pushStyle(
                                    SpanStyle(
                                        color = if (isRecording) Slate400 else Slate200,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            append(word)
                            pop()
                            if (index < words.size - 1) append(" ")
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Slate950,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                    ) {
                        Text(
                            text = annotatedText,
                            style = MaterialTheme.typography.titleMedium,
                            lineHeight = 28.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tip box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate850, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentTwister.tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Live Speedometer & Audio Waveform HUD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CardSurfaceBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LIVE TIMER",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                            Text(
                                text = String.format(Locale.US, "%.1fs", elapsedSeconds),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isRecording) EmeraldGreen else Color.White
                            )
                        }

                        val liveWpm = if (elapsedSeconds > 0.5f) {
                            ((matchedWordsIndex.size / (elapsedSeconds / 60f))).toInt().coerceIn(0, 300)
                        } else 0

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "SPEED ESTIMATE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                            Text(
                                text = "$liveWpm WPM",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (liveWpm >= currentTwister.targetWpm) EmeraldGreen else CyanNeon
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Audio Waveform visualizer
                    AudioWaveform(
                        isListening = isRecording,
                        soundLevel = soundLevel,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (liveSpokenText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Detected: \"$liveSpokenText\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // Interactive Action Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hear Native Model Button
                Button(
                    onClick = {
                        if (isSpeakingModel) {
                            viewModel.stopModelAudio()
                        } else {
                            viewModel.playModelAudio(slowMotion = false)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hear_model_audio_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpeakingModel) Slate700 else Slate850
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSpeakingModel) "Stop Audio" else "Hear Native",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Primary Record / Stop Button
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.06f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Button(
                    onClick = {
                        if (!hasMicPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (isRecording) {
                                viewModel.stopListeningManually()
                            } else {
                                viewModel.startListening()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .scale(if (isRecording) pulseScale else 1f)
                        .testTag("twister_record_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) RoseError else EmeraldGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        tint = Slate950,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRecording) "Finish & Rate" else "Start Drills",
                        color = Slate950,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Evaluation Result Modal / Card
        if (evaluation != null) {
            item {
                TwisterEvaluationCard(
                    evaluation = evaluation!!,
                    targetWpm = currentTwister.targetWpm,
                    onRetry = { viewModel.startListening() }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun TwisterEvaluationCard(
    evaluation: TongueTwisterEvaluation,
    targetWpm: Int,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("twister_evaluation_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (evaluation.accuracyScore >= 80) EmeraldGreen else AmberWarning
            )
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (evaluation.accuracyScore >= 80) EmeraldGreen else AmberWarning,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Drill Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    color = if (evaluation.isTargetWpmBeaten) EmeraldGreen.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (evaluation.isTargetWpmBeaten) "⚡ SPEED BEATEN!" else "${evaluation.wordsPerMinute} WPM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (evaluation.isTargetWpmBeaten) EmeraldGreen else AmberWarning,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    label = "ACCURACY",
                    value = "${evaluation.accuracyScore}%",
                    color = if (evaluation.accuracyScore >= 80) EmeraldGreen else AmberWarning,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "SPEED",
                    value = "${evaluation.wordsPerMinute} WPM",
                    color = CyanNeon,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "TIME",
                    value = String.format(Locale.US, "%.1fs", evaluation.elapsedSeconds),
                    color = VioletNeon,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = evaluation.feedbackPraise,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            if (evaluation.mispronouncedWords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Words to polish: ${evaluation.mispronouncedWords.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = RoseError
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("twister_retry_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Slate950,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Practice Again",
                    color = Slate950,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Slate950,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
