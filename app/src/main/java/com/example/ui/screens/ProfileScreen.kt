package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SCoinHexagonBadge
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.SahityaGold
import com.example.ui.theme.SahityaRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextCrispWhite
import com.example.ui.theme.TextMutedGray

data class SettingsRowItem(
    val title: String,
    val icon: ImageVector,
    val isDanger: Boolean = false
)

@Composable
fun ProfileScreen(
    userPhoneNumber: String = "+91 98765 43210",
    onNavigateToStore: () -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val settingsRows = listOf(
        SettingsRowItem("Profile Details", Icons.Default.Person),
        SettingsRowItem("Purchase History", Icons.Default.Receipt),
        SettingsRowItem("Message History", Icons.Default.History),
        SettingsRowItem("Support & Helpdesk", Icons.Default.Support),
        SettingsRowItem("FAQ & Guidance", Icons.Default.Help),
        SettingsRowItem("Terms of Service", Icons.Default.Shield),
        SettingsRowItem("Privacy Policy", Icons.Default.Lock),
        SettingsRowItem("Account Setting", Icons.Default.Settings),
        SettingsRowItem("Log out", Icons.Default.ExitToApp, isDanger = true)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .testTag("profile_screen_container"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. User Account Summary Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_account_header"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PitchBlack)
                            .border(2.dp, SahityaRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Avatar",
                            tint = TextCrispWhite,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ayush Kumar",
                            color = TextCrispWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ID: #SAHITYA-8842",
                            color = SahityaGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (userPhoneNumber.isNotBlank()) userPhoneNumber else "+91 98765 43210",
                            color = TextMutedGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 2. Settings Rows List
        items(settingsRows) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(18.dp))
                    .clickable {
                        if (row.title == "Purchase History") {
                            onNavigateToStore()
                        } else if (row.title == "Log out") {
                            onLogoutClick()
                        } else {
                            Toast
                                .makeText(context, "${row.title} selected", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    .padding(16.dp)
                    .testTag("settings_row_${row.title.replace(" ", "_").lowercase()}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = row.icon,
                        contentDescription = row.title,
                        tint = if (row.isDanger) SahityaRed else TextCrispWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = row.title,
                        color = if (row.isDanger) SahityaRed else TextCrispWhite,
                        fontSize = 15.sp,
                        fontWeight = if (row.isDanger) FontWeight.Bold else FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Navigate",
                    tint = TextMutedGray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // 3. Footer Section
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sahitya AI Roleplay v2.4.0-build.108",
                    color = TextMutedGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Android 14 (API 34)  •  Engine: Gemini 3.5 Flash",
                    color = TextMutedGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Made with ❤️ in India",
                    color = SahityaGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
