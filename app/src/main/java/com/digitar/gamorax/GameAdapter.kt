package com.digitar.gamorax

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitar.gamorax.util.AnimationUtils

class GameAdapter(
    private var games: List<GameModel>,
    private val onGameClick: (String) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gameImage: ImageView = view.findViewById(R.id.gameImage)
        val gameTitle: TextView = view.findViewById(R.id.gameTitle)
        val card: View = view.findViewById(R.id.gameCard)
        val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_game_card, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        val context = holder.itemView.context

        holder.gameTitle.text = game.title
        holder.gameImage.setImageResource(game.imageRes)

        // Handle favorite status UI
        val isFav = FavoritesManager.isFavorite(context, game.url)
        holder.ivFavorite.setImageResource(if (isFav) R.drawable.ic_favorite else R.drawable.ic_favorite)
        holder.ivFavorite.setColorFilter(
            if (isFav) context.getColor(android.R.color.holo_red_dark)
            else context.getColor(android.R.color.white)
        )

        holder.card.setOnClickListener { onGameClick(game.url) }

        holder.ivFavorite.setOnClickListener {
            val isCurrentlyFav = FavoritesManager.isFavorite(context, game.url)
            if (!isCurrentlyFav) {
                // Only show animation when adding to favorites
                AnimationUtils.createHeartAnimation(holder.ivFavorite)
                bounceAnimation(holder.ivFavorite)
            }

            FavoritesManager.toggleFavorite(context, game)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = games.size

    fun updateData(newGames: List<GameModel>) {
        this.games = newGames
        notifyDataSetChanged()
    }

    private fun bounceAnimation(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1.2f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1.2f, 1.0f)
        scaleX.duration = 500
        scaleY.duration = 500
        scaleX.interpolator = BounceInterpolator()
        scaleY.interpolator = BounceInterpolator()
        scaleX.start()
        scaleY.start()
    }
}
