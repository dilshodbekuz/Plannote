package uz.apprica.plannote.domain.model

enum class Frequency { DAILY, WEEKLY, MONTHLY }

data class Habit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconEmoji: String = "⭐",
    val color: Int = 0,
    val frequency: Frequency = Frequency.DAILY,

    /**
     * Haftaning qaysi kunlari bajarilishi kerak — bit-mask.
     * Bit 0 = Yakshanba, Bit 1 = Dushanba, …, Bit 6 = Shanba.
     * Misol: 0b0111110 = Dush–Juma (ish kunlari).
     */
    val targetDays: Int = 0b1111111,
    val targetCount: Int = 1,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val isActive: Boolean = true,
    val reminderTime: String? = null,   // "HH:mm"
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Bugungi kun uchun mo'ljallangan ekanligini tekshiradi */
    fun isScheduledFor(dayOfWeek: Int): Boolean {
        require(dayOfWeek in 0..6) { "dayOfWeek must be 0–6" }
        return (targetDays shr dayOfWeek) and 1 == 1
    }

    val completionRate: Float
        get() {
            val daysSinceCreation = ((System.currentTimeMillis() - createdAt) /
                    (1000 * 60 * 60 * 24)).coerceAtLeast(1)
            return totalCompletions.toFloat() / daysSinceCreation
        }
}
