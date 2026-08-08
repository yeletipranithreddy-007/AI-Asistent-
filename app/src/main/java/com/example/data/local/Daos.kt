package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getConversations(userId: String = "local_user"): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("DELETE FROM conversations WHERE userId = :userId")
    suspend fun clearAllConversations(userId: String = "local_user")
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesListForConversation(conversationId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)
}

@Dao
interface PersonalMemoryDao {
    @Query("SELECT * FROM personal_memories WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMemories(userId: String = "local_user"): Flow<List<PersonalMemoryEntity>>

    @Query("SELECT * FROM personal_memories WHERE userId = :userId")
    suspend fun getMemoriesList(userId: String = "local_user"): List<PersonalMemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: PersonalMemoryEntity)

    @Query("DELETE FROM personal_memories WHERE id = :memoryId")
    suspend fun deleteMemory(memoryId: Long)

    @Query("DELETE FROM personal_memories WHERE userId = :userId")
    suspend fun clearAllMemories(userId: String = "local_user")
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoals(userId: String = "local_user"): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: Long)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY isCompleted ASC, createdAt DESC")
    fun getTasks(userId: String = "local_user"): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_topics WHERE userId = :userId ORDER BY isCompleted ASC, revisionDate ASC")
    fun getStudyTopics(userId: String = "local_user"): Flow<List<StudyTopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: StudyTopicEntity)

    @Update
    suspend fun updateTopic(topic: StudyTopicEntity)

    @Query("DELETE FROM study_topics WHERE id = :topicId")
    suspend fun deleteTopic(topicId: Long)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getUserProfile(userId: String = "local_user"): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    suspend fun getUserProfileDirect(userId: String = "local_user"): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}
