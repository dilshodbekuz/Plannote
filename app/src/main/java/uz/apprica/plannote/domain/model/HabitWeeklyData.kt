package uz.apprica.plannote.domain.model

/** Bir habit uchun joriy hafta (Dush-Yak) bajarilganlik ma'lumoti */
data class HabitWeeklyData(
    val habit: Habit,
    /** 0 = Dushanba … 6 = Yakshanba */
    val completedDays: Set<Int>
) {
    val completionRate: Float
        get() = if (completedDays.isEmpty()) 0f
                else completedDays.size.toFloat() / 7f
}
