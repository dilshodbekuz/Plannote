package uz.apprica.plannote.domain.model

/**
 * Ilovaning umumiy statistikasi (StatsScreen pastki qismi uchun).
 */
data class OverallStats(
    val totalTasks: Int       = 0,
    val completedTasks: Int   = 0,
    val totalNotes: Int       = 0,
    val currentStreak: Int    = 0,
    val bestStreak: Int       = 0,
    val totalHabits: Int      = 0,
    val activeHabits: Int     = 0
) {
    /** Vazifalarni bajarish foizi (0f–100f) */
    val completionPercent: Float
        get() = if (totalTasks > 0) completedTasks.toFloat() / totalTasks * 100f else 0f
}
