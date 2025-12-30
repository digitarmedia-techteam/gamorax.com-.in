package com.digitar.gamorax

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.HeroCarouselStrategy
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var adView: AdView
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        initDrawer()
        setupCarousel()
        setupCategories()

        MobileAds.initialize(this) {
            loadBannerAd()
        }
    }

    private fun initDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        findViewById<ImageView>(R.id.menuButton).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                R.id.nav_wallet -> Toast.makeText(this, "Wallet clicked", Toast.LENGTH_SHORT).show()
                R.id.nav_logout -> finish()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupCarousel() {
        val recyclerView = findViewById<RecyclerView>(R.id.carouselRecyclerView)
        recyclerView.layoutManager = CarouselLayoutManager(HeroCarouselStrategy())
        CarouselSnapHelper().attachToRecyclerView(recyclerView)

        val items = listOf(
            CarouselItem("TOWER CRASH", R.drawable.tower_crash, "https://appslabs.store/games/tower-crash-3d/A1000-10.html"),
            CarouselItem("LUDUM DARE", R.drawable.ludum_dare, "https://antila.github.io/ludum-dare-28/"),
            CarouselItem("ZOO BOOM", R.drawable.zoo_boom, "https://appslabs.store/games/zoo-boom/A1000-10.html")
        )

        recyclerView.adapter = CarouselAdapter(items) { url -> openGame(url) }
    }

    private fun setupCategories() {
        // Data models (This part is ready for API integration)
        val gamesList = listOf(
            GameModel("Tower Crash", R.drawable.tower_crash, "https://appslabs.store/games/tower-crash-3d/A1000-10.html"),
            GameModel("Ludum Dare", R.drawable.ludum_dare, "https://antila.github.io/ludum-dare-28/"),
            GameModel("Zoo Boom", R.drawable.zoo_boom, "https://appslabs.store/games/zoo-boom/A1000-10.html"),
            GameModel("Sudoku", R.drawable.sudoku, "https://appslabs.store/games/sudoku/A1000-10.html"),
            GameModel("Words", R.drawable.words_of_wonders, "https://appslabs.store/games/wow/A1000-10.html")
        )

        val impList = emptyList<GameModel>()

        // Create categories list (This will come from API later)
        val allCategories = listOf(
            CategoryModel("Quick Play", gamesList.reversed()),
            CategoryModel("Puzzle & Brain Games", gamesList.shuffled()),
            CategoryModel("Educational Games", gamesList.reversed()),
            CategoryModel("Action Games", gamesList.shuffled()),
            CategoryModel("Casino Games", gamesList.reversed()),
            CategoryModel("Empty Category", impList), // Testing empty category
        )

        // Filter out categories that have no games
        val filteredCategories = allCategories.filter { it.games.isNotEmpty() }

        // Initialize the main dynamic RecyclerView
        val rvMainCategories = findViewById<RecyclerView>(R.id.rvMainCategories)
        rvMainCategories.adapter = CategoryAdapter(filteredCategories) { url ->
            openGame(url)
        }
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

    override fun onBackPressed() {
        if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}