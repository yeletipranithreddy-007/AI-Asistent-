package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val fileUri: String? = null,
    val fileName: String? = null
)

@Entity(tableName = "personal_memories")
data class PersonalMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "Learning Goal", "Preference", "Project", "Interest", "Fact"
    val memoryKey: String,
    val memoryValue: String,
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val deadline: String,
    val priority: String, // "High", "Medium", "Low"
    val progressPercent: Int = 0, // 0 to 100
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long? = null,
    val title: String,
    val deadline: String,
    val priority: String = "Medium",
    val isCompleted: Boolean = false,
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = "local_user"
)

@Entity(tableName = "study_topics")
data class StudyTopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val topicName: String,
    val isCompleted: Boolean = false,
    val revisionDate: String,
    val notesText: String = "",
    val userId: String = "local_user"
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val userId: String = "local_user",
    val name: String = "Commander",
    val email: String = "user@naniai.app",
    val isLoggedIn: Boolean = true,
    val aiPersonality: String = "JARVIS Professional", // "JARVIS Professional", "Casual Mentor", "Strict Tutor"
    val voiceName: String = "Kore",
    val responseLength: String = "Balanced", // "Concise", "Balanced", "Detailed"
    val themeMode: String = "Dark" // "Dark", "Light", "System"
)
