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
    private lateinit var categoryAdapter: CategoryAdapter
    private var allGamesList = listOf<GameModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        initDrawer()
        setupCarousel()
        setupCategories()
        
        // Setup notification button
        findViewById<ImageView>(R.id.notificationButton).setOnClickListener {
            val intent = Intent(this, notification::class.java)
            startActivity(intent)
        }

        // Setup Bottom Nav click for Favorites
        findViewById<android.view.View>(R.id.nav_fav).setOnClickListener {
            showFavorites()
        }

        findViewById<android.view.View>(R.id.nav_home).setOnClickListener {
            setupCategories() // Show all again
        }

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
            CarouselItem("TOWER CRASH", R.drawable.tower_crash, "https://play.famobi.com/wrapper/tower-crash-3d/A1000-10"),
            CarouselItem("LUDUM DARE", R.drawable.ludum_dare, "https://antila.github.io/ludum-dare-28/"),
            CarouselItem("ZOO BOOM", R.drawable.zoo_boom, "https://appslabs.store/games/zoo-boom/wrapper/zoo-boom/A1000-10.html")
        )

        recyclerView.adapter = CarouselAdapter(items) { url -> openGame(url) }
    }

    private fun setupCategories() {
        allGamesList = listOf(
            GameModel("Tower Crash", R.drawable.tower_crash, "https://play.famobi.com/wrapper/tower-crash-3d/A1000-10"),
            GameModel("Ludum Dare", R.drawable.ludum_dare, "https://antila.github.io/ludum-dare-28/"),
            GameModel("Zoo Boom", R.drawable.zoo_boom, "https://appslabs.store/games/zoo-boom/wrapper/zoo-boom/A1000-10.html"),
            GameModel("Sudoku", R.drawable.sudoku, "https://appslabs.store/games/sudoku/"),
            GameModel("Words", R.drawable.words_of_wonders, "http://appslabs.store/games/words-of-wonders/"),
            GameModel("Quiz Master", R.drawable.quiz, "https://appslabs.store/games/click-combo-quiz")
        )

        val allCategories = listOf(
            CategoryModel("Quick Play", allGamesList.shuffled()),
            CategoryModel("Puzzle & Brain Games", allGamesList.shuffled()),
            CategoryModel("Action Games", allGamesList.shuffled())
        )

        val filteredCategories = allCategories.filter { it.games.isNotEmpty() }
        val rvMainCategories = findViewById<RecyclerView>(R.id.rvMainCategories)
        categoryAdapter = CategoryAdapter(filteredCategories) { url -> openGame(url) }
        rvMainCategories.adapter = categoryAdapter
    }

    private fun showFavorites() {
        val favGames = FavoritesManager.getFavorites(this)
        if (favGames.isEmpty()) {
            Toast.makeText(this, "No favorites added yet!", Toast.LENGTH_SHORT).show()
            return
        }

        // Group favorites (In a real API, games would have category tags. 
        // For now, we put all favorites in a "My Favorites" category)
        val favCategories = listOf(
            CategoryModel("My Favorites ❤️", favGames)
        )

        val rvMainCategories = findViewById<RecyclerView>(R.id.rvMainCategories)
        categoryAdapter = CategoryAdapter(favCategories) { url -> openGame(url) }
        rvMainCategories.adapter = categoryAdapter
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
        // Refresh UI to show updated favorites status if we returned from another screen
        if (::categoryAdapter.isInitialized) {
            categoryAdapter.notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}