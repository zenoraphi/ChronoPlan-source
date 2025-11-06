package com.chronoplan.domain.model

data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val birthDate: String? = null,
    val gender: String? = null,
    val level: String = "Newbie",
    val createdAt: Long = 0L
)
