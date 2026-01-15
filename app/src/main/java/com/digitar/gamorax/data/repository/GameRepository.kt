package com.digitar.gamorax.data.repository

import com.digitar.gamorax.R
import com.digitar.gamorax.data.model.GameModel
import com.digitar.gamorax.data.model.CategoryModel

class GameRepository {

    fun getAllGames(): List<GameModel> {
        return listOf(
            GameModel("Tower Crash", R.drawable.tower_crash, "https://play.famobi.com/wrapper/tower-crash-3d/A1000-10"),
            GameModel("Ludum Dare", R.drawable.ludum_dare, "https://antila.github.io/ludum-dare-28/"),
            GameModel("Zoo Boom", R.drawable.zoo_boom, "https://appslabs.store/games/zoo-boom/wrapper/zoo-boom/A1000-10.html"),
            GameModel("Sudoku", R.drawable.sudoku, "https://appslabs.store/games/sudoku/"),
            GameModel("Jammu Flight", R.drawable.words_of_wonders, "https://www.google.com/"),
            GameModel("Quiz Master", R.drawable.quiz, "https://appslabs.store/games/click-combo-quiz"),
            GameModel("Color Match", R.drawable.ic_coin, "https://gamemmm.netlify.app/colormatch"),
            GameModel("Circle Shooter", R.drawable.digitarmedia_logo, "https://gamemmm.netlify.app/circleshooter"),
            GameModel("Bubble Shooter", R.drawable.hart_bg, "https://gamemmm.netlify.app/"),
            GameModel("Endless Runner Dash", R.drawable.header_glow_gradient, "https://gamemmm.netlify.app/endlessrunnerdash"),
            GameModel("Snake", R.drawable.wallet_bgr, "http://gamemmm.netlify.app/snakereloaded")
        )
    }

    fun getCategorizedGames(): List<CategoryModel> {
        val allGames = getAllGames()
        return listOf(
            CategoryModel("Quick Play", allGames.shuffled()),
            CategoryModel("Puzzle & Brain Games", allGames.shuffled()),
            CategoryModel("Action Games", allGames.shuffled()),
            CategoryModel("Adventure Games", allGames.shuffled())
        )
    }
}
