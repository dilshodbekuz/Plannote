package uz.apprica.plannote.domain.model

enum class Priority { LOW, MEDIUM, HIGH }
enum class Status   { TODO, IN_PROGRESS, DONE }

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val status: Status = Status.TODO,
    val dueDate: Long? = null,
    val reminderAt: Long? = null,
    val isCompleted: Boolean = false,
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isOverdue: Boolean
        get() = dueDate != null && dueDate < System.currentTimeMillis() && !isCompleted

    val hasReminder: Boolean
        get() = reminderAt != null
}
