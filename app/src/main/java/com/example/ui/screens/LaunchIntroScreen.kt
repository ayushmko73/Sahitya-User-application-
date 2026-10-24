package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CharacterStoryEntity
import com.example.ui.theme.SahityaGoldBright
import com.example.ui.theme.SahityaRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class IntroThumbnailItem(
    val id: String,
    val title: String,
    val genre: String,
    val imageResId: Int,
    val overlayColorStart: Color,
    val overlayColorEnd: Color
)

private val defaultTop20Templates = listOf(
    IntroThumbnailItem("s1", "My Hacker Wife", "Drama", R.drawable.img_hero_banner_1786271312636, Color(0xFF800020), Color(0xFF1A0008)),
    IntroThumbnailItem("s2", "The Boy I Hurt", "Romance", R.drawable.img_genre_drama_1786271332993, Color(0xFF3D0C1E), Color(0xFF0F0307)),
    IntroThumbnailItem("s3", "Ronin Zero: Crimson Blade", "Fantasy", R.drawable.img_genre_fantasy_1786271322868, Color(0xFF1E3A8A), Color(0xFF0A1128)),
    IntroThumbnailItem("s4", "The Crime Lord's Daughter", "Horror", R.drawable.img_genre_horror_1786271344414, Color(0xFF4A0E17), Color(0xFF120306)),
    IntroThumbnailItem("s5", "She Hates Me", "Drama", R.drawable.img_genre_drama_1786271332993, Color(0xFF2E003E), Color(0xFF0D0012)),
    IntroThumbnailItem("s6", "Prince Karan: Blood Oath", "Romance", R.drawable.img_genre_fantasy_1786271322868, Color(0xFF500000), Color(0xFF140000)),
    IntroThumbnailItem("s7", "Cyberpunk Merc: Kiara", "Anime", R.drawable.img_hero_banner_1786271312636, Color(0xFF003E33), Color(0xFF001410)),
    IntroThumbnailItem("s8", "Regalwood Secrets", "Drama", R.drawable.img_genre_drama_1786271332993, Color(0xFF3B1F2B), Color(0xFF12090E))
)

private val genreOverlayGradients = listOf(
    Pair(Color(0xFF800020), Color(0xFF1A0008)),
    Pair(Color(0xFF3D0C1E), Color(0xFF0F0307)),
    Pair(Color(0xFF1E3A8A), Color(0xFF0A1128)),
    Pair(Color(0xFF4A0E17), Color(0xFF120306)),
    Pair(Color(0xFF2E003E), Color(0xFF0D0012)),
    Pair(Color(0xFF500000), Color(0xFF140000)),
    Pair(Color(0xFF1C3144), Color(0xFF0A131A)),
    Pair(Color(0xFF3B1F2B), Color(0xFF12090E))
)

fun getIntroImageRes(imageType: String): Int {
    return when (imageType) {
        "hero" -> R.drawable.img_hero_banner_1786271312636
        "fantasy" -> R.drawable.img_genre_fantasy_1786271322868
        "drama" -> R.drawable.img_genre_drama_1786271332993
        "horror" -> R.drawable.img_genre_horror_1786271344414
        else -> R.drawable.img_hero_banner_1786271312636
    }
}

/**
 * Builds the Top 20 Trending story thumbnail list from app database stories.
 * If stories list is empty or has fewer than 20 stories, cycles them to form
 * a complete Top 20 list.
 */
fun buildTop20TrendingItems(stories: List<CharacterStoryEntity>): List<IntroThumbnailItem> {
    val items = if (stories.isNotEmpty()) {
        stories.mapIndexed { index, story ->
            val gradient = genreOverlayGradients[index % genreOverlayGradients.size]
            IntroThumbnailItem(
                id = story.id,
                title = story.title,
                genre = story.genre,
                imageResId = getIntroImageRes(story.imageType),
                overlayColorStart = gradient.first,
                overlayColorEnd = gradient.second
            )
        }
    } else {
        defaultTop20Templates
    }

    val result = mutableListOf<IntroThumbnailItem>()
    while (result.size < 20) {
        result.addAll(items)
    }
    return result.take(20)
}

/**
 * Sahitya Application Launch Intro Sequence
 *
 * Smooth Infinite Rolling Intro Animation (4.0-second total duration):
 * 1. Dual side-by-side rolling columns of Top 20 Trending story thumbnails scrolling in OPPOSITE directions:
 *    - Left Column scrolls UP continuously
 *    - Right Column scrolls DOWN continuously
 * 2. At 0.8s, "Sahitya" title pops up elegantly in center with sleek compact glass card backdrop.
 * 3. At 3.3s, dissolves out smoothly over 700ms, completing at exactly 4.0s (4000ms).
 */
