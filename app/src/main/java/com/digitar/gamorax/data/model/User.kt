package com.digitar.gamorax.data.model

data class User(
    val userId: String = "",
    val username: String = "",
    val bio: String = "",
    val profileImage: String = "",
    val email: String = "",
    val isGuest: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
