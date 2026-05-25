package uz.apprica.plannote.data.local.mapper

import uz.apprica.plannote.data.local.entity.HabitLogEntity
import uz.apprica.plannote.domain.model.HabitLog

fun HabitLogEntity.toDomain() = HabitLog(
    id          = id,
    habitId     = habitId,
    date        = date,
    completedAt = completedAt
)

fun HabitLog.toEntity() = HabitLogEntity(
    id          = id,
    habitId     = habitId,
    date        = date,
    completedAt = completedAt
)

fun List<HabitLogEntity>.toDomain() = map { it.toDomain() }
