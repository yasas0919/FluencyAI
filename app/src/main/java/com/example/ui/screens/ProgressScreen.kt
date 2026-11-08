package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardSurfaceBorder
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.VioletNeon
import com.example.ui.theme.VioletPrimary
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.VocabularyViewModel

@Composable
fun ProgressScreen(
    homeViewModel: HomeViewModel,
    vocabularyViewModel: VocabularyViewModel? = null
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0: Analytics, 1: Word Bank
    val stats by homeViewModel.stats.collectAsState()
    val conversations by homeViewModel.conversations.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        if (vocabularyViewModel != null) {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = Slate900,
                contentColor = CyanNeon,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                        color = CyanNeon
                    )
                }
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = {
                        Text(
                            text = "Fluency Analytics",
                            fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSubTab == 0) CyanNeon else Slate400
                        )
                    }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = {
                        Text(
                            text = "Saved Word Bank",
                            fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSubTab == 1) CyanNeon else Slate400
                        )
                    }
                )
            }
        }

        if (selectedSubTab == 1 && vocabularyViewModel != null) {
            VocabularyScreen(viewModel = vocabularyViewModel)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Speaking Fluency Mastery",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Track your pronunciation improvements over time",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }
        }

        // Daily Streak Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streak_card"),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardSurfaceBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = AmberWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daily Speaking Streak",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Practice at least 1 speaking session per day to maintain muscle memory.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }

                    Text(
                        text = "🔥 7d",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberWarning
                    )
                }
            }
        }

        // Mastery Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardSurfaceBorder))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "FLUENCY PILLARS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SkillBar(
                        title = "Phoneme Articulation",
                        score = if (stats.avgPronunciation > 0) stats.avgPronunciation else 78,
                        barColor = CyanNeon
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SkillBar(
                        title = "Speaking Flow & Cadence",
                        score = if (stats.avgFluency > 0) stats.avgFluency else 82,
                        barColor = EmeraldGreen
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SkillBar(
                        title = "Grammar & Native Phrasing",
                        score = 85,
                        barColor = VioletNeon
                    )
                }
            }
        }

        // Badges Section
        item {
            Text(
                text = "ACHIEVEMENT BADGES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BadgeCard(
                    icon = "🎯",
                    title = "First Word Nailed",
                    desc = "Received your first instant pronunciation correction",
                    unlocked = true
                )
                BadgeCard(
                    icon = "⚡",
                    title = "Fluency Champion",
                    desc = "Achieved 85%+ spoken fluency on a full dialogue",
                    unlocked = stats.avgFluency >= 85 || stats.totalSessions >= 2
                )
                BadgeCard(
                    icon = "💼",
                    title = "Interview Ready",
                    desc = "Completed the Tech Job Interview scenario",
                    unlocked = conversations.any { it.scenarioId == "job_interview" }
                )
                BadgeCard(
                    icon = "🎓",
                    title = "IELTS 8.0 Candidate",
                    desc = "Completed formal academic speaking practice",
                    unlocked = conversations.any { it.scenarioId == "ielts_exam" }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
            }
        }
    }
}

@Composable
private fun SkillBar(
    title: String,
    score: Int,
    barColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Slate200,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$score%",
                style = MaterialTheme.typography.bodySmall,
                color = barColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = Slate800
        )
    }
}

@Composable
private fun BadgeCard(
    icon: String,
    title: String,
    desc: String,
    unlocked: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) Slate900 else Slate900.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardSurfaceBorder))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (unlocked) VioletPrimary.copy(alpha = 0.25f) else Slate800),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) Color.White else Slate400
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    fontSize = 11.sp
                )
            }
            if (unlocked) {
                Surface(
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Unlocked",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
