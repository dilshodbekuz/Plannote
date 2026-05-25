package uz.apprica.plannote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HabitFrequency { DAILY, WEEKLY, MONTHLY }

@Entity(tableName = "habits")
data class HabitEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "icon_emoji")
    val iconEmoji: String = "⭐",

    @ColumnInfo(name = "color")
    val color: Int = 0,

    @ColumnInfo(name = "frequency")
    val frequency: HabitFrequency = HabitFrequency.DAILY,

    /** Haftaning qaysi kunlari: bit-mask  0=Yak, 1=Du, ..., 6=Shan */
    @ColumnInfo(name = "target_days")
    val targetDays: Int = 0b1111111,   // default: har kun

    @ColumnInfo(name = "target_count")
    val targetCount: Int = 1,

    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 0,

    @ColumnInfo(name = "best_streak")
    val bestStreak: Int = 0,

    @ColumnInfo(name = "total_completions")
    val totalCompletions: Int = 0,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: String? = null,   // "HH:mm" format

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
