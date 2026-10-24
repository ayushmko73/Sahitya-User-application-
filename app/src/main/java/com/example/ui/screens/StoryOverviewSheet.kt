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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CharacterStoryEntity
import com.example.ui.theme.SahityaGold
import com.example.ui.theme.SahityaRed
import com.example.ui.theme.TextCrispWhite
import com.example.ui.theme.TextMutedGray

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryOverviewSheet(
    story: CharacterStoryEntity,
    allStories: List<CharacterStoryEntity>,
    onDismiss: () -> Unit,
    onStartChat: (CharacterStoryEntity) -> Unit
) {
    val context = LocalContext.current
    var isAboutExpanded by remember { mutableStateOf(false) }
    var userCommentText by remember { mutableStateOf("") }
    var showFullCommentsSheet by remember { mutableStateOf(false) }
    var selectedEmojiForSheet by remember { mutableStateOf("👍") }
    
    val comments = remember {
        listOf(
            CommentItem("1", "Chetan", "1w", "Read mine chat if you can ..damn! Kya story bana di 🥺", 5),
            CommentItem("2", "Hsk", "3w", "Main to use africa bech aya", 16),
            CommentItem("3", "Aarav", "4w", "Super realistic dialogues! The character response timing is amazing.", 24)
        )
    }

    val reactionCounts = remember {
        mutableStateMapOf(
            "❤️" to 142,
            "👍" to 89,
            "😂" to 65,
            "😗" to 34,
            "🥺" to 92,
            "🔥" to 110
        )
    }

    val similarStories = remember(story, allStories) {
        allStories.filter { it.id != story.id }.take(4)
    }

    if (showFullCommentsSheet) {
        FullCommentsSheet(
            storyTitle = story.title,
            initialEmoji = selectedEmojiForSheet,
            onDismiss = { showFullCommentsSheet = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0E0E10),
        contentColor = TextCrispWhite,
        modifier = Modifier.testTag("story_overview_sheet")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // 1. Hero Poster Banner & Header Controls
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    ) {
                        val imageUrl = if (story.bannerImage.isNotBlank()) story.bannerImage else story.mainThumbnail
                        if (imageUrl.isNotBlank()) {
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

                        // Top Gradient Overlay with Controls
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xBB000000),
                                            Color.Transparent,
                                            Color(0xCC0E0E10),
                                            Color(0xFF0E0E10)
                                        )
                                    )
                                )
                        )

                        // Header Actions Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x66000000))
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x66000000))
                                    .clickable {
                                        Toast.makeText(context, "Story link shared!", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White
                                )
                            }
                        }

                        // Bottom Poster Info Overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = story.title,
                                color = TextCrispWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 30.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Category Pills
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Roleplay", story.genre, "Popular").forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0x40FFFFFF))
                                            .border(0.5.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            color = TextCrispWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Stats Summary Row Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF18181C))
                            .padding(vertical = 14.dp, horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = story.chatCount,
                                    color = TextCrispWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Chats",
                                    color = TextMutedGray,
                                    fontSize = 12.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(28.dp)
                                    .background(Color(0xFF2E2E34))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = SahityaGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "4.46",
                                        color = TextCrispWhite,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Rating",
                                    color = TextMutedGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // 3. About Container Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF18181C))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "About",
                                color = TextCrispWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isAboutExpanded) story.systemPrompt else story.tagline,
                                color = Color(0xFFD1D1D6),
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                maxLines = if (isAboutExpanded) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .clickable { isAboutExpanded = !isAboutExpanded }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isAboutExpanded) "Show Less" else "Show More",
                                    color = TextMutedGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = if (isAboutExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle",
                                    tint = TextMutedGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Media Library Container Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF18181C))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Media Library (20)",
                                    color = TextCrispWhite,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "More",
                                    tint = TextMutedGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(4) { idx ->
                                    Box(
                                        modifier = Modifier
                                            .size(width = 110.dp, height = 130.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF24242A))
                                    ) {
                                        Image(
                                            painter = painterResource(id = getGenreImageRes(story.imageType)),
                                            contentDescription = "Media",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Story Creator Container Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF18181C))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Story Creator",
                                color = TextCrispWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2C2C34))
                                ) {
                                    Image(
                                        painter = painterResource(id = getGenreImageRes(story.imageType)),
                                        contentDescription = "Creator",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Saksham",
                                            color = TextCrispWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified",
                                            tint = Color(0xFF00B0FF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "78.2k total chats · Total 6 bots",
                                        color = TextMutedGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Comments Container Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF18181C))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Comments (98)",
                                color = TextCrispWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Individual Comments
                            comments.forEach { comment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF33333C)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = comment.username.take(1),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = comment.username,
                                                    color = TextCrispWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = comment.timeAgo,
                                                    color = TextMutedGray,
                                                    fontSize = 11.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(3.dp))

                                            Text(
                                                text = comment.text,
                                                color = Color(0xFFE5E5EA),
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                                Text("Reply", color = TextMutedGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                Text("View 3 Replies", color = TextMutedGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.FavoriteBorder,
                                            contentDescription = "Like",
                                            tint = TextMutedGray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text("${comment.likes}", color = TextMutedGray, fontSize = 10.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Reaction Emojis Row
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                items(reactionCounts.keys.toList()) { emoji ->
                                    val count = reactionCounts[emoji] ?: 0
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFF24242C))
                                            .clickable {
                                                selectedEmojiForSheet = emoji
                                                showFullCommentsSheet = true
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = emoji, fontSize = 18.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Input Box (Click opens Comments sheet)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF101012))
                                    .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(20.dp))
                                    .clickable {
                                        showFullCommentsSheet = true
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (userCommentText.isEmpty()) "What's your thoughts?" else userCommentText,
                                        color = if (userCommentText.isEmpty()) TextMutedGray else TextCrispWhite,
                                        fontSize = 13.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.SentimentSatisfied,
                                        contentDescription = "Emoji",
                                        tint = TextMutedGray,
                                        modifier = Modifier.clickable {
                                            selectedEmojiForSheet = "😊"
                                            showFullCommentsSheet = true
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showFullCommentsSheet = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show all comments", color = TextMutedGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "More", tint = TextMutedGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // 7. Similar To This Story Container Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF18181C))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Similar to this story",
                                color = TextCrispWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(similarStories) { simStory ->
                                    Column(
                                        modifier = Modifier
                                            .width(115.dp)
                                            .clickable {
                                                onStartChat(simStory)
                                                onDismiss()
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(3f / 4.2f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF24242A))
                                        ) {
                                            Image(
                                                painter = painterResource(id = getGenreImageRes(simStory.imageType)),
                                                contentDescription = simStory.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = simStory.title,
                                            color = TextCrispWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = "Roleplay",
                                            color = TextMutedGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. STICKY BOTTOM "CHAT NOW" CTA BUTTON
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xF20E0E10), Color(0xFF0E0E10))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        onStartChat(story)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("chat_now_sticky_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(
                        text = "Chat now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

private data class CommentItem(
    val id: String,
    val username: String,
    val timeAgo: String,
    val text: String,
    val likes: Int
)

private data class FullCommentData(
    val id: String,
    val username: String,
    val timeAgo: String,
    val text: String,
    val likes: Int,
    val replyCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullCommentsSheet(
    storyTitle: String,
    initialEmoji: String = "👍",
    onDismiss: () -> Unit
) {
    var selectedEmoji by remember { mutableStateOf(initialEmoji) }
    var inputText by remember { mutableStateOf("") }
    
    val commentList = remember {
        mutableStateListOf(
            FullCommentData(
                id = "1",
                username = "Raj",
                timeAgo = "3hr",
                text = "Matlab yehbhi wohi tarah ke ek or faltu kahani..! Jaha mai innosent hoon per mere wife ek carecter less hai, mai sach kahungga per sabhi mujhe hi galat kahega..! Matlab ab kavana app ap toxic hookup culture ke upar stroy creat kar neme jayda dil chaaspi rakhta hai, purane cavana app pe kahaniya shayed aise nahi tha per woh achha lagta tha lekin aj ke kavana aap bohot hi jayda professional ho chuka hai, jo mujhe or mujjjaise logoko yeh pasand nahi..!",
                likes = 2,
                replyCount = 2
            ),
            FullCommentData(
                id = "2",
                username = "Kris",
                timeAgo = "2hr",
                text = "Actually love that character development! Excited for the next chapter.",
                likes = 5,
                replyCount = 0
            ),
            FullCommentData(
                id = "3",
                username = "Aarav",
                timeAgo = "5hr",
                text = "The plot twists keep getting better and better!",
                likes = 12,
                replyCount = 1
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF121214),
        contentColor = TextCrispWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF44444C))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Top Header: Title "Comments (10)" and Close "X" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comments (${commentList.size + 7})",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF26262B))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Scrollable List of Comments
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(commentList) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f)) {
                            // Avatar DP
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF33333C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.username.take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.username,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.timeAgo,
                                        color = Color(0xFF8E8E93),
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = item.text,
                                    color = Color(0xFFE5E5EA),
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(
                                        text = "Reply",
                                        color = Color(0xFF8E8E93),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable { }
                                    )
                                    if (item.replyCount > 0) {
                                        Text(
                                            text = "View ${item.replyCount} Replies",
                                            color = Color(0xFF8E8E93),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.clickable { }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        val idx = commentList.indexOf(item)
                                        if (idx != -1) {
                                            commentList[idx] = item.copy(likes = item.likes + 1)
                                        }
                                    }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${item.likes}",
                                color = Color(0xFF8E8E93),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Bottom Sticky Section (Emoji Reaction Bar + Input Box)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121214))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top Emoji Row: ❤️ 👍 😂 😗 🥺 🔥
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("❤️", "👍", "😂", "😗", "🥺", "🔥").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 26.sp,
                            modifier = Modifier
                                .clickable {
                                    selectedEmoji = emoji
                                    // Directly post emoji comment to chat
                                    commentList.add(
                                        0,
                                        FullCommentData(
                                            id = System.currentTimeMillis().toString(),
                                            username = "You",
                                            timeAgo = "Just now",
                                            text = emoji,
                                            likes = 0,
                                            replyCount = 0
                                        )
                                    )
                                }
                                .padding(4.dp)
                        )
                    }
                }

                // Input Pill Container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF202024))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selected Emoji on Left - clicking posts it directly to chat
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable {
                                commentList.add(
                                    0,
                                    FullCommentData(
                                        id = System.currentTimeMillis().toString(),
                                        username = "You",
                                        timeAgo = "Just now",
                                        text = selectedEmoji,
                                        likes = 0,
                                        replyCount = 0
                                    )
                                )
                            }
                    ) {
                        Text(text = selectedEmoji, fontSize = 22.sp)
                    }

                    // Text Field
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Add a comment...",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 15.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Send Button Arrow Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) SahityaRed else Color(0xFF323238))
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    commentList.add(
                                        0,
                                        FullCommentData(
                                            id = System.currentTimeMillis().toString(),
                                            username = "You",
                                            timeAgo = "Just now",
                                            text = inputText,
                                            likes = 0,
                                            replyCount = 0
                                        )
                                    )
                                    inputText = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
