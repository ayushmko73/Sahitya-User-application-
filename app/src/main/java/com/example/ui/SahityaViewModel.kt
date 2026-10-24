package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActiveChatEntity
import com.example.data.CharacterStoryEntity
import com.example.data.ChatMessageEntity
import com.example.data.ChatSummaryEntity
import com.example.data.SahityaDatabase
import com.example.data.SahityaRepository
import com.example.data.TransactionEntity
import com.example.data.UserChallengeEntity
import com.example.data.UserWalletEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BottomTab {
    HOME, SEARCH, CHATS, STORE, PROFILE
}

class SahityaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SahityaDatabase.getDatabase(application)
    private val repository = SahityaRepository(db.sahityaDao())

    // Bottom Navigation State
    private val _currentTab = MutableStateFlow(BottomTab.HOME)
    val currentTab: StateFlow<BottomTab> = _currentTab.asStateFlow()

    // Home Sub-nav Genre Filter Pill
    private val _selectedGenreFilter = MutableStateFlow("For You")
    val selectedGenreFilter: StateFlow<String> = _selectedGenreFilter.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Active Roleplay Chat Session (when user opens a chat)
    private val _activeChatCharacter = MutableStateFlow<CharacterStoryEntity?>(null)
    val activeChatCharacter: StateFlow<CharacterStoryEntity?> = _activeChatCharacter.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    // Data Flows
    val stories: StateFlow<List<CharacterStoryEntity>> = repository.allStories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredStory: StateFlow<CharacterStoryEntity?> = repository.featuredStory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeChats: StateFlow<List<ActiveChatEntity>> = repository.activeChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wallet: StateFlow<UserWalletEntity?> = repository.wallet
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<TransactionEntity>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val challenges: StateFlow<List<UserChallengeEntity>> = repository.challenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCachedMessages = repository.allCachedMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChatSummaries = repository.allChatSummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat messages for current active character
    private val _currentChatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val currentChatMessages: StateFlow<List<ChatMessageEntity>> = _currentChatMessages.asStateFlow()

    // 80/20 Rolling Summaries for current active character
    private val _currentChatSummaries = MutableStateFlow<List<ChatSummaryEntity>>(emptyList())
    val currentChatSummaries: StateFlow<List<ChatSummaryEntity>> = _currentChatSummaries.asStateFlow()

    // Daily Reward and Notification Prompt States
    private val _dailyLoginRewardAmount = MutableStateFlow<Int?>(null)
    val dailyLoginRewardAmount: StateFlow<Int?> = _dailyLoginRewardAmount.asStateFlow()

    private val _showNotificationPrompt = MutableStateFlow(false)
    val showNotificationPrompt: StateFlow<Boolean> = _showNotificationPrompt.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
            _isSyncing.value = true
            repository.fetchAndSyncPublishedStoriesFromSupabase()
            _isSyncing.value = false

            // Periodic background loop running every 15 seconds to fetch new stories directly from Supabase
            // Runs silently in the background without setting _isSyncing so user experience remains smooth
            while (true) {
                kotlinx.coroutines.delay(15000)
                try {
                    repository.fetchAndSyncPublishedStoriesFromSupabase()
                } catch (_: Exception) {}
            }
        }
    }

    fun refreshStoriesFromSupabase() {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.fetchAndSyncPublishedStoriesFromSupabase()
            _isSyncing.value = false
        }
    }

    fun saveSupabaseConfig(url: String, key: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isSyncing.value = true
            val success = repository.saveSupabaseConfigAndSync(url, key)
            _isSyncing.value = false
            onComplete(success)
        }
    }

    fun checkDailyAppOpenEvents() {
        viewModelScope.launch {
            val reward = repository.checkAndProcessDailyLoginReward()
            if (reward != null) {
                _dailyLoginRewardAmount.value = reward
            } else {
                // If no daily reward popup today, trigger notification prompt on app open
                _showNotificationPrompt.value = true
            }
        }
    }

    fun dismissDailyRewardPopup() {
        _dailyLoginRewardAmount.value = null
        _showNotificationPrompt.value = true
    }

    fun dismissNotificationPrompt() {
        _showNotificationPrompt.value = false
    }

    fun selectBottomTab(tab: BottomTab) {
        _currentTab.value = tab
        // If navigating away from active chat screen back to bottom tabs, clear character view if desired or keep state
    }

    fun setGenreFilter(genre: String) {
        _selectedGenreFilter.value = genre
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openChatWithStory(story: CharacterStoryEntity) {
        _activeChatCharacter.value = story
        viewModelScope.launch {
            repository.startChatWithCharacter(story)
            launch {
                repository.getMessages(story.id).collect { messages ->
                    _currentChatMessages.value = messages
                }
            }
            launch {
                repository.getSummaries(story.id).collect { summaries ->
                    _currentChatSummaries.value = summaries
                }
            }
        }
    }

    fun openChatWithActiveChatEntity(chat: ActiveChatEntity) {
        viewModelScope.launch {
            val story = stories.value.find { it.id == chat.characterId }
                ?: CharacterStoryEntity(
                    id = chat.characterId,
                    title = chat.characterName,
                    handle = chat.characterHandle,
                    genre = chat.genre,
                    tagline = "Interactive AI roleplay character.",
                    chatCount = "50K",
                    rank = 1,
                    systemPrompt = "You are ${chat.characterName}.",
                    greetingMessage = chat.lastMessage,
                    isFeatured = false,
                    imageType = chat.avatarType
                )
            openChatWithStory(story)
        }
    }

    fun closeActiveChat() {
        _activeChatCharacter.value = null
    }

    fun sendMessage(userText: String) {
        val currentStory = _activeChatCharacter.value ?: return
        if (userText.isBlank()) return

        viewModelScope.launch {
            _isSendingMessage.value = true
            try {
                repository.sendMessage(currentStory.id, userText)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSendingMessage.value = false
            }
        }
    }

    fun claimDailyBonus() {
        viewModelScope.launch {
            repository.claimDailyBonus()
        }
    }

    fun purchaseCoinPack(coins: Int, priceInr: String, title: String) {
        viewModelScope.launch {
            repository.purchaseCoinPack(coins, priceInr, title)
        }
    }

    fun deleteCurrentChat() {
        val story = _activeChatCharacter.value ?: return
        viewModelScope.launch {
            repository.deleteChat(story.id)
            _activeChatCharacter.value = null
        }
    }

    fun replayCurrentChat() {
        val story = _activeChatCharacter.value ?: return
        viewModelScope.launch {
            repository.replayChat(story)
        }
    }

    fun acceptChallenge(id: String) {
        viewModelScope.launch {
            repository.acceptChallenge(id)
        }
    }

    fun loginUser(phoneNumber: String, onLoginSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.loginUser(phoneNumber)
            onLoginSuccess()
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            repository.logoutUser()
        }
    }

    fun updateSelectedTheme(theme: String) {
        viewModelScope.launch {
            repository.updateSelectedTheme(theme)
        }
    }
}
