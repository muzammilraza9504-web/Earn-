package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobManager {
    private const val TAG = "AdMobManager"

    // Official Google AdMob Test Ad Unit IDs
    const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val BANNER_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var isInitialized = false

    fun initialize(context: Context) {
        if (!isInitialized) {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob Initialized: $initializationStatus")
            }
            isInitialized = true
        }
    }

    /**
     * Loads and immediately displays a Google AdMob Rewarded Test Ad.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (rewardAmount: Int, rewardType: String) -> Unit,
        onAdDismissed: () -> Unit = {},
        onAdFailed: (String) -> Unit = {}
    ) {
        initialize(activity)
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            activity,
            REWARDED_TEST_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(TAG, "Rewarded test ad loaded successfully.")
                    rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad dismissed.")
                            onAdDismissed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                            onAdFailed(adError.message)
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad showed full screen.")
                        }
                    }

                    rewardedAd.show(activity) { rewardItem ->
                        val amount = rewardItem.amount
                        val type = rewardItem.type
                        Log.d(TAG, "User earned reward: $amount $type")
                        onRewardEarned(amount, type)
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    val errorMsg = "Rewarded ad failed to load: ${loadAdError.message}"
                    Log.e(TAG, errorMsg)
                    onAdFailed(loadAdError.message)
                }
            }
        )
    }

    /**
     * Loads and immediately displays a Google AdMob Interstitial Test Ad.
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdFailed: (String) -> Unit = {}
    ) {
        initialize(activity)
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            INTERSTITIAL_TEST_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Interstitial test ad loaded successfully.")
                    interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad dismissed.")
                            onAdDismissed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                            onAdFailed(adError.message)
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad showed.")
                        }
                    }
                    interstitialAd.show(activity)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    onAdFailed(loadAdError.message)
                }
            }
        )
    }
}

/**
 * Composable Banner Ad component using Google AdMob Banner Test ID.
 */
@Composable
fun AdMobBannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.BANNER_TEST_AD_UNIT_ID
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admob_banner_ad"),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    setAdUnitId(adUnitId)
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("AdMobBannerAd", "Banner test ad loaded.")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e("AdMobBannerAd", "Banner test ad failed: ${error.message}")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { adView ->
                try {
                    adView.destroy()
                } catch (e: Exception) {
                    Log.e("AdMobBannerAd", "Error destroying adView", e)
                }
            }
        )
    }
}
