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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActiveChatEntity
import com.example.ui.components.cleanChatPreviewText
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.SahityaRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextCrispWhite
import com.example.ui.theme.TextMutedGray

@Composable
fun ChatsScreen(
    activeChats: List<ActiveChatEntity>,
    onChatClick: (ActiveChatEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(16.dp)
            .testTag("chats_screen_container")
    ) {
        Text(
            text = "Active Conversations",
            color = TextCrispWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Continue your interactive AI roleplays",
            color = TextMutedGray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (activeChats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active conversations yet.\nBrowse Home or Search to start a chat!",
                    color = TextMutedGray,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeChats) { chat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChatClick(chat) }
                            .testTag("active_chat_item_${chat.characterId}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(contentAlignment = Alignment.TopEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceVariantDark)
                                ) {
                                    Image(
                                        painter = painterResource(id = getGenreImageRes(chat.avatarType)),
                                        contentDescription = chat.characterName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                if (chat.unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(SahityaRed)
                                            .border(1.5.dp, SurfaceDark, CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Message preview
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = chat.characterName,
                                        color = TextCrispWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Active",
                                        color = SahityaRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = cleanChatPreviewText(chat.lastMessage),
                                    color = TextMutedGray,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
