package com.chronoplan.data.model

data class UserProfileDto(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val birthDate: String? = null, // yyyy-MM-dd optional
    val gender: String? = null,
    val level: String = "Newbie",
    val createdAt: Long = 0L
)
