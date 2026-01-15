package com.digitar.gamorax.ui.carousel

import com.digitar.gamorax.R

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView

class PremiumCarouselActivity : AppCompatActivity() {

    private lateinit var carouselManager: PremiumCarouselManager
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_carousel)

        setupViews()
        setupCarousel()
    }

    private fun setupViews() {
        viewPager = findViewById(R.id.premium_carousel_viewpager)
    }

    private fun setupCarousel() {
        // Sample carousel items - replace with your actual data
        val carouselItems = listOf(
            PremiumCarouselItem(
                id = "1",
                title = "Cyber Racing 2077",
                subtitle = "Racing • Futuristic",
                category = "FEATURED",
                imageRes = R.drawable.ic_launcher_foreground, // Replace with actual images
                url = "cyber_racing_2077",
                isFeatured = true
            ),
            PremiumCarouselItem(
                id = "2",
                title = "Neon Warriors",
                subtitle = "Action • Multiplayer",
                category = "NEW",
                imageRes = R.drawable.ic_launcher_foreground,
                url = "neon_warriors"
            ),
            PremiumCarouselItem(
                id = "3",
                title = "Space Odyssey",
                subtitle = "Adventure • Sci-Fi",
                category = "POPULAR",
                imageRes = R.drawable.ic_launcher_foreground,
                url = "space_odyssey"
            ),
            PremiumCarouselItem(
                id = "4",
                title = "Dragon Legends",
                subtitle = "RPG • Fantasy",
                category = "TRENDING",
                imageRes = R.drawable.ic_launcher_foreground,
                url = "dragon_legends"
            ),
            PremiumCarouselItem(
                id = "5",
                title = "Speed Demons",
                subtitle = "Racing • Street",
                category = "HOT",
                imageRes = R.drawable.ic_launcher_foreground,
                url = "speed_demons"
            )
        )

        carouselManager = PremiumCarouselManager(this, lifecycle)
        
        carouselManager.initialize(
            viewPager = viewPager,
            indicatorContainer = null,
            items = carouselItems
        ) { item ->
            // Handle item click
            onCarouselItemClick(item)
        }
    }

    private fun onCarouselItemClick(item: PremiumCarouselItem) {
        Toast.makeText(this, "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()
        // Navigate to detail screen or handle click as needed
        // For example: startActivity(Intent(this, GameDetailActivity::class.java).apply {
        //     putExtra("game_id", item.id)
        //     putExtra("game_title", item.title)
        // })
    }

    override fun onResume() {
        super.onResume()
        // Auto-scroll will be managed by the carousel manager's lifecycle observer
    }

    override fun onPause() {
        super.onPause()
        // Auto-scroll will be automatically paused by the carousel manager
    }
}
