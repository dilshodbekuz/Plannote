package uz.apprica.plannote.data.local.mapper

import uz.apprica.plannote.data.local.entity.NoteEntity
import uz.apprica.plannote.domain.model.Note

fun NoteEntity.toDomain() = Note(
    id          = id,
    title       = title,
    content     = content,
    color       = color,
    isPinned    = isPinned,
    isArchived  = isArchived,
    createdAt   = createdAt,
    updatedAt   = updatedAt,
    reminderAt  = reminderAt
)

fun Note.toEntity() = NoteEntity(
    id          = id,
    title       = title,
    content     = content,
    color       = color,
    isPinned    = isPinned,
    isArchived  = isArchived,
    createdAt   = createdAt,
    updatedAt   = updatedAt,
    reminderAt  = reminderAt
)

fun List<NoteEntity>.toDomain() = map { it.toDomain() }
