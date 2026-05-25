package uz.apprica.plannote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("habit_id"),
        Index(value = ["habit_id", "date"], unique = true)   // bir kunda faqat 1 log
    ]
)
data class HabitLogEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "habit_id")
    val habitId: Long,

    /** Kuni boshi (midnight) timestamp — startOfDay() orqali hisoblangan */
    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long = System.currentTimeMillis()
)
