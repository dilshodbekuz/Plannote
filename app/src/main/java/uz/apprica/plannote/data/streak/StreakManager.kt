package uz.apprica.plannote.data.streak

import uz.apprica.plannote.data.datastore.PreferencesDataStore
import uz.apprica.plannote.data.datastore.PreferencesDataStore.Companion.todayStr
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App ochilish streakini boshqaradi.
 *
 * Qoidalar:
 * - Har kuni app birinchi marta ochilganda [checkAndUpdateStreak] chaqiriladi.
 * - Kecha ham ochilgan bo'lsa → streak + 1
 * - 2 yoki undan ko'p kun o'tgan bo'lsa → streak = 1 (bugundan qayta boshlanadi)
 * - Bir kunda bir necha marta ochilsa → faqat birinchi marta hisoblanadi
 */
@Singleton
class StreakManager @Inject constructor(
    private val prefs: PreferencesDataStore
) {
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * App ochilganda chaqiriladi. Streak logikasini tekshiradi va yangilaydi.
     */
    suspend fun checkAndUpdateStreak() {
        val today     = todayStr()
        val lastDate  = prefs.getLastActiveDateStr().first()
        val streak    = prefs.getStreak().first()

        when {
            // Bugun allaqachon hisobga olingan — hech narsa qilma
            lastDate == today -> return

            // Ilk marta ochilmoqda yoki uzoq vaqt ochilmagan
            lastDate == null -> {
                prefs.saveStreak(1)
                prefs.saveLastActiveDate(today)
                prefs.addActivityDate(today)
            }

            // Kecha ochilganmi? → streak + 1
            isYesterday(lastDate) -> {
                prefs.saveStreak(streak + 1)
                prefs.saveLastActiveDate(today)
                prefs.addActivityDate(today)
            }

            // 2 yoki undan ko'p kun o'tgan → resetlash
            else -> {
                prefs.saveStreak(1)
                prefs.saveLastActiveDate(today)
                prefs.addActivityDate(today)
            }
        }
    }

    /**
     * Joriy streakni real-time stream sifatida qaytaradi.
     */
    fun getTodayStreak(): Flow<Int> = prefs.getStreak()

    /**
     * Eng yaxshi streak rekordini real-time stream sifatida qaytaradi.
     */
    fun getBestStreak(): Flow<Int> = prefs.getBestStreak()

    /**
     * So'nggi 7 kunning faollik holatini qaytaradi.
     * List[0] = 6 kun oldin … List[6] = bugun
     */
    fun getWeeklyActivity(): Flow<List<Boolean>> = prefs.getWeeklyActivity()

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Berilgan sana string ("yyyy-MM-dd") kecha bo'lganligini tekshiradi.
     */
    private fun isYesterday(dateStr: String): Boolean {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayStr = dateFmt.format(yesterday.time)
        return dateStr == yesterdayStr
    }
}
