package com.digitar.gamorax

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameAdapter(
    private val games: List<GameModel>,
    private val onGameClick: (String) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gameImage: ImageView = view.findViewById(R.id.gameImage)
        val gameTitle: TextView = view.findViewById(R.id.gameTitle)
        val card: View = view.findViewById(R.id.gameCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_game_card, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.gameTitle.text = game.title
        holder.gameImage.setImageResource(game.imageRes)
        holder.card.setOnClickListener { onGameClick(game.url) }
    }

    override fun getItemCount() = games.size
}

data class GameModel(val title: String, val imageRes: Int, val url: String)