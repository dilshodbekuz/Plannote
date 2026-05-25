package uz.apprica.plannote.domain.model

/**
 * Bir odat uchun haftalik bajarilish statistikasi.
 *
 * @param habitId     odat identifikatori
 * @param habitName   odat nomi
 * @param iconEmoji   odat ikonkasi
 * @param weekDays    7 ta Boolean: index 0 = Dushanba … 6 = Yakshanba
 * @param percentage  haftalik bajarilish foizi (0f–1f)
 * @param currentStreak joriy ketma-ket streak kuni
 */
data class HabitStats(
    val habitId: Long,
    val habitName: String,
    val iconEmoji: String,
    val weekDays: List<Boolean>,   // size = 7 (Mon=0 .. Sun=6)
    val percentage: Float,
    val currentStreak: Int = 0
)
