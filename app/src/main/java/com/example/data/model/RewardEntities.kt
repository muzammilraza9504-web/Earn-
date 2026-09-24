package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    WATCH_AD,
    SPINNER_WIN,
    DAILY_BONUS,
    REDEEM_UPI,
    REDEEM_PLAY_STORE,
    REDEEM_AMAZON,
    REDEEM_FLIPKART,
    REDEEM_PAYPAL,
    REDEEM_GOOGLE_PAY
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    PROCESSING
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Int, // Positive for earnings, negative for withdrawals
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val targetAddress: String = "", // UPI ID or Email address
    val referenceId: String = ""
)

@Entity(tableName = "user_wallet")
data class UserWalletEntity(
    @PrimaryKey
    val id: Int = 1,
    val isLoggedIn: Boolean = true,
    val userName: String = "Google User",
    val userEmail: String = "xhkhckhjxk@gmail.com",
    val userPhotoUrl: String = "",
    val authProvider: String = "GOOGLE", // "GOOGLE" or "GUEST"
    val coins: Int = 200, // Starting welcome bonus (₹20)
    val totalCoinsEarned: Int = 200,
    val totalCoinsRedeemed: Int = 0,
    val adsWatchedCount: Int = 0,
    val spinsCount: Int = 0,
    val dailyStreak: Int = 1,
    val lastCheckInDate: String = "",
    val dailyAdsWatched: Int = 0,
    val dailySpinsUsed: Int = 0,
    val lastActiveDay: String = ""
)

data class RedeemPackage(
    val coins: Int,
    val amountInRupees: Int,
    val title: String,
    val isPopular: Boolean = false
)

enum class PayoutMethod(
    val displayName: String,
    val shortName: String,
    val iconEmoji: String,
    val addressLabel: String,
    val placeholder: String,
    val currencySymbol: String = "₹",
    val description: String
) {
    UPI(
        displayName = "Instant UPI",
        shortName = "UPI",
        iconEmoji = "⚡",
        addressLabel = "UPI ID / VPA",
        placeholder = "e.g. 9876543210@paytm or name@okaxis",
        currencySymbol = "₹",
        description = "Direct bank deposit via PhonePe, Paytm, BHIM, or GPay UPI."
    ),
    FLIPKART(
        displayName = "Flipkart Gift Card",
        shortName = "Flipkart",
        iconEmoji = "🛍️",
        addressLabel = "Email ID for Voucher",
        placeholder = "e.g. yourname@gmail.com",
        currencySymbol = "₹",
        description = "Instant Flipkart Digital Gift Card Voucher PIN delivered to your email."
    ),
    AMAZON(
        displayName = "Amazon Pay Gift Card",
        shortName = "Amazon",
        iconEmoji = "📦",
        addressLabel = "Email ID for Amazon Code",
        placeholder = "e.g. yourname@gmail.com",
        currencySymbol = "₹",
        description = "Amazon Pay Gift Card code delivered to your email for instant shopping."
    ),
    PLAY_STORE_CODE(
        displayName = "Google Play Code",
        shortName = "Google Play",
        iconEmoji = "🎮",
        addressLabel = "Email ID for Play Code",
        placeholder = "e.g. yourname@gmail.com",
        currencySymbol = "₹",
        description = "Google Play Store recharge code delivered directly to your email."
    ),
    PAYPAL(
        displayName = "PayPal Cash (USD/INR)",
        shortName = "PayPal",
        iconEmoji = "🌐",
        addressLabel = "PayPal Email ID",
        placeholder = "e.g. paypal_user@gmail.com",
        currencySymbol = "$",
        description = "International PayPal cash transfer credited directly to your PayPal account."
    )
}

val UPI_PACKAGES = listOf(
    RedeemPackage(coins = 10000, amountInRupees = 100, title = "₹100 UPI Transfer", isPopular = true),
    RedeemPackage(coins = 20000, amountInRupees = 200, title = "₹200 UPI Transfer"),
    RedeemPackage(coins = 30000, amountInRupees = 300, title = "₹300 UPI Transfer"),
    RedeemPackage(coins = 50000, amountInRupees = 500, title = "₹500 Instant UPI")
)

val FLIPKART_PACKAGES = listOf(
    RedeemPackage(coins = 10000, amountInRupees = 100, title = "₹100 Flipkart Voucher", isPopular = true),
    RedeemPackage(coins = 25000, amountInRupees = 250, title = "₹250 Flipkart Voucher"),
    RedeemPackage(coins = 50000, amountInRupees = 500, title = "₹500 Flipkart Gift Card"),
    RedeemPackage(coins = 100000, amountInRupees = 1000, title = "₹1000 Flipkart Card")
)

val AMAZON_PACKAGES = listOf(
    RedeemPackage(coins = 10000, amountInRupees = 100, title = "₹100 Amazon Pay Card", isPopular = true),
    RedeemPackage(coins = 25000, amountInRupees = 250, title = "₹250 Amazon Pay Card"),
    RedeemPackage(coins = 50000, amountInRupees = 500, title = "₹500 Amazon Gift Card"),
    RedeemPackage(coins = 100000, amountInRupees = 1000, title = "₹1000 Amazon Gift Card")
)

val PLAY_STORE_PACKAGES = listOf(
    RedeemPackage(coins = 10000, amountInRupees = 100, title = "₹100 Play Store Code", isPopular = true),
    RedeemPackage(coins = 20000, amountInRupees = 200, title = "₹200 Play Store Code"),
    RedeemPackage(coins = 30000, amountInRupees = 300, title = "₹300 Play Store Code"),
    RedeemPackage(coins = 50000, amountInRupees = 500, title = "₹500 Play Store Code")
)

val PAYPAL_PACKAGES = listOf(
    RedeemPackage(coins = 10000, amountInRupees = 1, title = "$1 PayPal Cash", isPopular = true),
    RedeemPackage(coins = 25000, amountInRupees = 3, title = "$3 PayPal Cash"),
    RedeemPackage(coins = 40000, amountInRupees = 5, title = "$5 PayPal Cash"),
    RedeemPackage(coins = 80000, amountInRupees = 10, title = "$10 PayPal Cash")
)

fun getPackagesForMethod(method: PayoutMethod): List<RedeemPackage> = when (method) {
    PayoutMethod.UPI -> UPI_PACKAGES
    PayoutMethod.FLIPKART -> FLIPKART_PACKAGES
    PayoutMethod.AMAZON -> AMAZON_PACKAGES
    PayoutMethod.PLAY_STORE_CODE -> PLAY_STORE_PACKAGES
    PayoutMethod.PAYPAL -> PAYPAL_PACKAGES
}
