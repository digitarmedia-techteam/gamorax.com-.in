package com.digitar.gamorax.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.Date

object AdManager {

    private const val TAG = "AdManager"

    // Configurable flag to easily switch between test and real ads.
    private const val USE_TEST_ADS = true

    // Ad Unit IDs
    private object AdUnitIds {
        // Corrected App Open test ad unit ID
        val APP_OPEN = if (USE_TEST_ADS) "ca-app-pub-3940256099942544/9257395921" else "YOUR_REAL_APP_OPEN_AD_UNIT_ID"
        val INTERSTITIAL = if (USE_TEST_ADS) "ca-app-pub-3940256099942544/1033173712" else "YOUR_REAL_INTERSTITIAL_AD_UNIT_ID"
        val REWARDED = if (USE_TEST_ADS) "ca-app-pub-3940256099942544/5224354917" else "YOUR_REAL_REWARDED_AD_UNIT_ID"
    }

    // Ad-related properties
    private var appOpenAd: AppOpenAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var isAppOpenAdShowing = false
    private var isInterstitialAdShowing = false
    private var isRewardedAdShowing = false
    private var appOpenAdLoadTime: Long = 0

    // Timestamps to control ad frequency
    private var lastInterstitialAdShowTime: Long = 0
    private var minInterstitialAdGapMs: Long = 60 * 1000 // 60 seconds (default)

    /**
     * Initializes the Mobile Ads SDK and Remote Config.
     */
    fun initialize(context: Context) {
        MobileAds.initialize(context) { }
        setupRemoteConfig()
    }

    private fun setupRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        val defaults = mapOf<String, Any>(
            "min_interstitial_ad_gap_ms" to 60000L
        )
        remoteConfig.setDefaultsAsync(defaults)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                minInterstitialAdGapMs = remoteConfig.getLong("min_interstitial_ad_gap_ms")
                Log.d(TAG, "Remote Config fetched and activated successfully.")
            } else {
                Log.w(TAG, "Remote Config fetch failed.")
            }
        }
    }

    fun getAppOpenAdUnitId(): String {
        return AdUnitIds.APP_OPEN
    }

    /**
     * Pre-loads all ad types.
     */
    fun preloadAllAds(context: Context) {
        preloadAppOpenAd(context.applicationContext)
        preloadInterstitialAd(context.applicationContext)
        preloadRewardedAd(context.applicationContext)
    }

    // --- App Open Ad ---

    fun preloadAppOpenAd(context: Context) {
        if (appOpenAd != null) return
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AdUnitIds.APP_OPEN,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    appOpenAdLoadTime = Date().time
                    Log.d(TAG, "App Open ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                    Log.e(TAG, "App Open ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (isAppOpenAdShowing || !isAppOpenAdAvailable()) {
            Log.d(TAG, "App Open ad not shown: already showing or not available.")
            onAdDismissed()
            return
        }


        appOpenAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isAppOpenAdShowing = false
                preloadAppOpenAd(activity)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                appOpenAd = null
                isAppOpenAdShowing = false
                preloadAppOpenAd(activity)
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                isAppOpenAdShowing = true
            }
        }
        Log.d(TAG, "Showing App Open ad.")
        appOpenAd?.show(activity)
    }

    private fun isAppOpenAdAvailable(): Boolean {
        return appOpenAd != null
    }

    // --- Interstitial Ad ---

    fun preloadInterstitialAd(context: Context) {
        if (interstitialAd != null) return
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdUnitIds.INTERSTITIAL,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInterstitialAdShowTime < minInterstitialAdGapMs) {
            Log.d(TAG, "Interstitial ad not shown: not enough time has passed since the last one.")
            onAdDismissed()
            return
        }

        if (interstitialAd != null && !isInterstitialAdShowing) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    isInterstitialAdShowing = false
                    preloadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    isInterstitialAdShowing = false
                    preloadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    isInterstitialAdShowing = true
                    lastInterstitialAdShowTime = currentTime
                }
            }
            Log.d(TAG, "Showing Interstitial ad.")
            interstitialAd?.show(activity)
        } else {
            Log.w(TAG, "Interstitial ad was not ready to be shown. Preloading for next time.")
            preloadInterstitialAd(activity)
            onAdDismissed()
        }
    }

    // --- Rewarded Ad ---

    fun preloadRewardedAd(context: Context) {
        if (rewardedAd != null) return
        Log.d(TAG, "Requesting a new Rewarded ad.")
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            AdUnitIds.REWARDED,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "Rewarded ad was loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    Log.e(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onAdDismissed: () -> Unit, onUserEarnedReward: () -> Unit) {
        Log.d(TAG, "Attempting to show rewarded ad.")
        if (rewardedAd != null && !isRewardedAdShowing) {
            rewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    isRewardedAdShowing = false
                    preloadRewardedAd(activity) // Preload for the next time
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    rewardedAd = null
                    isRewardedAdShowing = false
                    Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                    preloadRewardedAd(activity)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    isInterstitialAdShowing = true
                    Log.d(TAG, "Rewarded ad was shown successfully.")
                }
            }

            rewardedAd?.show(activity) {
                Log.d(TAG, "User earned the reward.")
                onUserEarnedReward()
            }
        } else {
            Log.w(TAG, "Rewarded ad was not ready to be shown. Preloading for next time.")
            preloadRewardedAd(activity) // Preload for the next time
            onAdDismissed()
        }
    }
}
