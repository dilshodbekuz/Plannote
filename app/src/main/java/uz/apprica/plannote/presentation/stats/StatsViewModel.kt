package uz.apprica.plannote.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import uz.apprica.plannote.data.datastore.PreferencesDataStore
import uz.apprica.plannote.data.streak.StreakManager
import uz.apprica.plannote.domain.model.HabitStats
import uz.apprica.plannote.domain.model.HabitWeeklyData
import uz.apprica.plannote.domain.model.OverallStats
import uz.apprica.plannote.domain.model.TaskDayStats
import uz.apprica.plannote.domain.model.WeeklyStats
import uz.apprica.plannote.domain.repository.StatsRepository
import uz.apprica.plannote.domain.repository.HabitRepository
import uz.apprica.plannote.domain.repository.TaskRepository
import uz.apprica.plannote.domain.usecase.habit.GetAllHabitsUseCase
import uz.apprica.plannote.domain.usecase.habit.GetWeeklyStatsUseCase
import uz.apprica.plannote.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class StatsUiState(
    // ── Mavjud (habit-based haftalik chart) ─────────────────────────────────
    val weeklyStats: WeeklyStats?               = null,
    val habitWeeklyData: List<HabitWeeklyData>  = emptyList(),
    val completedTasksThisWeek: Int             = 0,
    val totalTasksThisWeek: Int                 = 0,

    // ── Yangi (task-based haftalik chart) ────────────────────────────────────
    val weeklyTaskStats: List<TaskDayStats>     = emptyList(),

    // ── Yangi (odat haftalik foiz jadvali) ───────────────────────────────────
    val habitStats: List<HabitStats>            = emptyList(),

    // ── Yangi (umumiy statistika) ────────────────────────────────────────────
    val overallStats: OverallStats              = OverallStats(),

    // ── Streak ───────────────────────────────────────────────────────────────
    val streak: Int                             = 0,
    val bestStreak: Int                         = 0,
    val weeklyActivity: List<Boolean>           = List(7) { false },

    // ── Kayfiyat tarixi (Map<"yyyy-MM-dd", 1..5>) ────────────────────────────
    val moodHistory: Map<String, Int>           = emptyMap(),

    val isLoading: Boolean                      = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getWeeklyStatsUseCase: GetWeeklyStatsUseCase,
    private val getAllHabitsUseCase: GetAllHabitsUseCase,
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val statsRepository: StatsRepository,
    private val streakManager: StreakManager,
    private val prefsDataStore: PreferencesDataStore
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = buildUiStateFlow()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState()
        )

    private fun buildUiStateFlow() = run {
        val startOfWeek = DateUtils.startOfCurrentWeek()
        val endOfWeek   = startOfWeek + 7 * 86_400_000L - 1

        // ── Qism 1: Mavjud habit-based chart + task counts ───────────────────
        data class HabitChartData(
            val weeklyStats: WeeklyStats?,
            val habitWeeklyData: List<HabitWeeklyData>,
            val completedTasks: Int,
            val totalTasks: Int
        )

        val habitChartFlow = combine(
            getWeeklyStatsUseCase(),
            getAllHabitsUseCase(),
            habitRepository.getLogsForRange(startOfWeek, endOfWeek),
            taskRepository.getTasksForDay(startOfWeek, endOfWeek)
        ) { weeklyStats, habits, logs, weekTasks ->
            val habitData = habits.map { habit ->
                val days = logs
                    .filter { it.habitId == habit.id }
                    .map { log ->
                        val cal = Calendar.getInstance().apply { timeInMillis = log.date }
                        (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    }
                    .toSet()
                HabitWeeklyData(habit = habit, completedDays = days)
            }
            HabitChartData(
                weeklyStats      = weeklyStats,
                habitWeeklyData  = habitData,
                completedTasks   = weekTasks.count { it.isCompleted },
                totalTasks       = weekTasks.size
            )
        }

        // ── Qism 2: Yangi statslar ────────────────────────────────────────────
        data class NewStatsData(
            val taskStats: List<TaskDayStats>,
            val habitStats: List<HabitStats>,
            val overallStats: OverallStats
        )

        val newStatsFlow = combine(
            statsRepository.getWeeklyTaskStats(),
            statsRepository.getWeeklyHabitStats(),
            statsRepository.getOverallStats()
        ) { taskStats, habitStats, overallStats ->
            NewStatsData(taskStats, habitStats, overallStats)
        }

        // ── Qism 3: Streak + Mood tarixi ─────────────────────────────────────
        data class StreakData(
            val streak: Int,
            val bestStreak: Int,
            val weeklyActivity: List<Boolean>,
            val moodHistory: Map<String, Int>
        )

        val streakFlow = combine(
            streakManager.getTodayStreak(),
            streakManager.getBestStreak(),
            streakManager.getWeeklyActivity(),
            prefsDataStore.getMoodHistory()
        ) { streak, best, activity, moodHistory ->
            StreakData(streak, best, activity, moodHistory)
        }

        // ── Hammasi birlashtirildi ────────────────────────────────────────────
        combine(habitChartFlow, newStatsFlow, streakFlow) { habit, new, streak ->
            StatsUiState(
                weeklyStats            = habit.weeklyStats,
                habitWeeklyData        = habit.habitWeeklyData,
                completedTasksThisWeek = habit.completedTasks,
                totalTasksThisWeek     = habit.totalTasks,
                weeklyTaskStats        = new.taskStats,
                habitStats             = new.habitStats,
                overallStats           = new.overallStats,
                streak                 = streak.streak,
                bestStreak             = streak.bestStreak,
                weeklyActivity         = streak.weeklyActivity,
                moodHistory            = streak.moodHistory,
                isLoading              = false
            )
        }
    }
}
