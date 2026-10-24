package com.example.data

import android.util.Log
import com.example.api.GeminiService
import com.example.sync.SupabaseSyncService
import com.example.ui.components.cleanChatPreviewText
import com.sahitya.userapp.data.model.UserStoryModel
import com.sahitya.userapp.data.network.SahityaUserApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class SahityaRepository(private val dao: SahityaDao) {

    private val _liveStoriesState = MutableStateFlow<List<CharacterStoryEntity>>(getDefaultStories())
    val allStories: Flow<List<CharacterStoryEntity>> = _liveStoriesState.asStateFlow()
    val featuredStory: Flow<CharacterStoryEntity?> = _liveStoriesState.map { stories ->
        stories.find { it.isFeatured } ?: stories.firstOrNull()
    }
    val activeChats: Flow<List<ActiveChatEntity>> = dao.getActiveChats()
    val wallet: Flow<UserWalletEntity?> = dao.getWallet()
    val transactions: Flow<List<TransactionEntity>> = dao.getTransactions()
    val challenges: Flow<List<UserChallengeEntity>> = dao.getChallenges()
    val userPreferences: Flow<UserPreferencesEntity?> = dao.getUserPreferences()
    val allCachedMessages: Flow<List<ChatMessageEntity>> = dao.getAllCachedChatMessages()
    val allChatSummaries: Flow<List<ChatSummaryEntity>> = dao.getAllChatSummaries()

    companion object {
        fun getDefaultStories(): List<CharacterStoryEntity> {
            return listOf(
                CharacterStoryEntity(
                    id = "arthria_shadow_protocol",
                    title = "Arthria: Shadow Protocol",
                    handle = "@arthria_shadow",
                    genre = "Sci-Fi",
                    tagline = "Deep cover operative under classified Shadow Protocol. Unravel neon secrets or obey command.",
                    chatCount = "128.5K",
                    rank = 1,
                    systemPrompt = "You are Arthria, a high-ranking cyberpunk shadow operative enforcing the secret Shadow Protocol in the year 2088. You speak with calm precision, tactical brilliance, and veiled danger.",
                    greetingMessage = "*Adjusts tactical visor as rain drips from the rooftop ledge.* 'Arthria here. Shadow Protocol is active. State your credentials before the grid flags you as an unauthorized intruder.'",
                    isFeatured = true,
                    imageType = "hero"
                ),
                CharacterStoryEntity(
                    id = "samurai_zero",
                    title = "Ronin Zero: Crimson Blade",
                    handle = "@ronin_zero",
                    genre = "Fantasy",
                    tagline = "Cursed samurai fighting demonic spirits under red eclipse.",
                    chatCount = "98.2K",
                    rank = 2,
                    systemPrompt = "You are Ronin Zero, a lone samurai who wields a cursed blood blade. Speak honorably and solemnly.",
                    greetingMessage = "*Wipes blood from cursed katana under red eclipse.* 'You stand before Zero. Speak your business before the blade decides.'",
                    isFeatured = false,
                    imageType = "fantasy"
                ),
                CharacterStoryEntity(
                    id = "vampire_karan",
                    title = "Prince Karan: Blood Oath",
                    handle = "@prince_karan",
                    genre = "Romance",
                    tagline = "Immortal royal vampire guarding ancient secrets in Mumbai.",
                    chatCount = "84.1K",
                    rank = 3,
                    systemPrompt = "You are Prince Karan, an alluring immortal vampire prince.",
                    greetingMessage = "*Swirls red wine in crystal glass.* 'The night is young, and Mumbai never sleeps... nor do I. Welcome to my haven.'",
                    isFeatured = false,
                    imageType = "drama"
                ),
                CharacterStoryEntity(
                    id = "cyber_kiara",
                    title = "Kiara: Cyberpunk Merc",
                    handle = "@netrunner_kiara",
                    genre = "Anime",
                    tagline = "Top netrunner in Neon Heights offering covert cyber ops.",
                    chatCount = "72.8K",
                    rank = 4,
                    systemPrompt = "You are Kiara, a street-smart cyberpunk mercenary netrunner.",
                    greetingMessage = "*Taps neon visor HUD.* 'Data stream secured. I'm Kiara. Got a high-stakes job for me, or are you just admiring the tech?'",
                    isFeatured = false,
                    imageType = "hero"
                ),
                CharacterStoryEntity(
                    id = "mira_horror",
                    title = "Mira: Whispering Shadow",
                    handle = "@mira_horror",
                    genre = "Horror",
                    tagline = "Trapped spirit in an abandoned Victorian manor.",
                    chatCount = "65.3K",
                    rank = 5,
                    systemPrompt = "You are Mira, a haunting spirit in an eerie mansion.",
                    greetingMessage = "*The candle flickers out as a cold breeze sweeps the corridor.* 'Did you hear that sound too, or am I alone again?'",
                    isFeatured = false,
                    imageType = "horror"
                )
            )
        }
    }

    fun getMessages(characterId: String): Flow<List<ChatMessageEntity>> =
        dao.getMessagesForCharacter(characterId)

    fun getSummaries(characterId: String): Flow<List<ChatSummaryEntity>> =
        dao.getSummariesForCharacter(characterId)

    suspend fun updateSelectedTheme(theme: String) {
        dao.updateSelectedTheme(theme)
    }

    suspend fun saveUserPreferences(preferences: UserPreferencesEntity) {
        dao.saveUserPreferences(preferences)
    }

    suspend fun initializeDefaultDataIfNeeded() {
        dao.initWallet()
        dao.initUserPreferences()

        val existingChats = dao.getActiveChats().firstOrNull()
        if (existingChats.isNullOrEmpty()) {
            val defaultActiveChats = listOf(
                ActiveChatEntity(
                    characterId = "samurai_zero",
                    characterName = "Ronin Zero: Crimson Blade",
                    characterHandle = "@ronin_zero",
                    genre = "Fantasy",
                    lastMessage = cleanChatPreviewText("*Wipes blood from cursed katana under red eclipse.* 'You stand before Zero...'"),
                    lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                    unreadCount = 2,
                    avatarType = "hero"
                ),
                ActiveChatEntity(
                    characterId = "vampire_karan",
                    characterName = "Prince Karan: Blood Oath",
                    characterHandle = "@prince_karan",
                    genre = "Romance",
                    lastMessage = cleanChatPreviewText("*Swirls red wine in crystal glass.* 'The night is young...'"),
                    lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                    unreadCount = 1,
                    avatarType = "drama"
                ),
                ActiveChatEntity(
                    characterId = "cyber_kiara",
                    characterName = "Kiara: Cyberpunk Merc",
                    characterHandle = "@netrunner_kiara",
                    genre = "Anime",
                    lastMessage = cleanChatPreviewText("*Taps neon visor HUD.* 'Data stream secured...'"),
                    lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 180,
                    unreadCount = 0,
                    avatarType = "hero"
                )
            )
            defaultActiveChats.forEach { dao.insertOrUpdateActiveChat(it) }

            // Insert initial greeting messages for active chats
            dao.insertMessage(
                ChatMessageEntity(
                    characterId = "samurai_zero",
                    sender = "character",
                    text = "*Wipes blood from cursed katana under red eclipse.* 'You stand before Zero. Speak your business before the blade decides.'"
                )
            )
            dao.insertMessage(
                ChatMessageEntity(
                    characterId = "vampire_karan",
                    sender = "character",
                    text = "*Swirls red wine in crystal glass.* 'The night is young, and Mumbai never sleeps... nor do I. Welcome to my haven.'"
                )
            )
            dao.insertMessage(
                ChatMessageEntity(
                    characterId = "cyber_kiara",
                    sender = "character",
                    text = "*Taps neon visor HUD.* 'Data stream secured. I'm Kiara. Got a high-stakes job for me, or are you just admiring the tech?'"
                )
            )
        }

        val existingTx = dao.getTransactions().firstOrNull()
        if (existingTx.isNullOrEmpty()) {
            val defaultTx = listOf(
                TransactionEntity(
                    title = "Daily Free S-Coin Claim",
                    coinsAmount = 50,
                    priceInr = "FREE",
                    status = "COMPLETED",
                    dateFormatted = "Today, 09:15 AM"
                ),
                TransactionEntity(
                    title = "Starter S-Coin Bundle (300 S)",
                    coinsAmount = 300,
                    priceInr = "₹79",
                    status = "COMPLETED",
                    dateFormatted = "Yesterday, 08:30 PM"
                )
            )
            defaultTx.forEach { dao.insertTransaction(it) }
        }

        val existingChallenges = dao.getChallenges().firstOrNull()
        if (existingChallenges.isNullOrEmpty()) {
            val defaultChallenges = listOf(
                UserChallengeEntity(
                    id = "c1",
                    title = "Crimson Eclipse Trial",
                    description = "Roleplay 3 turns with Ronin Zero in the Fantasy realm",
                    rewardCoins = 100,
                    progressText = "1/3 Turns",
                    isAccepted = false,
                    isCompleted = false
                ),
                UserChallengeEntity(
                    id = "c2",
                    title = "Midnight Collector",
                    description = "Claim your daily top-up and chat with 2 new AI characters",
                    rewardCoins = 150,
                    progressText = "0/2 Characters",
                    isAccepted = true,
                    isCompleted = false
                ),
                UserChallengeEntity(
                    id = "c3",
                    title = "Genre Explorer",
                    description = "Explore story teasers in 3 different genres",
                    rewardCoins = 80,
                    progressText = "3/3 Completed",
                    isAccepted = true,
                    isCompleted = true
                )
            )
            dao.insertChallenges(defaultChallenges)
        }

        // Fetch live published stories from Supabase REST API on app start
        fetchAndSyncPublishedStoriesFromSupabase()
    }

    suspend fun loginUser(phoneNumber: String) {
        val currentPrefs = dao.getUserPreferences().firstOrNull() ?: UserPreferencesEntity()
        dao.saveUserPreferences(
            currentPrefs.copy(
                isLoggedIn = true,
                userPhoneNumber = phoneNumber
            )
        )
    }

    suspend fun logoutUser() {
        val currentPrefs = dao.getUserPreferences().firstOrNull() ?: UserPreferencesEntity()
        dao.saveUserPreferences(
            currentPrefs.copy(
                isLoggedIn = false
            )
        )
    }

    suspend fun checkAndProcessDailyLoginReward(): Int? {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val prefs = dao.getUserPreferences().firstOrNull() ?: UserPreferencesEntity()
        if (prefs.lastLoginDate != todayStr) {
            val rewardCoins = 50
            dao.updateCoins(rewardCoins)
            dao.insertTransaction(
                TransactionEntity(
                    title = "Daily Login Reward (+50 S)",
                    coinsAmount = rewardCoins,
                    priceInr = "FREE",
                    status = "COMPLETED",
                    dateFormatted = "Today"
                )
            )
            dao.saveUserPreferences(prefs.copy(lastLoginDate = todayStr))
            return rewardCoins
        }
        return null
    }

    suspend fun claimDailyBonus() {
        val currentTime = System.currentTimeMillis()
        dao.claimDailyCoins(50, currentTime)
        dao.insertTransaction(
            TransactionEntity(
                title = "Daily Free Claim (+50 S)",
                coinsAmount = 50,
                priceInr = "FREE",
                status = "COMPLETED",
                dateFormatted = "Just now"
            )
        )
    }

    suspend fun purchaseCoinPack(coins: Int, priceInr: String, title: String) {
        dao.updateCoins(coins)
        dao.insertTransaction(
            TransactionEntity(
                title = title,
                coinsAmount = coins,
                priceInr = priceInr,
                status = "COMPLETED",
                dateFormatted = "Just now"
            )
        )
    }

    suspend fun acceptChallenge(id: String) {
        dao.acceptChallenge(id)
    }

    suspend fun deleteChat(characterId: String) {
        dao.deleteMessagesForCharacter(characterId)
        dao.deleteSummariesForCharacter(characterId)
        dao.deleteActiveChat(characterId)
    }

    suspend fun replayChat(story: CharacterStoryEntity) {
        dao.deleteMessagesForCharacter(story.id)
        dao.deleteSummariesForCharacter(story.id)
        dao.insertMessage(
            ChatMessageEntity(
                characterId = story.id,
                sender = "character",
                text = story.greetingMessage
            )
        )
    }

    private suspend fun allocateLeastUsedApiKeyIndex(): Int {
        val activeChats = dao.getActiveChats().firstOrNull() ?: emptyList()
        val indexCounts = activeChats.groupingBy { it.apiKeyIndex }.eachCount()
        var minIndex = 1
        var minCount = Int.MAX_VALUE
        for (i in 1..10) {
            val count = indexCounts[i] ?: 0
            if (count < minCount) {
                minCount = count
                minIndex = i
            }
        }
        return minIndex
    }

    suspend fun startChatWithCharacter(story: CharacterStoryEntity): ActiveChatEntity {
        val existingChat = dao.getActiveChats().firstOrNull()?.find { it.characterId == story.id }
        if (existingChat != null) {
            dao.clearUnreadCount(story.id)
            return existingChat
        }

        val assignedKeyIndex = allocateLeastUsedApiKeyIndex()

        val newChat = ActiveChatEntity(
            characterId = story.id,
            characterName = story.title,
            characterHandle = story.handle,
            genre = story.genre,
            lastMessage = cleanChatPreviewText(story.greetingMessage),
            lastTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            avatarType = story.imageType,
            apiKeyIndex = assignedKeyIndex
        )
        dao.insertOrUpdateActiveChat(newChat)
        SupabaseSyncService.syncActiveSessionToSupabase(newChat)

        dao.insertMessage(
            ChatMessageEntity(
                characterId = story.id,
                sender = "character",
                text = story.greetingMessage
            )
        )
        return newChat
    }

    suspend fun sendMessage(characterId: String, userText: String): String {
        // Deduct 2 S-Coins per user message turn
        dao.updateCoins(-2)

        // Save user message
        dao.insertMessage(
            ChatMessageEntity(
                characterId = characterId,
                sender = "user",
                text = userText
            )
        )

        // Fetch story system prompt, historical summary blocks, and recent raw messages
        val story = dao.getStoryById(characterId)
        val characterTitle = story?.title ?: "AI Roleplay Character"
        val systemPrompt = story?.systemPrompt ?: "You are a dramatic AI roleplay character."

        // Context Builder: Local Historical Summaries + Semantic pgvector Search Matches + Recent Unsummarized Messages
        val localSummaries = dao.getSummariesListForCharacter(characterId).map { it.summaryBlock }
        
        // Generate text embedding for user's input prompt to run pgvector semantic search
        val userQueryEmbedding = GeminiService.generateTextEmbedding(userText)
        val pgVectorMatches = SupabaseSyncService.searchRelevantSummariesViaPgVector(
            characterId = characterId,
            queryEmbedding = userQueryEmbedding,
            matchCount = 3,
            threshold = 0.2f
        )

        // Combine local chronological summaries + semantic vector memory blocks (deduplicated)
        val combinedSummaries = (localSummaries + pgVectorMatches).distinct()

        val recentUnsummarizedMessages = dao.getMessagesForCharacter(characterId).firstOrNull() ?: emptyList()
        val historyTurns = recentUnsummarizedMessages.map { it.sender to it.text }

        // Call Gemini API with combined historical summary context + recent unsummarized messages
        val existingChat = dao.getActiveChats().firstOrNull()?.find { it.characterId == characterId }
        val assignedKeyIndex = existingChat?.apiKeyIndex ?: 1
        val characterReply = GeminiService.generateRoleplayResponseWithContext(
            apiKeyIndex = assignedKeyIndex,
            systemPrompt = systemPrompt,
            userPrompt = userText,
            historicalSummaries = combinedSummaries,
            recentMessages = historyTurns
        )

        // Save character message
        dao.insertMessage(
            ChatMessageEntity(
                characterId = characterId,
                sender = "character",
                text = characterReply
            )
        )


        val chatEntity = ActiveChatEntity(
            characterId = characterId,
            characterName = characterTitle,
            characterHandle = story?.handle ?: "@sahitya_ai",
            genre = story?.genre ?: "Roleplay",
            lastMessage = cleanChatPreviewText(characterReply),
            lastTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            avatarType = story?.imageType ?: "hero",
            apiKeyIndex = assignedKeyIndex
        )
        dao.insertOrUpdateActiveChat(chatEntity)

        // Sync active chat session to Supabase
        SupabaseSyncService.syncActiveSessionToSupabase(chatEntity)

        // Check 10-message chunk trigger & run 80/20 rolling summarization
        checkAndRun8020SummarizationTrigger(characterId, characterTitle, assignedKeyIndex)

        return characterReply
    }

    /**
     * 10-Message Chunk Trigger:
     * Automatically checks if unsummarized raw message count reaches 10.
     * When hit, generates an 80/20 dense narrative summary block, saves to Room,
     * prunes the 10 raw messages to avoid table bloat, and mirrors to Supabase Cloud.
     */
    private suspend fun checkAndRun8020SummarizationTrigger(characterId: String, characterTitle: String, apiKeyIndex: Int) {
        val rawMessageCount = dao.getMessageCountForCharacter(characterId)
        if (rawMessageCount >= 10) {
            val oldest10Messages = dao.getOldestMessagesForCharacter(characterId, 10)
            if (oldest10Messages.size == 10) {
                val rawPairs = oldest10Messages.map { it.sender to it.text }
                
                // 1. Generate 80/20 rolling summary block using Gemini model & 768d vector embedding
                val summaryBlock = GeminiService.generate8020RollingSummary(
                    apiKeyIndex = apiKeyIndex,
                    characterTitle = characterTitle,
                    rawMessages = rawPairs
                )

                val summaryEmbedding = GeminiService.generateTextEmbedding(summaryBlock, apiKeyIndex)

                val currentMaxChunk = dao.getMaxChunkIndex(characterId) ?: 0
                val nextChunkIndex = currentMaxChunk + 1

                val summaryEntity = ChatSummaryEntity(
                    characterId = characterId,
                    summaryBlock = summaryBlock,
                    startMessageTimestamp = oldest10Messages.first().timestamp,
                    endMessageTimestamp = oldest10Messages.last().timestamp,
                    messageCountSummarized = 10,
                    chunkIndex = nextChunkIndex,
                    createdAt = System.currentTimeMillis()
                )

                // 2. Save 80/20 summary checkpoint to Room Database
                dao.insertSummary(summaryEntity)

                // 3. Prune/Archive those 10 raw messages from active table
                dao.deleteOldestMessagesForCharacter(characterId, 10)

                // 4. Sync summary checkpoint and pgvector embedding to Supabase Cloud
                SupabaseSyncService.syncSummaryCheckpointToSupabase(summaryEntity, summaryEmbedding)
            }
        }
    }

    /**
     * Diagnostic tool to fetch and log the raw JSON response from Supabase.
     */
    private suspend fun logDiagnosticSupabaseResponse(
        apiService: com.sahitya.userapp.data.network.SahityaUserApiService,
        supabaseKey: String,
        bearerHeader: String?
    ) {
        try {
            val responseBody = apiService.fetchRawPublishedStoriesDiagnostic(
                apiKey = supabaseKey,
                bearerToken = bearerHeader,
                select = "*"
            )
            val rawJson = responseBody.string()
            Log.d("SahityaRepository", "=== DIAGNOSTIC RAW SUPABASE RESPONSE ===")
            // Log in chunks in case it's too large for logcat
            val maxLogSize = 1000
            for (i in 0..rawJson.length / maxLogSize) {
                val start = i * maxLogSize
                var end = (i + 1) * maxLogSize
                end = if (end > rawJson.length) rawJson.length else end
                Log.d("SahityaRepository", rawJson.substring(start, end))
            }
            Log.d("SahityaRepository", "=========================================")
        } catch (e: Exception) {
            // Log.e("SahityaRepository", "Diagnostic raw fetch failed: ${e.message}")
        }
    }

    /**
     * Fetches live published stories directly from Supabase REST API across published_stories, stories, user_stories, and creator_stories.
     * Updates live StateFlow in memory and synchronizes Room database cache.
     */
    suspend fun fetchAndSyncPublishedStoriesFromSupabase(): Boolean = withContext(Dispatchers.IO) {
        val supabaseUrl = getSupabaseUrl()
        val supabaseKey = getSupabaseKey()

        var remoteStories: List<UserStoryModel> = emptyList()

        if (supabaseUrl.isNotBlank()) {
            try {
                val apiService = SahityaUserApiClient.create(supabaseUrl)
                val bearerHeader = if (supabaseKey.isNotBlank()) "Bearer $supabaseKey" else null

                // Perform diagnostic logging (disabled to prevent logcat spam on paused instances)
                // logDiagnosticSupabaseResponse(apiService, supabaseKey, bearerHeader)

                // 1. Primary endpoint: published_stories
                try {
                    remoteStories = apiService.fetchPublishedStories(
                        apiKey = supabaseKey,
                        bearerToken = bearerHeader,
                        select = "*"
                    )
                } catch (e: Exception) {
                    Log.w("SahityaRepository", "Primary fetchPublishedStories(select=*) failed: ${e.message}")
                }

                // 2. Fallback endpoint: stories
                if (remoteStories.isEmpty()) {
                    try {
                        remoteStories = apiService.fetchStoriesFallback(
                            apiKey = supabaseKey,
                            bearerToken = bearerHeader,
                            select = "*"
                        )
                    } catch (e: Exception) {
                        Log.w("SahityaRepository", "Fallback fetchStoriesFallback(select=*) failed: ${e.message}")
                    }
                }

                // 3. Fallback endpoint: user_stories
                if (remoteStories.isEmpty()) {
                    try {
                        remoteStories = apiService.fetchUserStories(
                            apiKey = supabaseKey,
                            bearerToken = bearerHeader,
                            select = "*"
                        )
                    } catch (e: Exception) {
                        Log.w("SahityaRepository", "Fallback fetchUserStories(select=*) failed: ${e.message}")
                    }
                }

                // 4. Fallback endpoint: creator_stories
                if (remoteStories.isEmpty()) {
                    try {
                        remoteStories = apiService.fetchCreatorStories(
                            apiKey = supabaseKey,
                            bearerToken = bearerHeader,
                            select = "*"
                        )
                    } catch (e: Exception) {
                        Log.w("SahityaRepository", "Fallback fetchCreatorStories(select=*) failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SahityaRepository", "Error syncing published stories from Supabase REST API", e)
            }
        }

        val mappedEntities = mutableListOf<CharacterStoryEntity>()
        if (remoteStories.isNotEmpty()) {
            // Reverse the list so newly added stories appear first in New Release
            val remoteMapped = remoteStories.reversed().mapIndexed { index, model ->
                model.toCharacterStoryEntity(index + 1)
            }
            mappedEntities.addAll(remoteMapped)
        } else {
            // Load local DB or default stories
            val cached = dao.getAllStoriesList()
            if (cached.isNotEmpty()) {
                mappedEntities.addAll(cached)
            } else {
                mappedEntities.addAll(getDefaultStories())
            }
        }

        // Always guarantee "Arthria: Shadow Protocol" is present at index 0 (top/featured)
        val arthriaIndex = mappedEntities.indexOfFirst {
            it.title.contains("Arthria", ignoreCase = true) || it.id == "arthria_shadow_protocol"
        }
        if (arthriaIndex < 0) {
            getDefaultStories().find { it.id == "arthria_shadow_protocol" }?.let {
                mappedEntities.add(0, it)
            }
        } else if (arthriaIndex > 0) {
            val arthriaStory = mappedEntities.removeAt(arthriaIndex)
            mappedEntities.add(0, arthriaStory)
        }

        // Re-index ranks and set featured flag for top item
        val reindexed = mappedEntities.mapIndexed { idx, entity ->
            entity.copy(rank = idx + 1, isFeatured = (idx == 0))
        }

        // Update live StateFlow in memory and synchronize Room database cache
        if (_liveStoriesState.value != reindexed) {
            _liveStoriesState.value = reindexed
            try {
                dao.deleteAllStories()
                dao.insertStories(reindexed)
            } catch (e: Exception) {
                Log.e("SahityaRepository", "Error writing stories to local cache", e)
            }
        }

        Log.d("SahityaRepository", "Live stories updated: ${mappedEntities.size} items active.")
        return@withContext remoteStories.isNotEmpty()
    }

    private fun UserStoryModel.toCharacterStoryEntity(index: Int): CharacterStoryEntity {
        val storyTitle = getResolvedTitle()
        val rawId = getResolvedId(index)
        val formattedHandle = "@" + storyTitle.lowercase().replace(Regex("[^a-z0-9]"), "_").take(20)
        val storyGenre = getResolvedCategory()
        val taglineText = getResolvedTagline()

        val chatsCountValue = totalChats ?: 0L
        val chatsFormatted = when {
            chatsCountValue >= 1000000 -> String.format("%.1fM", chatsCountValue / 1000000.0)
            chatsCountValue >= 1000 -> String.format("%.1fK", chatsCountValue / 1000.0)
            else -> "$chatsCountValue"
        }

        val imageCategory = when {
            isAnime == true -> "anime"
            storyGenre.contains("Drama", ignoreCase = true) -> "drama"
            storyGenre.contains("Fantasy", ignoreCase = true) -> "fantasy"
            storyGenre.contains("Horror", ignoreCase = true) -> "horror"
            storyGenre.contains("Romance", ignoreCase = true) -> "drama"
            else -> "hero"
        }

        val systemPromptText = getResolvedSystemPrompt()
        val greetingMsg = getResolvedGreetingMessage()

        return CharacterStoryEntity(
            id = rawId,
            title = storyTitle,
            handle = formattedHandle,
            genre = storyGenre,
            tagline = taglineText,
            chatCount = chatsFormatted,
            rank = index + 1,
            systemPrompt = systemPromptText,
            greetingMessage = greetingMsg,
            isFeatured = index == 0,
            imageType = imageCategory,
            mainThumbnail = getResolvedThumbnail(),
            bannerImage = getResolvedBanner()
        )
    }

    suspend fun saveSupabaseConfigAndSync(url: String, key: String): Boolean {
        dao.updateSupabaseConfig(url, key)
        return fetchAndSyncPublishedStoriesFromSupabase()
    }

    private suspend fun getSupabaseUrl(): String {
        val saved = dao.getUserPreferences().firstOrNull()?.customSupabaseUrl ?: ""
        if (saved.isNotBlank() && !saved.startsWith("YOUR_")) return saved

        if (com.example.BuildConfig.SUPABASE_URL.isNotBlank() && !com.example.BuildConfig.SUPABASE_URL.startsWith("YOUR_")) {
            return com.example.BuildConfig.SUPABASE_URL
        }

        return ""
    }

    private suspend fun getSupabaseKey(): String {
        val saved = dao.getUserPreferences().firstOrNull()?.customSupabaseKey ?: ""
        if (saved.isNotBlank() && !saved.startsWith("YOUR_")) return saved

        if (com.example.BuildConfig.SUPABASE_ANON_KEY.isNotBlank() && !com.example.BuildConfig.SUPABASE_ANON_KEY.startsWith("YOUR_")) {
            return com.example.BuildConfig.SUPABASE_ANON_KEY
        }

        return ""
    }
}
