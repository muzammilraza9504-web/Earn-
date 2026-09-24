package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdMobManager
import com.example.data.model.UserWalletEntity
import com.example.ui.components.CoinCelebrationDialog
import com.example.ui.components.WithdrawSuccessDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RedeemScreen
import com.example.ui.screens.ReferScreen
import com.example.ui.screens.SpinnerScreen
import com.example.ui.screens.StartAuthScreen
import com.example.ui.screens.WatchAdsScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WatchAndEarnTheme
import com.example.ui.viewmodel.RewardViewModel

enum class AppDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    WATCH_ADS("Watch Ads", Icons.Filled.Videocam, Icons.Outlined.Videocam, "nav_watch_ads"),
    SPINNER("1-20 Spin", Icons.Filled.Casino, Icons.Outlined.Casino, "nav_spinner"),
    REFER("Refer ₹100", Icons.Filled.CardGiftcard, Icons.Outlined.CardGiftcard, "nav_refer"),
    REDEEM("Redeem", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_redeem"),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History, "nav_history")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize Google AdMob SDK
        AdMobManager.initialize(this)

        setContent {
            WatchAndEarnTheme(darkTheme = true) {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: RewardViewModel = viewModel()
) {
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    val isSpinning by viewModel.isSpinning.collectAsStateWithLifecycle()
    val targetRotation by viewModel.targetRotationAngle.collectAsStateWithLifecycle()
    val lastWonNumber by viewModel.lastSpinWonNumber.collectAsStateWithLifecycle()
    val pendingSpinReward by viewModel.pendingSpinReward.collectAsStateWithLifecycle()
    val spinCycleCount by viewModel.spinCycleCount.collectAsStateWithLifecycle()

    val celebrationReward by viewModel.celebrationReward.collectAsStateWithLifecycle()
    val withdrawSuccess by viewModel.withdrawSuccess.collectAsStateWithLifecycle()

    val selectedPayoutMethod by viewModel.selectedPayoutMethod.collectAsStateWithLifecycle()
    val selectedPackage by viewModel.selectedPackage.collectAsStateWithLifecycle()
    val inputTargetAddress by viewModel.inputTargetAddress.collectAsStateWithLifecycle()
    val inputUserName by viewModel.inputUserName.collectAsStateWithLifecycle()
    val redeemErrorMessage by viewModel.redeemErrorMessage.collectAsStateWithLifecycle()
    val isRedeeming by viewModel.isRedeeming.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(redeemErrorMessage) {
        redeemErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearRedeemError()
        }
    }

    val safeWallet = wallet ?: UserWalletEntity()

    if (!safeWallet.isLoggedIn) {
        StartAuthScreen(
            onGoogleSignInSuccess = { name, email, photoUrl ->
                viewModel.signInWithGoogle(name, email, photoUrl)
            }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar")
                ) {
                    AppDestination.entries.forEach { destination ->
                        val isSelected = currentDestination == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = GoldAccent,
                                indicatorColor = PrimaryPurple,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag(destination.testTag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                when (currentDestination) {
                    AppDestination.HOME -> HomeScreen(
                        wallet = safeWallet,
                        recentTransactions = transactions,
                        onNavigateToAds = { currentDestination = AppDestination.WATCH_ADS },
                        onNavigateToSpinner = { currentDestination = AppDestination.SPINNER },
                        onNavigateToRedeem = { currentDestination = AppDestination.REDEEM },
                        onNavigateToHistory = { currentDestination = AppDestination.HISTORY },
                        onClaimCheckIn = { day, coins -> viewModel.claimDailyCheckIn(day, coins) },
                        onSignOut = { viewModel.signOut() }
                    )

                    AppDestination.WATCH_ADS -> WatchAdsScreen(
                        wallet = safeWallet,
                        onWatchAdClicked = { ad -> viewModel.onRewardedAdCompleted(ad) }
                    )

                    AppDestination.SPINNER -> SpinnerScreen(
                        wallet = safeWallet,
                        isSpinning = isSpinning,
                        targetRotation = targetRotation,
                        lastWonNumber = lastWonNumber,
                        pendingSpinReward = pendingSpinReward,
                        spinCycleCount = spinCycleCount,
                        onSpinClick = { viewModel.spinWheel() },
                        onClaimSpinRewardWithAd = { num, coins -> viewModel.claimSpinRewardAfterAd(num, coins) },
                        onDismissPendingReward = { viewModel.dismissPendingSpinReward() },
                        onWatchAdForSpins = { currentDestination = AppDestination.WATCH_ADS }
                    )

                    AppDestination.REFER -> ReferScreen(
                        wallet = safeWallet
                    )

                    AppDestination.REDEEM -> RedeemScreen(
                        wallet = safeWallet,
                        selectedMethod = selectedPayoutMethod,
                        selectedPackage = selectedPackage,
                        targetAddress = inputTargetAddress,
                        userName = inputUserName,
                        errorMessage = null,
                        isRedeeming = isRedeeming,
                        onMethodSelected = { viewModel.setPayoutMethod(it) },
                        onPackageSelected = { viewModel.selectPackage(it) },
                        onTargetAddressChanged = { viewModel.updateTargetAddress(it) },
                        onUserNameChanged = { viewModel.updateUserName(it) },
                        onSubmitRedeem = { viewModel.submitWithdrawal() }
                    )

                    AppDestination.HISTORY -> HistoryScreen(
                        wallet = safeWallet,
                        transactions = transactions
                    )
                }

                // Coin Celebration Burst Dialog
                celebrationReward?.let { reward ->
                    CoinCelebrationDialog(
                        reward = reward,
                        onDismiss = { viewModel.dismissCelebration() }
                    )
                }

                // Payout Success Receipt Dialog
                withdrawSuccess?.let { tx ->
                    WithdrawSuccessDialog(
                        transaction = tx,
                        onDismiss = { viewModel.dismissWithdrawSuccess() }
                    )
                }
            }
        }
    }
}
