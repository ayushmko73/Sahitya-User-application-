package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CharacterStoryEntity
import com.example.data.ChatMessageEntity
import com.example.data.ChatSummaryEntity
import com.example.ui.components.SCoinHexagonBadge
import com.example.ui.components.SahityaFormattedMessage
import com.example.ui.components.SahityaTypingSyntaxPreview
import com.example.ui.components.buildSahityaAnnotatedString
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.SahityaGold
import com.example.ui.theme.SahityaGoldBright
import com.example.ui.theme.SahityaRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextCrispWhite
import com.example.ui.theme.TextMutedGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleplayChatScreen(
    character: CharacterStoryEntity,
    messages: List<ChatMessageEntity>,
    summaries: List<ChatSummaryEntity> = emptyList(),
    sCoinBalance: Int,
    isSending: Boolean,
    onSendMessage: (String) -> Unit,
    onBackClick: () -> Unit,
    onReplayClick: () -> Unit = {},
    onDeleteChatClick: () -> Unit = {},
    onPurchaseCoinPack: (coins: Int, priceInr: String, title: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Interactive UI State
    var isDarkMode by remember { mutableStateOf(true) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showCoinDrawer by remember { mutableStateOf(false) }
    var showCharacterSheet by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showMemoryDialog by remember { mutableStateOf(false) }
    var showLightSuggestions by remember { mutableStateOf(false) }

    // Dynamic Theme Colors
    val chatBgColor = if (isDarkMode) PitchBlack else Color(0xFFF4F4F6)
    val headerBgColor = if (isDarkMode) Color(0xFF18181A) else Color.White
    val headerBorderColor = if (isDarkMode) CardBorderDark else Color(0xFFE5E5EA)
    val textColorPrimary = if (isDarkMode) TextCrispWhite else Color(0xFF111111)
    val textColorSecondary = if (isDarkMode) TextMutedGray else Color(0xFF6C6C70)
    val inputBgColor = if (isDarkMode) SurfaceDark else Color.White
    val userBubbleBg = if (isDarkMode) SurfaceDark else Color.White
    val userBubbleBorder = if (isDarkMode) CardBorderDark else Color(0xFFE0E0E0)
    val characterBubbleBg = if (isDarkMode) SurfaceDark else Color.White
    val characterBubbleBorder = if (isDarkMode) CardBorderDark else Color(0xFFE0E0E0)

    val suggestedActionChips = remember(character.genre) {
        when (character.genre) {
            "Fantasy" -> listOf("*Draw katana*", "*Demand answers*", "*Offer blood oath*", "*Cast flame spell*")
            "Romance" -> listOf("*Step closer*", "*Whisper softly*", "*Accept the offer*", "*Inquire about the past*")
            "Anime" -> listOf("*Hack mainframe*", "*Activate visor*", "*Infiltrate server*", "*Negotiate price*")
            else -> listOf("*Step into shadow*", "*Ask a question*", "*Challenge authority*", "*Investigate further*")
        }
    }

    LaunchedEffect(messages.size, isSending) {
        val targetIndex = if (isSending) messages.size else messages.size - 1
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(chatBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("roleplay_chat_container")
    ) {
        // 1. ACTIVE CHAT TOP BAR HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBgColor)
                .border(width = 1.dp, color = headerBorderColor)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Back button & Character DP + Info (Clickable for profile)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColorPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showCharacterSheet = true }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .testTag("character_header_info")
                ) {
                    // Character DP Avatar with Green Online Status Dot
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                    ) {
                        val avatarUrl = if (character.mainThumbnail.isNotBlank()) character.mainThumbnail else character.bannerImage
                        if (avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = character.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(1.5.dp, SahityaRed, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = getGenreImageRes(character.imageType)),
                                contentDescription = character.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(1.5.dp, SahityaRed, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // Online Status Green Indicator Dot
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                                .border(1.5.dp, headerBgColor, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = character.title,
                            color = textColorPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Roleplay",
                            color = textColorSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right Actions: Theme Toggle, S-Coin Pill, and Triple-Dot Menu
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Functional Theme Switcher (Sun/Moon Toggle)
                IconButton(
                    onClick = { isDarkMode = !isDarkMode },
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Outlined.DarkMode,
                        contentDescription = "Toggle Dark/Light Mode",
                        tint = if (isDarkMode) Color.White else Color(0xFF555555)
                    )
                }

                // S-Coin Badge Button (Opens Drawer)
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkMode) Color(0xFF1D1810) else Color(0xFFFFF7E6))
                        .border(1.dp, SahityaGold, RoundedCornerShape(16.dp))
                        .clickable { showCoinDrawer = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("coin_drawer_shortcut"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SCoinHexagonBadge(size = 20.dp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "9/Msg",
                        color = if (isDarkMode) SahityaGoldBright else Color(0xFF8A6200),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Vertical Triple-Dot Button (Triggers Dropdown Menu)
                Box {
                    IconButton(
                        onClick = { showDropdownMenu = true },
                        modifier = Modifier.testTag("chat_triple_dot_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = textColorPrimary
                        )
                    }

                    // 2. INTERACTIVE DROPDOWN MENU
                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false },
                        modifier = Modifier
                            .background(Color(0xFF1E1E22))
                            .border(1.dp, Color(0xFF333338), RoundedCornerShape(12.dp))
                            .testTag("chat_dropdown_menu")
                    ) {
                        // 80/20 Rolling Memory Checkpoints
                        DropdownMenuItem(
                            text = { Text("Memory Engine (80/20)", color = SahityaGoldBright, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Text("🧠", fontSize = 16.sp)
                            },
                            onClick = {
                                showDropdownMenu = false
                                showMemoryDialog = true
                            }
                        )

                        // Replay
                        DropdownMenuItem(
                            text = { Text("Replay", color = Color.White, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, contentDescription = "Replay", tint = Color.White, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showDropdownMenu = false
                                onReplayClick()
                                Toast.makeText(context, "Roleplay replayed!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // Report
                        DropdownMenuItem(
                            text = { Text("Report", color = Color.White, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.OutlinedFlag, contentDescription = "Report", tint = Color.White, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showDropdownMenu = false
                                showReportDialog = true
                            }
                        )

                        // Share
                        DropdownMenuItem(
                            text = { Text("Share", color = Color.White, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showDropdownMenu = false
                                Toast.makeText(context, "Story link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        HorizontalDivider(color = Color(0xFF2C2C30), modifier = Modifier.padding(vertical = 4.dp))

                        // Coin Cost Indicator / Store Shortcut (9/Message)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SCoinHexagonBadge(size = 18.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("9/Message", color = SahityaGoldBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Open Store",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            onClick = {
                                showDropdownMenu = false
                                showCoinDrawer = true
                            }
                        )

                        HorizontalDivider(color = Color(0xFF2C2C30), modifier = Modifier.padding(vertical = 4.dp))

                        // Delete Chat
                        DropdownMenuItem(
                            text = { Text("Delete Chat", color = SahityaRed, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SahityaRed, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showDropdownMenu = false
                                onDeleteChatClick()
                                Toast.makeText(context, "Chat deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // MESSAGES LIST
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
                .clickable { showLightSuggestions = false },
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 16.dp
                                )
                            )
                            .background(if (isUser) userBubbleBg else characterBubbleBg)
                            .border(
                                width = 1.dp,
                                color = if (isUser) userBubbleBorder else characterBubbleBorder,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 16.dp
                                )
                            )
                            .padding(12.dp)
                            .testTag("chat_bubble_${if (isUser) "user" else "character"}")
                    ) {
                        Column {
                            Text(
                                text = if (isUser) "You" else character.title,
                                color = if (isUser) (if (isDarkMode) TextCrispWhite else Color.Black) else SahityaGoldBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            SahityaFormattedMessage(
                                text = msg.text,
                                isUser = isUser,
                                defaultTextColor = textColorPrimary
                            )
                        }
                    }
                }
            }

            if (isSending) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = 2.dp,
                                        bottomEnd = 16.dp
                                    )
                                )
                                .background(characterBubbleBg)
                                .border(
                                    width = 1.dp,
                                    color = characterBubbleBorder,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = 2.dp,
                                        bottomEnd = 16.dp
                                    )
                                )
                                .padding(12.dp)
                                .testTag("ai_typing_indicator")
                        ) {
                            Column {
                                Text(
                                    text = character.title,
                                    color = SahityaGoldBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${character.title.takeWhile { it != ':' }} is typing",
                                        color = textColorSecondary,
                                        fontSize = 13.sp,
                                        fontStyle = FontStyle.Italic
                                    )
                                    AnimatedTypingDots(dotColor = SahityaGoldBright)
                                }
                            }
                        }
                    }
                }
            }
        }

        // LIGHTBULB 2 STORY CONTINUATION SUGGESTIONS POPOVER
        if (showLightSuggestions) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDarkMode) Color(0xFF221D10) else Color(0xFFFFFBEB))
                    .border(1.dp, SahityaGold, RoundedCornerShape(16.dp))
                    .padding(12.dp)
                    .testTag("lightbulb_suggestions_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💡", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Continue Story Suggestions",
                                color = SahityaGoldBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Suggestions",
                            tint = TextMutedGray,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { showLightSuggestions = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val suggestions = listOf(
                        suggestedActionChips.getOrElse(0) { "*Draw katana and step into shadow*" },
                        suggestedActionChips.getOrElse(1) { "*Inquire about the hidden truth*" }
                    )

                    suggestions.forEachIndexed { index, option ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDarkMode) SurfaceDark else Color.White)
                                .border(1.dp, if (isDarkMode) CardBorderDark else Color(0xFFE5E5EA), RoundedCornerShape(10.dp))
                                .clickable {
                                    onSendMessage(option)
                                    showLightSuggestions = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("lightbulb_suggestion_item_$index")
                        ) {
                            Text(
                                text = option,
                                color = if (isDarkMode) TextCrispWhite else Color(0xFF222222),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // LIVE TYPING SYNTAX PREVIEW INDICATOR
        SahityaTypingSyntaxPreview(inputText = inputText)

        // INPUT FIELD & LIGHTBULB SUGGESTION ICON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBgColor)
                .border(1.dp, headerBorderColor)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    showLightSuggestions = false
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                placeholder = {
                    Text(
                        "Write your roleplay action (-2 S)...",
                        color = textColorSecondary,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBgColor,
                    unfocusedContainerColor = inputBgColor,
                    focusedBorderColor = SahityaRed,
                    unfocusedBorderColor = headerBorderColor,
                    focusedTextColor = textColorPrimary,
                    unfocusedTextColor = textColorPrimary
                ),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Custom Yellow 💡 Light Icon Button above/beside send icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (showLightSuggestions) Color(0xFF4A3800) else (if (isDarkMode) Color(0xFF2A2200) else Color(0xFFFFF9C4)))
                    .border(1.dp, SahityaGold, CircleShape)
                    .clickable { showLightSuggestions = !showLightSuggestions }
                    .testTag("lightbulb_suggestion_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💡",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) SahityaRed else SurfaceVariantDark)
                    .clickable(enabled = inputText.isNotBlank() && !isSending) {
                        onSendMessage(inputText)
                        inputText = ""
                        showLightSuggestions = false
                    }
                    .testTag("chat_send_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) TextCrispWhite else TextMutedGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // 3. COIN STORE / MESSAGE COST BOTTOM DRAWER (`6639.jpg` reference)
    if (showCoinDrawer) {
        ModalBottomSheet(
            onDismissRequest = { showCoinDrawer = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF121214),
            contentColor = TextCrispWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .navigationBarsPadding()
                    .testTag("coin_store_bottom_drawer")
            ) {
                // Header Bar: Story Cost & Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SCoinHexagonBadge(size = 26.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "This Story: 9/Message",
                                color = TextCrispWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Balance: $sCoinBalance Coins Left",
                                color = SahityaGoldBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(onClick = { showCoinDrawer = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Limited Time Offer Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF382302), Color(0xFF5E3F00), Color(0xFF382302))
                            )
                        )
                        .border(1.dp, SahityaGold, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LIMITED TIME OFFER",
                                color = SahityaGoldBright,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "126% bonus + 1 Month ad free",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SahityaRed)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "126% BONUS",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Coin Package Tiers (Dark Containers #141414)
                val coinPacks = listOf(
                    CoinTierItem(800, 1005, "₹29", "STARTER BUNDLE", null),
                    CoinTierItem(1452, 2904, "₹79", "POPULAR BUNDLE", "MOST POPULAR"),
                    CoinTierItem(3000, 6000, "₹119", "MEGA BUNDLE", "BEST VALUE"),
                    CoinTierItem(6500, 13000, "₹249", "ULTIMATE BUNDLE", "126% BONUS")
                )

                coinPacks.forEach { pack ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF18181C))
                            .border(1.dp, Color(0xFF2A2A30), RoundedCornerShape(14.dp))
                            .clickable {
                                onPurchaseCoinPack(pack.coins + pack.bonus, pack.price, pack.title)
                                showCoinDrawer = false
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SCoinHexagonBadge(size = 28.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${pack.coins} Coins",
                                            color = TextCrispWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (pack.tag != null) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF332300))
                                                    .border(0.5.dp, SahityaGold, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = pack.tag,
                                                    color = SahityaGoldBright,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "+ ${pack.bonus} bonus (${pack.coins + pack.bonus} Total S)",
                                        color = SahityaGoldBright,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SahityaRed)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = pack.price,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // 4. CHARACTER DETAILS PROFILE SHEET (Triggered by Clicking Header DP/Title)
    if (showCharacterSheet) {
        StoryOverviewSheet(
            story = character,
            allStories = emptyList(),
            onDismiss = { showCharacterSheet = false },
            onStartChat = { showCharacterSheet = false }
        )
    }

    // 80/20 ROLLING MEMORY ENGINE DIALOG
    if (showMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showMemoryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🧠 ", fontSize = 18.sp)
                    Text("80/20 Memory Summaries", color = SahityaGoldBright, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Active rolling summary blocks extracted every 10 turns via Gemini model:",
                        color = TextMutedGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (summaries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(10.dp))
                                .border(1.dp, CardBorderDark, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "No 10-message summary checkpoints yet. Send messages to trigger automatic 80/20 summarization & raw message pruning!",
                                color = TextMutedGray,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(240.dp)
                        ) {
                            items(summaries) { summary ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceDark, RoundedCornerShape(10.dp))
                                        .border(1.dp, CardBorderDark, RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Checkpoint #${summary.chunkIndex} (10 Messages)",
                                        color = SahityaRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = summary.summaryBlock,
                                        color = TextCrispWhite,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMemoryDialog = false }) {
                    Text("Close", color = SahityaGoldBright, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E1E22),
            titleContentColor = TextCrispWhite
        )
    }

    // REPORT CONFIRMATION DIALOG
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Character", color = TextCrispWhite) },
            text = { Text("Are you sure you want to report ${character.title}? Our moderation safety team will review this roleplay profile.", color = TextMutedGray) },
            confirmButton = {
                TextButton(onClick = {
                    showReportDialog = false
                    Toast.makeText(context, "Report submitted for moderation.", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Report", color = SahityaRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel", color = TextMutedGray)
                }
            },
            containerColor = Color(0xFF1E1E22),
            titleContentColor = TextCrispWhite
        )
    }
}

private data class CoinTierItem(
    val coins: Int,
    val bonus: Int,
    val price: String,
    val title: String,
    val tag: String?
)

@Composable
fun AnimatedTypingDots(
    dotColor: Color = SahityaGoldBright,
    dotSize: Dp = 6.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .graphicsLayer { alpha = alpha1 }
                .clip(CircleShape)
                .background(dotColor)
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .graphicsLayer { alpha = alpha2 }
                .clip(CircleShape)
                .background(dotColor)
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .graphicsLayer { alpha = alpha3 }
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}
