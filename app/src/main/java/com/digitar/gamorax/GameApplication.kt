package com.digitar.gamorax

import android.app.Application
import com.google.android.gms.ads.MobileAds

class GameApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize theme based on user preference
        val savedTheme = ThemeManager.getSavedTheme(this)
        ThemeManager.applyTheme(savedTheme)

        // Initialize the Mobile Ads SDK
        MobileAds.initialize(this) {
            // SDK initialization complete
        }
    }
}