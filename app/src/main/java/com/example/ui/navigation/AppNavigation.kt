package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.DailyReadingScreen
import com.example.ui.screens.DrillScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PresentationScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.VioletNeon
import com.example.ui.viewmodel.BookReadingViewModel
import com.example.ui.viewmodel.ConversationViewModel
import com.example.ui.viewmodel.DrillViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.PresentationViewModel
import com.example.ui.viewmodel.VocabularyViewModel

enum class NavTab {
    SCENARIOS,
    PRESENTATION,
    BOOK_READING,
    DRILL_LAB,
    PROGRESS
}

@Composable
fun AppNavigation(
    conversationViewModel: ConversationViewModel = viewModel(),
    drillViewModel: DrillViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    vocabularyViewModel: VocabularyViewModel = viewModel(),
    presentationViewModel: PresentationViewModel = viewModel(),
    bookReadingViewModel: BookReadingViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(NavTab.SCENARIOS) }
    var inConversationScreen by remember { mutableStateOf(false) }

    if (inConversationScreen) {
        BackHandler {
            inConversationScreen = false
        }
        ConversationScreen(
            viewModel = conversationViewModel,
            onBack = { inConversationScreen = false }
        )
    } else {
        if (currentTab != NavTab.SCENARIOS) {
            BackHandler {
                currentTab = NavTab.SCENARIOS
            }
        }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Slate900,
                    contentColor = Color.White,
                    tonalElevation = androidx.compose.ui.unit.Dp(0f),
                    modifier = Modifier
                        .testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentTab == NavTab.SCENARIOS,
                        onClick = { currentTab = NavTab.SCENARIOS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = "Scenarios"
                            )
                        },
                        label = { Text("Speak", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate950,
                            selectedTextColor = CyanNeon,
                            indicatorColor = CyanNeon,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("tab_scenarios")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.PRESENTATION,
                        onClick = { currentTab = NavTab.PRESENTATION },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CoPresent,
                                contentDescription = "AI Presentation"
                            )
                        },
                        label = { Text("Present", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate950,
                            selectedTextColor = VioletNeon,
                            indicatorColor = VioletNeon,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("tab_presentation")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.BOOK_READING,
                        onClick = { currentTab = NavTab.BOOK_READING },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = "Daily Reading"
                            )
                        },
                        label = { Text("Read", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate950,
                            selectedTextColor = EmeraldGreen,
                            indicatorColor = EmeraldGreen,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("tab_reading")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.DRILL_LAB,
                        onClick = { currentTab = NavTab.DRILL_LAB },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Drill Lab"
                            )
                        },
                        label = { Text("Drills", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate950,
                            selectedTextColor = CyanNeon,
                            indicatorColor = CyanNeon,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("tab_drill_lab")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.PROGRESS,
                        onClick = { currentTab = NavTab.PROGRESS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Progress"
                            )
                        },
                        label = { Text("Progress", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate950,
                            selectedTextColor = CyanNeon,
                            indicatorColor = CyanNeon,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("tab_progress")
                    )
                }
            },
            containerColor = Slate950
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavTab.SCENARIOS -> {
                        HomeScreen(
                            homeViewModel = homeViewModel,
                            onStartScenario = { scenario, persona ->
                                conversationViewModel.startSession(scenario, persona)
                                inConversationScreen = true
                            },
                            onResumeConversation = { convId, scenario, persona ->
                                conversationViewModel.resumeSession(convId, scenario, persona)
                                inConversationScreen = true
                            },
                            onNavigateToPresentation = {
                                currentTab = NavTab.PRESENTATION
                            },
                            onNavigateToReading = {
                                currentTab = NavTab.BOOK_READING
                            }
                        )
                    }

                    NavTab.PRESENTATION -> {
                        PresentationScreen(viewModel = presentationViewModel)
                    }

                    NavTab.BOOK_READING -> {
                        DailyReadingScreen(viewModel = bookReadingViewModel)
                    }

                    NavTab.DRILL_LAB -> {
                        DrillScreen(viewModel = drillViewModel)
                    }

                    NavTab.PROGRESS -> {
                        ProgressScreen(
                            homeViewModel = homeViewModel,
                            vocabularyViewModel = vocabularyViewModel
                        )
                    }
                }
            }
        }
    }
}
