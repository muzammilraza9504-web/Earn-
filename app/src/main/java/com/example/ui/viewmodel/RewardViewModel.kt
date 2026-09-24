package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.PayoutMethod
import com.example.data.model.RedeemPackage
import com.example.data.model.TransactionEntity
import com.example.data.model.UPI_PACKAGES
import com.example.data.model.UserWalletEntity
import com.example.data.model.getPackagesForMethod
import com.example.data.repository.RewardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class VideoAdItem(
    val id: String,
    val title: String,
    val sponsorName: String,
    val sponsorTagline: String,
    val durationSeconds: Int,
    val rewardCoins: Int,
    val iconName: String,
    val badge: String = "High Reward"
)

val SAMPLE_ADS = listOf(
    VideoAdItem(
        id = "ad_1",
        title = "Top Gaming App Spotlight",
        sponsorName = "Epic Battle Quest 3D",
        sponsorTagline = "Join 10M+ players in the ultimate online arena!",
        durationSeconds = 15,
        rewardCoins = 40,
        iconName = "gamepad",
        badge = "🔥 40 Coins"
    ),
    VideoAdItem(
        id = "ad_2",
        title = "Exclusive Shopping Deals",
        sponsorName = "MegaMart Online Store",
        sponsorTagline = "Get 80% discount on first 3 orders today!",
        durationSeconds = 20,
        rewardCoins = 40,
        iconName = "shopping",
        badge = "💰 40 Coins"
    ),
    VideoAdItem(
        id = "ad_3",
        title = "FinTech & Crypto Wallet",
        sponsorName = "SecurePay Global",
        sponsorTagline = "Instant zero-fee money transfers worldwide.",
        durationSeconds = 30,
        rewardCoins = 40,
        iconName = "wallet",
        badge = "💎 40 Coins"
    ),
    VideoAdItem(
        id = "ad_4",
        title = "AI Photo Studio Pro",
        sponsorName = "PicMagic AI Art",
        sponsorTagline = "Turn selfies into stunning studio portraits instantly.",
        durationSeconds = 15,
        rewardCoins = 40,
        iconName = "photo",
        badge = "⚡ 40 Coins"
    ),
    VideoAdItem(
        id = "ad_5",
        title = "Smart Fitness & Health",
        sponsorName = "FitPulse Tracker",
        sponsorTagline = "Daily workouts, calories & step counter coach.",
        durationSeconds = 25,
        rewardCoins = 40,
        iconName = "fitness",
        badge = "🌟 40 Coins"
    )
)

data class CelebrationReward(
    val coins: Int,
    val title: String,
    val description: String
)

class RewardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RewardRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RewardRepository(database.rewardDao())
        viewModelScope.launch {
            repository.getOrCreateWallet()
        }
    }

    val wallet: StateFlow<UserWalletEntity?> = repository.userWalletFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserWalletEntity()
    )

    val transactions: StateFlow<List<TransactionEntity>> = repository.transactionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Ad Player State
    private val _activeAd = MutableStateFlow<VideoAdItem?>(null)
    val activeAd: StateFlow<VideoAdItem?> = _activeAd.asStateFlow()

    private val _adRemainingSeconds = MutableStateFlow(0)
    val adRemainingSeconds: StateFlow<Int> = _adRemainingSeconds.asStateFlow()

    private val _adProgress = MutableStateFlow(0f)
    val adProgress: StateFlow<Float> = _adProgress.asStateFlow()

    private val _isAdCompleted = MutableStateFlow(false)
    val isAdCompleted: StateFlow<Boolean> = _isAdCompleted.asStateFlow()

    private var adTimerJob: Job? = null

    // Spinner State (1 se 20 Spinner Money)
    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    private val _targetRotationAngle = MutableStateFlow(0f)
    val targetRotationAngle: StateFlow<Float> = _targetRotationAngle.asStateFlow()

    private val _lastSpinWonNumber = MutableStateFlow<Int?>(null)
    val lastSpinWonNumber: StateFlow<Int?> = _lastSpinWonNumber.asStateFlow()

    // Pending reward to be claimed on the 3rd spin via Rewarded Video Ad
    private val _pendingSpinReward = MutableStateFlow<Pair<Int, Int>?>(null)
    val pendingSpinReward: StateFlow<Pair<Int, Int>?> = _pendingSpinReward.asStateFlow()

    // 3-Spins Cycle Tracker (0, 1, 2, 3 where 3 triggers rewarded ad)
    private val _spinCycleCount = MutableStateFlow(0)
    val spinCycleCount: StateFlow<Int> = _spinCycleCount.asStateFlow()

    // Celebration dialog
    private val _celebrationReward = MutableStateFlow<CelebrationReward?>(null)
    val celebrationReward: StateFlow<CelebrationReward?> = _celebrationReward.asStateFlow()

    // Redeem State
    private val _selectedPayoutMethod = MutableStateFlow(PayoutMethod.UPI)
    val selectedPayoutMethod: StateFlow<PayoutMethod> = _selectedPayoutMethod.asStateFlow()

    private val _selectedPackage = MutableStateFlow(UPI_PACKAGES.first())
    val selectedPackage: StateFlow<RedeemPackage> = _selectedPackage.asStateFlow()

    private val _inputTargetAddress = MutableStateFlow("")
    val inputTargetAddress: StateFlow<String> = _inputTargetAddress.asStateFlow()

    private val _inputUserName = MutableStateFlow("")
    val inputUserName: StateFlow<String> = _inputUserName.asStateFlow()

    private val _redeemErrorMessage = MutableStateFlow<String?>(null)
    val redeemErrorMessage: StateFlow<String?> = _redeemErrorMessage.asStateFlow()

    private val _isRedeeming = MutableStateFlow(false)
    val isRedeeming: StateFlow<Boolean> = _isRedeeming.asStateFlow()

    private val _withdrawSuccess = MutableStateFlow<TransactionEntity?>(null)
    val withdrawSuccess: StateFlow<TransactionEntity?> = _withdrawSuccess.asStateFlow()

    // Ad Actions (Rewarded Video Ads)
    fun onRewardedAdCompleted(ad: VideoAdItem) {
        viewModelScope.launch {
            repository.watchAdAndEarn(ad.title, ad.rewardCoins)
            _celebrationReward.value = CelebrationReward(
                coins = ad.rewardCoins,
                title = "Video Reward Earned!",
                description = "You earned +${ad.rewardCoins} coins successfully from the sponsor video!"
            )
        }
    }

    fun onBonusAdWatchedForCoins(coins: Int, title: String = "Bonus Video Ad") {
        viewModelScope.launch {
            repository.watchAdAndEarn(title, coins)
            _celebrationReward.value = CelebrationReward(
                coins = coins,
                title = "Bonus Reward Earned!",
                description = "You earned +$coins coins successfully!"
            )
        }
    }

    fun setAdError(message: String) {
        _redeemErrorMessage.value = message
    }

    // Spinner Action (1 to 20)
    // 20 slices, each slice is 360/20 = 18 degrees
    fun spinWheel() {
        val currentWallet = wallet.value ?: return
        if (_isSpinning.value) return

        // 10 free spins daily, or can watch ad for extra spins
        if (currentWallet.dailySpinsUsed >= 10) {
            _redeemErrorMessage.value = "Daily 10 spins limit reached! Watch a video ad to get bonus spins."
            return
        }

        _isSpinning.value = true
        _lastSpinWonNumber.value = null
        _pendingSpinReward.value = null

        // Pick a number from 1 to 20
        val wonNumber = Random.nextInt(1, 21) // 1 to 20

        // Calculate exact target rotation so the top pointer lands with 100% precision on wonNumber
        // Slice 1 (i=0) center is at -81 deg (9 deg clockwise from top 12 o'clock -90 deg)
        // Rotating clockwise by (360 - (i*18 + 9)) aligns slice i center with the top pointer
        val targetAngle = (360f - ((wonNumber - 1) * 18f + 9f)) % 360f
        val current = _targetRotationAngle.value
        val currentMod = (current % 360f + 360f) % 360f
        var diff = targetAngle - currentMod
        if (diff < 0f) {
            diff += 360f
        }
        val extraRounds = 360f * 6f // 6 smooth rotations
        val finalRotation = current + extraRounds + diff

        _targetRotationAngle.value = finalRotation

        viewModelScope.launch {
            // Spin duration: 3.5 seconds
            delay(3600)
            _isSpinning.value = false
            _lastSpinWonNumber.value = wonNumber

            // Exact 1:1 reward matching: Whatever number appears on the wheel is the exact coin amount won!
            val wonCoins = wonNumber

            val nextStep = (_spinCycleCount.value % 3) + 1
            if (nextStep < 3) {
                // Spin 1 & 2: Direct instant credit without ad popup or delay!
                _spinCycleCount.value = nextStep
                repository.spinWheelAndEarn(wonNumber, wonCoins)

                _celebrationReward.value = CelebrationReward(
                    coins = wonCoins,
                    title = "🎉 Wheel Landed on #$wonNumber!",
                    description = "Exact Number Won: #$wonNumber = +$wonCoins Coins added directly to your wallet!"
                )
            } else {
                // Spin 3: Triggers the Rewarded Ad popup!
                _spinCycleCount.value = 3
                _pendingSpinReward.value = Pair(wonNumber, wonCoins)
            }
        }
    }

    /**
     * Claim the reward coins after watching the AdMob Rewarded Ad on the 3rd spin.
     */
    fun claimSpinRewardAfterAd(wonNumber: Int, wonCoins: Int) {
        viewModelScope.launch {
            _pendingSpinReward.value = null

            // Record and credit reward coins
            repository.spinWheelAndEarn(wonNumber, wonCoins)

            // Reset 3-spin cycle back to 0 for next spins
            _spinCycleCount.value = 0

            // Show Celebration Reward Popup
            _celebrationReward.value = CelebrationReward(
                coins = wonCoins,
                title = "🎉 3rd Spin Bonus Slot #$wonNumber!",
                description = "Rewarded Ad completed! +$wonCoins Coins claimed successfully!"
            )
        }
    }

    fun dismissPendingSpinReward() {
        val pending = _pendingSpinReward.value
        _pendingSpinReward.value = null
        if (pending != null) {
            val (wonNumber, wonCoins) = pending
            viewModelScope.launch {
                repository.spinWheelAndEarn(wonNumber, wonCoins)
                _spinCycleCount.value = 0
                _celebrationReward.value = CelebrationReward(
                    coins = wonCoins,
                    title = "Slot #$wonNumber Landed!",
                    description = "+$wonCoins Coins added to wallet!"
                )
            }
        }
    }

    fun claimDailyCheckIn(dayIndex: Int, coins: Int) {
        viewModelScope.launch {
            val success = repository.claimDailyCheckIn(dayIndex, coins)
            if (success) {
                _celebrationReward.value = CelebrationReward(
                    coins = coins,
                    title = "Day $dayIndex Bonus Claimed!",
                    description = "+$coins Streak Coins added to your wallet!"
                )
            }
        }
    }

    fun setPayoutMethod(method: PayoutMethod) {
        _selectedPayoutMethod.value = method
        val availablePackages = getPackagesForMethod(method)
        _selectedPackage.value = availablePackages.first()
        _redeemErrorMessage.value = null
    }

    fun selectPackage(pkg: RedeemPackage) {
        _selectedPackage.value = pkg
        _redeemErrorMessage.value = null
    }

    fun updateTargetAddress(address: String) {
        _inputTargetAddress.value = address
        _redeemErrorMessage.value = null
    }

    fun updateUserName(name: String) {
        _inputUserName.value = name
    }

    fun submitWithdrawal() {
        val currentWallet = wallet.value ?: return
        val pkg = _selectedPackage.value
        val method = _selectedPayoutMethod.value
        val address = _inputTargetAddress.value.trim()
        val name = _inputUserName.value.trim()

        if (currentWallet.coins < pkg.coins) {
            val needed = pkg.coins - currentWallet.coins
            _redeemErrorMessage.value = "Insufficient coins! You need $needed more coins to withdraw ${method.currencySymbol}${pkg.amountInRupees}."
            return
        }

        if (address.isEmpty()) {
            _redeemErrorMessage.value = when (method) {
                PayoutMethod.UPI -> "Please enter your UPI ID (e.g., yourname@upi)"
                PayoutMethod.FLIPKART -> "Please enter your Email ID to receive the Flipkart voucher"
                PayoutMethod.AMAZON -> "Please enter your Email ID to receive the Amazon Pay code"
                PayoutMethod.PLAY_STORE_CODE -> "Please enter your Email ID to receive the Google Play code"
                PayoutMethod.PAYPAL -> "Please enter your PayPal account Email ID"
            }
            return
        }

        if (method == PayoutMethod.UPI && !address.contains("@")) {
            _redeemErrorMessage.value = "Invalid UPI ID! Please include '@' (e.g. 9876543210@paytm or name@okaxis)"
            return
        }

        if (method != PayoutMethod.UPI && (!address.contains("@") || !address.contains("."))) {
            _redeemErrorMessage.value = "Invalid Email! Please enter a valid email address for delivery."
            return
        }

        _isRedeeming.value = true
        _redeemErrorMessage.value = null

        viewModelScope.launch {
            delay(1200) // Realistic processing feedback
            val result = repository.redeem(
                coins = pkg.coins,
                rupees = pkg.amountInRupees,
                method = method,
                targetAddress = address
            )
            _isRedeeming.value = false
            result.onSuccess { tx ->
                _withdrawSuccess.value = tx
                _inputTargetAddress.value = ""
            }.onFailure { err ->
                _redeemErrorMessage.value = err.message ?: "Redeem failed. Please try again."
            }
        }
    }

    fun dismissCelebration() {
        _celebrationReward.value = null
    }

    fun dismissWithdrawSuccess() {
        _withdrawSuccess.value = null
    }

    fun clearRedeemError() {
        _redeemErrorMessage.value = null
    }

    // Auth Actions
    fun signInWithGoogle(name: String, email: String, photoUrl: String = "") {
        viewModelScope.launch {
            repository.signInWithGoogle(name, email, photoUrl)
            _celebrationReward.value = CelebrationReward(
                coins = 50,
                title = "Welcome $name!",
                description = "Google Account connected successfully! Enjoy earning coins."
            )
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            repository.signInAsGuest()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }
}
