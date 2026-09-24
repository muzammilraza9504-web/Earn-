package com.example.data.repository

import com.example.data.db.RewardDao
import com.example.data.model.PayoutMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserWalletEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RewardRepository(private val rewardDao: RewardDao) {

    val userWalletFlow: Flow<UserWalletEntity?> = rewardDao.getUserWalletFlow()
    val transactionsFlow: Flow<List<TransactionEntity>> = rewardDao.getAllTransactionsFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getOrCreateWallet(): UserWalletEntity {
        var wallet = rewardDao.getUserWallet()
        val todayStr = dateFormat.format(Date())

        if (wallet == null) {
            wallet = UserWalletEntity(
                isLoggedIn = true,
                userName = "Google User",
                userEmail = "xhkhckhjxk@gmail.com",
                authProvider = "GOOGLE",
                coins = 200, // 200 Welcome Bonus Coins
                totalCoinsEarned = 200,
                lastActiveDay = todayStr
            )
            rewardDao.saveUserWallet(wallet)
            rewardDao.insertTransaction(
                TransactionEntity(
                    type = TransactionType.DAILY_BONUS,
                    amount = 200,
                    title = "Welcome Signup Bonus",
                    description = "Bonus credited for joining Earnora!"
                )
            )
        } else if (!wallet.isLoggedIn) {
            wallet = wallet.copy(
                isLoggedIn = true,
                userName = if (wallet.userName.isBlank()) "Google User" else wallet.userName,
                userEmail = if (wallet.userEmail.isBlank()) "xhkhckhjxk@gmail.com" else wallet.userEmail,
                authProvider = if (wallet.authProvider.isBlank()) "GOOGLE" else wallet.authProvider
            )
            rewardDao.saveUserWallet(wallet)
        } else if (wallet.lastActiveDay != todayStr) {
            // Reset daily counters on new day
            wallet = wallet.copy(
                dailyAdsWatched = 0,
                dailySpinsUsed = 0,
                lastActiveDay = todayStr
            )
            rewardDao.saveUserWallet(wallet)
        }
        return wallet
    }

    suspend fun watchAdAndEarn(adTitle: String, rewardCoins: Int): UserWalletEntity {
        return rewardDao.addRewardCoins(
            addedCoins = rewardCoins,
            title = "Watched $adTitle",
            description = "Completed video ad and earned +$rewardCoins Coins",
            type = TransactionType.WATCH_AD,
            isAd = true
        )
    }

    suspend fun spinWheelAndEarn(number: Int, rewardCoins: Int): UserWalletEntity {
        return rewardDao.addRewardCoins(
            addedCoins = rewardCoins,
            title = "Lucky Wheel Spin (Slot #$number)",
            description = "Won slot #$number reward on 1-20 Spinner!",
            type = TransactionType.SPINNER_WIN,
            isSpin = true
        )
    }

    suspend fun claimDailyCheckIn(dayIndex: Int, coins: Int): Boolean {
        val todayStr = dateFormat.format(Date())
        val wallet = getOrCreateWallet()
        if (wallet.lastCheckInDate == todayStr) {
            return false // already claimed today
        }

        val nextStreak = if (wallet.dailyStreak >= 7) 1 else wallet.dailyStreak + 1
        val updated = wallet.copy(
            coins = wallet.coins + coins,
            totalCoinsEarned = wallet.totalCoinsEarned + coins,
            dailyStreak = nextStreak,
            lastCheckInDate = todayStr
        )
        rewardDao.saveUserWallet(updated)
        rewardDao.insertTransaction(
            TransactionEntity(
                type = TransactionType.DAILY_BONUS,
                amount = coins,
                title = "Day $dayIndex Daily Check-In",
                description = "Claimed daily streak streak bonus!"
            )
        )
        return true
    }

    suspend fun redeem(
        coins: Int,
        rupees: Int,
        method: PayoutMethod,
        targetAddress: String
    ): Result<TransactionEntity> {
        return rewardDao.redeemCoins(
            deductCoins = coins,
            rupees = rupees,
            payoutMethod = method,
            targetAddress = targetAddress
        )
    }

    suspend fun signInWithGoogle(
        name: String,
        email: String,
        photoUrl: String
    ): UserWalletEntity {
        val currentWallet = getOrCreateWallet()
        val updated = currentWallet.copy(
            isLoggedIn = true,
            userName = name.ifEmpty { "Google User" },
            userEmail = email,
            userPhotoUrl = photoUrl,
            authProvider = "GOOGLE"
        )
        rewardDao.saveUserWallet(updated)
        return updated
    }

    suspend fun signInAsGuest(): UserWalletEntity {
        val currentWallet = getOrCreateWallet()
        val updated = currentWallet.copy(
            isLoggedIn = true,
            userName = "Guest User",
            userEmail = "guest@watchandearn.local",
            userPhotoUrl = "",
            authProvider = "GUEST"
        )
        rewardDao.saveUserWallet(updated)
        return updated
    }

    suspend fun signOut(): UserWalletEntity {
        val currentWallet = getOrCreateWallet()
        val updated = currentWallet.copy(
            isLoggedIn = false,
            authProvider = ""
        )
        rewardDao.saveUserWallet(updated)
        return updated
    }
}
