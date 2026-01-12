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

class SignUpActivity : AppCompatActivity() {
    private var dX = 0f
    private var initialX = 0f
    private var isSlid = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupTabNavigation()
        setupSlideButton()
        animateContentEntryFromBottomRight()
        setupSkipButton()
    }

    private fun setupTabNavigation() {
//        val loginTab = findViewById<TextView>(R.id.loginTab)
        val signUpTab = findViewById<TextView>(R.id.tvSwitchToSignup)
//        val selectionIndicator = findViewById<View>(R.id.selectionIndicator)

//        loginTab.setOnClickListener {
//            selectionIndicator.animate()
//                .translationX(-selectionIndicator.width.toFloat())
//                .setDuration(300)
//                .withStartAction {
//                    loginTab.setTextColor(ContextCompat.getColor(this, R.color.white))
//                    signUpTab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
//                }
//                .withEndAction {
//                    val intent = Intent(this, LoginActivity::class.java)
//                    startActivity(intent)
////                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
//                    finish()
//                }
//                .start()
//        }
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
                    true // We are handling the touch event
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
                            handleSignUpSuccess()
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
        }
    }

    private fun handleSignUpSuccess() {
        findViewById<View>(R.id.slideThumb).performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
        // Navigate to MainActivity or Home
        val intent = Intent(this, com.digitar.gamorax.MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun animateContentEntryFromBottomRight() {
        val form = findViewById<View>(R.id.formContainer)
        val d = resources.displayMetrics.density
        val dx = 40f * d
        val dy = 40f * d
        form.translationX = dx
        form.translationY = dy
        form.alpha = 0f
        form.animate()
            .translationX(0f)
            .translationY(0f)
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun setupSkipButton() {
        findViewById<View>(R.id.skipAuth)?.setOnClickListener {
            startActivity(Intent(this, com.digitar.gamorax.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }
    }
}
