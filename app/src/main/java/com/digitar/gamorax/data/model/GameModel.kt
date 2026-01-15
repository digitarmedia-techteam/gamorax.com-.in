package com.digitar.gamorax.data.model

import com.google.gson.annotations.SerializedName

data class GameModel(
    @SerializedName("title") val title: String,
    @SerializedName("imageRes") val imageRes: Int,
    @SerializedName("url") val url: String
)

data class CarouselItem(
    val title: String,
    val imageRes: Int,
    val url: String
)

data class CategoryModel(
    val title: String,
    val games: List<GameModel>
)