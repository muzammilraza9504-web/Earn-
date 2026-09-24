package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.TransactionEntity
import com.example.data.model.UserWalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    @Query("SELECT * FROM user_wallet WHERE id = 1")
    fun getUserWalletFlow(): Flow<UserWalletEntity?>

    @Query("SELECT * FROM user_wallet WHERE id = 1")
    suspend fun getUserWallet(): UserWalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserWallet(wallet: UserWalletEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Transaction
    suspend fun addRewardCoins(
        addedCoins: Int,
        title: String,
        description: String,
        type: com.example.data.model.TransactionType,
        isAd: Boolean = false,
        isSpin: Boolean = false
    ): UserWalletEntity {
        val currentWallet = getUserWallet() ?: UserWalletEntity()
        val newCoins = currentWallet.coins + addedCoins
        val newTotal = currentWallet.totalCoinsEarned + addedCoins
        val newAds = if (isAd) currentWallet.adsWatchedCount + 1 else currentWallet.adsWatchedCount
        val newDailyAds = if (isAd) currentWallet.dailyAdsWatched + 1 else currentWallet.dailyAdsWatched
        val newSpins = if (isSpin) currentWallet.spinsCount + 1 else currentWallet.spinsCount
        val newDailySpins = if (isSpin) currentWallet.dailySpinsUsed + 1 else currentWallet.dailySpinsUsed

        val updatedWallet = currentWallet.copy(
            coins = newCoins,
            totalCoinsEarned = newTotal,
            adsWatchedCount = newAds,
            dailyAdsWatched = newDailyAds,
            spinsCount = newSpins,
            dailySpinsUsed = newDailySpins
        )
        saveUserWallet(updatedWallet)

        insertTransaction(
            TransactionEntity(
                type = type,
                amount = addedCoins,
                title = title,
                description = description,
                status = com.example.data.model.TransactionStatus.COMPLETED
            )
        )

        return updatedWallet
    }

    @Transaction
    suspend fun redeemCoins(
        deductCoins: Int,
        rupees: Int,
        payoutMethod: com.example.data.model.PayoutMethod,
        targetAddress: String
    ): Result<TransactionEntity> {
        val currentWallet = getUserWallet() ?: UserWalletEntity()
        if (currentWallet.coins < deductCoins) {
            return Result.failure(Exception("Insufficient coins balance"))
        }

        val updatedWallet = currentWallet.copy(
            coins = currentWallet.coins - deductCoins,
            totalCoinsRedeemed = currentWallet.totalCoinsRedeemed + deductCoins
        )
        saveUserWallet(updatedWallet)

        val refId = "TXN" + System.currentTimeMillis().toString().takeLast(8)
        val currency = payoutMethod.currencySymbol
        val title = when (payoutMethod) {
            com.example.data.model.PayoutMethod.UPI -> "₹$rupees Instant UPI Payout"
            com.example.data.model.PayoutMethod.FLIPKART -> "₹$rupees Flipkart Gift Voucher"
            com.example.data.model.PayoutMethod.AMAZON -> "₹$rupees Amazon Pay Gift Card"
            com.example.data.model.PayoutMethod.PLAY_STORE_CODE -> "₹$rupees Google Play Store Code"
            com.example.data.model.PayoutMethod.PAYPAL -> "$$rupees PayPal Cash Transfer"
        }

        val description = when (payoutMethod) {
            com.example.data.model.PayoutMethod.UPI -> "Direct UPI Transfer to: $targetAddress"
            com.example.data.model.PayoutMethod.FLIPKART -> "Flipkart voucher delivered to: $targetAddress"
            com.example.data.model.PayoutMethod.AMAZON -> "Amazon Pay code delivered to: $targetAddress"
            com.example.data.model.PayoutMethod.PLAY_STORE_CODE -> "Google Play code sent to: $targetAddress"
            com.example.data.model.PayoutMethod.PAYPAL -> "PayPal transfer to: $targetAddress"
        }

        val txType = when (payoutMethod) {
            com.example.data.model.PayoutMethod.UPI -> com.example.data.model.TransactionType.REDEEM_UPI
            com.example.data.model.PayoutMethod.FLIPKART -> com.example.data.model.TransactionType.REDEEM_FLIPKART
            com.example.data.model.PayoutMethod.AMAZON -> com.example.data.model.TransactionType.REDEEM_AMAZON
            com.example.data.model.PayoutMethod.PLAY_STORE_CODE -> com.example.data.model.TransactionType.REDEEM_PLAY_STORE
            com.example.data.model.PayoutMethod.PAYPAL -> com.example.data.model.TransactionType.REDEEM_PAYPAL
        }

        val tx = TransactionEntity(
            type = txType,
            amount = -deductCoins,
            title = title,
            description = description,
            status = com.example.data.model.TransactionStatus.PROCESSING,
            targetAddress = targetAddress,
            referenceId = refId
        )

        val insertedId = insertTransaction(tx)
        return Result.success(tx.copy(id = insertedId))
    }
}
