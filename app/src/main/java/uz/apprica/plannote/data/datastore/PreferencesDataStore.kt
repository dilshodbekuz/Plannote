package uz.apprica.plannote.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

// ── Singleton DataStore instance ──────────────────────────────────────────────
private val Context.plannotePrefs: DataStore<Preferences>
        by preferencesDataStore(name = "plannote_prefs")

/**
 * Streak, mood va haftalik faollikni saqlash uchun DataStore wrapper.
 *
 * Sana formati: "yyyy-MM-dd" (ISO 8601 qisqartmasi)
 * Mood tarixi formati: "yyyy-MM-dd=mood;..." (nuqtali-vergul bilan ajratilgan)
 * Haftalik faollik: "yyyy-MM-dd,yyyy-MM-dd,..." (vergul bilan ajratilgan so'nggi 30 kun)
 */
@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences>
        get() = context.plannotePrefs

    // ── Keys ──────────────────────────────────────────────────────────────────

    companion object {
        // ── Streak ──────────────────────────────────────────────────────────
        val KEY_STREAK           = intPreferencesKey("streak")
        val KEY_BEST_STREAK      = intPreferencesKey("best_streak")
        val KEY_LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        val KEY_ACTIVITY_DATES   = stringPreferencesKey("activity_dates")
        // ── Mood ────────────────────────────────────────────────────────────
        val KEY_TODAY_MOOD       = intPreferencesKey("today_mood")
        val KEY_TODAY_MOOD_DATE  = stringPreferencesKey("today_mood_date")
        val KEY_MOOD_HISTORY     = stringPreferencesKey("mood_history")
        // ── Settings ────────────────────────────────────────────────────────
        val KEY_FIRST_LAUNCH         = booleanPreferencesKey("first_launch")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_REMINDER_HOUR        = intPreferencesKey("reminder_hour")
        val KEY_REMINDER_MINUTE      = intPreferencesKey("reminder_minute")
        val KEY_IS_DARK_MODE         = booleanPreferencesKey("is_dark_mode")
        // ── Kunlik reset ─────────────────────────────────────────────────────
        val KEY_LAST_TASK_RESET      = stringPreferencesKey("last_task_reset_date")

        private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fun todayStr(): String = DATE_FMT.format(Date())
        fun dateStr(ms: Long): String = DATE_FMT.format(Date(ms))
        fun parseDate(s: String): Calendar? = runCatching {
            Calendar.getInstance().also { it.time = DATE_FMT.parse(s)!! }
        }.getOrNull()
    }

    // ── Streak ────────────────────────────────────────────────────────────────

    suspend fun saveStreak(days: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_STREAK] = days
            val best = prefs[KEY_BEST_STREAK] ?: 0
            if (days > best) prefs[KEY_BEST_STREAK] = days
        }
    }

    fun getStreak(): Flow<Int> = dataStore.data
        .catchIO()
        .map { it[KEY_STREAK] ?: 0 }

    fun getBestStreak(): Flow<Int> = dataStore.data
        .catchIO()
        .map { it[KEY_BEST_STREAK] ?: 0 }

    // ── Last Active Date ──────────────────────────────────────────────────────

    suspend fun saveLastActiveDate(dateStr: String) {
        dataStore.edit { it[KEY_LAST_ACTIVE_DATE] = dateStr }
    }

    fun getLastActiveDateStr(): Flow<String?> = dataStore.data
        .catchIO()
        .map { it[KEY_LAST_ACTIVE_DATE] }

    // ── Activity Dates (haftalik dots uchun) ──────────────────────────────────

    /**
     * Faollik sanasini qo'shadi va 30 kundan eski yozuvlarni tozalaydi.
     */
    suspend fun addActivityDate(dateStr: String) {
        dataStore.edit { prefs ->
            val existing = (prefs[KEY_ACTIVITY_DATES] ?: "")
                .split(",")
                .filter { it.isNotBlank() }
                .toMutableSet()
            existing.add(dateStr)
            // 30 kundan eski yozuvlarni olib tashlash
            val cutoff = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
            val cutoffStr = DATE_FMT.format(cutoff.time)
            val trimmed = existing.filter { it >= cutoffStr }
            prefs[KEY_ACTIVITY_DATES] = trimmed.joinToString(",")
        }
    }

    /**
     * So'nggi 7 kun uchun faollik ro'yxatini qaytaradi.
     * index 0 = 6 kun oldin … index 6 = bugun
     */
    fun getWeeklyActivity(): Flow<List<Boolean>> = dataStore.data
        .catchIO()
        .map { prefs ->
            val dates = (prefs[KEY_ACTIVITY_DATES] ?: "")
                .split(",")
                .filter { it.isNotBlank() }
                .toSet()
            (6 downTo 0).map { daysAgo ->
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
                DATE_FMT.format(cal.time) in dates
            }
        }

    // ── Mood ──────────────────────────────────────────────────────────────────

    /**
     * Bugungi kayfiyatni saqlaydi (1–5) va tarixga qo'shadi.
     */
    suspend fun saveMood(mood: Int) {
        val today = todayStr()
        dataStore.edit { prefs ->
            prefs[KEY_TODAY_MOOD]      = mood
            prefs[KEY_TODAY_MOOD_DATE] = today

            // Tarixni yangilash
            val history = parseMoodHistory(prefs[KEY_MOOD_HISTORY] ?: "").toMutableMap()
            history[today] = mood
            // 30 kundan eski yozuvlarni olib tashlash
            val cutoff = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
            val cutoffStr = DATE_FMT.format(cutoff.time)
            val trimmed = history.filter { (k, _) -> k >= cutoffStr }
            prefs[KEY_MOOD_HISTORY] = encodeMoodHistory(trimmed)
        }
    }

    /**
     * Bugungi kayfiyatni qaytaradi (null = bugun hali tanlanmagan).
     */
    fun getTodayMood(): Flow<Int?> = dataStore.data
        .catchIO()
        .map { prefs ->
            if (prefs[KEY_TODAY_MOOD_DATE] == todayStr()) prefs[KEY_TODAY_MOOD]
            else null
        }

    /**
     * Kayfiyat tarixi: Map<"yyyy-MM-dd", 1..5>
     */
    fun getMoodHistory(): Flow<Map<String, Int>> = dataStore.data
        .catchIO()
        .map { prefs -> parseMoodHistory(prefs[KEY_MOOD_HISTORY] ?: "") }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun parseMoodHistory(encoded: String): Map<String, Int> {
        if (encoded.isBlank()) return emptyMap()
        return encoded.split(";")
            .mapNotNull { entry ->
                val idx = entry.indexOf('=')
                if (idx > 0) {
                    val date = entry.substring(0, idx)
                    val mood = entry.substring(idx + 1).toIntOrNull()
                    if (mood != null) date to mood else null
                } else null
            }
            .toMap()
    }

    private fun encodeMoodHistory(history: Map<String, Int>): String =
        history.entries.joinToString(";") { "${it.key}=${it.value}" }

    // ── Settings ──────────────────────────────────────────────────────────────

    /** Birinchi ishga tushish: true = yangi o'rnatish */
    fun isFirstLaunch(): Flow<Boolean> = dataStore.data
        .catchIO()
        .map { it[KEY_FIRST_LAUNCH] ?: true }

    suspend fun setFirstLaunchDone() {
        dataStore.edit { it[KEY_FIRST_LAUNCH] = false }
    }

    /** Notificationlar yoqilgan/o'chirilgan */
    fun isNotificationsEnabled(): Flow<Boolean> = dataStore.data
        .catchIO()
        .map { it[KEY_NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    /** Ertalabki eslatish soati (default 8:00) */
    fun getReminderHour(): Flow<Int> = dataStore.data
        .catchIO()
        .map { it[KEY_REMINDER_HOUR] ?: 8 }

    fun getReminderMinute(): Flow<Int> = dataStore.data
        .catchIO()
        .map { it[KEY_REMINDER_MINUTE] ?: 0 }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_REMINDER_HOUR]   = hour
            prefs[KEY_REMINDER_MINUTE] = minute
        }
    }

    /** Dark mode (default true — ilova dark theme bilan keladi) */
    fun isDarkMode(): Flow<Boolean> = dataStore.data
        .catchIO()
        .map { it[KEY_IS_DARK_MODE] ?: true }

    suspend fun setDarkMode(dark: Boolean) {
        dataStore.edit { it[KEY_IS_DARK_MODE] = dark }
    }

    // ── Kunlik vazifalar reset ─────────────────────────────────────────────────

    /** Vazifalar oxirgi marta qaysi kuni reset qilinganini qaytaradi */
    fun getLastTaskResetDate(): Flow<String?> = dataStore.data
        .catchIO()
        .map { it[KEY_LAST_TASK_RESET] }

    /** Reset sanasini bugun deb belgilaydi */
    suspend fun markTaskResetDone() {
        dataStore.edit { it[KEY_LAST_TASK_RESET] = todayStr() }
    }

    /** Streakni nolga qaytarish */
    suspend fun resetStreak() {
        dataStore.edit { prefs ->
            prefs[KEY_STREAK]           = 0
            prefs[KEY_LAST_ACTIVE_DATE] = ""
            prefs[KEY_ACTIVITY_DATES]   = ""
        }
    }

    // ── Extension ─────────────────────────────────────────────────────────────

    private fun Flow<Preferences>.catchIO(): Flow<Preferences> =
        catch { if (it is IOException) emit(emptyPreferences()) else throw it }
}
