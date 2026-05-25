package uz.apprica.plannote.domain.model

/**
 * Bir kun uchun vazifalar statistikasi (haftalik bar chart uchun).
 * [DayStats] dan farqli — bu VAZIFA (task) asosida hisoblangan.
 */
data class TaskDayStats(
    val date: Long,
    val dayLabel: String,       // "Dush", "Sesh", ...
    val total: Int,             // o'sha kunda rejalashtirilgan vazifalar soni
    val completed: Int,         // bajarilgani
    val isToday: Boolean
) {
    val completionRate: Float
        get() = if (total > 0) completed.toFloat() / total.toFloat() else 0f

    val isEmpty: Boolean
        get() = total == 0
}
