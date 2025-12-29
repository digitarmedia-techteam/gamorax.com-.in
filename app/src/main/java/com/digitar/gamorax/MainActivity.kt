package com.digitar.gamorax

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set orientation to portrait for main screen
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        // Click listeners for game cards
        findViewById<MaterialCardView>(R.id.card1).setOnClickListener {
            openGame("https://appslabs.store/games/zoo-boom/wrapper/zoo-boom/A1000-10.html")
        }

        findViewById<MaterialCardView>(R.id.card2).setOnClickListener {
            openGame("https://antila.github.io/ludum-dare-28/")
        }
        findViewById<MaterialCardView>(R.id.card3).setOnClickListener {
            openGame("https://appslabs.store/games/zoo-boom/wrapper/zoo-boom/A1000-10.html")
        }
        findViewById<MaterialCardView>(R.id.card4).setOnClickListener {
            openGame("https://appslabs.store/games/sudoku/")
        }
        findViewById<MaterialCardView>(R.id.card5).setOnClickListener {
            openGame("https://appslabs.store/games/click-combo-quiz")
        }
        findViewById<MaterialCardView>(R.id.card6).setOnClickListener {
            openGame("http://appslabs.store/games/words-of-wonders")
        }

        // Initialize AdMob
        MobileAds.initialize(this) {
            loadBannerAd()
        }
    }

    private fun openGame(url: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra("URL", url)
        }
        startActivity(intent)
    }

    private fun loadBannerAd() {
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun onResume() {
        super.onResume()
        if (::adView.isInitialized) {
            adView.resume()
        }
    }

    override fun onPause() {
        if (::adView.isInitialized) {
            adView.pause()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (::adView.isInitialized) {
            adView.destroy()
        }
        super.onDestroy()
    }
}