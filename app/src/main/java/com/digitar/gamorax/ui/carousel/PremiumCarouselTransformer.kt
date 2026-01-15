package com.digitar.gamorax.ui.carousel

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class PremiumCarouselTransformer : ViewPager2.PageTransformer {

    companion object {
        private const val MIN_SCALE = 0.75f
        private const val MIN_ALPHA = 0.5f
        private const val MAX_ELEVATION = 30f
        private const val MIN_ELEVATION = 8f
    }

    override fun transformPage(page: View, position: Float) {
        val absPosition = abs(position)
        
        when {
            // Center page (fully visible)
            absPosition <= 1f -> {
                // Scale effect - center item is larger
                val scaleFactor = (MIN_SCALE + (1 - MIN_SCALE) * (1 - absPosition))
                page.scaleX = scaleFactor
                page.scaleY = scaleFactor
                
                // Alpha effect - center item is fully opaque
                page.alpha = (MIN_ALPHA + (1 - MIN_ALPHA) * (1 - absPosition))
                
                // Elevation effect - center item has higher elevation
                if (page is com.google.android.material.card.MaterialCardView) {
                    page.cardElevation = (MIN_ELEVATION + (MAX_ELEVATION - MIN_ELEVATION) * (1 - absPosition))
                }
                
                // Translation Z for depth effect
                page.translationZ = (MAX_ELEVATION - MIN_ELEVATION) * (1 - absPosition)
                
                // Subtle rotation for 3D effect
                page.rotationY = -position * 15f
                
                // Translation X for overlapping effect (accounts for margins)
                page.translationX = -position * (page.width * 0.3f + 40f) // Added margin offset
                
            }
            // Side pages (partially visible)
            else -> {
                page.alpha = 0f
                page.scaleX = MIN_SCALE
                page.scaleY = MIN_SCALE
                if (page is com.google.android.material.card.MaterialCardView) {
                    page.cardElevation = MIN_ELEVATION
                }
                page.translationZ = 0f
                page.rotationY = 0f
                page.translationX = 0f
            }
        }
        
        // Ensure the page clips properly
        page.clipToOutline = true
    }
}
