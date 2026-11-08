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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.CallDebrief
import com.example.data.model.CallStatus
import com.example.data.model.NativeCallAccent
import com.example.data.model.NativeCallData
import com.example.data.model.NativeCallPersona
import com.example.data.model.NativeCallScenario
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
import com.example.ui.viewmodel.NativeVoiceCallViewModel

@Composable
fun NativeVoiceCallScreen(
    viewModel: NativeVoiceCallViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val persona by viewModel.persona.collectAsState()
    val scenario by viewModel.scenario.collectAsState()
    val accent by viewModel.accent.collectAsState()
    val callStatus by viewModel.callStatus.collectAsState()
    val callDuration by viewModel.callDurationSeconds.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val showCaptions by viewModel.showCaptions.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentTranscript by viewModel.currentSpokenTranscript.collectAsState()
    val debrief by viewModel.callDebrief.collectAsState()
    val soundLevel by viewModel.speechRecognizerHelper.soundLevel.collectAsState()

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

    val isCallActive = callStatus == CallStatus.CALLING ||
            callStatus == CallStatus.CONNECTED ||
            callStatus == CallStatus.LISTENING ||
            callStatus == CallStatus.AI_SPEAKING

    if (debrief != null) {
        // Debrief Screen
        CallDebriefView(
            debrief = debrief!!,
            onCallAgain = { viewModel.startCall() },
            onDone = { viewModel.resetToDialer() }
        )
    } else if (isCallActive) {
        // Active Phone Call Interface
        ActiveCallInterface(
            persona = persona,
            scenario = scenario,
            accent = accent,
            callStatus = callStatus,
            durationSeconds = callDuration,
            soundLevel = soundLevel,
            isMuted = isMuted,
            isSpeakerOn = isSpeakerOn,
            showCaptions = showCaptions,
            messages = messages,
            currentTranscript = currentTranscript,
            onToggleMute = { viewModel.toggleMute() },
            onToggleSpeaker = { viewModel.toggleSpeaker() },
            onToggleCaptions = { viewModel.toggleCaptions() },
            onSwitchAccent = { newAccent -> viewModel.setAccent(newAccent) },
            onEndCall = { viewModel.endCall() }
        )
    } else {
        // Setup & Scenario Selector Screen
        CallSetupView(
            selectedPersona = persona,
            selectedScenario = scenario,
            selectedAccent = accent,
            onSelectPersona = { viewModel.selectPersona(it) },
            onSelectScenario = { viewModel.selectScenario(it) },
            onSelectAccent = { viewModel.setAccent(it) },
            onStartCall = {
                if (!hasMicPermission) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    viewModel.startCall()
                }
            },
            onBack = onBack
        )
    }
}

