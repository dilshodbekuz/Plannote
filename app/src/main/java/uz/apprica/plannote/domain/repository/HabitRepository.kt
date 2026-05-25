package uz.apprica.plannote.domain.repository

import uz.apprica.plannote.domain.model.Habit
import uz.apprica.plannote.domain.model.HabitLog
import kotlinx.coroutines.flow.Flow

interface HabitRepository {

    /** Faol odatlar (real-time) */
    fun getActiveHabits(): Flow<List<Habit>>

    /** Barcha odatlar */
    fun getAllHabits(): Flow<List<Habit>>

    /** Bugungi kun uchun mo'ljallangan odatlar (bitmask asosida) */
    fun getHabitsForDay(dayBit: Int): Flow<List<Habit>>

    /** ID bo'yicha bir odat */
    suspend fun getHabitById(id: Long): Habit?

    /** Yangi odat qo'shish → ID */
    suspend fun addHabit(habit: Habit): Long

    /** Odatni yangilash */
    suspend fun updateHabit(habit: Habit)

    /** Odatni o'chirish */
    suspend fun deleteHabit(habit: Habit)

    /** ID orqali o'chirish */
    suspend fun deleteHabitById(id: Long)

    /** Faollik holatini o'zgartirish */
    suspend fun setActive(id: Long, isActive: Boolean)

    /**
     * Muayyan kun uchun odat bajarilishini toggle qilish.
     * [date] — ixtiyoriy timestamp, startOfDay() orqali normalize qilinadi.
     */
    suspend fun toggleCompletion(habitId: Long, date: Long, isCompleted: Boolean)

    /** Muayyan kun bajarilganmi? */
    suspend fun isCompletedOnDate(habitId: Long, date: Long): Boolean

    /** Berilgan oraliqda barcha habit loglari (haftalik stats uchun) */
    fun getLogsForRange(startDate: Long, endDate: Long): Flow<List<HabitLog>>

    /** Bitta habit ning barcha loglari */
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>
}
