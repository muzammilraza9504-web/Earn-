package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.DailyOpeningBonusDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobBannerAd
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserWalletEntity
import com.example.ui.components.TopRewardHeader
import com.example.ui.theme.AmazonOrange
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.FlipkartBlue
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.PayPalBlue
import com.example.ui.theme.PlayStoreBlue
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UpiOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val STREAK_DAYS = listOf(
    Pair(1, 20),
    Pair(2, 30),
    Pair(3, 40),
    Pair(4, 50),
    Pair(5, 60),
    Pair(6, 80),
    Pair(7, 100)
)

@Composable
fun HomeScreen(
    wallet: UserWalletEntity,
    recentTransactions: List<TransactionEntity>,
    onNavigateToAds: () -> Unit,
    onNavigateToSpinner: () -> Unit,
    onNavigateToRedeem: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onClaimCheckIn: (dayIndex: Int, coins: Int) -> Unit,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val hasCheckedInToday = wallet.lastCheckInDate == todayStr

    var showDailyBonusDialog by remember { mutableStateOf(true) }
    if (showDailyBonusDialog && !hasCheckedInToday) {
        val todayCoins = STREAK_DAYS.find { it.first == wallet.dailyStreak }?.second ?: 50
        DailyOpeningBonusDialog(
            dayIndex = wallet.dailyStreak,
            coins = todayCoins,
            onClaim = {
                showDailyBonusDialog = false
                onClaimCheckIn(wallet.dailyStreak, todayCoins)
            },
            onDismiss = { showDailyBonusDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            // Main Top Wallet Header with Coins & ₹ Progress
            TopRewardHeader(
                wallet = wallet,
                onWithdrawClick = onNavigateToRedeem,
                onSignOutClick = onSignOut
            )
        }

        // Daily Check-in Streak Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("daily_streak_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Daily Bonus",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "7-Day Daily Streak Rewards",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        if (hasCheckedInToday) {
                            Text(
                                text = "✓ Claimed Today",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal Scrollable Day Cards
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        STREAK_DAYS.forEach { (day, bonusCoins) ->
                            val isClaimed = day < wallet.dailyStreak || (day == wallet.dailyStreak && hasCheckedInToday)
                            val isTodayActive = day == wallet.dailyStreak && !hasCheckedInToday

                            Box(
                                modifier = Modifier
                                    .width(68.dp)
                                    .background(
                                        if (isTodayActive) {
                                            PrimaryPurple
                                        } else if (isClaimed) {
                                            Color(0xFF1E3A2F)
                                        } else {
                                            DarkSurface
                                        },
                                        RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isTodayActive) GoldAccent else if (isClaimed) EmeraldGreen else Color(0xFF31384F),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = isTodayActive) {
                                        onClaimCheckIn(day, bonusCoins)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Day $day",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isTodayActive) Color.White else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isClaimed) "✓" else "🪙",
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+$bonusCoins",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isClaimed) EmeraldGreenLight else GoldAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Feature Banner: 1 to 20 Lucky Spinner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Banner 2: 1 to 20 Lucky Spinner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSpinner() }
                        .testTag("home_spinner_banner"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF42280A), Color(0xFF2E1A05))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(GoldAccentDark, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Casino,
                                        contentDescription = "Lucky Wheel",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "1 se 20 Lucky Spinner",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "1 Rewarded Ad = 1 Spin • Win up to 50 Coins",
                                        fontSize = 12.sp,
                                        color = GoldAccent
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Google AdMob Test Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                AdMobBannerAd()
            }
        }

        // Redeem Payout Options Preview (UPI & Play Store Code)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("home_redeem_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Redeem Methods (Min 10,000 Coins)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "Instant Payouts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenLight
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gateway showcase cards
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // UPI Box
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .background(Color(0xFF26180B), RoundedCornerShape(14.dp))
                                .border(1.dp, UpiOrange.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { onNavigateToRedeem() }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "⚡ Instant UPI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = UpiOrange
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "GPay / PhonePe / Paytm",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "10,000 Coins = ₹100",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Flipkart Box
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .background(Color(0xFF0C1B38), RoundedCornerShape(14.dp))
                                .border(1.dp, FlipkartBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { onNavigateToRedeem() }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "🛍️ Flipkart Card",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = FlipkartBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Voucher PIN on Email",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "10,000 Coins = ₹100",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Amazon Box
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .background(Color(0xFF2B1A0A), RoundedCornerShape(14.dp))
                                .border(1.dp, AmazonOrange.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { onNavigateToRedeem() }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "📦 Amazon Pay",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AmazonOrange
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Gift Card via Email",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "10,000 Coins = ₹100",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Play Store Box
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .background(Color(0xFF0C2436), RoundedCornerShape(14.dp))
                                .border(1.dp, PlayStoreBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { onNavigateToRedeem() }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "🎮 Google Play",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = PlayStoreBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Code via Email",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "10,000 Coins = ₹100",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // PayPal Box
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .background(Color(0xFF0A1C33), RoundedCornerShape(14.dp))
                                .border(1.dp, PayPalBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { onNavigateToRedeem() }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "🌐 PayPal Cash",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = PayPalBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "USD / INR Payout",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "10,000 Coins = $1",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurpleLight,
                    modifier = Modifier.clickable { onNavigateToHistory() }
                )
            }
        }

        // Top 3 Recent transactions preview
        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions yet. Watch ads or spin the wheel to earn coins!",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(recentTransactions.take(4).size) { index ->
                val tx = recentTransactions[index]
                TransactionRowItem(
                    transaction = tx,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    modifier: Modifier = Modifier
) {
    val isEarning = transaction.amount > 0
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isEarning) Color(0xFF133628) else Color(0xFF3B1E1E),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (transaction.type) {
                            TransactionType.WATCH_AD -> "🎬"
                            TransactionType.SPINNER_WIN -> "🎡"
                            TransactionType.DAILY_BONUS -> "🎁"
                            TransactionType.REDEEM_UPI -> "⚡"
                            TransactionType.REDEEM_FLIPKART -> "🛍️"
                            TransactionType.REDEEM_AMAZON -> "📦"
                            TransactionType.REDEEM_PLAY_STORE -> "🎮"
                            TransactionType.REDEEM_PAYPAL -> "🌐"
                            TransactionType.REDEEM_GOOGLE_PAY -> "💳"
                        },
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isEarning) "+${transaction.amount} Coins" else "${transaction.amount} Coins",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isEarning) EmeraldGreenLight else Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (transaction.status == com.example.data.model.TransactionStatus.COMPLETED) EmeraldGreen else UpiOrange
                )
            }
        }
    }
}
