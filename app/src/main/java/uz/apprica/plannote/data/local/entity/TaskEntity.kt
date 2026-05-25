package uz.apprica.plannote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority { LOW, MEDIUM, HIGH }
enum class TaskStatus  { TODO, IN_PROGRESS, DONE }

@Entity(tableName = "tasks")
data class TaskEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "priority")
    val priority: TaskPriority = TaskPriority.MEDIUM,

    @ColumnInfo(name = "status")
    val status: TaskStatus = TaskStatus.TODO,

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,

    @ColumnInfo(name = "reminder_at")
    val reminderAt: Long? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "category")
    val category: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
