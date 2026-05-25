package uz.apprica.plannote.data.repository

import uz.apprica.plannote.data.datastore.PreferencesDataStore
import uz.apprica.plannote.domain.model.Habit
import uz.apprica.plannote.domain.model.HabitStats
import uz.apprica.plannote.domain.model.Note
import uz.apprica.plannote.domain.model.OverallStats
import uz.apprica.plannote.domain.model.Task
import uz.apprica.plannote.domain.model.TaskDayStats
import uz.apprica.plannote.domain.repository.HabitRepository
import uz.apprica.plannote.domain.repository.NoteRepository
import uz.apprica.plannote.domain.repository.StatsRepository
import uz.apprica.plannote.domain.repository.TaskRepository
import uz.apprica.plannote.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val noteRepository: NoteRepository,
    private val prefsDataStore: PreferencesDataStore
) : StatsRepository {

    // ── Weekly Task Stats ─────────────────────────────────────────────────────

    override fun getWeeklyTaskStats(): Flow<List<TaskDayStats>> {
        val weekStart = DateUtils.startOfCurrentWeek()

        // getAllTasks() — dueDate = null bo'lgan "kunlik" vazifalarni ham olish uchun
        return taskRepository.getAllTasks()
            .map { allTasks -> buildWeeklyTaskStats(weekStart, allTasks) }
    }

    private fun buildWeeklyTaskStats(
        weekStart: Long,
        tasks: List<Task>
    ): List<TaskDayStats> {
        val todayStart = DateUtils.startOfDay()

        return (0 until 7).map { dayIndex ->
            val dayStart = weekStart + dayIndex * 86_400_000L
            val dayEnd   = dayStart + 86_400_000L - 1
            val isToday  = dayStart == todayStart

            val dayTasks = when {
                isToday -> {
                    // Bugun: dueDate = null (kunlik) YOKI bugungi sanaga to'g'ri keladigan
                    tasks.filter { task ->
                        task.dueDate == null || task.dueDate in dayStart..dayEnd
                    }
                }
                else -> {
                    // O'tgan/kelajakdagi kunlar: faqat aniq dueDate bo'lganlar
                    tasks.filter { task ->
                        task.dueDate != null && task.dueDate in dayStart..dayEnd
                    }
                }
            }

            TaskDayStats(
                date      = dayStart,
                dayLabel  = DAY_LABELS_UZ[dayIndex],
                total     = dayTasks.size,
                completed = dayTasks.count { it.isCompleted },
                isToday   = isToday
            )
        }
    }

    // ── Weekly Habit Stats ────────────────────────────────────────────────────

    override fun getWeeklyHabitStats(): Flow<List<HabitStats>> {
        val weekStart = DateUtils.startOfCurrentWeek()
        val weekEnd   = weekStart + 7 * 86_400_000L - 1

        return combine(
            habitRepository.getAllHabits(),
            habitRepository.getLogsForRange(weekStart, weekEnd)
        ) { habits, logs ->
            habits.filter { it.isActive }.map { habit ->
                // Har bir kun uchun log bor/yo'qligini tekshirish (Mon=0..Sun=6)
                val completedDays = logs
                    .filter { it.habitId == habit.id }
                    .map { log ->
                        val cal = Calendar.getInstance().apply { timeInMillis = log.date }
                        (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7   // ISO Mon=0..Sun=6
                    }
                    .toSet()

                val weekDays = (0 until 7).map { it in completedDays }
                val pct      = completedDays.size.toFloat() / 7f

                HabitStats(
                    habitId      = habit.id,
                    habitName    = habit.name,
                    iconEmoji    = habit.iconEmoji,
                    weekDays     = weekDays,
                    percentage   = pct,
                    currentStreak = habit.currentStreak
                )
            }
        }
    }

    // ── Overall Stats ─────────────────────────────────────────────────────────

    override fun getOverallStats(): Flow<OverallStats> {
        // combine() 5 ta flow qabul qiladi — ikkiga bo'lib yig'amiz
        val streakFlow = combine(
            prefsDataStore.getStreak(),
            prefsDataStore.getBestStreak()
        ) { streak, best -> streak to best }

        val repoFlow = combine(
            taskRepository.getAllTasks(),
            noteRepository.getAllNotes(),
            habitRepository.getAllHabits()
        ) { tasks, notes, habits ->
            Triple(tasks, notes, habits)
        }

        return combine(repoFlow, streakFlow) { repoData, streakData ->
            val tasks: List<Task>   = repoData.first
            val notes: List<Note>   = repoData.second
            val habits: List<Habit> = repoData.third
            val streak: Int         = streakData.first
            val best: Int           = streakData.second
            OverallStats(
                totalTasks     = tasks.size,
                completedTasks = tasks.count { it.isCompleted },
                totalNotes     = notes.size,
                currentStreak  = streak,
                bestStreak     = best,
                totalHabits    = habits.size,
                activeHabits   = habits.count { it.isActive }
            )
        }
    }

    // ── Constants ─────────────────────────────────────────────────────────────

    companion object {
        val DAY_LABELS_UZ = listOf("Du", "Se", "Ch", "Pa", "Ju", "Sh", "Ya")
    }
}
