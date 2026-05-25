package uz.apprica.plannote.domain.model

data class WeeklyStats(
    /** Dushanba → Yakshanba (7 ta element) */
    val days: List<DayStats>,
    val totalHabits: Int,
    val avgCompletionRate: Float,
    val bestStreak: Int,
    val bestStreakHabitName: String
)

data class DayStats(
    val date: Long,
    val dayLabel: String,       // "Dush", "Sesh", ...
    val completedCount: Int,
    val totalHabits: Int,
    val isToday: Boolean
) {
    val completionRate: Float
        get() = if (totalHabits > 0) completedCount.toFloat() / totalHabits else 0f

    val isEmpty: Boolean
        get() = totalHabits == 0
}
