package com.chronoplan.data.model

data class AgendaDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "", // yyyy-MM-dd
    val startAt: Long = 0L, // epoch millis
    val endAt: Long = 0L,
    val status: String = "pending", // pending|done|missed|canceled
    val isFavorite: Boolean = false,
    val reminderMinutesBefore: Int = 30,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
