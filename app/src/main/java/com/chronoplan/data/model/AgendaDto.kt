package com.chronoplan.data.model

data class AgendaDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val startAt: Long = 0L,
    val endAt: Long = 0L,
    val status: String = "pending",
    val isFavorite: Boolean = false, // ✅ PASTIKAN ADA & TYPO-FREE
    val category: String = "",
    val reminderMinutesBefore: Int = 30,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)