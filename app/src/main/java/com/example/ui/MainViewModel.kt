package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceAssistantManager
import com.example.audio.VoiceState
import com.example.data.local.*
import com.example.data.repository.NaniRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class NavigationScreen {
    HOME, CHAT, STUDY, TOOLS, GOALS, DASHBOARD, SETTINGS, AUTH
}

data class UiState(
    val currentScreen: NavigationScreen = NavigationScreen.HOME,
    val selectedConversationId: String = "",
    val isVoiceOverlayOpen: Boolean = false,
    val isSendingMessage: Boolean = false,
    val attachedImageUri: String? = null,
    val attachedFileUri: String? = null,
    val attachedFileName: String? = null,
    val attachedBase64Image: String? = null,
    val studySelectedSubject: String = "All",
    val searchQuery: String = "",
    val isOfflineMode: Boolean = false,
    val userErrorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = NaniRepository(database)
    val voiceManager = VoiceAssistantManager(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val conversations = repository.conversations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val memories = repository.memories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val goals = repository.goals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasks = repository.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val studyTopics = repository.studyTopics.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentMessages: StateFlow<List<ChatMessageEntity>> = _uiState.flatMapLatest { state ->
        if (state.selectedConversationId.isNotBlank()) {
            repository.getMessagesForConversation(state.selectedConversationId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceState: StateFlow<VoiceState> = voiceManager.voiceState

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
            // Create default active conversation if none exists
            val existing = repository.conversations.firstOrNull()
            if (existing.isNullOrEmpty()) {
                val newId = repository.createNewConversation("NANI Primary Chat")
                _uiState.update { it.copy(selectedConversationId = newId) }
            } else {
                _uiState.update { it.copy(selectedConversationId = existing.first().id) }
            }
        }

        // Handle voice command results
        viewModelScope.launch {
            voiceManager.lastSpokenText.collect { spoken ->
                if (spoken.isNotBlank()) {
                    sendMessage(userText = spoken, speakResponse = true)
                }
            }
        }
    }

    fun navigateTo(screen: NavigationScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun selectConversation(id: String) {
        _uiState.update { it.copy(selectedConversationId = id, currentScreen = NavigationScreen.CHAT) }
    }

    fun startNewChat() {
        viewModelScope.launch {
            val newId = repository.createNewConversation("New Conversation")
            _uiState.update { it.copy(selectedConversationId = newId, currentScreen = NavigationScreen.CHAT) }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            val remaining = repository.conversations.firstOrNull()
            if (!remaining.isNullOrEmpty()) {
                _uiState.update { it.copy(selectedConversationId = remaining.first().id) }
            } else {
                val newId = repository.createNewConversation("NANI AI Chat")
                _uiState.update { it.copy(selectedConversationId = newId) }
            }
        }
    }

    fun attachImage(uri: String, base64: String) {
        _uiState.update { it.copy(attachedImageUri = uri, attachedBase64Image = base64) }
    }

    fun attachFile(uri: String, fileName: String) {
        _uiState.update { it.copy(attachedFileUri = uri, attachedFileName = fileName) }
    }

    fun clearAttachments() {
        _uiState.update { it.copy(attachedImageUri = null, attachedBase64Image = null, attachedFileUri = null, attachedFileName = null) }
    }

    fun sendMessage(userText: String, speakResponse: Boolean = false) {
        if (userText.isBlank() && _uiState.value.attachedImageUri == null && _uiState.value.attachedFileUri == null) return

        val convId = _uiState.value.selectedConversationId.ifBlank {
            return
        }

        val state = _uiState.value
        _uiState.update { it.copy(isSendingMessage = true) }

        viewModelScope.launch {
            try {
                val responseText = repository.sendMessage(
                    conversationId = convId,
                    userText = userText,
                    imageUri = state.attachedImageUri,
                    fileUri = state.attachedFileUri,
                    fileName = state.attachedFileName,
                    base64ImageData = state.attachedBase64Image
                )
                clearAttachments()
                _uiState.update { it.copy(isSendingMessage = false) }

                if (speakResponse) {
                    voiceManager.speak(responseText)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSendingMessage = false, userErrorMessage = e.localizedMessage) }
            }
        }
    }

    fun toggleVoiceOverlay(open: Boolean) {
        _uiState.update { it.copy(isVoiceOverlayOpen = open) }
        if (open) {
            voiceManager.startListening()
        } else {
            voiceManager.stopListening()
            voiceManager.stopSpeaking()
        }
    }

    // Quick Action launchers from Home / Tools
    fun launchQuickAction(actionPrompt: String) {
        _uiState.update { it.copy(currentScreen = NavigationScreen.CHAT) }
        sendMessage(actionPrompt)
    }

    // Memory actions
    fun addMemory(category: String, key: String, value: String) {
        viewModelScope.launch {
            repository.addMemory(category, key, value)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    // Goals & Tasks
    fun addGoal(title: String, description: String, category: String, deadline: String, priority: String) {
        viewModelScope.launch {
            repository.addGoal(title, description, category, deadline, priority)
        }
    }

    fun updateGoalProgress(goal: GoalEntity, newProgress: Int) {
        viewModelScope.launch {
            repository.updateGoalProgress(goal, newProgress)
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    fun addTask(title: String, deadline: String = "Today", priority: String = "Medium", goalId: Long? = null) {
        viewModelScope.launch {
            repository.addTask(title, deadline, priority, goalId)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    // Study
    fun addStudyTopic(subject: String, topicName: String) {
        viewModelScope.launch {
            repository.addStudyTopic(subject, topicName)
        }
    }

    fun toggleStudyTopic(topic: StudyTopicEntity) {
        viewModelScope.launch {
            repository.toggleTopicCompleted(topic)
        }
    }

    // Settings
    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(userErrorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
