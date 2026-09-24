package com.example.ui.screens

import android.app.Activity
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobBannerAd
import com.example.ads.AdMobManager
import com.example.data.model.UserWalletEntity
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WheelColors
import com.example.ui.viewmodel.SAMPLE_ADS
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpinnerScreen(
    wallet: UserWalletEntity,
    isSpinning: Boolean,
    targetRotation: Float,
    lastWonNumber: Int?,
    pendingSpinReward: Pair<Int, Int>?,
    spinCycleCount: Int,
    onSpinClick: () -> Unit,
    onClaimSpinRewardWithAd: (wonNumber: Int, wonCoins: Int) -> Unit,
    onDismissPendingReward: () -> Unit,
    onWatchAdForSpins: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isLoadingAd by remember { mutableStateOf(false) }

    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(
            durationMillis = 3500,
            easing = FastOutSlowInEasing
        ),
        label = "wheel_spin_animation"
    )

    val remainingSpins = (10 - wallet.dailySpinsUsed).coerceAtLeast(0)

    fun handleClaimWithRewardedAd(wonNumber: Int, wonCoins: Int) {
        if (activity != null) {
            isLoadingAd = true
            Toast.makeText(context, "Loading video reward...", Toast.LENGTH_SHORT).show()
            AdMobManager.showRewardedAd(
                activity = activity,
                onRewardEarned = { _, _ ->
                    isLoadingAd = false
                    onClaimSpinRewardWithAd(wonNumber, wonCoins)
                },
                onAdDismissed = {
                    isLoadingAd = false
                    onClaimSpinRewardWithAd(wonNumber, wonCoins)
                },
                onAdFailed = {
                    isLoadingAd = false
                    onClaimSpinRewardWithAd(wonNumber, wonCoins)
                }
            )
        } else {
            onClaimSpinRewardWithAd(wonNumber, wonCoins)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .testTag("spinner_screen"),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Header Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = "Wheel",
                    tint = GoldAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "1 se 20 Lucky Spinner",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }

            Text(
                text = "Spin the wheel numbered 1 to 20 & win guaranteed real coins!",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            // Spin Wheel Container with Pointer
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Outer Glow
                Box(
                    modifier = Modifier
                        .size(310.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(PrimaryPurple.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Rotating 20-Segment Canvas Wheel (Hardware accelerated GPU layer)
                Canvas(
                    modifier = Modifier
                        .size(280.dp)
                        .graphicsLayer {
                            rotationZ = animatedRotation
                        }
                        .shadow(8.dp, CircleShape)
                        .testTag("lucky_wheel_canvas")
                ) {
                    drawLuckyWheel(wheelTextPaint)
                }

                // Center Golden Hub Cap
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(GoldAccent, GoldAccentDark)
                            ),
                            shape = CircleShape
                        )
                        .border(3.dp, GoldGlow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Spin",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Top Pointer Arrow pointing DOWN at the wheel
                Canvas(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.TopCenter)
                        .padding(top = 2.dp)
                ) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, size.height)
                        lineTo(size.width * 0.2f, 0f)
                        lineTo(size.width * 0.8f, 0f)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFFF4500))
                        )
                    )
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Real-time Landed Number Badge (Exact number matching the wheel pointer)
            if (lastWonNumber != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2846)),
                    border = BorderStroke(1.5.dp, GoldAccent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Landed",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Pointer Landed On:",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Lucky Slot #$lastWonNumber",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldAccent
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(EmeraldGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, EmeraldGreen, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+$lastWonNumber COINS WON",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3-Spins Round Indicator (Spin 1: Instant, Spin 2: Instant, Spin 3: Rewarded Ad Pop-up)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Cycle",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3-Spin Cycle Tracker",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Spin ${if (spinCycleCount == 0) 1 else spinCycleCount} of 3",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Step Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 1..3) {
                            val isCompleted = i < spinCycleCount
                            val isCurrent = i == spinCycleCount || (spinCycleCount == 0 && i == 1)
                            val isAdSpin = i == 3

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        when {
                                            isCompleted -> EmeraldGreen.copy(alpha = 0.25f)
                                            isCurrent -> PrimaryPurple.copy(alpha = 0.4f)
                                            isAdSpin -> GoldAccent.copy(alpha = 0.2f)
                                            else -> Color(0xFF1E2438)
                                        },
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        when {
                                            isCompleted -> EmeraldGreen
                                            isCurrent -> PrimaryPurpleLight
                                            isAdSpin -> GoldAccent
                                            else -> Color(0xFF333D5C)
                                        },
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Done",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else if (isAdSpin) {
                                        Icon(
                                            imageVector = Icons.Default.Videocam,
                                            contentDescription = "Rewarded Ad",
                                            tint = GoldAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Instant",
                                            tint = if (isCurrent) PrimaryPurpleLight else TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Spin $i",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent || isCompleted) TextPrimary else TextSecondary
                                    )
                                    Text(
                                        text = if (isAdSpin) "Rewarded Ad" else "Instant Win",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isAdSpin) GoldAccent else if (isCompleted) EmeraldGreen else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Spins remaining Info Badge
            Row(
                modifier = Modifier
                    .background(Color(0x33000000), RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Spins",
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Daily Free Spins: $remainingSpins / 10 Remaining",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spin CTA Button (No cooldown delay!)
            Button(
                onClick = onSpinClick,
                enabled = !isSpinning && !isLoadingAd && remainingSpins > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("spin_wheel_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccentDark,
                    disabledContainerColor = Color(0xFF2E334D),
                    disabledContentColor = Color(0xFF64748B)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                if (isSpinning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SPINNING 1-20 WHEEL...",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                } else if (remainingSpins <= 0) {
                    Text(
                        text = "NO SPINS LEFT TODAY",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                } else {
                    val currentStep = (spinCycleCount % 3) + 1
                    Icon(
                        imageVector = if (currentStep == 3) Icons.Default.Videocam else Icons.Default.PlayArrow,
                        contentDescription = "Spin",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (currentStep) {
                            1 -> "SPIN (1/3) • INSTANT WIN"
                            2 -> "SPIN (2/3) • INSTANT WIN"
                            else -> "SPIN (3/3) • REWARD AD BONUS"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            if (remainingSpins <= 3) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onWatchAdForSpins,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("watch_ad_for_spins_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryPurpleLight
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video Ad",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Watch 1 Video Ad for Free Bonus Spins",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Google AdMob Test Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AdMobBannerAd()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prize Table Explainer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Prizes",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1 se 20 Spinner Prize Chart",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PrizeChip(slot = "Slot #20", coins = "20 Coins", isJackpot = true)
                        PrizeChip(slot = "Slot #15", coins = "15 Coins")
                        PrizeChip(slot = "Slot #10", coins = "10 Coins")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PrizeChip(slot = "Slot #5", coins = "5 Coins")
                        PrizeChip(slot = "Slot #1", coins = "1 Coin")
                        PrizeChip(slot = "Any Slot #N", coins = "N Coins (1:1)")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "✨ 100% Fair & Transparent: Pointer par jo number aayega, utne hi exact coins aapke wallet mein add honge! (Slot #1 to #20 = 1 to 20 Coins).",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // Post-Spin Rewarded Ad Claim Dialog
    pendingSpinReward?.let { (wonNumber, wonCoins) ->
        AlertDialog(
            onDismissRequest = onDismissPendingReward,
            containerColor = DarkSurfaceElevated,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Reward",
                        tint = GoldAccent,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "3rd Spin Bonus (#$wonNumber)!",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "3rd Spin Round Reward",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+$wonCoins COINS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🎬 You reached the 3rd spin milestone! Watch 1 quick bonus video to claim these coins into your balance!",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        handleClaimWithRewardedAd(wonNumber, wonCoins)
                    },
                    enabled = !isLoadingAd,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccentDark,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("claim_spin_reward_ad_button")
                ) {
                    if (isLoadingAd) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading Video...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Watch Video",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WATCH VIDEO & CLAIM (+$wonCoins)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissPendingReward) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun PrizeChip(
    slot: String,
    coins: String,
    isJackpot: Boolean = false
) {
    Box(
        modifier = Modifier
            .background(
                if (isJackpot) Color(0xFF4A154B) else Color(0xFF1E2438),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (isJackpot) GoldAccent else Color(0xFF333D5C),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = slot,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isJackpot) GoldAccent else TextPrimary
            )
            Text(
                text = coins,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldGreen
            )
        }
    }
}

private val wheelTextPaint = Paint().apply {
    color = android.graphics.Color.WHITE
    textSize = 28f
    isFakeBoldText = true
    textAlign = Paint.Align.CENTER
    setShadowLayer(3f, 0f, 1.5f, android.graphics.Color.BLACK)
}

private fun DrawScope.drawLuckyWheel(textPaint: Paint) {
    val totalSlices = 20
    val sweepAngle = 360f / totalSlices.toFloat() // 18 degrees per slice
    val radius = size.minDimension / 2f
    val centerOffset = Offset(size.width / 2f, size.height / 2f)

    // 1. Draw Outer Gold Rim
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFDF00), Color(0xFFB8860B)),
            center = centerOffset,
            radius = radius
        ),
        radius = radius,
        center = centerOffset
    )

    // Inner wheel radius
    val innerRadius = radius - 8.dp.toPx()

    // 2. Draw 20 Slices
    for (i in 0 until totalSlices) {
        val startAngle = i * sweepAngle - 90f // Start top
        val color = WheelColors[i % WheelColors.size]

        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(centerOffset.x - innerRadius, centerOffset.y - innerRadius),
            size = Size(innerRadius * 2f, innerRadius * 2f),
            style = Fill
        )

        // Draw Slice Divider lines
        val angleRad = Math.toRadians(startAngle.toDouble())
        val lineEnd = Offset(
            (centerOffset.x + innerRadius * cos(angleRad)).toFloat(),
            (centerOffset.y + innerRadius * sin(angleRad)).toFloat()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = centerOffset,
            end = lineEnd,
            strokeWidth = 1.5.dp.toPx()
        )

        // Draw Number (1 to 20) on each slice
        val sliceNumber = (i + 1).toString()
        val textAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
        val textRadius = innerRadius * 0.72f
        val textX = (centerOffset.x + textRadius * cos(textAngleRad)).toFloat()
        val textY = (centerOffset.y + textRadius * sin(textAngleRad)).toFloat()

        drawContext.canvas.nativeCanvas.apply {
            save()
            // Rotate canvas to align text towards center
            val textAngleDeg = (startAngle + sweepAngle / 2f) + 90f
            rotate(textAngleDeg, textX, textY)
            drawText(sliceNumber, textX, textY + 9f, textPaint)
            restore()
        }
    }

    // 3. Draw Outer rim decorative LED dots
    val dotCount = 20
    for (d in 0 until dotCount) {
        val dotAngleRad = Math.toRadians((d * (360f / dotCount)).toDouble())
        val dotRadius = radius - 4.dp.toPx()
        val dotX = (centerOffset.x + dotRadius * cos(dotAngleRad)).toFloat()
        val dotY = (centerOffset.y + dotRadius * sin(dotAngleRad)).toFloat()

        drawCircle(
            color = if (d % 2 == 0) Color.White else Color(0xFFFFD700),
            radius = 2.5.dp.toPx(),
            center = Offset(dotX, dotY)
        )
    }

    // Inner rim stroke
    drawCircle(
        color = Color(0x44FFFFFF),
        radius = innerRadius,
        center = centerOffset,
        style = Stroke(width = 2.dp.toPx())
    )
}
