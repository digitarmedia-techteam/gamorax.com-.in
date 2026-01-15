package com.digitar.gamorax.ui.main

import com.digitar.gamorax.R
import com.digitar.gamorax.data.model.CarouselItem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CarouselAdapter(
    private val items: List<CarouselItem>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.carousel_image)
        val title: TextView = view.findViewById(R.id.carousel_title)
        val container: View = view.findViewById(R.id.carousel_item_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.image.setImageResource(item.imageRes)
        holder.title.text = item.title
        holder.container.setOnClickListener { onClick(item.url) }
    }

    override fun getItemCount() = items.size
}