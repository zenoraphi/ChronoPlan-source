package com.chronoplan.data.model

data class AchievementDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconRes: Int = 0,
    val requiredCount: Int = 0,
    val currentCount: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L,
    val category: String = "" // agenda, note, streak
)

data class UserStatsDto(
    val uid: String = "",
    val totalAgendas: Int = 0,
    val completedAgendas: Int = 0,
    val totalNotes: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: String = "", // yyyy-MM-dd
    val joinedDate: Long = 0L,
    val level: Int = 1,
    val experience: Int = 0
)