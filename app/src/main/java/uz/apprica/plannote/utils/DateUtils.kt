package uz.apprica.plannote.utils

import java.util.Calendar

object DateUtils {

    private const val MS_IN_DAY = 86_400_000L

    /** Berilgan timestamp-ning kuni boshi (00:00:00.000) */
    fun startOfDay(timestamp: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** Berilgan timestamp-ning kuni oxiri (23:59:59.999) */
    fun endOfDay(timestamp: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

    /** Joriy haftaning Dushanba kuni boshi */
    fun startOfCurrentWeek(): Long =
        Calendar.getInstance().apply {
            // ISO: haftaning birinchi kuni — Dushanba
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** Bugunning hafta ichidagi indeksi (Dush=0 … Yak=6) */
    fun currentWeekDayIndex(): Int {
        val cal = Calendar.getInstance()
        val raw = cal.get(Calendar.DAY_OF_WEEK)   // Sun=1 … Sat=7
        // ISO Monday-first: Sun → 6, Mon → 0, Tue → 1, …
        return (raw + 5) % 7
    }

    /**
     * HabitEntity.targetDays bitmask uchun bugunning bit qiymati.
     * Bit 0 = Yakshanba, Bit 1 = Dushanba, …, Bit 6 = Shanba
     */
    fun todayDayBit(): Int {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // Sun=1..Sat=7
        return 1 shl (dayOfWeek - 1)
    }

    /** offset-kun oldingi kuni boshi */
    fun daysAgoStart(daysAgo: Int): Long = startOfDay(System.currentTimeMillis() - daysAgo * MS_IN_DAY)

    /** Hafta kunining qisqa nomini qaytaradi (O'zbek) */
    fun weekDayLabel(date: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = date }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY    -> "Dush"
            Calendar.TUESDAY   -> "Sesh"
            Calendar.WEDNESDAY -> "Chor"
            Calendar.THURSDAY  -> "Pay"
            Calendar.FRIDAY    -> "Jum"
            Calendar.SATURDAY  -> "Shan"
            Calendar.SUNDAY    -> "Yak"
            else               -> "?"
        }
    }
}
