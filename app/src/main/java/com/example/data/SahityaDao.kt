package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SahityaDao {

    // --- Agent Learning (M1 Blueprint Blocks) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgentLearningStory(story: AgentLearningStoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgentLearningM1Blocks(blocks: List<AgentLearningM1BlockEntity>)

    @Query("SELECT * FROM agent_learning_m1_blocks WHERE storyId = :storyId ORDER BY blockOrder ASC LIMIT :limit OFFSET :offset")
    suspend fun getUpcomingM1Blocks(storyId: String, limit: Int, offset: Int): List<AgentLearningM1BlockEntity>
    // Stories
    @Query("SELECT * FROM character_stories ORDER BY rank ASC")
    fun getAllStories(): Flow<List<CharacterStoryEntity>>

    @Query("SELECT * FROM character_stories ORDER BY rank ASC")
    suspend fun getAllStoriesList(): List<CharacterStoryEntity>

    @Query("SELECT * FROM character_stories WHERE isFeatured = 1 LIMIT 1")
    fun getFeaturedStory(): Flow<CharacterStoryEntity?>

    @Query("SELECT * FROM character_stories WHERE genre = :genre ORDER BY rank ASC")
    fun getStoriesByGenre(genre: String): Flow<List<CharacterStoryEntity>>

    @Query("SELECT * FROM character_stories WHERE id = :id")
    suspend fun getStoryById(id: String): CharacterStoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<CharacterStoryEntity>)

    @Query("DELETE FROM character_stories")
    suspend fun deleteAllStories()

    // Active Chats
    @Query("SELECT * FROM active_chats ORDER BY lastTimestamp DESC")
    fun getActiveChats(): Flow<List<ActiveChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateActiveChat(chat: ActiveChatEntity)

    @Query("UPDATE active_chats SET unreadCount = 0 WHERE characterId = :characterId")
    suspend fun clearUnreadCount(characterId: String)

    @Query("DELETE FROM chat_messages WHERE characterId = :characterId")
    suspend fun deleteMessagesForCharacter(characterId: String)

    @Query("DELETE FROM active_chats WHERE characterId = :characterId")
    suspend fun deleteActiveChat(characterId: String)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE characterId = :characterId ORDER BY timestamp ASC")
    fun getMessagesForCharacter(characterId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllCachedChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    // Chat Summaries (80/20 Rolling Summarization)
    @Query("SELECT * FROM chat_summaries WHERE characterId = :characterId ORDER BY chunkIndex ASC")
    fun getSummariesForCharacter(characterId: String): Flow<List<ChatSummaryEntity>>

    @Query("SELECT * FROM chat_summaries WHERE characterId = :characterId ORDER BY chunkIndex ASC")
    suspend fun getSummariesListForCharacter(characterId: String): List<ChatSummaryEntity>

    @Query("SELECT * FROM chat_summaries ORDER BY createdAt DESC")
    fun getAllChatSummaries(): Flow<List<ChatSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: ChatSummaryEntity)

    @Query("SELECT MAX(chunkIndex) FROM chat_summaries WHERE characterId = :characterId")
    suspend fun getMaxChunkIndex(characterId: String): Int?

    @Query("DELETE FROM chat_summaries WHERE characterId = :characterId")
    suspend fun deleteSummariesForCharacter(characterId: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE characterId = :characterId")
    suspend fun getMessageCountForCharacter(characterId: String): Int

    @Query("SELECT * FROM chat_messages WHERE characterId = :characterId ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getOldestMessagesForCharacter(characterId: String, limit: Int): List<ChatMessageEntity>

    @Query("DELETE FROM chat_messages WHERE id IN (SELECT id FROM chat_messages WHERE characterId = :characterId ORDER BY timestamp ASC LIMIT :limit)")
    suspend fun deleteOldestMessagesForCharacter(characterId: String, limit: Int)

    // User Preferences
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>

    @Query("INSERT OR IGNORE INTO user_preferences (id, selectedTheme, isNotificationsEnabled) VALUES (1, 'DARK', 1)")
    suspend fun initUserPreferences()

    @Query("UPDATE user_preferences SET selectedTheme = :theme WHERE id = 1")
    suspend fun updateSelectedTheme(theme: String)

    @Query("UPDATE user_preferences SET customSupabaseUrl = :url, customSupabaseKey = :key WHERE id = 1")
    suspend fun updateSupabaseConfig(url: String, key: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPreferences(preferences: UserPreferencesEntity)

    // Wallet
    @Query("SELECT * FROM user_wallet WHERE id = 1")
    fun getWallet(): Flow<UserWalletEntity?>

    @Query("INSERT OR IGNORE INTO user_wallet (id, sCoins) VALUES (1, 350)")
    suspend fun initWallet()

    @Query("UPDATE user_wallet SET sCoins = sCoins + :delta WHERE id = 1")
    suspend fun updateCoins(delta: Int)

    @Query("UPDATE user_wallet SET sCoins = sCoins + :delta, lastClaimDate = :timestamp WHERE id = 1")
    suspend fun claimDailyCoins(delta: Int, timestamp: Long)

    // Transactions
    @Query("SELECT * FROM transaction_ledger ORDER BY id DESC")
    fun getTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // Challenges
    @Query("SELECT * FROM user_challenges")
    fun getChallenges(): Flow<List<UserChallengeEntity>>

    @Query("UPDATE user_challenges SET isAccepted = 1 WHERE id = :id")
    suspend fun acceptChallenge(id: String)

    @Query("UPDATE user_challenges SET isCompleted = 1 WHERE id = :id")
    suspend fun completeChallenge(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<UserChallengeEntity>)
}
