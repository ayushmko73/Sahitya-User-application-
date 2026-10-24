package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.BottomTab
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.SahityaGold
import com.example.ui.theme.SahityaGoldBright
import com.example.ui.theme.SahityaRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextCrispWhite
import com.example.ui.theme.TextMutedGray
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom Hexagon Badge featuring the letter "S" representing Sahitya Coins
 */
@Composable
fun SCoinHexagonBadge(
    size: Dp = 28.dp,
    borderWidth: Dp = 1.5.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .testTag("scoin_hexagon_badge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = this.size.width
            val height = this.size.height
            val centerX = width / 2f
            val centerY = height / 2f
            val radius = width.coerceAtMost(height) / 2f - borderWidth.toPx()

            val hexPath = Path()
            for (i in 0 until 6) {
                val angleRad = Math.toRadians((60 * i - 30).toDouble())
                val x = (centerX + radius * cos(angleRad)).toFloat()
                val y = (centerY + radius * sin(angleRad)).toFloat()
                if (i == 0) {
                    hexPath.moveTo(x, y)
                } else {
                    hexPath.lineTo(x, y)
                }
            }
            hexPath.close()

            // Fill with subtle gold radial glow
            drawPath(
                path = hexPath,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF382A00), Color(0xFF141000))
                )
            )

            // Gold Stroke Border
            drawPath(
                path = hexPath,
                brush = Brush.linearGradient(
                    colors = listOf(SahityaGoldBright, SahityaGold, Color(0xFF997A00))
                ),
                style = Stroke(width = borderWidth.toPx())
            )
        }

        Text(
            text = "S",
            color = SahityaGoldBright,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.55f).sp,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
    }
}

/**
 * Top Bar featuring "sahitya" lowercase title and S-Coin counter
 */
@Composable
fun SahityaTopBar(
    sCoinBalance: Int,
    onCoinClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title in Times New Roman (Serif) typography
        Text(
            text = "Sahitya",
            color = TextCrispWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            letterSpacing = 0.sp,
            modifier = Modifier.testTag("app_title_sahitya")
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Profile Avatar Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(1.dp, CardBorderDark, CircleShape)
                    .clickable { onProfileClick() }
                    .testTag("topbar_profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Settings",
                    tint = TextCrispWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Bottom Navigation Bar attached to screen edges with curved top corners and black background
 */
@Composable
fun SahityaBottomNav(
    currentTab: BottomTab,
    unreadMessagesCount: Int = 3,
    onTabSelected: (BottomTab) -> Unit
) {
    val topCurvedShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(topCurvedShape)
            .border(width = 1.dp, color = CardBorderDark, shape = topCurvedShape),
        color = PitchBlack
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home
            BottomNavItem(
                label = "Home",
                isSelected = currentTab == BottomTab.HOME,
                activeIcon = { Icon(Icons.Filled.Home, contentDescription = "Home", tint = SahityaRed) },
                inactiveIcon = { Icon(Icons.Outlined.Home, contentDescription = "Home", tint = TextMutedGray) },
                onClick = { onTabSelected(BottomTab.HOME) },
                testTag = "nav_tab_home"
            )

            // Tab 2: Search
            BottomNavItem(
                label = "Search",
                isSelected = currentTab == BottomTab.SEARCH,
                activeIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = SahityaRed) },
                inactiveIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = TextMutedGray) },
                onClick = { onTabSelected(BottomTab.SEARCH) },
                testTag = "nav_tab_search"
            )

            // Tab 3: Chats (with unread badge)
            BottomNavItem(
                label = "Chats",
                isSelected = currentTab == BottomTab.CHATS,
                activeIcon = {
                    BadgedBox(badge = {
                        if (unreadMessagesCount > 0) {
                            Badge(containerColor = SahityaRed, contentColor = Color.White) {
                                Text("$unreadMessagesCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Send, contentDescription = "Chats", tint = SahityaRed)
                    }
                },
                inactiveIcon = {
                    BadgedBox(badge = {
                        if (unreadMessagesCount > 0) {
                            Badge(containerColor = SahityaRed, contentColor = Color.White) {
                                Text("$unreadMessagesCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }) {
                        Icon(Icons.Outlined.Send, contentDescription = "Chats", tint = TextMutedGray)
                    }
                },
                onClick = { onTabSelected(BottomTab.CHATS) },
                testTag = "nav_tab_chats"
            )

            // Tab 4: Store (Hexagon S-Coin icon)
            BottomNavItem(
                label = "Store",
                isSelected = currentTab == BottomTab.STORE,
                activeIcon = {
                    Box(contentAlignment = Alignment.Center) {
                        SCoinHexagonBadge(size = 24.dp)
                    }
                },
                inactiveIcon = {
                    Box(contentAlignment = Alignment.Center) {
                        SCoinHexagonBadge(size = 22.dp)
                    }
                },
                onClick = { onTabSelected(BottomTab.STORE) },
                testTag = "nav_tab_store"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    isSelected: Boolean,
    activeIcon: @Composable () -> Unit,
    inactiveIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        if (isSelected) activeIcon() else inactiveIcon()
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) SahityaRed else TextMutedGray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun DailyLoginRewardDialog(
    rewardCoins: Int,
    onClaim: () -> Unit
) {
    Dialog(onDismissRequest = onClaim) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark)
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(SahityaGoldBright, SahityaRed)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
                .testTag("daily_login_reward_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2B2005))
                        .border(1.5.dp, SahityaGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    SCoinHexagonBadge(size = 38.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DAILY LOGIN REWARD 🎁",
                    color = SahityaGoldBright,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "+$rewardCoins S-Coins",
                    color = TextCrispWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Welcome back to Sahitya! You've unlocked your daily login bonus S-Coins to continue roleplaying with your favorite characters.",
                    color = TextMutedGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Claim Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(SahityaRed, Color(0xFFC02A38))
                            )
                        )
                        .clickable { onClaim() }
                        .padding(vertical = 14.dp)
                        .testTag("btn_claim_daily_login_reward"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CLAIM REWARD",
                        color = TextCrispWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationPromptDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted || NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            onDismiss()
        }
    }

    // Auto-dismiss if notifications are already enabled or become enabled on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    onDismiss()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            onDismiss()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark)
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(SahityaRed, CardBorderDark)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
                .testTag("notification_prompt_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Bell Icon Header
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C1217))
                        .border(1.5.dp, SahityaRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = SahityaRed,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Enable Notifications 🔔",
                    color = TextCrispWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Stay updated on new story releases, character messages, and daily bonus S-Coin top-ups!",
                    color = TextMutedGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Turn On Notifications Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(SahityaRed, Color(0xFFC02A38))
                            )
                        )
                        .clickable {
                            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                                onDismiss()
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val intent = Intent().apply {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    } else {
                                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        .padding(vertical = 14.dp)
                        .testTag("btn_turn_on_notifications"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TURN ON NOTIFICATIONS",
                        color = TextCrispWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Dismiss Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_dismiss_notification_prompt")
                ) {
                    Text(
                        text = "Not Now",
                        color = TextMutedGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
