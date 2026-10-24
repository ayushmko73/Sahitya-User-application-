package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character_stories")
data class CharacterStoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val handle: String,
    val genre: String,
    val tagline: String,
    val chatCount: String,
    val rank: Int,
    val systemPrompt: String,
    val greetingMessage: String,
    val isFeatured: Boolean,
    val imageType: String, // "hero", "fantasy", "drama", "horror", "default"
    val mainThumbnail: String = "",
    val bannerImage: String = ""
)

@Entity(tableName = "active_chats")
data class ActiveChatEntity(
    @PrimaryKey val characterId: String,
    val characterName: String,
    val characterHandle: String,
    val genre: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int,
    val avatarType: String,
    val apiKeyIndex: Int = 1
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterId: String,
    val sender: String, // "user" or "character"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_summaries")
data class ChatSummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterId: String,
    val summaryBlock: String, // Dense 80/20 summary block
    val startMessageTimestamp: Long = System.currentTimeMillis(),
    val endMessageTimestamp: Long = System.currentTimeMillis(),
    val messageCountSummarized: Int = 10,
    val chunkIndex: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_wallet")
data class UserWalletEntity(
    @PrimaryKey val id: Int = 1,
    val sCoins: Int = 350,
    val lastClaimDate: Long = 0L
)

@Entity(tableName = "transaction_ledger")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val coinsAmount: Int,
    val priceInr: String,
    val status: String = "COMPLETED",
    val dateFormatted: String
)

@Entity(tableName = "user_challenges")
data class UserChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val progressText: String,
    val isAccepted: Boolean = false,
    val isCompleted: Boolean = false
)

@Entity(tableName = "agent_learning_stories")
data class AgentLearningStoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val genre: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "agent_learning_m1_blocks")
data class AgentLearningM1BlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storyId: String,
    val season: Int,
    val timeline: String,
    val characters: String,
    val writing: String,
    val blockOrder: Int // To order the blocks within a story
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val selectedTheme: String = "DARK", // "DARK" or "LIGHT"
    val isNotificationsEnabled: Boolean = true,
    val lastLoginDate: String = "",
    val isLoggedIn: Boolean = false,
    val userPhoneNumber: String = "",
    val customSupabaseUrl: String = "",
    val customSupabaseKey: String = ""
)
