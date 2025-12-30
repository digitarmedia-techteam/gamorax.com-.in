package com.digitar.gamorax

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FavoritesManager {
    private const val PREFS_NAME = "gamorax_prefs"
    private const val KEY_FAVORITES = "favorite_games"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun toggleFavorite(context: Context, game: GameModel) {
        val favorites = getFavorites(context).toMutableList()
        val existing = favorites.find { it.url == game.url }
        
        if (existing != null) {
            favorites.remove(existing)
        } else {
            favorites.add(game)
        }
        
        saveFavorites(context, favorites)
    }

    fun isFavorite(context: Context, gameUrl: String): Boolean {
        return getFavorites(context).any { it.url == gameUrl }
    }

    fun getFavorites(context: Context): List<GameModel> {
        val json = getPrefs(context).getString(KEY_FAVORITES, null) ?: return emptyList()
        val type = object : TypeToken<List<GameModel>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun saveFavorites(context: Context, favorites: List<GameModel>) {
        val json = Gson().toJson(favorites)
        getPrefs(context).edit().putString(KEY_FAVORITES, json).apply()
    }
}