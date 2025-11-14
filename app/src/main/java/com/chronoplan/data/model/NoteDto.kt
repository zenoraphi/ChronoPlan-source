package com.chronoplan.data.model

data class NoteDto(
    val id: String = "",
    val title: String = "",
    val contentPreview: String = "",
    val content: String = "",
    val labels: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),
    val isFavorite: Boolean = false, // ✅ PASTIKAN ADA & TYPO-FREE
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)