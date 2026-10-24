package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.data.UserWalletEntity
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

data class CoinPack(
    val title: String,
    val coinAmount: Int,
    val bonusTag: String,
    val priceInr: String,
    val isBestValue: Boolean = false
)

@Composable
fun StoreScreen(
    wallet: UserWalletEntity?,
    transactions: List<TransactionEntity>,
    onClaimDailyBonus: () -> Unit,
    onPurchasePack: (coins: Int, priceInr: String, title: String) -> Unit
) {
    val currentBalance = wallet?.sCoins ?: 350
    val lastClaimDate = wallet?.lastClaimDate ?: 0L
    val isClaimAvailable = (System.currentTimeMillis() - lastClaimDate) > 24 * 60 * 60 * 1000L

    val coinPacks = listOf(
        CoinPack("Starter Bundle", 100, "+20 Bonus", "₹29"),
        CoinPack("Popular Pack", 300, "200% BONUS + AD FREE", "₹79", isBestValue = true),
        CoinPack("Pro Roleplayer", 1000, "+400 Bonus Coins", "₹119"),
        CoinPack("Mythic Vault", 2500, "+1200 Bonus Coins", "₹299"),
        CoinPack("Emperor Chest", 6000, "+3000 Bonus + VIP Badge", "₹599")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .testTag("store_screen_container"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Balance Header Card with Golden Gradient Glow
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF3B2E05), Color(0xFF1E1700), Color(0xFF0F0B00))
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(SahityaGoldBright, SahityaGold, Color(0xFFB88600))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
                    .testTag("wallet_balance_header")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SAHITYA WALLET",
                                color = SahityaGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SCoinHexagonBadge(size = 36.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "$currentBalance",
                                    color = SahityaGoldBright,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "S-Coins",
                                    color = TextCrispWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Claim Daily Bonus Button
                        Button(
                            onClick = onClaimDailyBonus,
                            enabled = isClaimAvailable,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isClaimAvailable) SahityaRed else CardBorderDark,
                                disabledContainerColor = CardBorderDark
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("claim_daily_bonus_btn")
                        ) {
                            Icon(
                                Icons.Default.ElectricBolt,
                                contentDescription = "Daily Bonus",
                                tint = if (isClaimAvailable) TextCrispWhite else TextMutedGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isClaimAvailable) "Claim +50" else "Claimed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isClaimAvailable) TextCrispWhite else TextMutedGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "• Each AI roleplay turn consumes 2 S-Coins.",
                        color = TextMutedGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 2. Coin Packs Title
        item {
            Text(
                text = "Top-up Coin Packs",
                color = TextCrispWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 3. Coin Packs List
        items(coinPacks) { pack ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPurchasePack(pack.coinAmount, pack.priceInr, pack.title) }
                    .testTag("coin_pack_card_${pack.priceInr}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (pack.isBestValue) 1.5.dp else 1.dp,
                    color = if (pack.isBestValue) SahityaGold else CardBorderDark
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SCoinHexagonBadge(size = 38.dp)

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${pack.coinAmount} S-Coins",
                                    color = TextCrispWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (pack.isBestValue) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SahityaGold)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "BEST VALUE",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = pack.bonusTag,
                                color = SahityaRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { onPurchasePack(pack.coinAmount, pack.priceInr, pack.title) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pack.isBestValue) SahityaGold else SurfaceVariantDark
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("buy_btn_${pack.priceInr}")
                    ) {
                        Text(
                            text = pack.priceInr,
                            color = if (pack.isBestValue) Color.Black else TextCrispWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 4. Purchase History Ledger
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Purchase & Claim History",
                color = TextCrispWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "No past transactions found.",
                    color = TextMutedGray,
                    fontSize = 13.sp
                )
            }
        } else {
            items(transactions) { tx ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag("transaction_row_${tx.id}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = tx.title,
                                color = TextCrispWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tx.dateFormatted,
                                color = TextMutedGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+${tx.coinsAmount} S",
                            color = SahityaGoldBright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF102D1B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tx.status,
                                color = Color(0xFF00E676),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
