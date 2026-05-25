package com.xakim.plannote.data.local.dao

import androidx.room.*
import com.xakim.plannote.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // ── Queries ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM habits WHERE is_active = 1 ORDER BY name ASC")
    fun getActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY is_active DESC, name ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    /** Bugungi kunning bit-mask qiymati bo'yicha filtrlash */
    @Query("SELECT * FROM habits WHERE is_active = 1 AND (target_days & :dayBit) != 0 ORDER BY name ASC")
    fun getHabitsForDay(dayBit: Int): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE current_streak = (SELECT MAX(current_streak) FROM habits)")
    suspend fun getTopStreakHabit(): HabitEntity?

    // ── Mutations ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    @Query("UPDATE habits SET is_active = :isActive WHERE id = :id")
    suspend fun setActive(id: Long, isActive: Boolean)

    @Query("""
        UPDATE habits
        SET current_streak   = :currentStreak,
            best_streak      = :bestStreak,
            total_completions = total_completions + 1
        WHERE id = :id
    """)
    suspend fun recordCompletion(id: Long, currentStreak: Int, bestStreak: Int)

    @Query("UPDATE habits SET current_streak = 0 WHERE id = :id")
    suspend fun resetStreak(id: Long)
}
