package com.xakim.plannote.data.local.mapper

import com.xakim.plannote.data.local.entity.HabitEntity
import com.xakim.plannote.data.local.entity.HabitFrequency
import com.xakim.plannote.domain.model.Frequency
import com.xakim.plannote.domain.model.Habit

fun HabitEntity.toDomain() = Habit(
    id                = id,
    name              = name,
    description       = description,
    iconEmoji         = iconEmoji,
    color             = color,
    frequency         = frequency.toDomain(),
    targetDays        = targetDays,
    targetCount       = targetCount,
    currentStreak     = currentStreak,
    bestStreak        = bestStreak,
    totalCompletions  = totalCompletions,
    isActive          = isActive,
    reminderTime      = reminderTime,
    createdAt         = createdAt
)

fun HabitFrequency.toDomain() = when (this) {
    HabitFrequency.DAILY   -> Frequency.DAILY
    HabitFrequency.WEEKLY  -> Frequency.WEEKLY
    HabitFrequency.MONTHLY -> Frequency.MONTHLY
}

fun Habit.toEntity() = HabitEntity(
    id                = id,
    name              = name,
    description       = description,
    iconEmoji         = iconEmoji,
    color             = color,
    frequency         = frequency.toEntity(),
    targetDays        = targetDays,
    targetCount       = targetCount,
    currentStreak     = currentStreak,
    bestStreak        = bestStreak,
    totalCompletions  = totalCompletions,
    isActive          = isActive,
    reminderTime      = reminderTime,
    createdAt         = createdAt
)

fun Frequency.toEntity() = when (this) {
    Frequency.DAILY   -> HabitFrequency.DAILY
    Frequency.WEEKLY  -> HabitFrequency.WEEKLY
    Frequency.MONTHLY -> HabitFrequency.MONTHLY
}

fun List<HabitEntity>.toDomain() = map { it.toDomain() }
