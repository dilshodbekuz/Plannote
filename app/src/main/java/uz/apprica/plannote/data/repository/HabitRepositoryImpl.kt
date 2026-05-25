package uz.apprica.plannote.data.repository

import uz.apprica.plannote.data.local.dao.HabitDao
import uz.apprica.plannote.data.local.dao.HabitLogDao
import uz.apprica.plannote.data.local.entity.HabitLogEntity
import uz.apprica.plannote.data.local.mapper.toDomain
import uz.apprica.plannote.data.local.mapper.toEntity
import uz.apprica.plannote.domain.model.Habit
import uz.apprica.plannote.domain.model.HabitLog
import uz.apprica.plannote.domain.repository.HabitRepository
import uz.apprica.plannote.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao
) : HabitRepository {

    override fun getActiveHabits(): Flow<List<Habit>> =
        habitDao.getActiveHabits().map { it.toDomain() }

    override fun getAllHabits(): Flow<List<Habit>> =
        habitDao.getAllHabits().map { it.toDomain() }

    override fun getHabitsForDay(dayBit: Int): Flow<List<Habit>> =
        habitDao.getHabitsForDay(dayBit).map { it.toDomain() }

    override suspend fun getHabitById(id: Long): Habit? =
        habitDao.getHabitById(id)?.toDomain()

    override suspend fun addHabit(habit: Habit): Long =
        habitDao.insertHabit(habit.toEntity())

    override suspend fun updateHabit(habit: Habit) =
        habitDao.updateHabit(habit.toEntity())

    override suspend fun deleteHabit(habit: Habit) =
        habitDao.deleteHabit(habit.toEntity())

    override suspend fun deleteHabitById(id: Long) =
        habitDao.deleteHabitById(id)

    override suspend fun setActive(id: Long, isActive: Boolean) =
        habitDao.setActive(id, isActive)

    override suspend fun toggleCompletion(
        habitId: Long,
        date: Long,
        isCompleted: Boolean
    ) = withContext(Dispatchers.IO) {
        val dayStart = DateUtils.startOfDay(date)

        if (isCompleted) {
            // 1. Log yozuvi qo'shish (duplicate bo'lsa IGNORE)
            habitLogDao.insertLog(
                HabitLogEntity(habitId = habitId, date = dayStart)
            )
            // 2. Streak yangilash
            val habit = habitDao.getHabitById(habitId) ?: return@withContext
            val prevDayStart = dayStart - 86_400_000L
            val prevLog = habitLogDao.getLogForDate(habitId, prevDayStart)

            val newStreak = if (prevLog != null) habit.currentStreak + 1 else 1
            val newBest   = maxOf(newStreak, habit.bestStreak)
            habitDao.recordCompletion(habitId, newStreak, newBest)

        } else {
            // 1. Log yozuvini o'chirish
            habitLogDao.deleteLog(habitId, dayStart)
            // 2. Streak qayta hisoblash (soddalashtirilgan: 1 ta kamaytirish)
            val habit = habitDao.getHabitById(habitId) ?: return@withContext
            val newStreak = maxOf(0, habit.currentStreak - 1)
            val newTotal  = maxOf(0, habit.totalCompletions - 1)
            habitDao.updateHabit(
                habit.copy(currentStreak = newStreak, totalCompletions = newTotal)
            )
        }
    }

    override suspend fun isCompletedOnDate(habitId: Long, date: Long): Boolean =
        withContext(Dispatchers.IO) {
            habitLogDao.getLogForDate(habitId, DateUtils.startOfDay(date)) != null
        }

    override fun getLogsForRange(startDate: Long, endDate: Long): Flow<List<HabitLog>> =
        habitLogDao.getLogsForRange(startDate, endDate).map { it.toDomain() }

    override fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> =
        habitLogDao.getLogsForHabit(habitId).map { it.toDomain() }
}