@Composable
fun LaunchIntroScreen(
    stories: List<CharacterStoryEntity> = emptyList(),
    onIntroFinished: () -> Unit
) {
    val expansionProgressAnim = remember { Animatable(0f) }
    val titleScaleAnim = remember { Animatable(0.6f) }
    val titleAlphaAnim = remember { Animatable(0f) }
    val screenDissolveAlphaAnim = remember { Animatable(1f) }

    val top20Items = remember(stories) {
        buildTop20TrendingItems(stories)
    }

    val staggeredRightItems = remember(top20Items) {
        if (top20Items.size > 1) {
            val half = top20Items.size / 2
            top20Items.drop(half) + top20Items.take(half)
        } else {
            top20Items
        }
    }

    // Lazy list states initialized deep into infinite list (index 10000)
    val leftListState = rememberLazyListState(initialFirstVisibleItemIndex = 10000)
    val rightListState = rememberLazyListState(initialFirstVisibleItemIndex = 10000)

    // Smooth Opposite Rolling Animation Loop using LazyColumn's scroll scope
    LaunchedEffect(top20Items) {
        val scrollSpeedPxPerSec = 140f

        launch {
            leftListState.scroll(scrollPriority = MutatePriority.PreventUserInput) {
                val scrollScope = this
                var lastTime = 0L
                while (isActive) {
                    withFrameNanos { frameTime ->
                        if (lastTime != 0L) {
                            val dt = (frameTime - lastTime) / 1_000_000_000f
                            // Positive delta scrolls DOWN through items (items move UP)
                            scrollScope.scrollBy(scrollSpeedPxPerSec * dt)
                        }
                        lastTime = frameTime
                    }
                }
            }
        }

        launch {
            rightListState.scroll(scrollPriority = MutatePriority.PreventUserInput) {
                val scrollScope = this
                var lastTime = 0L
                while (isActive) {
                    withFrameNanos { frameTime ->
                        if (lastTime != 0L) {
                            val dt = (frameTime - lastTime) / 1_000_000_000f
                            // Negative delta scrolls UP through items (items move DOWN)
                            scrollScope.scrollBy(-scrollSpeedPxPerSec * dt)
                        }
                        lastTime = frameTime
                    }
                }
            }
        }
    }

    // Main 4.0-Second Animation Timeline (Exactly 4000ms)
    LaunchedEffect(Unit) {
        launch {
            expansionProgressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 3300, easing = EaseOutCubic)
            )
        }

        // At 0.8s (800ms), Sahitya brand title pops up
        delay(800)

        launch {
            titleAlphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            titleScaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }

        // At 3.3s (3300ms), dissolve out screen over 700ms to complete at exactly 4.0s (4000ms)
        delay(2500)

        screenDissolveAlphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )

        onIntroFinished()
    }

    val progress = expansionProgressAnim.value
    val currentScale = 0.92f + (0.08f * progress)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080808))
            .alpha(screenDissolveAlphaAnim.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onIntroFinished()
            }
            .testTag("launch_intro_container")
    ) {
        // DUAL SIDE-BY-SIDE ROLLING COLUMNS
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .graphicsLayer {
                    scaleX = currentScale
                    scaleY = currentScale
                },
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // LEFT COLUMN (Scrolling UP continuously)
            LazyColumn(
                state = leftListState,
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("intro_left_column")
            ) {
                items(
                    count = Int.MAX_VALUE,
                    key = { index -> "left_$index" }
                ) { index ->
                    val item = top20Items[index % top20Items.size]
                    SquareThumbnailBannerCard(item = item)
                }
            }

            // RIGHT COLUMN (Scrolling DOWN continuously)
            LazyColumn(
                state = rightListState,
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("intro_right_column")
            ) {
                items(
                    count = Int.MAX_VALUE,
                    key = { index -> "right_$index" }
                ) { index ->
                    val item = staggeredRightItems[index % staggeredRightItems.size]
                    SquareThumbnailBannerCard(item = item)
                }
            }
        }

        // AMBIENT TRANSLUCENT VIGNETTE OVERLAY
        if (titleAlphaAnim.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x99000000),
                                Color(0x44000000),
                                Color(0x11000000)
                            )
                        )
                    )
                    .alpha(titleAlphaAnim.value)
            )

            // BLURRED BACKDROP SCRIM DIRECTLY BEHIND SAHITYA TITLE
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(230.dp)
                    .height(170.dp)
                    .graphicsLayer {
                        scaleX = titleScaleAnim.value
                        scaleY = titleScaleAnim.value
                        alpha = titleAlphaAnim.value
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .blur(20.dp)
                    .background(Color(0xEE050505))
            )
        }

        // BRAND TITLE "Sahitya" APPEARS IN CENTER WITH COMPACT SLEEK GLASS CONTAINER
        if (titleAlphaAnim.value > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = titleScaleAnim.value
                        scaleY = titleScaleAnim.value
                        alpha = titleAlphaAnim.value
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xCC0D0D0D))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0x88FFFFFF),
                                    Color(0x33FFFFFF),
                                    SahityaGoldBright.copy(alpha = 0.5f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(2.5.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(SahityaRed, SahityaGoldBright, SahityaRed)
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Sahitya",
                            color = Color(0xFFFFFFFF),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = (-0.5).sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("intro_brand_title_sahitya")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "STORIES · ROLEPLAY",
                            color = Color(0xDDFFFFFF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(2.5.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(SahityaRed, SahityaGoldBright, SahityaRed)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1:1 Square Banner Thumbnail Card component used in the dual rolling columns
 */
@Composable
private fun SquareThumbnailBannerCard(
    item: IntroThumbnailItem
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF161616))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
    ) {
        Image(
            painter = painterResource(id = item.imageResId),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Tinted overlay gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            item.overlayColorStart.copy(alpha = 0.25f),
                            item.overlayColorEnd.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Bottom label details
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SahityaRed.copy(alpha = 0.9f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = item.genre.uppercase(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = item.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
