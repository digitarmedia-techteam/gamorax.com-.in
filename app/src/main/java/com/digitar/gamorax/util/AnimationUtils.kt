package com.digitar.gamorax.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.digitar.gamorax.R
import java.util.Random

object AnimationUtils {

    private val random = Random()

    fun createHeartAnimation(anchor: View) {
        val container = anchor.rootView as ViewGroup
        val heartCount = 6 + random.nextInt(5) // Create 6 to 10 hearts

        for (i in 0 until heartCount) {
            val heart = createHeartView(anchor.context, container, R.drawable.ic_favorite)
            val animator = createHeartAnimator(heart, anchor, container)
            animator.start()
        }
    }

    private fun createHeartView(context: Context, container: ViewGroup, @DrawableRes heartDrawable: Int): ImageView {
        val heart = ImageView(context)
        heart.setImageResource(heartDrawable)
        val size = (48 * context.resources.displayMetrics.density).toInt() // 48dp
        val params = ViewGroup.LayoutParams(size, size)
        heart.layoutParams = params
        container.addView(heart)
        return heart
    }

    private fun createHeartAnimator(heart: View, anchor: View, container: ViewGroup): AnimatorSet {
        // Initial position at the center of the anchor view
        val startLocation = IntArray(2)
        anchor.getLocationInWindow(startLocation)
        val anchorX = startLocation[0].toFloat() + anchor.width / 2f - heart.layoutParams.width / 2f
        val anchorY = startLocation[1].toFloat() + anchor.height / 2f - heart.layoutParams.height / 2f

        heart.x = anchorX
        heart.y = anchorY
        heart.alpha = 0.8f
        heart.scaleX = 0.5f
        heart.scaleY = 0.5f

        // Animate upwards and slightly to the sides
        val endY = anchorY - (200 + random.nextInt(150)) * container.context.resources.displayMetrics.density
        val endX = anchorX + (random.nextFloat() * 100 - 50) * container.context.resources.displayMetrics.density // -50dp to +50dp

        val translationY = ObjectAnimator.ofFloat(heart, View.TRANSLATION_Y, anchorY, endY)
        val translationX = ObjectAnimator.ofFloat(heart, View.TRANSLATION_X, anchorX, endX)
        translationY.interpolator = AccelerateDecelerateInterpolator()
        translationX.interpolator = AccelerateDecelerateInterpolator()

        val scaleX = ObjectAnimator.ofFloat(heart, View.SCALE_X, 0.5f, 1.2f)
        val scaleY = ObjectAnimator.ofFloat(heart, View.SCALE_Y, 0.5f, 1.2f)

        val fadeOut = ObjectAnimator.ofFloat(heart, View.ALPHA, 0.8f, 0f)
        fadeOut.startDelay = 500 // Start fading out after a delay

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(translationY, translationX, scaleX, scaleY, fadeOut)
        animatorSet.duration = 1500L + random.nextInt(500)

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                container.removeView(heart)
            }
        })
        return animatorSet
    }
}
