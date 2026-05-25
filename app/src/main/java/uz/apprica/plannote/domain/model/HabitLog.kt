package uz.apprica.plannote.domain.model

data class HabitLog(
    val id: Long = 0,
    val habitId: Long,
    /** Kuni boshi (midnight) timestamp */
    val date: Long,
    val completedAt: Long = System.currentTimeMillis()
)