@Composable
private fun CallSetupView(
    selectedPersona: NativeCallPersona,
    selectedScenario: NativeCallScenario,
    selectedAccent: NativeCallAccent,
    onSelectPersona: (NativeCallPersona) -> Unit,
    onSelectScenario: (NativeCallScenario) -> Unit,
    onSelectAccent: (NativeCallAccent) -> Unit,
    onStartCall: () -> Unit,
    onBack: () -> Unit
) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("call_setup_back_btn")
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
                            text = "Native Speaker Call",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "LIVE VOICE ROLEPLAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Conversational phone calls with US, UK & Australian native AI partners",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }
        }

        // Accent Selection Bar
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
                    Text(
                        text = "CHOOSE ACCENT TO PRACTICE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NativeCallAccent.values().forEach { acc ->
                            val isSelected = acc == selectedAccent
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectAccent(acc) }
                                    .testTag("accent_chip_${acc.code}"),
                                color = if (isSelected) Slate800 else Slate850,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) CyanNeon else Slate700
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = acc.flag, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = acc.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyanNeon else Slate200
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedAccent.regionNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Persona Selection
        item {
            Column {
                Text(
                    text = "SELECT AI CALL PARTNER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(NativeCallData.personas, key = { it.id }) { p ->
                        val isSelected = p.id == selectedPersona.id
                        Surface(
                            modifier = Modifier
                                .width(200.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectPersona(p) }
                                .testTag("call_persona_${p.id}"),
                            color = if (isSelected) Slate800 else Slate900,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) CyanNeon else CardSurfaceBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = p.avatarEmoji, fontSize = 26.sp)
                                    Text(
                                        text = "${p.defaultAccent.flag} ${p.defaultAccent.label}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = p.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scenario Selection
        item {
            Text(
                text = "SELECT CALL SCENARIO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
        }

        items(NativeCallData.scenarios, key = { it.id }) { sc ->
            val isSelected = sc.id == selectedScenario.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectScenario(sc) }
                    .testTag("call_scenario_${sc.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Slate800 else Slate900
                ),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isSelected) CyanNeon else CardSurfaceBorder
                    )
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = VioletPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = sc.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = VioletNeon,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (isSelected) {
                            Text(
                                text = "SELECTED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = sc.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sc.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Start Call Button
        item {
            Button(
                onClick = onStartCall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_voice_call_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Slate950,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Voice Call with ${selectedPersona.name}",
                    color = Slate950,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActiveCallInterface(
    persona: NativeCallPersona,
    scenario: NativeCallScenario,
    accent: NativeCallAccent,
    callStatus: CallStatus,
    durationSeconds: Int,
    soundLevel: Float,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    showCaptions: Boolean,
    messages: List<com.example.data.model.CallMessage>,
    currentTranscript: String,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleCaptions: () -> Unit,
    onSwitchAccent: (NativeCallAccent) -> Unit,
    onEndCall: () -> Unit
) {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val formattedDuration = String.format("%02d:%02d", minutes, seconds)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = accent.flag, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = scenario.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate200,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = persona.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = if (callStatus == CallStatus.CALLING) "Connecting..." else formattedDuration,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                color = if (callStatus == CallStatus.CALLING) AmberWarning else CyanNeon,
                fontWeight = FontWeight.Bold
            )
        }

        // Center Avatar & Pulsing Rings
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (callStatus == CallStatus.AI_SPEAKING || callStatus == CallStatus.LISTENING) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(pulseSize)
                        .clip(CircleShape)
                        .background(
                            if (callStatus == CallStatus.AI_SPEAKING)
                                CyanNeon.copy(alpha = 0.15f)
                            else
                                EmeraldGreen.copy(alpha = 0.15f)
                        )
                )
            }

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Slate900)
                    .border(
                        3.dp,
                        if (callStatus == CallStatus.AI_SPEAKING) CyanNeon else if (callStatus == CallStatus.LISTENING) EmeraldGreen else CardSurfaceBorder,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = persona.avatarEmoji, fontSize = 56.sp)
            }
        }

        // Live Audio Waveform & Status Tag
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AudioWaveform(
                isListening = callStatus == CallStatus.LISTENING && !isMuted,
                soundLevel = if (callStatus == CallStatus.AI_SPEAKING) 0.65f else soundLevel,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val statusText = when {
                isMuted -> "Microphone Muted"
                callStatus == CallStatus.CALLING -> "Dialing native partner..."
                callStatus == CallStatus.AI_SPEAKING -> "${persona.name} is speaking..."
                callStatus == CallStatus.LISTENING -> "Listening to you... Speak naturally"
                else -> "Connected"
            }

            Surface(
                color = if (isMuted) RoseError.copy(alpha = 0.2f) else Slate900,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isMuted) RoseError else CyanAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // Real-time Live Caption Box
            if (showCaptions) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    color = Slate900.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                ) {
                    val latest = messages.lastOrNull()
                    val textToDisplay = if (currentTranscript.isNotBlank()) {
                        "You: \"$currentTranscript\""
                    } else if (latest != null) {
                        "${latest.sender}: \"${latest.text}\""
                    } else {
                        "Call connected. Live captions will appear as you speak."
                    }

                    Text(
                        text = textToDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate200,
                        modifier = Modifier.padding(10.dp),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Call Controls Dock
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Accent Switcher Quick Bar
            Row(
                modifier = Modifier
                    .background(Slate900, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Accent:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400,
                    fontSize = 11.sp
                )
                NativeCallAccent.values().forEach { acc ->
                    val isSelected = acc == accent
                    Text(
                        text = "${acc.flag} ${acc.code}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CyanNeon else Slate400,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSwitchAccent(acc) }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Call Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute Toggle
                FilledIconButton(
                    onClick = onToggleMute,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isMuted) RoseError else Slate850
                    ),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("call_mute_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (isMuted) Color.White else CyanNeon
                    )
                }

                // Captions Toggle
                FilledIconButton(
                    onClick = onToggleCaptions,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (showCaptions) CyanNeon else Slate850
                    ),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ClosedCaption,
                        contentDescription = "Captions",
                        tint = if (showCaptions) Slate950 else Slate200
                    )
                }

                // Speakerphone Toggle
                FilledIconButton(
                    onClick = onToggleSpeaker,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isSpeakerOn) Slate700 else Slate850
                    ),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker",
                        tint = Color.White
                    )
                }

                // End Call Red Button
                FilledIconButton(
                    onClick = onEndCall,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = RoseError
                    ),
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("end_call_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CallDebriefView(
    debrief: CallDebrief,
    onCallAgain: () -> Unit,
    onDone: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Call Summary & Debrief",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Conversation with ${debrief.personaName} • ${debrief.accentUsed.flag} ${debrief.accentUsed.label}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
        }

        // Metrics Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(EmeraldGreen)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = debrief.praiseMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DebriefMetric(
                            title = "Fluency",
                            value = "${debrief.overallFluencyScore}%",
                            color = EmeraldGreen,
                            modifier = Modifier.weight(1f)
                        )
                        DebriefMetric(
                            title = "Clarity",
                            value = "${debrief.pronunciationClarity}%",
                            color = CyanNeon,
                            modifier = Modifier.weight(1f)
                        )
                        DebriefMetric(
                            title = "Duration",
                            value = "${debrief.callDurationSeconds}s",
                            color = VioletNeon,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Accent Coach Advice
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = debrief.accentUsed.flag, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${debrief.accentUsed.label.uppercase()} ACCENT COACHING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = debrief.accentTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate200,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Recommended Idioms & Phrasing
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
                    Text(
                        text = "RECOMMENDED EXPRESSIONS FOR THIS SCENARIO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    debrief.recommendedExpressions.forEach { expr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "•", color = CyanNeon, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = expr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDone,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate850),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Done", color = Slate200, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onCallAgain,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Slate950,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Call Again", color = Slate950, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DebriefMetric(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Slate950,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
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
