package uz.apprica.plannote.domain.usecase.habit

import uz.apprica.plannote.domain.model.DayStats
import uz.apprica.plannote.domain.model.WeeklyStats
import uz.apprica.plannote.domain.repository.HabitRepository
import uz.apprica.plannote.domain.usecase.base.NoParamFlowUseCase
import uz.apprica.plannote.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Joriy hafta (Dush–Yak) uchun habit statistikasini qaytaradi.
 *
 * Qaytarilgan [WeeklyStats] ichida:
 * - 7 kunlik [DayStats] (completed/total + label)
 * - Haftaning o'rtacha bajarilish foizi
 * - Eng uzun streak va uning sohibi
 */
class GetWeeklyStatsUseCase @Inject constructor(
    private val repository: HabitRepository
) : NoParamFlowUseCase<WeeklyStats>() {

    private companion object {
        const val MS_IN_DAY = 86_400_000L
    }

    override fun execute(): Flow<WeeklyStats> {
        val startOfWeek = DateUtils.startOfCurrentWeek()
        val endOfWeek   = startOfWeek + 7 * MS_IN_DAY - 1

        return combine(
            repository.getActiveHabits(),
            repository.getLogsForRange(startOfWeek, endOfWeek)
        ) { habits, logs ->

            val today = DateUtils.startOfDay()
            // Log → date bo'yicha guruhlash (har bir (habitId, date) juftligi unique)
            val logsByDate: Map<Long, Int> = logs
                .groupBy { it.date }
                .mapValues { (_, dayLogs) ->
                    // Shu kunda nechta unique habit bajarildi
                    dayLogs.distinctBy { it.habitId }.size
                }

            val days = (0..6).map { offset ->
                val date  = startOfWeek + offset * MS_IN_DAY
                DayStats(
                    date           = date,
                    dayLabel       = DateUtils.weekDayLabel(date),
                    completedCount = logsByDate[date] ?: 0,
                    totalHabits    = habits.size,
                    isToday        = date == today
                )
            }

            val bestHabit = habits.maxByOrNull { it.currentStreak }

            // Faqat o'tgan kunlar bo'yicha o'rtacha hisoblash
            val todayIndex    = DateUtils.currentWeekDayIndex()   // 0=Dush
            val passedDays    = days.take(todayIndex + 1)
            val avgCompletion = if (habits.isEmpty() || passedDays.isEmpty()) 0f
            else passedDays.map { it.completionRate }.average().toFloat()

            WeeklyStats(
                days                 = days,
                totalHabits          = habits.size,
                avgCompletionRate    = avgCompletion,
                bestStreak           = bestHabit?.currentStreak ?: 0,
                bestStreakHabitName  = bestHabit?.name ?: ""
            )
        }
    }
}
