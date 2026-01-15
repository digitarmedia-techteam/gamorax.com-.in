package com.digitar.gamorax.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Display
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.digitar.gamorax.R
import com.digitar.gamorax.authorization.LoginPopupDialog
import com.digitar.gamorax.core.ads.AdManager
import com.digitar.gamorax.data.FavoritesManager
import com.digitar.gamorax.data.model.CategoryModel
import com.digitar.gamorax.data.model.GameModel
import com.digitar.gamorax.data.repository.GameRepository
import com.digitar.gamorax.ui.auth.LoginActivity
import com.digitar.gamorax.ui.carousel.PremiumCarouselActivity
import com.digitar.gamorax.ui.carousel.PremiumCarouselItem
import com.digitar.gamorax.ui.carousel.PremiumCarouselManager
import com.digitar.gamorax.ui.game.WebViewActivity
import com.digitar.gamorax.ui.notification.NotificationActivity
import com.digitar.gamorax.ui.settings.SettingsActivity
import com.google.android.gms.ads.AdError
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
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var adView: AdView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var categoryAdapter: CategoryAdapter
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var pendingGameUrl: String? = null

    private var allGamesList = listOf<GameModel>()
    private var categorizedData = listOf<CategoryModel>()
    private lateinit var adViewContainer: FrameLayout
    private lateinit var premiumCarouselManager: PremiumCarouselManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdManager.initialize(this)
        MobileAds.initialize(this) { }
        loadInterstitialAd()
        loadRewardedAd()

        onBackPressedDispatcher.addCallback(this) {
            if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

//        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val rootLayout = findViewById<View>(R.id.drawerLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        initDrawer()
        setupCarousel()
        prepareData()
        setupSearch()
        setupFooter()
        setupScrollData()

        adViewContainer = findViewById(R.id.adViewContainer)
        findViewById<CardView>(R.id.moreGameCard).setOnClickListener {
            showRewardedAd {
                Toast.makeText(this, "Thanks for your support!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.notificationButton).setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        MobileAds.initialize(this) { }

        loadBannerAd()

        highlightMenuItem(R.id.nav_home)

        // Show Login Popup on start
        showLoginPopup()
    }

    private fun showLoginPopup() {
        val dialog = LoginPopupDialog()
        dialog.show(supportFragmentManager, "LoginPopup")
    }

    private fun setupFooter() {
        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            highlightMenuItem(R.id.nav_home)
            updateMainList(categorizedData)
        }

        findViewById<View>(R.id.nav_fav)?.setOnClickListener {
            highlightMenuItem(R.id.nav_fav)
            showFavorites()
        }

        findViewById<View>(R.id.nav_arcade)?.setOnClickListener {
            // Trigger Play Store and Browser together
//            openPlayStoreAndBrowserTogether()
            Share()
        }

        // Initialize footer animation state
        findViewById<View>(R.id.bottomNavigationInclude).apply {
            // Ensure proper initial state if needed
            translationY = 0f
        }
    }

    private fun setupScrollData() {
        val nestedScrollView =
            findViewById<NestedScrollView>(R.id.mainScrollView)
        val searchBar = findViewById<View>(R.id.searchBar)
        val bottomNav = findViewById<View>(R.id.bottomNavigationInclude)

        // Scroll State tracking
        var isScrollingDown = false

        // Handler for idle detection
        val scrollHandler = Handler(Looper.getMainLooper())
        val scrollRunnable = Runnable {
            // Scroll has stopped
            if (!isScrollingDown) {
                // Resume carousel only if not scrolling
                if (::premiumCarouselManager.isInitialized) {
                    premiumCarouselManager.startAutoScroll()
                }
            }
        }

        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val dy = scrollY - oldScrollY

            // 1. Carousel Sync & Animation
            val carouselView = findViewById<View>(R.id.premium_carousel_include)
            if (carouselView != null) {
                // Calculate scale factor based on scroll position - shrinks as you scroll down
                val maxScroll = 500f
                val scale = (1.0f - (0.15f * (scrollY.toFloat() / maxScroll).coerceIn(0f, 1f)))

                carouselView.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .alpha(if (scrollY > 600) 0.1f else 1f)
                    .setDuration(0)
                    .start()
            }

            // 2. Animate Game Cards / Categories based on screen position
            val rvMainCategories = findViewById<RecyclerView>(R.id.rvMainCategories)
            val screenHeight = resources.displayMetrics.heightPixels
            val scrollBounds = Rect()
            nestedScrollView.getHitRect(scrollBounds)

            for (i in 0 until rvMainCategories.childCount) {
                val child = rvMainCategories.getChildAt(i)
                val childRect = Rect()
                child.getGlobalVisibleRect(childRect)

                // Calculate vertical center of the view relative to screen
                val childCenterY = childRect.centerY()
                val screenCenterY = screenHeight / 2

                // Distance from center (0 to 1)
                val distanceFromCenter =
                    Math.abs(childCenterY - screenCenterY).toFloat() / (screenHeight / 2)

                // Scale factor: 1.0 at center, drops to 0.9 at edges
                val scaleFactor = 1.0f - (distanceFromCenter * 0.1f).coerceIn(0f, 0.1f)

                child.scaleX = scaleFactor
                child.scaleY = scaleFactor
                child.alpha = 1.0f - (distanceFromCenter * 0.3f).coerceIn(0f, 0.5f)
            }

            // Also animate the "More Games" section if visible
            val moreGamesCard = findViewById<View>(R.id.moreGameCard)
            if (moreGamesCard != null) {
                val rect = Rect()
                moreGamesCard.getGlobalVisibleRect(rect)
                val centerY = rect.centerY()
                val screenCenterY = screenHeight / 2
                val dist = Math.abs(centerY - screenCenterY).toFloat() / (screenHeight / 2)
                val scale = 1.0f - (dist * 0.1f).coerceIn(0f, 0.1f)
                moreGamesCard.scaleX = scale
                moreGamesCard.scaleY = scale
            }


            if (::premiumCarouselManager.isInitialized) {
                premiumCarouselManager.stopAutoScroll()
                scrollHandler.removeCallbacks(scrollRunnable)
                scrollHandler.postDelayed(scrollRunnable, 1000) // Resume after 1s of idle
            }

            // 3. Animations (Search Bar & Footer)
            if (dy > 10) {
                // Scrolling DOWN -> Hide Header & Footer
                if (!isScrollingDown) {
                    isScrollingDown = true
                    animateViews(searchBar, bottomNav, false)
                }
            } else if (dy < -10) {
                // Scrolling UP -> Show Header & Footer
                if (isScrollingDown) {
                    isScrollingDown = false
                    animateViews(searchBar, bottomNav, true)
                }
            }
        })
    }

    private fun animateViews(searchBar: View, bottomNav: View, show: Boolean) {
        val searchY = if (show) 0f else -searchBar.height.toFloat() * 1.5f
        val bottomY = if (show) 0f else bottomNav.height.toFloat()
        val alpha = if (show) 1f else 0f
        val duration = 300L

        searchBar.animate()
            .translationY(searchY)
            .alpha(alpha)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()

        bottomNav.animate()
            .translationY(bottomY)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun Share() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out Gamorax for cool games!")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    /**
     * Attempts to open the Play Store and then redirects the user to a browser link.
     */
    private fun openPlayStoreAndBrowserTogether() {

        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val playStoreIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            // 1️ Launch Play Store first
            startActivity(playStoreIntent)

            // 2️ Use a short delay to trigger the browser intent
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(browserIntent)
            }, 500) // 500ms delay ensures the first intent is processed

        } catch (e: Exception) {
            // Fallback: If Play Store isn't installed, just open the browser
            startActivity(browserIntent)
        }
    }

    private fun highlightMenuItem(activeId: Int) {
        val navItems = listOf(R.id.nav_home, R.id.nav_fav, R.id.nav_arcade)
        for (id in navItems) {
            val icon = when (id) {
                R.id.nav_home -> findViewById<ImageView>(R.id.iv_home)
                R.id.nav_fav -> findViewById<ImageView>(R.id.iv_fav)
                else -> findViewById<ImageView>(R.id.iv_arcade)
            }
            val text = when (id) {
                R.id.nav_home -> findViewById<TextView>(R.id.tv_home)
                R.id.nav_fav -> findViewById<TextView>(R.id.tv_fav)
                else -> findViewById<TextView>(R.id.tv_arcade)
            }

            if (id == activeId) {
                icon?.setColorFilter(ContextCompat.getColor(this, R.color.accent_orange))
                text?.setTextColor(ContextCompat.getColor(this, R.color.accent_orange))
            } else {
                icon?.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary))
                text?.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
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
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
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
                R.id.nav_profile -> startActivity(Intent(this, LoginActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.more_about_us -> startActivity(
                    Intent(
                        this,
                        PremiumCarouselActivity::class.java
                    )
                )

                R.id.nav_logout -> finish()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupCarousel() {
        // Initialize premium carousel manager
        premiumCarouselManager = PremiumCarouselManager(this, lifecycle)

        // Get views from included layout
        val carouselInclude = findViewById<View>(R.id.premium_carousel_include)
        val viewPager = carouselInclude.findViewById<ViewPager2>(R.id.premium_carousel_viewpager)
        // Indicator removed as per request
        val indicatorContainer: LinearLayout? = null

        // Create carousel items from existing games
        val carouselItems = listOf(
            PremiumCarouselItem(
                id = "1",
                title = "Tower Crash 3D",
                subtitle = "Puzzle • Strategy",
                category = "FEATURED",
                imageRes = R.drawable.tower_crash,
                url = "https://play.famobi.com/wrapper/tower-crash-3d/A1000-10",
                isFeatured = true
            ),
            PremiumCarouselItem(
                id = "2",
                title = "Ludum Dare 28",
                subtitle = "Action • Indie",
                category = "NEW",
                imageRes = R.drawable.ludum_dare,
                url = "https://antila.github.io/ludum-dare-28/"
            ),
            PremiumCarouselItem(
                id = "3",
                title = "Zoo Boom",
                subtitle = "Puzzle • Match-3",
                category = "POPULAR",
                imageRes = R.drawable.zoo_boom,
                url = "https://appslabs.store/games/zoo-boom/wrapper/zoo-boom/A1000-10.html"
            ),
            PremiumCarouselItem(
                id = "4",
                title = "Sudoku",
                subtitle = "Puzzle • Brain",
                category = "CLASSIC",
                imageRes = R.drawable.sudoku,
                url = "https://appslabs.store/games/sudoku/"
            ),
            PremiumCarouselItem(
                id = "5",
                title = "Jammu Flight",
                subtitle = "Adventure • Flying",
                category = "TRENDING",
                imageRes = R.drawable.words_of_wonders,
                url = "https://www.google.com/"
            )
        )

        // Initialize carousel
        premiumCarouselManager.initialize(
            viewPager = viewPager,
            indicatorContainer = indicatorContainer,
            items = carouselItems
        ) { item ->
            // Handle carousel item click
            if (item.url.isNotEmpty()) {
                openGame(item.url)
            } else {
                Toast.makeText(this, "${item.title} - Coming Soon!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepareData() {
        val gameRepository = GameRepository()
        allGamesList = gameRepository.getAllGames()
        categorizedData = gameRepository.getCategorizedGames()

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

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this, "ca-app-pub-3940256099942544/5224354917", // Test ID
            adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
            })
    }

    private fun showRewardedAd(onRewarded: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadRewardedAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Toast.makeText(this@MainActivity, "Ad failed to show.", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAdShowedFullScreenContent() {
                    rewardedAd = null
                }
            }
            rewardedAd?.show(this) {
                onRewarded()
            }
        } else {
            Toast.makeText(this, "The rewarded ad wasn't ready yet.", Toast.LENGTH_SHORT).show()
            loadRewardedAd()
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            updateMainList(categorizedData)
            return
        }
        val filteredGames = allGamesList.filter { it.title.contains(query, ignoreCase = true) }
        val searchResult = if (filteredGames.isNotEmpty()) listOf(
            CategoryModel(
                "Search Results",
                filteredGames
            )
        ) else emptyList()
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
            updateMainList(listOf(
                CategoryModel(
                    "My Favorites ❤️",
                    emptyList()
                )
            ))
            return
        }
        updateMainList(listOf(
            CategoryModel(
                "My Favorites ❤️",
                favGames
            )
        ))
    }

    private fun openGame(gameUrl: String) {
        pendingGameUrl = gameUrl

        if (interstitialAd != null) {

            interstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        loadInterstitialAd() // preload next
                        openGameDirect()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        interstitialAd = null
                        openGameDirect()
                    }
                }

            interstitialAd?.show(this)

        } else {
            openGameDirect()
        }
    }

    private fun isConsentExpired(): Boolean {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastConsentTime = prefs.getLong("last_consent_time", 0)
        return (System.currentTimeMillis() - lastConsentTime) > (60 * 60 * 60 * 1000)
    }

    private fun saveConsentTime() {
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
            .putLong("last_consent_time", System.currentTimeMillis()).apply()
    }

    private fun openGameDirect() {
        val url = pendingGameUrl ?: return

        if (isConsentExpired()) {
            openAffiliateThenGame(url)
        } else {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("EXTRA_URL", url)
            startActivity(intent)
        }
    }

