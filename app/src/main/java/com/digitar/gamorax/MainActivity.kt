package com.digitar.gamorax

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class MainActivity : AppCompatActivity() {
    private lateinit var adView: AdView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var categoryAdapter: CategoryAdapter
    private var allGamesList = listOf<GameModel>()
    private var categorizedData = listOf<CategoryModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        initDrawer()
        setupCarousel()
        prepareData()
        setupSearch()
        setupFooter()
        
        // Setup notification button
        findViewById<ImageView>(R.id.notificationButton).setOnClickListener {
            val intent = Intent(this, notification::class.java)
            startActivity(intent)
        }

        MobileAds.initialize(this) {
            loadBannerAd()
        }
        
        // Default highlight
        highlightMenuItem(R.id.nav_home)
    }

    private fun setupFooter() {
        findViewById<View>(R.id.nav_home).setOnClickListener {
            highlightMenuItem(R.id.nav_home)
            updateMainList(categorizedData)
        }

        findViewById<View>(R.id.nav_fav).setOnClickListener {
            highlightMenuItem(R.id.nav_fav)
            showFavorites()
        }

        findViewById<View>(R.id.nav_arcade).setOnClickListener {
            // Share action
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out Gamorax for cool games!")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, null))
        }
    }

    private fun highlightMenuItem(activeId: Int) {
        val navItems = listOf(R.id.nav_home, R.id.nav_fav, R.id.nav_arcade)
        
        for (id in navItems) {
            val view = findViewById<View>(id)
            val icon = when(id) {
                R.id.nav_home -> findViewById<ImageView>(R.id.iv_home)
                R.id.nav_fav -> findViewById<ImageView>(R.id.iv_fav)
                else -> findViewById<ImageView>(R.id.iv_arcade)
            }
            val text = when(id) {
                R.id.nav_home -> findViewById<TextView>(R.id.tv_home)
                R.id.nav_fav -> findViewById<TextView>(R.id.tv_fav)
                else -> findViewById<TextView>(R.id.tv_arcade)
            }

            if (id == activeId) {
                icon.setColorFilter(ContextCompat.getColor(this, R.color.accent_orange))
                text.setTextColor(ContextCompat.getColor(this, R.color.accent_orange))
            } else {
                icon.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary))
                text.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun initDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        findViewById<ImageView>(R.id.menuButton).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
//                R.id.nav_profile -> Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
//                R.id.nav_wallet -> Toast.makeText(this, "Wallet clicked", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_logout -> finish()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupCarousel() {
        val carousel: ImageCarousel = findViewById(R.id.carousel)
        carousel.registerLifecycle(lifecycle)

        val list = mutableListOf<CarouselItem>()
        list.add(CarouselItem(imageDrawable = R.drawable.tower_crash, caption = "Tower Crash 3D"))
        list.add(CarouselItem(imageDrawable = R.drawable.ludum_dare, caption = "Ludum Dare 28"))
        list.add(CarouselItem(imageDrawable = R.drawable.zoo_boom, caption = "Zoo Boom"))

        carousel.setData(list)
        carousel.carouselListener = object : CarouselListener {
            override fun onClick(position: Int, carouselItem: CarouselItem) {
                val urls = listOf(
                    "https://play.famobi.com/wrapper/tower-crash-3d/A1000-10",
                    "https://antila.github.io/ludum-dare-28/",
                    "https://appslabs.store/games/zoo-boom/wrapper/zoo-boom/A1000-10.html"
                )
                openGame(urls[position])
            }
        }
    }

    private fun prepareData() {
        allGamesList = listOf(
            GameModel("Tower Crash", R.drawable.tower_crash, "https://play.famobi.com/wrapper/tower-crash-3d/A1000-10"),
            GameModel("Ludum Dare", R.drawable.ludum_dare, "https://antila.github.io/ludum-dare-28/"),
            GameModel("Zoo Boom", R.drawable.zoo_boom, "https://appslabs.store/games/zoo-boom/wrapper/zoo-boom/A1000-10.html"),
            GameModel("Sudoku", R.drawable.sudoku, "https://appslabs.store/games/sudoku/"),
            GameModel("Words", R.drawable.words_of_wonders, "http://appslabs.store/games/words-of-wonders/"),
            GameModel("Quiz Master", R.drawable.quiz, "https://appslabs.store/games/click-combo-quiz")
        )

        categorizedData = listOf(
            CategoryModel("Quick Play", allGamesList.shuffled()),
            CategoryModel("Puzzle & Brain Games", allGamesList.shuffled()),
            CategoryModel("Action Games", allGamesList.shuffled())
        )

        updateMainList(categorizedData)
    }

    private fun setupSearch() {
        val searchBar = findViewById<EditText>(R.id.searchBar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            updateMainList(categorizedData)
            return
        }

        val filteredGames = allGamesList.filter { 
            it.title.contains(query, ignoreCase = true) 
        }

        val searchResult = if (filteredGames.isNotEmpty()) {
            listOf(CategoryModel("Search Results", filteredGames))
        } else {
            emptyList()
        }

        updateMainList(searchResult)
    }

    private fun updateMainList(data: List<CategoryModel>) {
        val rvMainCategories = findViewById<RecyclerView>(R.id.rvMainCategories)
        categoryAdapter = CategoryAdapter(data) { url -> openGame(url) }
        rvMainCategories.adapter = categoryAdapter
    }

    private fun showFavorites() {
        val favGames = FavoritesManager.getFavorites(this)
        if (favGames.isEmpty()) {
            Toast.makeText(this, "No favorites added yet!", Toast.LENGTH_SHORT).show()
            // If empty, keep highlight on Fav but maybe stay on Home data or show empty msg
            val favCategories = listOf(CategoryModel("My Favorites ❤️", emptyList()))
            updateMainList(favCategories)
            return
        }

        val favCategories = listOf(CategoryModel("My Favorites ❤️", favGames))
        updateMainList(favCategories)
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
        if (::categoryAdapter.isInitialized) {
            categoryAdapter.notifyDataSetChanged()
        }
        // Ensure Home is highlighted when returning from other activities (since it's the Home screen)
        highlightMenuItem(R.id.nav_home)
    }

    override fun onBackPressed() {
        if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}