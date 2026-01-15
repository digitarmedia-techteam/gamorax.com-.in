package com.digitar.gamorax.ui.carousel

import com.digitar.gamorax.R

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView

class PremiumCarouselManager(
    private val context: Context,
    private val lifecycle: Lifecycle
) : DefaultLifecycleObserver {

    companion object {
        private const val AUTO_SCROLL_INTERVAL = 3000L // 3 seconds
        private const val INITIAL_DELAY = 2000L // 2 seconds initial delay
    }

    private var viewPager: ViewPager2? = null
    private var indicatorContainer: LinearLayout? = null
    private var adapter: PremiumCarouselAdapter? = null
    private var items: List<PremiumCarouselItem> = emptyList()
    
    private val handler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null
    private var lastScrollTime = 0L
    private var isUserScrolling = false
    
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            updateIndicators(position)
            lastScrollTime = SystemClock.elapsedRealtime()
        }
        
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            when (state) {
                ViewPager2.SCROLL_STATE_DRAGGING -> {
                    isUserScrolling = true
                    stopAutoScroll()
                }
                ViewPager2.SCROLL_STATE_IDLE -> {
                    isUserScrolling = false
                    startAutoScroll()
                }
            }
        }
    }

    fun initialize(
        viewPager: ViewPager2,
        indicatorContainer: LinearLayout?,
        items: List<PremiumCarouselItem>,
        onItemClick: (PremiumCarouselItem) -> Unit
    ) {
        this.viewPager = viewPager
        this.indicatorContainer = indicatorContainer
        this.items = items
        
        setupViewPager(items, onItemClick)
        setupIndicators()
        setupAutoScroll()
        
        lifecycle.addObserver(this)
    }

    private fun setupViewPager(items: List<PremiumCarouselItem>, onItemClick: (PremiumCarouselItem) -> Unit) {
        adapter = PremiumCarouselAdapter(items, onItemClick).apply {
            enableInfiniteScroll(items.size > 1)
        }
        
        viewPager?.apply {
            this.adapter = this@PremiumCarouselManager.adapter
            setPageTransformer(PremiumCarouselTransformer())
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeCallback)
            
            // Set initial position to middle for infinite scroll effect
            if (items.size > 1) {
                setCurrentItem(Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2 % items.size), false)
            }
            
            // Configure RecyclerView for better performance
            (getChildAt(0) as? RecyclerView)?.let { recyclerView ->
                recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
                recyclerView.isNestedScrollingEnabled = false
                
                // Set item decoration for spacing and width
                recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        outRect.left = 20
                        outRect.right = 20
                    }
                })
            }
        }
    }

    private fun setupIndicators() {
        indicatorContainer?.removeAllViews()
        val itemCount = items.takeIf { it.isNotEmpty() }?.size ?: return
        
        repeat(itemCount) { index ->
            val indicator = View(context)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(4, 0, 4, 0)
            }
            indicator.layoutParams = params
            
            val size = if (index == 0) 12f else 8f
            indicator.setBackgroundResource(R.drawable.carousel_indicator)
            
            // Set initial size
            indicator.scaleX = size / 12f
            indicator.scaleY = size / 12f
            indicator.alpha = if (index == 0) 1f else 0.5f
            
            indicatorContainer?.addView(indicator)
        }
    }

    private fun updateIndicators(position: Int) {
        val itemCount = items.size
        if (itemCount == 0) return
        
        val realPosition = position % itemCount
        
        indicatorContainer?.let { container ->
            for (i in 0 until container.childCount) {
                val indicator = container.getChildAt(i)
                val isActive = i == realPosition
                
                indicator.animate()
                    .scaleX(if (isActive) 1f else 0.67f)
                    .scaleY(if (isActive) 1f else 0.67f)
                    .alpha(if (isActive) 1f else 0.5f)
                    .setDuration(200)
                    .start()
            }
        }
    }

    private fun setupAutoScroll() {
        autoScrollRunnable = object : Runnable {
            override fun run() {
                if (!isUserScrolling && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    viewPager?.let { vp ->
                        if (items.size > 1 && !vp.isFakeDragging) {
                            // Custom Smooth Scroll for Premium Feel
                            try {
                                if (vp.beginFakeDrag()) {
                                    val width = vp.width
                                    // Slower, cinematic duration (800ms) with ease-out
                                    val animator = android.animation.ValueAnimator.ofInt(0, width)
                                    animator.duration = 800L 
                                    animator.interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                                    
                                    var previousValue = 0
                                    animator.addUpdateListener { animation ->
                                        val currentValue = animation.animatedValue as Int
                                        val delta = currentValue - previousValue
                                        try {
                                            vp.fakeDragBy(-delta.toFloat())
                                            previousValue = currentValue
                                        } catch (e: Exception) {
                                            // Handle potential crash if drag is interrupted
                                        }
                                    }
                                    
                                    animator.addListener(object : android.animation.AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: android.animation.Animator) {
                                            try {
                                                vp.endFakeDrag()
                                            } catch (e: Exception) { }
                                        }
                                        
                                        override fun onAnimationCancel(animation: android.animation.Animator) {
                                            try {
                                                vp.endFakeDrag()
                                            } catch (e: Exception) { }
                                        }
                                    })
                                    animator.start()
                                }
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    }
                }
                
                // Schedule next scroll
                handler.postDelayed(this, AUTO_SCROLL_INTERVAL)
            }
        }
        
        startAutoScroll()
    }

    fun startAutoScroll() {
        if (items.size <= 1) return
        
        stopAutoScroll()
        handler.postDelayed(autoScrollRunnable!!, INITIAL_DELAY)
    }

    fun stopAutoScroll() {
        handler.removeCallbacks(autoScrollRunnable!!)
    }

    // Lifecycle callbacks
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        startAutoScroll()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        stopAutoScroll()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        viewPager?.unregisterOnPageChangeCallback(pageChangeCallback)
        stopAutoScroll()
        lifecycle.removeObserver(this)
    }

    // Public methods
    fun updateItems(newItems: List<PremiumCarouselItem>) {
        items = newItems
        adapter?.let { 
            // Create new adapter with updated items
            val currentCallback = object : ViewPager2.OnPageChangeCallback() {}
            viewPager?.unregisterOnPageChangeCallback(pageChangeCallback)
            
            adapter = PremiumCarouselAdapter(newItems) { item ->
                // Handle item click - you may want to expose this as a callback
            }.apply {
                enableInfiniteScroll(newItems.size > 1)
            }
            
            viewPager?.adapter = adapter
            viewPager?.registerOnPageChangeCallback(pageChangeCallback)
            
            setupIndicators()
        }
    }
}
