package com.digitar.gamorax

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

data class PremiumCarouselItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val imageRes: Int,
    val url: String,
    val isFeatured: Boolean = false
)

class PremiumCarouselAdapter(
    private val items: List<PremiumCarouselItem>,
    private val onClick: (PremiumCarouselItem) -> Unit
) : RecyclerView.Adapter<PremiumCarouselAdapter.ViewHolder>() {

    private var isInfinite = false
    private val realItemCount get() = if (items.isEmpty()) 0 else items.size
    private val infiniteItemCount get() = if (isInfinite && realItemCount > 1) Int.MAX_VALUE else realItemCount

    fun enableInfiniteScroll(enable: Boolean) {
        isInfinite = enable && realItemCount > 1
        notifyDataSetChanged()
    }

    fun getRealPosition(position: Int): Int {
        return if (isInfinite) position % realItemCount else position
    }

    override fun getItemCount(): Int = infiniteItemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.premium_carousel_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val realPosition = getRealPosition(position)
        val item = items[realPosition]
        
        holder.bind(item, onClick)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val container: MaterialCardView = view.findViewById(R.id.carousel_item_container)
        private val backgroundImage: ImageView = view.findViewById(R.id.carousel_background_image)
        private val title: TextView = view.findViewById(R.id.carousel_title)
        private val subtitle: TextView = view.findViewById(R.id.carousel_subtitle)
        private val category: TextView = view.findViewById(R.id.carousel_category)

        fun bind(item: PremiumCarouselItem, onClick: (PremiumCarouselItem) -> Unit) {
            backgroundImage.setImageResource(item.imageRes)
            title.text = item.title
            subtitle.text = item.subtitle
            category.text = item.category.uppercase()
            
            // Apply featured styling
            if (item.isFeatured) {
                container.strokeColor = 0xFF00FF88.toInt()
                container.strokeWidth = 2
                category.setBackgroundResource(R.drawable.featured_badge_background)
            } else {
                container.strokeWidth = 0
                category.setBackgroundResource(R.drawable.category_badge_background)
            }
            
            container.setOnClickListener {
                onClick(item)
            }
        }
    }
}
