package com.chronoplan.domain.mapper

import com.chronoplan.data.model.AgendaDto
import com.chronoplan.data.model.NoteDto
import com.chronoplan.data.model.UserProfileDto
import com.chronoplan.domain.model.Agenda
import com.chronoplan.domain.model.Note
import com.chronoplan.domain.model.UserProfile

fun AgendaDto.toDomain(): Agenda = Agenda(
    id = this.id,
    title = this.title,
    description = this.description,
    date = this.date,
    startAt = this.startAt,
    endAt = this.endAt,
    status = this.status,
    isFavorite = this.isFavorite,
    reminderMinutesBefore = this.reminderMinutesBefore,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun Agenda.toDto(): AgendaDto = AgendaDto(
    id = this.id,
    title = this.title,
    description = this.description,
    date = this.date,
    startAt = this.startAt,
    endAt = this.endAt,
    status = this.status,
    isFavorite = this.isFavorite,
    reminderMinutesBefore = this.reminderMinutesBefore,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun NoteDto.toDomain(): Note = Note(
    id = this.id,
    title = this.title,
    contentPreview = this.contentPreview,
    content = this.content,
    labels = this.labels,
    attachments = this.attachments,
    isFavorite = this.isFavorite,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun Note.toDto(): NoteDto = NoteDto(
    id = this.id,
    title = this.title,
    contentPreview = this.contentPreview,
    content = this.content,
    labels = this.labels,
    attachments = this.attachments,
    isFavorite = this.isFavorite,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    id = this.uid,
    displayName = this.displayName,
    email = this.email,
    avatarUrl = this.avatarUrl,
    birthDate = this.birthDate,
    gender = this.gender,
    level = this.level,
    createdAt = this.createdAt
)

fun UserProfile.toDto(): UserProfileDto = UserProfileDto(
    uid = this.id,
    displayName = this.displayName,
    email = this.email,
    avatarUrl = this.avatarUrl,
    birthDate = this.birthDate,
    gender = this.gender,
    level = this.level,
    createdAt = this.createdAt
)