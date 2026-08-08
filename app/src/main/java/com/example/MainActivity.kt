package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.UserProfileEntity
import com.example.ui.MainViewModel
import com.example.ui.NavigationScreen
import com.example.ui.components.NaniBottomBar
import com.example.ui.components.NaniTopBar
import com.example.ui.components.VoiceAssistantOverlay
import com.example.ui.screens.*
import com.example.ui.theme.NaniAiTheme
import com.example.ui.theme.ObsidianDark
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()

            NaniAiTheme {
                NaniMainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NaniMainContent(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val studyTopics by viewModel.studyTopics.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.userErrorMessage) {
        uiState.userErrorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearErrorMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        Scaffold(
            containerColor = ObsidianDark,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                NaniTopBar(
                    userName = userProfile?.name ?: "Alex",
                    currentScreen = uiState.currentScreen,
                    isOffline = uiState.isOfflineMode,
                    onVoiceClick = { viewModel.toggleVoiceOverlay(true) },
                    onProfileClick = { viewModel.navigateTo(NavigationScreen.SETTINGS) }
                )
            },
            bottomBar = {
                NaniBottomBar(
                    currentScreen = uiState.currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onVoiceClick = { viewModel.toggleVoiceOverlay(true) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(
                    targetState = uiState.currentScreen,
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        NavigationScreen.HOME -> HomeScreen(
                            userProfile = userProfile,
                            goals = goals,
                            tasks = tasks,
                            onQuickAction = { prompt -> viewModel.launchQuickAction(prompt) },
                            onNavigate = { target -> viewModel.navigateTo(target) },
                            onVoiceClick = { viewModel.toggleVoiceOverlay(true) }
                        )

                        NavigationScreen.CHAT -> ChatScreen(
                            conversations = conversations,
                            selectedConversationId = uiState.selectedConversationId,
                            messages = currentMessages,
                            isSending = uiState.isSendingMessage,
                            attachedImageUri = uiState.attachedImageUri,
                            attachedFileName = uiState.attachedFileName,
                            onSelectConversation = { id -> viewModel.selectConversation(id) },
                            onNewChat = { viewModel.startNewChat() },
                            onDeleteConversation = { id -> viewModel.deleteConversation(id) },
                            onSendMessage = { text -> viewModel.sendMessage(text) },
                            onAttachImage = { uri, base64 -> viewModel.attachImage(uri, base64) },
                            onAttachFile = { uri, name -> viewModel.attachFile(uri, name) },
                            onClearAttachments = { viewModel.clearAttachments() },
                            onVoiceClick = { viewModel.toggleVoiceOverlay(true) },
                            onDeleteMessage = { }
                        )

                        NavigationScreen.STUDY -> StudyScreen(
                            studyTopics = studyTopics,
                            onToggleTopicCompleted = { topic -> viewModel.toggleStudyTopic(topic) },
                            onAddStudyTopic = { topic, subject -> viewModel.addStudyTopic(subject, topic) },
                            onLaunchStudyPrompt = { prompt -> viewModel.launchQuickAction(prompt) }
                        )

                        NavigationScreen.TOOLS -> ToolsScreen(
                            onToolClick = { prompt -> viewModel.launchQuickAction(prompt) }
                        )

                        NavigationScreen.GOALS -> GoalsScreen(
                            goals = goals,
                            tasks = tasks,
                            onToggleTask = { task -> viewModel.toggleTask(task) },
                            onAddGoal = { title, _ -> viewModel.addGoal(title, "", "General", "Ongoing", "High") },
                            onAddTask = { goalId, title -> viewModel.addTask(title = title, goalId = goalId) }
                        )

                        NavigationScreen.DASHBOARD -> DashboardScreen(
                            userProfile = userProfile,
                            goals = goals,
                            tasks = tasks,
                            studyTopics = studyTopics,
                            onNavigate = { target -> viewModel.navigateTo(target) }
                        )

                        NavigationScreen.SETTINGS, NavigationScreen.AUTH -> ProfileScreen(
                            userProfile = userProfile,
                            isOffline = uiState.isOfflineMode,
                            onUpdateProfileName = { name ->
                                viewModel.updateProfile(userProfile?.copy(name = name) ?: UserProfileEntity(name = name))
                            }
                        )
                    }
                }
            }
        }

        if (uiState.isVoiceOverlayOpen) {
            VoiceAssistantOverlay(
                voiceState = voiceState,
                spokenText = viewModel.voiceManager.lastSpokenText.collectAsStateWithLifecycle(initialValue = "").value,
                aiResponseText = "",
                onStartListening = { viewModel.voiceManager.startListening() },
                onStopListening = { viewModel.voiceManager.stopListening() },
                onStopSpeaking = { viewModel.voiceManager.stopSpeaking() },
                onReplay = { },
                onClose = { viewModel.toggleVoiceOverlay(false) }
            )
        }
    }
}
