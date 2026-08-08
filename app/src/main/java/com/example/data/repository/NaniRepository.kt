package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class NaniRepository(private val db: AppDatabase) {

    val conversations: Flow<List<ConversationEntity>> = db.conversationDao().getConversations()
    val memories: Flow<List<PersonalMemoryEntity>> = db.memoryDao().getMemories()
    val goals: Flow<List<GoalEntity>> = db.goalDao().getGoals()
    val tasks: Flow<List<TaskEntity>> = db.taskDao().getTasks()
    val studyTopics: Flow<List<StudyTopicEntity>> = db.studyDao().getStudyTopics()
    val userProfile: Flow<UserProfileEntity?> = db.userDao().getUserProfile()

    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>> {
        return db.chatMessageDao().getMessagesForConversation(conversationId)
    }

    suspend fun createNewConversation(title: String = "New Chat"): String {
        val id = UUID.randomUUID().toString()
        val conv = ConversationEntity(id = id, title = title)
        db.conversationDao().insertConversation(conv)
        return id
    }

    suspend fun deleteConversation(conversationId: String) {
        db.chatMessageDao().deleteMessagesForConversation(conversationId)
        db.conversationDao().deleteConversation(conversationId)
    }

    suspend fun clearAllConversations() {
        db.conversationDao().clearAllConversations()
    }

    suspend fun deleteMessage(messageId: Long) {
        db.chatMessageDao().deleteMessage(messageId)
    }

    suspend fun sendMessage(
        conversationId: String,
        userText: String,
        imageUri: String? = null,
        fileUri: String? = null,
        fileName: String? = null,
        base64ImageData: String? = null
    ): String = withContext(Dispatchers.IO) {
        // 1. Save User Message
        val userMsg = ChatMessageEntity(
            conversationId = conversationId,
            sender = "USER",
            text = userText,
            imageUri = imageUri,
            fileUri = fileUri,
            fileName = fileName
        )
        db.chatMessageDao().insertMessage(userMsg)

        // Update conversation title if default
        val historyList = db.chatMessageDao().getMessagesListForConversation(conversationId)
        if (historyList.size <= 2) {
            val autoTitle = if (userText.length > 28) userText.take(28) + "..." else userText
            db.conversationDao().insertConversation(
                ConversationEntity(id = conversationId, title = autoTitle.ifBlank { "NANI Chat" })
            )
        }

        // 2. Check for explicit local Memory commands: "Remember this...", "Forget that...", "What do you remember..."
        val lowerText = userText.lowercase().trim()
        if (lowerText.startsWith("remember this") || lowerText.startsWith("remember that") || lowerText.contains("remember:")) {
            val fact = userText.substringAfter("remember", userText).replace("this", "", true).replace("that", "", true).replace(":", "", true).trim()
            if (fact.isNotBlank()) {
                db.memoryDao().insertMemory(
                    PersonalMemoryEntity(category = "Preference", memoryKey = "Saved Memory", memoryValue = fact)
                )
                val aiReply = "Got it! I have saved to my Personal Memory: \"$fact\"."
                db.chatMessageDao().insertMessage(ChatMessageEntity(conversationId = conversationId, sender = "AI", text = aiReply))
                return@withContext aiReply
            }
        } else if (lowerText.contains("what do you remember about me") || lowerText.contains("what do you remember")) {
            val memList = db.memoryDao().getMemoriesList()
            val aiReply = if (memList.isEmpty()) {
                "I don't have any personal memories saved yet. You can tell me 'Remember this: [your goal/preference]' and I'll keep track of it!"
            } else {
                val formatted = memList.joinToString("\n") { "• [${it.category}] ${it.memoryKey}: ${it.memoryValue}" }
                "Here is what I remember about you:\n\n$formatted"
            }
            db.chatMessageDao().insertMessage(ChatMessageEntity(conversationId = conversationId, sender = "AI", text = aiReply))
            return@withContext aiReply
        }

        // 3. Check for task creation commands: "Create a task to...", "Add task..."
        if (lowerText.contains("create a task") || lowerText.contains("add task")) {
            val taskTitle = userText.replace("create a task to", "", true)
                .replace("create a task", "", true)
                .replace("add task", "", true).trim()
            if (taskTitle.isNotBlank()) {
                db.taskDao().insertTask(
                    TaskEntity(title = taskTitle, deadline = "Tomorrow", priority = "High")
                )
            }
        }

        // 4. Gather Personal Memories context for AI system prompt
        val currentMemories = db.memoryDao().getMemoriesList()
        val profile = db.userDao().getUserProfileDirect()
        val userName = profile?.name ?: "Commander"
        val personality = profile?.aiPersonality ?: "JARVIS Professional"

        val memoryContext = if (currentMemories.isNotEmpty()) {
            "\nUser Personal Memories:\n" + currentMemories.joinToString("\n") { "- ${it.category}: ${it.memoryValue}" }
        } else ""

        val systemPrompt = """
            You are NANI AI, a personal AI operating system & futuristic JARVIS-style assistant for $userName.
            Tone / Personality: $personality.
            Always provide helpful, intelligent, clean Markdown formatted responses with code blocks where applicable.
            $memoryContext
        """.trimIndent()

        // 5. Call Gemini API
        val apiKey = GeminiApiClient.getApiKey()
        if (apiKey.isBlank()) {
            val fallbackText = generateOfflineOrDemoResponse(userText, userName)
            db.chatMessageDao().insertMessage(ChatMessageEntity(conversationId = conversationId, sender = "AI", text = fallbackText))
            return@withContext fallbackText
        }

        val geminiParts = mutableListOf<GeminiPart>()
        if (!base64ImageData.isNullOrBlank()) {
            geminiParts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64ImageData)))
        }
        geminiParts.add(GeminiPart(text = userText))

        // Build conversation history parts for recent 6 turns
        val recentHistory = historyList.takeLast(10).map { msg ->
            GeminiContent(
                role = if (msg.sender == "USER") "user" else "model",
                parts = listOf(GeminiPart(text = msg.text))
            )
        }

        val contentsList = mutableListOf<GeminiContent>()
        contentsList.addAll(recentHistory)
        contentsList.add(GeminiContent(role = "user", parts = geminiParts))

        val requestBody = GeminiRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(temperature = 0.7f)
        )

        val aiReplyText = try {
            val response = GeminiApiClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = requestBody
            )
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            text ?: "NANI AI core completed processing, but no text was generated."
        } catch (e: Exception) {
            "⚡ [Offline Mode / Network Alert]: Unable to reach AI server (${e.localizedMessage ?: "Connection error"}).\n\n" +
            generateOfflineOrDemoResponse(userText, userName)
        }

        db.chatMessageDao().insertMessage(ChatMessageEntity(conversationId = conversationId, sender = "AI", text = aiReplyText))
        return@withContext aiReplyText
    }

    private fun generateOfflineOrDemoResponse(query: String, userName: String): String {
        val q = query.lowercase()
        return when {
            q.contains("recursion") -> """
                ### Recursion in Programming
                Think of **recursion** as a function calling itself to solve smaller sub-problems until it hits a **base case**.

                ```c
                #include <stdio.stdio>

                int factorial(int n) {
                    if (n <= 1) return 1; // Base case
                    return n * factorial(n - 1); // Recursive call
                }

                int main() {
                    printf("Factorial of 5: %d\n", factorial(5));
                    return 0;
                }
                ```
                *Analogy*: Imagine standing between two parallel mirrors — the reflections repeat endlessly until blocked!
            """.trimIndent()

            q.contains("pointer") -> """
                ### C Pointers Explained Simply
                A **pointer** is a variable that stores the **memory address** of another variable.

                ```c
                int x = 42;
                int *ptr = &x; // ptr stores the address of x

                printf("Value of x: %d\n", x);
                printf("Address of x: %p\n", ptr);
                printf("Value at ptr: %d\n", *ptr); // Dereferencing
                ```
                - `&`: Address-of operator
                - `*`: Dereference operator (fetches value at address)
            """.trimIndent()

            q.contains("study plan") -> """
                ### NANI Daily Study Blueprint for $userName
                Here is your optimized 4-step focus schedule:

                1. 🎯 **09:00 AM - 10:30 AM**: Data Structures & Pointers (Deep Work)
                2. 💻 **11:00 AM - 12:30 PM**: System Architecture & Memory Management
                3. 🧠 **02:00 PM - 03:30 PM**: Practice Coding & Debugging
                4. 📚 **04:00 PM - 05:00 PM**: Active Recall & MCQ Review
            """.trimIndent()

            else -> """
                Greetings $userName! I am **NANI AI**, your personal operating assistant.

                I am standing by to assist with:
                - 📚 **Study & Exam Preparation**: Concept breakdowns, Flashcards, Quiz generator
                - 💻 **Software Engineering**: Debugging, Code explanations, Algorithms
                - 🎯 **Goals & Tasks**: Automated study plans & project tracking
                - 🧠 **Personal Memory**: I remember your key preferences, projects, and goals!
            """.trimIndent()
        }
    }

    // Memory Management
    suspend fun addMemory(category: String, key: String, value: String) {
        db.memoryDao().insertMemory(PersonalMemoryEntity(category = category, memoryKey = key, memoryValue = value))
    }

    suspend fun deleteMemory(id: Long) {
        db.memoryDao().deleteMemory(id)
    }

    suspend fun clearAllMemories() {
        db.memoryDao().clearAllMemories()
    }

    // Goals & Tasks Management
    suspend fun addGoal(title: String, description: String, category: String, deadline: String, priority: String): Long {
        return db.goalDao().insertGoal(
            GoalEntity(title = title, description = description, category = category, deadline = deadline, priority = priority)
        )
    }

    suspend fun updateGoalProgress(goal: GoalEntity, newProgress: Int) {
        db.goalDao().updateGoal(goal.copy(progressPercent = newProgress.coerceIn(0, 100)))
    }

    suspend fun deleteGoal(id: Long) {
        db.goalDao().deleteGoal(id)
    }

    suspend fun addTask(title: String, deadline: String = "Today", priority: String = "Medium", goalId: Long? = null) {
        db.taskDao().insertTask(TaskEntity(title = title, deadline = deadline, priority = priority, goalId = goalId))
    }

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        db.taskDao().updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun deleteTask(id: Long) {
        db.taskDao().deleteTask(id)
    }

    // Study Topics
    suspend fun addStudyTopic(subject: String, topicName: String, revisionDate: String = "Tomorrow") {
        db.studyDao().insertTopic(StudyTopicEntity(subject = subject, topicName = topicName, revisionDate = revisionDate))
    }

    suspend fun toggleTopicCompleted(topic: StudyTopicEntity) {
        db.studyDao().updateTopic(topic.copy(isCompleted = !topic.isCompleted))
    }

    // User Profile & Settings
    suspend fun updateProfile(profile: UserProfileEntity) {
        db.userDao().insertOrUpdateProfile(profile)
    }

    suspend fun initDefaultDataIfNeeded() {
        withContext(Dispatchers.IO) {
            val user = db.userDao().getUserProfileDirect()
            if (user == null) {
                db.userDao().insertOrUpdateProfile(UserProfileEntity())
            }

            // Seed default goal if empty
            val currentGoals = db.goalDao().getGoals().firstOrNull()
            if (currentGoals.isNullOrEmpty()) {
                val goalId = db.goalDao().insertGoal(
                    GoalEntity(
                        title = "Master Systems Programming & AI",
                        description = "Complete C/C++ memory management, algorithms, and Jetpack Compose Android app development",
                        category = "Computer Science",
                        deadline = "30 Days",
                        priority = "High",
                        progressPercent = 42
                    )
                )
                db.taskDao().insertTask(TaskEntity(title = "Study C Pointers & Memory Allocation", deadline = "Today", priority = "High", goalId = goalId))
                db.taskDao().insertTask(TaskEntity(title = "Practice Jetpack Compose UI Layouts", deadline = "Tomorrow", priority = "Medium", goalId = goalId))
                db.taskDao().insertTask(TaskEntity(title = "Implement Gemini API Integration", deadline = "In 2 days", priority = "High", goalId = goalId, isCompleted = true))
            }

            // Seed default personal memories if empty
            val currentMems = db.memoryDao().getMemoriesList()
            if (currentMems.isEmpty()) {
                db.memoryDao().insertMemory(PersonalMemoryEntity(category = "Learning Goal", memoryKey = "Primary Focus", memoryValue = "Android Development with Kotlin & AI Architecture"))
                db.memoryDao().insertMemory(PersonalMemoryEntity(category = "Preference", memoryKey = "Study Style", memoryValue = "Concise code snippets with real-world analogies and practice quizzes"))
                db.memoryDao().insertMemory(PersonalMemoryEntity(category = "Project", memoryKey = "Active App", memoryValue = "NANI AI - Personal Assistant OS"))
            }

            // Seed default study topics if empty
            val currentTopics = db.studyDao().getStudyTopics().firstOrNull()
            if (currentTopics.isNullOrEmpty()) {
                db.studyDao().insertTopic(StudyTopicEntity(subject = "C Programming", topicName = "Pointers & Memory", revisionDate = "Today"))
                db.studyDao().insertTopic(StudyTopicEntity(subject = "Data Structures", topicName = "Binary Search Trees", revisionDate = "Tomorrow"))
                db.studyDao().insertTopic(StudyTopicEntity(subject = "Cybersecurity", topicName = "Network Protocols & Encryption", revisionDate = "In 3 Days"))
            }
        }
    }
}
