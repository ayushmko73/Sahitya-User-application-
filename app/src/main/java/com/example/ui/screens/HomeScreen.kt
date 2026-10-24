package com.example.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ActiveChatEntity
import com.example.data.CharacterStoryEntity
import com.example.data.UserChallengeEntity
import com.example.ui.components.SCoinHexagonBadge
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.SahityaGold
import com.example.ui.theme.SahityaGoldBright
import com.example.ui.theme.SahityaRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextCrispWhite
import com.example.ui.theme.TextMutedGray

@Composable
fun HomeScreen(
    selectedGenreFilter: String,
    onFilterSelect: (String) -> Unit,
    featuredStory: CharacterStoryEntity?,
    stories: List<CharacterStoryEntity>,
    activeChats: List<ActiveChatEntity>,
    challenges: List<UserChallengeEntity>,
    onStoryClick: (CharacterStoryEntity) -> Unit,
    onActiveChatClick: (ActiveChatEntity) -> Unit,
    onAcceptChallenge: (String) -> Unit,
    onNavigateToStore: () -> Unit
) {
    val filters = listOf("For you", "New Release", "Top Rank", "Story", "Sci-Fi", "Drama", "Romance", "Fantasy", "Horror")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .testTag("home_screen_container"),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // 1. Interactive Top Category Filter Bar (Kavana Underline Style)
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = filter.equals(selectedGenreFilter, ignoreCase = true)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onFilterSelect(filter) }
                            .testTag("filter_pill_$filter")
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) TextCrispWhite else TextMutedGray,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(SahityaRed)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }
        }

        // DYNAMIC CONTENT BASED ON ACTIVE CATEGORY TAB
        when {
            // TAB 1: FOR YOU (Default Home View)
            selectedGenreFilter.equals("For you", ignoreCase = true) || selectedGenreFilter.equals("For You", ignoreCase = true) -> {
                // Hero Featured Banner (Top 5 Weekly Trending Stories Auto-Scrolling Carousel)
                item {
                    HeroBannerCard(
                        hero = featuredStory ?: stories.firstOrNull(),
                        onStoryClick = onStoryClick,
                        stories = stories
                    )
                }

                // Continue Chat Section
                if (activeChats.isNotEmpty()) {
                    item {
                        ContinueChatSection(
                            activeChats = activeChats,
                            onActiveChatClick = onActiveChatClick
                        )
                    }
                }

                // New Release Section (3 Stories Horizontal x 3 Vertical = 9 Stories)
                item {
                    Text(
                        text = "New Release",
                        color = TextCrispWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 10.dp)
                    )
                }

                val newReleaseTriples = stories.take(9).chunked(3)
                items(newReleaseTriples.size) { rowIndex ->
                    val triple = newReleaseTriples[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CompactGridCard(
                            story = triple[0],
                            modifier = Modifier.weight(1f),
                            onClick = { onStoryClick(triple[0]) }
                        )
                        if (triple.size > 1) {
                            CompactGridCard(
                                story = triple[1],
                                modifier = Modifier.weight(1f),
                                onClick = { onStoryClick(triple[1]) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        if (triple.size > 2) {
                            CompactGridCard(
                                story = triple[2],
                                modifier = Modifier.weight(1f),
                                onClick = { onStoryClick(triple[2]) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // New Release View All Button
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF2C1217))
                            .border(1.dp, SahityaRed.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .clickable { onFilterSelect("New Release") }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View All  ›",
                            color = SahityaRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Top 10 This Week Section (Shifted Below New Release)
                item {
                    Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)) {
                        Text(
                            text = "Top 10 This Week",
                            color = TextCrispWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val top10Pairs = stories.take(4).chunked(2)
                items(top10Pairs.size) { rowIndex ->
                    val pair = top10Pairs[rowIndex]
                    val startIndex = rowIndex * 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RankedGridCard(
                            rank = startIndex + 1,
                            story = pair[0],
                            modifier = Modifier.weight(1f),
                            onClick = { onStoryClick(pair[0]) }
                        )
                        if (pair.size > 1) {
                            RankedGridCard(
                                rank = startIndex + 2,
                                story = pair[1],
                                modifier = Modifier.weight(1f),
                                onClick = { onStoryClick(pair[1]) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Top 10 View All Button
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF2C1217))
                            .border(1.dp, SahityaRed.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .clickable { onFilterSelect("Top Rank") }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View All  ›",
                            color = SahityaRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // S-Coin Promo Banner
                item {
                    CoinPromoBanner(onNavigateToStore = onNavigateToStore)
                }
            }

            // TAB 2: TOP RANK VIEW (Matching Image 4)
            selectedGenreFilter.equals("Top Rank", ignoreCase = true) -> {
                item {
                    Text(
                        text = "Top Rank",
                        color = TextCrispWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp)
                    )
                }

                val allRankedPairs = stories.chunked(2)
                items(allRankedPairs.size) { rowIndex ->
                    val pair = allRankedPairs[rowIndex]
                    val startIndex = rowIndex * 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RankedGridCard(
                            rank = startIndex + 1,
                            story = pair[0],
                            modifier = Modifier.weight(1f),
                            onClick = { onStoryClick(pair[0]) }
                        )
                        if (pair.size > 1) {
                            RankedGridCard(
                                rank = startIndex + 2,
                                story = pair[1],
                                modifier = Modifier.weight(1f),
                                onClick = { onStoryClick(pair[1]) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // TAB 3: NEW RELEASE VIEW (Matching Image 8)
            selectedGenreFilter.equals("New Release", ignoreCase = true) -> {
                item {
                    Text(
                        text = "New Release",
                        color = TextCrispWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp)
                    )
                }

                val newReleasePairs = stories.chunked(2)
                items(newReleasePairs.size) { rowIndex ->
                    val pair = newReleasePairs[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RankedGridCard(
                            rank = null,
                            story = pair[0],
                            modifier = Modifier.weight(1f),
                            onClick = { onStoryClick(pair[0]) }
                        )
                        if (pair.size > 1) {
                            RankedGridCard(
                                rank = null,
                                story = pair[1],
                                modifier = Modifier.weight(1f),
                                onClick = { onStoryClick(pair[1]) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // TAB 4+: CATEGORY GENRE FILTER (Story, Drama, Romance, Fantasy, Horror)
            else -> {
                item {
                    Text(
                        text = "$selectedGenreFilter Stories",
                        color = TextCrispWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp)
                    )
                }

                val filteredStories = stories.filter {
                    it.genre.equals(selectedGenreFilter, ignoreCase = true) ||
                            it.title.contains(selectedGenreFilter, ignoreCase = true)
                }.ifEmpty { stories }

                val genrePairs = filteredStories.chunked(2)
                items(genrePairs.size) { rowIndex ->
                    val pair = genrePairs[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RankedGridCard(
                            rank = null,
                            story = pair[0],
                            modifier = Modifier.weight(1f),
                            onClick = { onStoryClick(pair[0]) }
                        )
                        if (pair.size > 1) {
                            RankedGridCard(
                                rank = null,
                                story = pair[1],
                                modifier = Modifier.weight(1f),
                                onClick = { onStoryClick(pair[1]) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT HELPERS
// -----------------------------------------------------------------------------

@Composable
private fun HeroBannerCard(
    hero: CharacterStoryEntity?,
    onStoryClick: (CharacterStoryEntity) -> Unit,
    stories: List<CharacterStoryEntity>
) {
    val top5Stories = remember(stories, hero) {
        val list = mutableListOf<CharacterStoryEntity>()
        if (hero != null) list.add(hero)
        list.addAll(stories.filter { it.id != hero?.id })
        list.take(5)
    }

    if (top5Stories.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { top5Stories.size })
    var isPressed by remember { mutableStateOf(false) }

    // Auto-scroll horizontally with increased speed (every 2.5 seconds), pauses when user holds image
    LaunchedEffect(pagerState, isPressed, top5Stories.size) {
        if (top5Stories.size > 1) {
            while (true) {
                delay(2500L) // Faster scroll interval (2.5s speed)
                if (!isPressed) {
                    val nextPage = (pagerState.currentPage + 1) % top5Stories.size
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 400)
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, CardBorderDark, RoundedCornerShape(22.dp))
            .testTag("hero_featured_card")
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val currentStory = top5Stories[page]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp) // 1.4x vertical thumbnail size
                    .pointerInput(currentStory.id) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                try {
                                    awaitRelease()
                                } finally {
                                    isPressed = false
                                }
                            },
                            onTap = {
                                onStoryClick(currentStory)
                            }
                        )
                    }
            ) {
                val imageUrl = if (currentStory.bannerImage.isNotBlank()) currentStory.bannerImage else currentStory.mainThumbnail
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = currentStory.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = getGenreImageRes(currentStory.imageType)),
                        placeholder = painterResource(id = getGenreImageRes(currentStory.imageType))
                    )
                } else {
                    Image(
                        painter = painterResource(id = getGenreImageRes(currentStory.imageType)),
                        contentDescription = currentStory.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.35f to Color.Transparent,
                                    1.0f to Color(0xEE000000)
                                )
                            )
                        )
                )

                // Story Title & Details
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 76.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentStory.title,
                        color = TextCrispWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Roleplay · ${currentStory.genre} · ${currentStory.chatCount} Chats",
                        color = TextMutedGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Frosted Glass "START CHAT" Extended Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 18.dp)
                        .fillMaxWidth(0.72f) // Extended horizontally
                        .clip(RoundedCornerShape(8.dp)) // Reduced edge curvature
                        .background(Color(0x66FFFFFF))
                        .border(1.dp, Color(0xAAFFFFFF), RoundedCornerShape(8.dp))
                        .clickable { onStoryClick(currentStory) }
                        .padding(vertical = 14.dp)
                        .testTag("hero_btn_start_chat_${currentStory.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "START CHAT",
                        color = TextCrispWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        // Pager Indicator Dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(top5Stories.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SahityaRed else Color(0x66FFFFFF))
                )
            }
        }
    }
}

@Composable
private fun ContinueChatSection(
    activeChats: List<ActiveChatEntity>,
    onActiveChatClick: (ActiveChatEntity) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = "Continue Chat",
            color = TextCrispWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activeChats) { chat ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onActiveChatClick(chat) }
                        .testTag("continue_chat_avatar_${chat.characterId}")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SurfaceDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = getGenreImageRes(chat.avatarType)
                                ),
                                contentDescription = chat.characterName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = chat.characterName,
                        color = TextCrispWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(72.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RankedGridCard(
    rank: Int?,
    story: CharacterStoryEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .testTag("ranked_card_${story.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (rank != null) 22.dp else 0.dp)
        ) {
            // Poster Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDark)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(14.dp))
            ) {
                val imageUrl = if (story.mainThumbnail.isNotBlank()) story.mainThumbnail else story.bannerImage
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = story.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = getGenreImageRes(story.imageType)),
                        placeholder = painterResource(id = getGenreImageRes(story.imageType))
                    )
                } else {
                    Image(
                        painter = painterResource(id = getGenreImageRes(story.imageType)),
                        contentDescription = story.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Dark Bottom Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.4f to Color.Transparent,
                                    1.0f to Color(0xDD000000)
                                )
                            )
                        )
                )

                // Translucent Overlay Chat Volume Badge at Bottom-Right of Thumbnail
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xAA0A0A0A))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Comment,
                            contentDescription = "Chat Volume",
                            tint = TextCrispWhite,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = story.chatCount,
                            color = TextCrispWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bold Oversized Sequence Rank Number (1, 2, 3...) at Bottom Beside Thumbnail
            if (rank != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-20).dp, y = 8.dp)
                ) {
                    // Outlined Bright Red Stroke (Inverted color)
                    Text(
                        text = "$rank",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        style = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFFFF3B4E),
                            drawStyle = Stroke(width = 8f)
                        )
                    )
                    // Inner Dark Crimson Fill (Inverted color)
                    Text(
                        text = "$rank",
                        color = Color(0xFF60000A),
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = story.title,
            color = TextCrispWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Roleplay · ${story.genre}",
            color = TextMutedGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CompactGridCard(
    story: CharacterStoryEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .testTag("compact_card_${story.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
        ) {
            val imageUrl = if (story.mainThumbnail.isNotBlank()) story.mainThumbnail else story.bannerImage
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = story.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = getGenreImageRes(story.imageType)),
                    placeholder = painterResource(id = getGenreImageRes(story.imageType))
                )
            } else {
                Image(
                    painter = painterResource(id = getGenreImageRes(story.imageType)),
                    contentDescription = story.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Dark Bottom Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.5f to Color.Transparent,
                                1.0f to Color(0xDD000000)
                            )
                        )
                    )
            )

            // Translucent Chat Count Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xAA0A0A0A))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Comment,
                        contentDescription = "Chats",
                        tint = TextCrispWhite,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = story.chatCount,
                        color = TextCrispWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = story.title,
            color = TextCrispWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = story.genre,
            color = TextMutedGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ChallengeGridCard(
    challenge: UserChallengeEntity,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.testTag("challenge_card_${challenge.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4.1f)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = getGenreImageRes(challenge.id)),
                contentDescription = challenge.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Chat badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x99000000))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Comment,
                        contentDescription = "Chats",
                        tint = TextCrispWhite,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "3.3K",
                        color = TextCrispWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = challenge.title,
            color = TextCrispWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.height(38.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (challenge.isCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF102D1B)),
                contentAlignment = Alignment.Center
            ) {
                Text("CLAIMED", color = Color(0xFF00E676), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        } else if (challenge.isAccepted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Text("IN PROGRESS", color = SahityaGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("accept_btn_${challenge.id}")
            ) {
                Text("ACCEPT", color = PitchBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CoinPromoBanner(onNavigateToStore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF2A1F00), Color(0xFF4A3500), Color(0xFF1F1500))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(SahityaGoldBright, SahityaGold)),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onNavigateToStore() }
            .padding(16.dp)
            .testTag("coin_promo_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SahityaRed)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "200% BONUS COINS",
                        color = TextCrispWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Unlock Unlimited Roleplay Turns",
                    color = SahityaGoldBright,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Get 300 S-Coins + 1 Month Ad Free at ₹79",
                    color = TextMutedGray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            SCoinHexagonBadge(size = 44.dp)
        }
    }
}

fun getGenreImageRes(imageType: String): Int {
    return when (imageType) {
        "hero" -> R.drawable.img_hero_banner_1786271312636
        "fantasy" -> R.drawable.img_genre_fantasy_1786271322868
        "drama" -> R.drawable.img_genre_drama_1786271332993
        "horror" -> R.drawable.img_genre_horror_1786271344414
        else -> R.drawable.img_hero_banner_1786271312636
    }
}
