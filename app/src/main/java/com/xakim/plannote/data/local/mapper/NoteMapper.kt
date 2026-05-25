package com.xakim.plannote.data.local.mapper

import com.xakim.plannote.data.local.entity.NoteEntity
import com.xakim.plannote.domain.model.Note

fun NoteEntity.toDomain() = Note(
    id          = id,
    title       = title,
    content     = content,
    color       = color,
    isPinned    = isPinned,
    isArchived  = isArchived,
    createdAt   = createdAt,
    updatedAt   = updatedAt
)

fun Note.toEntity() = NoteEntity(
    id          = id,
    title       = title,
    content     = content,
    color       = color,
    isPinned    = isPinned,
    isArchived  = isArchived,
    createdAt   = createdAt,
    updatedAt   = updatedAt
)

fun List<NoteEntity>.toDomain() = map { it.toDomain() }
