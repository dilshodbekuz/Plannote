package uz.apprica.plannote.data.local.mapper

import uz.apprica.plannote.data.local.entity.TaskEntity
import uz.apprica.plannote.data.local.entity.TaskPriority
import uz.apprica.plannote.data.local.entity.TaskStatus
import uz.apprica.plannote.domain.model.Priority
import uz.apprica.plannote.domain.model.Status
import uz.apprica.plannote.domain.model.Task

// ── Entity → Domain ────────────────────────────────────────────────────────

fun TaskEntity.toDomain() = Task(
    id          = id,
    title       = title,
    description = description,
    priority    = priority.toDomain(),
    status      = status.toDomain(),
    dueDate     = dueDate,
    reminderAt  = reminderAt,
    isCompleted = isCompleted,
    category    = category,
    createdAt   = createdAt,
    updatedAt   = updatedAt
)

fun TaskPriority.toDomain() = when (this) {
    TaskPriority.LOW    -> Priority.LOW
    TaskPriority.MEDIUM -> Priority.MEDIUM
    TaskPriority.HIGH   -> Priority.HIGH
}

fun TaskStatus.toDomain() = when (this) {
    TaskStatus.TODO        -> Status.TODO
    TaskStatus.IN_PROGRESS -> Status.IN_PROGRESS
    TaskStatus.DONE        -> Status.DONE
}

// ── Domain → Entity ────────────────────────────────────────────────────────

fun Task.toEntity() = TaskEntity(
    id          = id,
    title       = title,
    description = description,
    priority    = priority.toEntity(),
    status      = status.toEntity(),
    dueDate     = dueDate,
    reminderAt  = reminderAt,
    isCompleted = isCompleted,
    category    = category,
    createdAt   = createdAt,
    updatedAt   = updatedAt
)

fun Priority.toEntity() = when (this) {
    Priority.LOW    -> TaskPriority.LOW
    Priority.MEDIUM -> TaskPriority.MEDIUM
    Priority.HIGH   -> TaskPriority.HIGH
}

fun Status.toEntity() = when (this) {
    Status.TODO        -> TaskStatus.TODO
    Status.IN_PROGRESS -> TaskStatus.IN_PROGRESS
    Status.DONE        -> TaskStatus.DONE
}

fun List<TaskEntity>.toDomain() = map { it.toDomain() }
