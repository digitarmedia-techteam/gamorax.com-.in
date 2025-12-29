package com.digitar.gamorax

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.HeroCarouselStrategy
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        setupCarousel()
        setupClickListeners()

        MobileAds.initialize(this) {
            loadBannerAd()
        }
    }

    private fun setupCarousel() {
        val recyclerView = findViewById<RecyclerView>(R.id.carouselRecyclerView)
        
        // Material 3 Carousel setup
        recyclerView.layoutManager = CarouselLayoutManager(HeroCarouselStrategy())
        
        // Optional: Add SnapHelper to make items snap to center
        CarouselSnapHelper().attachToRecyclerView(recyclerView)

        val items = listOf(
            CarouselItem("TOWER CRASH", R.drawable.tower_crash, "https://appslabs.store/games/zoo-boom/wrapper/zoo-boom/A1000-10.html"),
            CarouselItem("LUDUM DARE", R.drawable.ludum_dare, "https://antila.github.io/ludum-dare-28/"),
            CarouselItem("ZOO BOOM", R.drawable.zoo_boom, "https://appslabs.store/games/zoo-boom/wrapper/zoo-boom/A1000-10.html")
        )

        recyclerView.adapter = CarouselAdapter(items) { url ->
            openGame(url)
        }
    }

    private fun setupClickListeners() {
        // Grid cards
        findViewById<MaterialCardView>(R.id.card1).setOnClickListener {
            openGame("https://appslabs.store/games/zoo-boom/wrapper/zoo-boom/A1000-10.html")
        }
        findViewById<MaterialCardView>(R.id.card2).setOnClickListener {
            openGame("https://antila.github.io/ludum-dare-28/")
        }
        // ... set other card listeners as needed
    }

    private fun openGame(url: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra("EXTRA_URL", url)
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
        if (::adView.isInitialized) adView.resume()
    }

    override fun onPause() {
        if (::adView.isInitialized) adView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        if (::adView.isInitialized) adView.destroy()
        super.onDestroy()
    }
}