//    private fun showAffiliateConsent(gameUrl: String) {
//        androidx.appcompat.app.AlertDialog.Builder(this)
//            .setTitle("Support Us")
//            .setMessage("This game is supported by our partners. You may see a sponsor page briefly before the game starts.")
//            .setPositiveButton("Continue") { _, _ ->
//                saveConsentTime()
//                openAffiliateThenGame(gameUrl)
//            }
//            .setNegativeButton("Cancel") { _, _ -> openGame(gameUrl) }
//            .setCancelable(false)
//            .show()
//    }

    private fun openAffiliateThenGame(gameUrl: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
//            putExtra("EXTRA_URL", "https://track.digitarmedia.com/c?o=1370&a=1011")
            putExtra("NEXT_URL", gameUrl)
        }
        startActivity(intent)
    }

    private fun loadBannerAd() {
        adView = AdView(this)
        adView.adUnitId = "ca-app-pub-3940256099942544/9214589741"
        adViewContainer.addView(adView)

        adView.setAdSize(adSize)  // 👈 directly use property

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712", // ✅ TEST interstitial ID
            adRequest,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    private val adSize: AdSize
        get() {
            val display: Display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val widthPixels = outMetrics.widthPixels.toFloat()
            val density = outMetrics.density

            val adWidth = (widthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    override fun onResume() {
        super.onResume()
        if (::categoryAdapter.isInitialized) categoryAdapter.notifyDataSetChanged()
        highlightMenuItem(R.id.nav_home)
    }
}
