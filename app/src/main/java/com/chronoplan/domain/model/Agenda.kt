package com.chronoplan.domain.model

data class Agenda(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val startAt: Long = 0L,
    val endAt: Long = 0L,
    val status: String = "pending",
    val isFavorite: Boolean = false,
    val reminderMinutesBefore: Int = 30,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
