package com.xakim.plannote.data.local.converter

import androidx.room.TypeConverter
import com.xakim.plannote.data.local.entity.HabitFrequency
import com.xakim.plannote.data.local.entity.TaskPriority
import com.xakim.plannote.data.local.entity.TaskStatus

class RoomConverters {

    // ── TaskPriority ───────────────────────────────────────────────────────
    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    // ── TaskStatus ─────────────────────────────────────────────────────────
    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    // ── HabitFrequency ─────────────────────────────────────────────────────
    @TypeConverter
    fun fromHabitFrequency(value: HabitFrequency): String = value.name

    @TypeConverter
    fun toHabitFrequency(value: String): HabitFrequency = HabitFrequency.valueOf(value)
}
