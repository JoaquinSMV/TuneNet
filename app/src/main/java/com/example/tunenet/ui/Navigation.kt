package com.example.tunenet.ui

sealed class Screen(val route: String) {
    object List : Screen("list")
    object Detail : Screen("detail/{trackId}") {
        fun createRoute(trackId: Long) = "detail/$trackId"
    }
    object Favorites : Screen("favorites")
    object FavDetail : Screen("fav_detail/{trackId}") {
        fun createRoute(trackId: Long) = "fav_detail/$trackId"
    }
    object Profile : Screen("profile")
    object About : Screen("about")
}
