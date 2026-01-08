package com.digitar.gamorax.authorization

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.digitar.gamorax.R
import kotlin.math.max
import kotlin.math.min

class loginActivity : AppCompatActivity() {
    private var dX = 0f
    private var initialX = 0f
    private var isSlid = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupTabNavigation()
        setupSlideButton()
        animateContentEntryFromTopLeft()
        setupSkipButton()
    }
    
    private fun setupSkipButton() {
        findViewById<View>(R.id.skipAuth)?.setOnClickListener {
            startActivity(Intent(this, com.digitar.gamorax.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }
    }

    private fun setupTabNavigation() {
        val loginTab = findViewById<TextView>(R.id.loginTab)
        val signUpTab = findViewById<TextView>(R.id.signUpTab)
        val selectionIndicator = findViewById<View>(R.id.selectionIndicator)

        signUpTab.setOnClickListener {
            selectionIndicator.animate()
                .translationX(selectionIndicator.width.toFloat())
                .setDuration(300)
                .withStartAction {
                    loginTab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                    signUpTab.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                .withEndAction {
                    val intent = Intent(this, SignUpActivity::class.java)
                    startActivity(intent)
//                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }
                .start()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSlideButton() {
        val track = findViewById<View>(R.id.slideButtonTrack)
        val thumb = findViewById<ImageView>(R.id.slideThumb)

        thumb.setOnTouchListener { view, event ->
            val maxSlide = track.width - thumb.width - 10 // Padding

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    initialX = view.x
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isSlid) {
                        var newX = event.rawX + dX
                        newX = max(initialX, min(newX, initialX + maxSlide))
                        view.animate()
                            .x(newX)
                            .setDuration(0)
                            .start()

                        if (newX >= initialX + maxSlide * 0.95) {
                            isSlid = true
                            handleLoginSuccess()
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!isSlid) {
                        view.animate()
                            .x(initialX)
                            .setDuration(300)
                            .start()
                    }
                    true
                }

                else -> false
            }
            true
        }
    }

    private fun handleLoginSuccess() {
        // Trigger haptic feedback
        findViewById<View>(R.id.slideThumb).performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)

        // Navigate to MainActivity or Home
        val intent = Intent(this, com.digitar.gamorax.MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun animateContentEntryFromTopLeft() {
        val form = findViewById<View>(R.id.formContainer)
        val forgot = findViewById<View>(R.id.forgotPassword)
        val d = resources.displayMetrics.density
        val dx = -40f * d
        val dy = -40f * d
        form.translationX = dx
        form.translationY = dy
        form.alpha = 0f
        forgot.translationX = dx
        forgot.translationY = dy
        forgot.alpha = 0f
        form.animate().translationX(0f).translationY(0f).alpha(1f).setDuration(800)
            .setInterpolator(DecelerateInterpolator()).start()
        forgot.animate().translationX(0f).translationY(0f).alpha(1f).setDuration(800)
            .setInterpolator(DecelerateInterpolator()).start()
    }
}