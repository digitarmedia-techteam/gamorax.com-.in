package com.digitar.gamorax.ui.profile

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView // Added
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide // Added
import com.digitar.gamorax.R
import android.graphics.Rect
import android.widget.LinearLayout
import com.digitar.gamorax.data.auth.AuthManager // Changed
import com.digitar.gamorax.data.model.User

class profile : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnClose: ImageButton
    private lateinit var cardProfileHeader: CardView
    private lateinit var achievementLayout: View
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var avatarGlow: View
    private lateinit var ivProfileAvatar: ImageView // Added
    private lateinit var pbLevel: ProgressBar
    private lateinit var layoutStats: LinearLayout
    private var hasAnimatedAchievements = false
    
    // User data
    private lateinit var authManager: AuthManager // Changed
    private lateinit var tvUsername: TextView
    private lateinit var tvUserTitle: TextView
    private lateinit var tvEmail: TextView // Added
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() - Commented out as it might cause issues if not setup correctly in themes
        setContentView(R.layout.activity_profile)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.toolbar).setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        authManager = AuthManager(this)
        initViews()
        setupListeners()
        loadUserData()
        startEntranceAnimations()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
//        btnClose = findViewById(R.id.btnClose)
        cardProfileHeader = findViewById(R.id.cardProfileHeader)
        achievementLayout = findViewById(R.id.achievementLayout)
        nestedScrollView = findViewById(R.id.nestedScrollView)
        avatarGlow = findViewById(R.id.avatarGlow)
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar) // Added join
        pbLevel = findViewById(R.id.pbLevel)
        layoutStats = findViewById(R.id.layoutStats)
        tvUsername = findViewById(R.id.tvUsername)
        tvUserTitle = findViewById(R.id.tvUserTitle)
        tvEmail = findViewById(R.id.tvEmail) // Added join
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
//        btnClose.setOnClickListener { finish() }
        
        // Logout button
        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            handleLogout()
        }

        // Add edit profile button listener
        tvUsername.setOnClickListener {
            showEditProfileDialog()
        }

        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            checkScrollAnimations(scrollY)
        })
    }
    
    private fun handleLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout? You can login again anytime.")
            .setPositiveButton("Yes, Logout") { _, _ ->
                authManager.signOut()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                finish() // Close profile and return to main
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun loadUserData() {
        val userId = authManager.getCurrentUserId()
        
        if (userId != null) {
            authManager.getUserData(
                userId = userId,
                onSuccess = { user ->
                    currentUser = user
                    updateUI(user)
                },
                onFailure = { exception ->
                    Toast.makeText(
                        this,
                        "Failed to load profile: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } else {
            // No user logged in
            tvUsername.text = "Guest User"
            tvUserTitle.text = "Not logged in"
            tvEmail.visibility = View.GONE
        }
    }
    
    private fun updateUI(user: User) {
        tvUsername.text = user.username
        tvUserTitle.text = if (user.bio.isNotEmpty()) user.bio else "Elite Warrior"
        
        // Email
        if (user.email.isNotEmpty()) {
            tvEmail.text = user.email
            tvEmail.visibility = View.VISIBLE
        } else {
            tvEmail.visibility = View.GONE
        }

        // Profile Image
        if (user.profileImage.isNotEmpty()) {
            if (user.profileImage.startsWith("http")) {
                Glide.with(this)
                    .load(user.profileImage)
                    .circleCrop()
                    .placeholder(R.drawable.profile_avatar_v2)
                    .error(R.drawable.profile_avatar_v2)
                    .into(ivProfileAvatar)
            } else {
                // Handle local drawable logic if needed, or just keep default
                // existing logic seemed to rely on default
            }
        }
    }
    
    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etBio = dialogView.findViewById<EditText>(R.id.etBio)
        
        // Pre-fill current data
        currentUser?.let {
            etUsername.setText(it.username)
            etBio.setText(it.bio)
        }
        
        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = etUsername.text.toString().trim()
                val newBio = etBio.text.toString().trim()
                
                if (newUsername.isEmpty()) {
                    Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (newUsername.length > 20) {
                    Toast.makeText(this, "Username too long (max 20 characters)", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                saveProfileChanges(newUsername, newBio)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun saveProfileChanges(username: String, bio: String) {
        val userId = authManager.getCurrentUserId() ?: return
        
        authManager.updateUserProfile(
            userId = userId,
            username = username,
            bio = bio,
            onSuccess = {
                // Update local user object
                currentUser = currentUser?.copy(username = username, bio = bio)
                
                // Update UI
                currentUser?.let { updateUI(it) }
                
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            },
            onFailure = { exception ->
                Toast.makeText(
                    this,
                    "Failed to update profile: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun startEntranceAnimations() {
        // 1. Card Slide Up
        val slideUpFade = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
        cardProfileHeader.startAnimation(slideUpFade)

        // 2. Avatar Pulse
        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse_glow)
        avatarGlow.startAnimation(pulse)

        // 3. Progress Bar Animation
        val anim = android.animation.ObjectAnimator.ofInt(pbLevel, "progress", 0, 75)
        anim.duration = 1000
        anim.interpolator = android.view.animation.DecelerateInterpolator()
        anim.start()

        // 4. Staggered Stats Fade In
        layoutStats.alpha = 0f
        layoutStats.animate()
            .alpha(1f)
            .setStartDelay(300)
            .setDuration(600)
            .start()
    }

    private fun checkScrollAnimations(scrollY: Int) {
        val scrollBounds = Rect()
        nestedScrollView.getHitRect(scrollBounds)

        if (achievementLayout is LinearLayout) {
            val layout = achievementLayout as LinearLayout
            for (i in 0 until layout.childCount) {
                val child = layout.getChildAt(i)
                if (child.tag != "animated" && child.getLocalVisibleRect(scrollBounds)) {
                    val scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in)
                    scaleIn.startOffset = (i * 100).toLong()
                    child.startAnimation(scaleIn)
                    child.tag = "animated"
                }
            }
        }
    }
}