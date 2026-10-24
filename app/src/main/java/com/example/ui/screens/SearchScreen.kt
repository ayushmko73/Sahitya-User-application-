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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CharacterStoryEntity
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.SahityaRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextCrispWhite
import com.example.ui.theme.TextMutedGray

@Composable
fun SearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    stories: List<CharacterStoryEntity>,
    onStoryClick: (CharacterStoryEntity) -> Unit
) {
    var selectedCategoryChoice by remember { mutableStateOf("All") }
    val categories = listOf("All", "Drama", "Romance", "Fantasy", "Horror", "Action")

    // Dynamic story recommendation filtering logic based on user choice & search query
    val recommendedStories = remember(searchQuery, selectedCategoryChoice, stories) {
        var result = stories
        if (selectedCategoryChoice != "All") {
            result = result.filter {
                it.genre.contains(selectedCategoryChoice, ignoreCase = true) ||
                        it.title.contains(selectedCategoryChoice, ignoreCase = true)
            }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            val matches = result.filter {
                it.title.lowercase().contains(q) ||
                        it.genre.lowercase().contains(q) ||
                        it.tagline.lowercase().contains(q) ||
                        it.handle.lowercase().contains(q) ||
                        it.systemPrompt.lowercase().contains(q)
            }
            if (matches.isNotEmpty()) {
                return@remember matches
            }
            val tokens = q.split(Regex("\\s+")).filter { it.isNotBlank() }
            val tokenMatches = result.filter { story ->
                tokens.any { token ->
                    story.title.lowercase().contains(token) ||
                            story.genre.lowercase().contains(token) ||
                            story.tagline.lowercase().contains(token) ||
                            story.handle.lowercase().contains(token) ||
                            story.systemPrompt.lowercase().contains(token)
                }
            }
            if (tokenMatches.isNotEmpty()) {
                return@remember tokenMatches
            }
        }
        if (searchQuery.isBlank() && selectedCategoryChoice == "All") {
            result.take(6)
        } else {
            result
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .testTag("search_screen_container"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp)
    ) {
        // Search Input Box
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field"),
                placeholder = {
                    Text(
                        text = "Search for Characters",
                        color = Color(0xFF8E8E93),
                        fontSize = 15.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier
                                .clickable { onSearchQueryChange("") }
                                .padding(4.dp)
                        )
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1B1C1E),
                    unfocusedContainerColor = Color(0xFF1B1C1E),
                    focusedBorderColor = Color(0xFF3A3A3C),
                    unfocusedBorderColor = Color(0xFF2C2C2E),
                    focusedTextColor = TextCrispWhite,
                    unfocusedTextColor = TextCrispWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Quick Choice Filter Bar for Automated Story Switching
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category.equals(selectedCategoryChoice, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SahityaRed else Color(0xFF1E1E22))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) SahityaRed else Color(0xFF2F2F35),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategoryChoice = category }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("choice_pill_$category"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else TextMutedGray,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 4. "Recommended" Title
        item {
            Text(
                text = "Recommended",
                color = TextCrispWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 5. Grid of 6 Recommended Stories (2-Column Layout matching Screenshot)
        val storyPairs = recommendedStories.chunked(2)
        items(storyPairs.size) { rowIndex ->
            val pair = storyPairs[rowIndex]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RecommendedStoryCard(
                    story = pair[0],
                    modifier = Modifier.weight(1f),
                    onClick = { onStoryClick(pair[0]) }
                )
                if (pair.size > 1) {
                    RecommendedStoryCard(
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

@Composable
private fun RecommendedStoryCard(
    story: CharacterStoryEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .testTag("recommended_story_${story.id}")
    ) {
        // Poster Frame (3:4 Aspect Ratio)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4.2f)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, Color(0xFF28282B), RoundedCornerShape(20.dp))
        ) {
            val imageUrl = if (story.mainThumbnail.isNotBlank()) story.mainThumbnail else story.bannerImage
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

            // Dark Bottom Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.5f to Color.Transparent,
                                1.0f to Color(0x99000000)
                            )
                        )
                    )
            )

            // Translucent Chat Volume Badge with custom vector message icon
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
                        text = story.chatCount,
                        color = TextCrispWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Story Title
        Text(
            text = story.title,
            color = TextCrispWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Subtitle (Roleplay · Genre)
        Text(
            text = "Roleplay · ${story.genre}",
            color = Color(0xFFA1A1A1),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
