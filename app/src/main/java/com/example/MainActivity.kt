package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BottomTab
import com.example.ui.SahityaViewModel
import com.example.ui.components.DailyLoginRewardDialog
import com.example.ui.components.NotificationPromptDialog
import com.example.ui.components.SahityaBottomNav
import com.example.ui.components.SahityaTopBar
import com.example.ui.screens.ChatsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LaunchIntroScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoleplayChatScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.theme.SahityaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SahityaTheme {
                SahityaApp()
            }
        }
    }
}

@Composable
fun SahityaApp(
    viewModel: SahityaViewModel = viewModel()
) {
    val context = LocalContext.current
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val isLoggedIn = userPreferences?.isLoggedIn ?: false
    val userPhoneNumber = userPreferences?.userPhoneNumber ?: ""

    var isIntroActive by remember { mutableStateOf(false) }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedGenreFilter by viewModel.selectedGenreFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeChatCharacter by viewModel.activeChatCharacter.collectAsStateWithLifecycle()
    val currentChatMessages by viewModel.currentChatMessages.collectAsStateWithLifecycle()
    val currentChatSummaries by viewModel.currentChatSummaries.collectAsStateWithLifecycle()
    val isSendingMessage by viewModel.isSendingMessage.collectAsStateWithLifecycle()

    val dailyLoginRewardAmount by viewModel.dailyLoginRewardAmount.collectAsStateWithLifecycle()
    val showNotificationPrompt by viewModel.showNotificationPrompt.collectAsStateWithLifecycle()

    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val featuredStory by viewModel.featuredStory.collectAsStateWithLifecycle()
    val activeChats by viewModel.activeChats.collectAsStateWithLifecycle()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val challenges by viewModel.challenges.collectAsStateWithLifecycle()

    val sCoinBalance = wallet?.sCoins ?: 350
    val unreadTotal = activeChats.sumOf { it.unreadCount }

    LaunchedEffect(isLoggedIn, isIntroActive) {
        if (isLoggedIn && !isIntroActive) {
            viewModel.checkDailyAppOpenEvents()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isLoggedIn) {
            // STEP 1: MUST LOGIN FIRST IF NOT LOGGED IN
            LoginScreen(
                onLoginSuccess = { phoneNumber ->
                    viewModel.loginUser(phoneNumber) {
                        isIntroActive = true
                    }
                }
            )
        } else if (isIntroActive) {
            // STEP 2: SHOW INTRO ANIMATION AFTER LOGIN
            LaunchIntroScreen(
                stories = stories,
                onIntroFinished = { isIntroActive = false }
            )
        } else {
            // STEP 3: MAIN APPLICATION SCREEN CONTENT
            val character = activeChatCharacter
            if (character != null) {
                RoleplayChatScreen(
                    character = character,
                    messages = currentChatMessages,
                    summaries = currentChatSummaries,
                    sCoinBalance = sCoinBalance,
                    isSending = isSendingMessage,
                    onSendMessage = { text -> viewModel.sendMessage(text) },
                    onBackClick = { viewModel.closeActiveChat() },
                    onReplayClick = { viewModel.replayCurrentChat() },
                    onDeleteChatClick = { viewModel.deleteCurrentChat() },
                    onPurchaseCoinPack = { _, _, _ ->
                        android.widget.Toast.makeText(context, "This feature is currently Unavailable.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        SahityaTopBar(
                            sCoinBalance = sCoinBalance,
                            onCoinClick = { viewModel.selectBottomTab(BottomTab.STORE) },
                            onProfileClick = { viewModel.selectBottomTab(BottomTab.PROFILE) }
                        )
                    },
                    bottomBar = {
                        SahityaBottomNav(
                            currentTab = currentTab,
                            unreadMessagesCount = unreadTotal,
                            onTabSelected = { tab -> viewModel.selectBottomTab(tab) }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            BottomTab.HOME -> {
                                HomeScreen(
                                    selectedGenreFilter = selectedGenreFilter,
                                    onFilterSelect = { genre -> viewModel.setGenreFilter(genre) },
                                    featuredStory = featuredStory,
                                    stories = stories,
                                    activeChats = activeChats,
                                    challenges = challenges,
                                    onStoryClick = { story -> viewModel.openChatWithStory(story) },
                                    onActiveChatClick = { chat -> viewModel.openChatWithActiveChatEntity(chat) },
                                    onAcceptChallenge = { id -> viewModel.acceptChallenge(id) },
                                    onNavigateToStore = { viewModel.selectBottomTab(BottomTab.STORE) }
                                )
                            }

                            BottomTab.SEARCH -> {
                                SearchScreen(
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                                    stories = stories,
                                    onStoryClick = { story -> viewModel.openChatWithStory(story) }
                                )
                            }

                            BottomTab.CHATS -> {
                                ChatsScreen(
                                    activeChats = activeChats,
                                    onChatClick = { chat -> viewModel.openChatWithActiveChatEntity(chat) }
                                )
                            }

                            BottomTab.STORE -> {
                                StoreScreen(
                                    wallet = wallet,
                                    transactions = transactions,
                                    onClaimDailyBonus = { viewModel.claimDailyBonus() },
                                    onPurchasePack = { _, _, _ ->
                                        android.widget.Toast.makeText(context, "This feature is currently Unavailable.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            BottomTab.PROFILE -> {
                                ProfileScreen(
                                    userPhoneNumber = userPhoneNumber,
                                    onNavigateToStore = { viewModel.selectBottomTab(BottomTab.STORE) },
                                    onLogoutClick = { viewModel.logoutUser() }
                                )
                            }
                        }
                    }
                }
            }

            // Daily Login Reward Dialog (First login of the day)
            dailyLoginRewardAmount?.let { rewardCoins ->
                DailyLoginRewardDialog(
                    rewardCoins = rewardCoins,
                    onClaim = { viewModel.dismissDailyRewardPopup() }
                )
            }
        }
    }
}
