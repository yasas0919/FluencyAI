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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.MessageEntity
import com.example.data.model.FeedbackResult
import com.example.data.model.GrammarCorrection
import com.example.data.model.PronunciationItem
import com.example.ui.components.AppCheckStatusDialog
import com.example.ui.components.AudioWaveform
import com.example.ui.components.PronunciationFeedbackSheet
import com.example.ui.theme.CardSurfaceBorder
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.GlowViolet
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
import com.example.ui.viewmodel.ConversationUiState
import com.example.ui.viewmodel.ConversationViewModel
import com.example.util.SpeechState
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scenario by viewModel.activeScenario.collectAsState()
    val persona by viewModel.activePersona.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val latestFeedback by viewModel.latestFeedback.collectAsState()
    val showFeedbackSheet by viewModel.showFeedbackSheet.collectAsState()
    val ttsRate by viewModel.ttsRate.collectAsState()
    val isSpeakingAudio by viewModel.ttsManager.isSpeaking.collectAsState()

    val speechState by viewModel.speechRecognizerHelper.speechState.collectAsState()
    val soundLevel by viewModel.speechRecognizerHelper.soundLevel.collectAsState()

    var showKeyboardInput by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showAppCheckDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Permission launcher for microphone
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission = isGranted
        if (isGranted) {
            viewModel.startListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VioletPrimary.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = persona.avatarEmoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = scenario.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${persona.name} (${persona.accent} Accent)",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanAccent
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("conversation_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // TTS Speed Menu
                    Box {
                        IconButton(
                            onClick = { showSpeedMenu = true },
                            modifier = Modifier.testTag("speed_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Playback Speed",
                                tint = if (isSpeakingAudio) CyanNeon else Slate400
                            )
                        }
                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false },
                            modifier = Modifier.background(Slate900)
                        ) {
                            DropdownMenuItem(
                                text = { Text("0.75x Slow", color = Color.White) },
                                onClick = {
                                    viewModel.setTtsRate(0.75f)
                                    showSpeedMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("1.0x Normal", color = Color.White) },
                                onClick = {
                                    viewModel.setTtsRate(1.0f)
                                    showSpeedMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("1.25x Brisk", color = Color.White) },
                                onClick = {
                                    viewModel.setTtsRate(1.25f)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }

                    // App Check Status / Diagnostics
                    IconButton(
                        onClick = { showAppCheckDialog = true },
                        modifier = Modifier.testTag("app_check_status_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "App Check Status",
                            tint = CyanNeon
                        )
                    }

                    // Toggle keyboard / text input
                    IconButton(
                        onClick = { showKeyboardInput = !showKeyboardInput },
                        modifier = Modifier.testTag("toggle_keyboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Type with keyboard",
                            tint = if (showKeyboardInput) CyanNeon else Slate400
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950
                )
            )
        },
        containerColor = Slate950,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Error banner if any
            if (uiState is ConversationUiState.Error) {
                Surface(
                    color = RoseError.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = (uiState as ConversationUiState.Error).message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Error",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Scenario role header banner
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ROLEPLAY OBJECTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You are: ${scenario.roleB}  •  AI is: ${scenario.roleA}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate200
                            )
                            Text(
                                text = "Speak clearly. After each response, AI analyzes your pronunciation, syllable stress & fluency in real-time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        personaEmoji = persona.avatarEmoji,
                        isSpeaking = isSpeakingAudio,
                        onPlayAudio = { viewModel.speakText(msg.text) },
                        onReviewFeedback = {
                            val feedback = parseMessageFeedback(msg)
                            viewModel.setLatestFeedback(feedback)
                        }
                    )
                }

                // Processing indicator
                if (uiState is ConversationUiState.Processing) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = CyanNeon,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Analyzing speech & pronunciation with Gemini AI...",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanAccent
                            )
                        }
                    }
                }
            }

            // Real-time Partial Speech Transcript display
            AnimatedVisibility(visible = speechState is SpeechState.Listening || speechState is SpeechState.Partial) {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(RoseError)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE SPEECH TRANSCRIPTION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        }
                        val partialText = (speechState as? SpeechState.Partial)?.text.orEmpty()
                        Text(
                            text = if (partialText.isNotBlank()) "\"$partialText\"" else "Listening... speak now!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Quick suggested replies chips
            latestFeedback?.suggestedReplies?.takeIf { it.isNotEmpty() }?.let { suggestions ->
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    suggestions.forEach { reply ->
                        SuggestionChip(
                            onClick = {
                                viewModel.submitUserResponse(reply)
                            },
                            label = {
                                Text(
                                    text = "💬 $reply",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Slate200
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Slate850
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = Slate700
                            )
                        )
                    }
                }
            }

            // Live Audio Waveform Visualizer
            AudioWaveform(
                isListening = speechState is SpeechState.Listening || speechState is SpeechState.Partial,
                soundLevel = soundLevel,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            // Text Input Bar (when keyboard is toggled)
            AnimatedVisibility(visible = showKeyboardInput) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type what you want to say...", color = Slate400) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("text_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900,
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = CardSurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                val text = textInput
                                textInput = ""
                                viewModel.submitUserResponse(text)
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = CyanNeon,
                            contentColor = Slate900
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("send_text_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }

            // Bottom Primary Microphone Control Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                val isListening = speechState is SpeechState.Listening || speechState is SpeechState.Partial

                val pulseTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by pulseTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isListening) 1.25f else 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )

                // Background glow ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(if (isListening) GlowCyan else GlowViolet)
                )

                // Main Mic Button (min 64dp for comfortable tap target)
                FilledIconButton(
                    onClick = {
                        if (isListening) {
                            viewModel.stopListening()
                        } else {
                            if (hasRecordPermission) {
                                viewModel.startListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("mic_record_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isListening) RoseError else CyanNeon,
                        contentColor = if (isListening) Color.White else Slate950
                    )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Recording" else "Speak into microphone",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }

    // App Check Status Dialog
    if (showAppCheckDialog) {
        AppCheckStatusDialog(onDismiss = { showAppCheckDialog = false })
    }

    // Modal Pronunciation Feedback Sheet
    if (showFeedbackSheet && latestFeedback != null) {
        PronunciationFeedbackSheet(
            feedback = latestFeedback!!,
            onDismiss = { viewModel.setFeedbackSheetVisible(false) },
            onPlayWord = { word -> viewModel.speakText(word) },
            onSaveWord = { item ->
                // ViewModel saves word
                // Coroutine runs in repository
            },
            onSelectSuggestion = { suggestion ->
                viewModel.submitUserResponse(suggestion)
            }
        )
    }
}

@Composable
private fun MessageBubble(
    message: MessageEntity,
    personaEmoji: String,
    isSpeaking: Boolean,
    onPlayAudio: () -> Unit,
    onReviewFeedback: () -> Unit
) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Slate800),
                contentAlignment = Alignment.Center
            ) {
                Text(text = personaEmoji, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Surface(
                color = if (isUser) VioletPrimary else Slate900,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        lineHeight = 22.sp
                    )

                    // Audio replay button for AI messages
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPlayAudio() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Play Audio",
                                tint = CyanNeon,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Listen",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanNeon,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // For User messages, display instant pronunciation score tag & feedback button
            if (isUser && message.fluencyScore > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate850)
                        .clickable { onReviewFeedback() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Fluency: ${message.fluencyScore}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (message.fluencyScore >= 80) EmeraldGreen else CyanNeon
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• Pronunciation: ${message.pronunciationScore}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🔍 View Tips",
                        style = MaterialTheme.typography.labelSmall,
                        color = VioletNeon,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun parseMessageFeedback(msg: MessageEntity): FeedbackResult {
    val pronItems = mutableListOf<PronunciationItem>()
    try {
        if (msg.pronunciationJson.isNotBlank()) {
            val array = JSONArray(msg.pronunciationJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                pronItems.add(
                    PronunciationItem(
                        word = obj.optString("word", ""),
                        phoneticIpa = obj.optString("phoneticIpa", ""),
                        syllableStress = obj.optString("syllableStress", ""),
                        tip = obj.optString("tip", ""),
                        severity = obj.optString("severity", "needs_work")
                    )
                )
            }
        }
    } catch (_: Exception) {}

    val replies = mutableListOf<String>()
    try {
        if (msg.suggestedRepliesJson.isNotBlank()) {
            val array = JSONArray(msg.suggestedRepliesJson)
            for (i in 0 until array.length()) {
                replies.add(array.getString(i))
            }
        }
    } catch (_: Exception) {}

    val grammar = if (msg.grammarOriginal.isNotBlank() && msg.grammarBetter.isNotBlank()) {
        GrammarCorrection(
            originalSegment = msg.grammarOriginal,
            betterAlternative = msg.grammarBetter,
            explanation = msg.grammarExplanation
        )
    } else null

    return FeedbackResult(
        aiReply = "",
        fluencyScore = msg.fluencyScore,
        pronunciationScore = msg.pronunciationScore,
        grammarScore = msg.grammarScore,
        feedbackSummary = msg.feedbackSummary.ifBlank { "Personalized pronunciation analysis for your response." },
        pronunciationItems = pronItems,
        grammarCorrection = grammar,
        vocabularyBooster = msg.vocabularyBooster.takeIf { it.isNotBlank() },
        suggestedReplies = replies
    )
}
