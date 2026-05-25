package uz.apprica.plannote.data.local.dao

import androidx.room.*
import uz.apprica.plannote.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {

    // ── Queries ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("""
        SELECT * FROM habit_logs
        WHERE habit_id = :habitId
          AND date BETWEEN :startDate AND :endDate
        ORDER BY date ASC
    """)
    fun getLogsForHabitInRange(
        habitId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<HabitLogEntity>>

    /** Barcha habit'larning berilgan oraliqda yozilgan loglari (haftalik statistika uchun) */
    @Query("""
        SELECT * FROM habit_logs
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date ASC
    """)
    fun getLogsForRange(startDate: Long, endDate: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId AND date = :date LIMIT 1")
    suspend fun getLogForDate(habitId: Long, date: Long): HabitLogEntity?

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habit_id = :habitId AND date BETWEEN :startDate AND :endDate")
    suspend fun countCompletionsInRange(habitId: Long, startDate: Long, endDate: Long): Int

    // ── Mutations ──────────────────────────────────────────────────────────

    /** Duplicate (habitId, date) kelsa IGNORE — ikki marta bosilmasin */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habit_id = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: Long)

    @Query("DELETE FROM habit_logs WHERE habit_id = :habitId")
    suspend fun deleteAllLogsForHabit(habitId: Long)
}
