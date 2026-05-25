package uz.apprica.plannote.presentation.streak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import uz.apprica.plannote.data.datastore.PreferencesDataStore
import uz.apprica.plannote.data.streak.StreakManager
import uz.apprica.plannote.domain.model.HabitStats
import uz.apprica.plannote.domain.model.OverallStats
import uz.apprica.plannote.domain.model.TaskDayStats
import uz.apprica.plannote.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StreakUiState(
    val currentStreak: Int                 = 0,
    val bestStreak: Int                    = 0,
    val weeklyActivity: List<Boolean>      = List(7) { false },
    val weeklyTaskStats: List<TaskDayStats> = emptyList(),
    val habitStats: List<HabitStats>       = emptyList(),
    val moodHistory: Map<String, Int>      = emptyMap(),
    val overallStats: OverallStats         = OverallStats(),
    val isLoading: Boolean                 = true
)

@HiltViewModel
class StreakViewModel @Inject constructor(
    private val streakManager: StreakManager,
    private val statsRepository: StatsRepository,
    private val prefsDataStore: PreferencesDataStore
) : ViewModel() {

    val uiState: StateFlow<StreakUiState> = buildFlow()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = StreakUiState()
        )

    private fun buildFlow() = run {

        // ── Qism 1: Streak ─────────────────────────────────────────────────────
        data class StreakData(
            val streak: Int,
            val best: Int,
            val activity: List<Boolean>
        )

        val streakFlow = combine(
            streakManager.getTodayStreak(),
            streakManager.getBestStreak(),
            streakManager.getWeeklyActivity()
        ) { streak, best, activity ->
            StreakData(streak, best, activity)
        }

        // ── Qism 2: Statistika ─────────────────────────────────────────────────
        data class StatsData(
            val taskStats: List<TaskDayStats>,
            val habitStats: List<HabitStats>,
            val overallStats: OverallStats
        )

        val statsFlow = combine(
            statsRepository.getWeeklyTaskStats(),
            statsRepository.getWeeklyHabitStats(),
            statsRepository.getOverallStats()
        ) { taskStats, habitStats, overallStats ->
            StatsData(taskStats, habitStats, overallStats)
        }

        // ── Hammasi birlashtirildi ──────────────────────────────────────────────
        combine(
            streakFlow,
            statsFlow,
            prefsDataStore.getMoodHistory()
        ) { streakData, statsData, moodHistory ->
            StreakUiState(
                currentStreak   = streakData.streak,
                bestStreak      = streakData.best,
                weeklyActivity  = streakData.activity,
                weeklyTaskStats = statsData.taskStats,
                habitStats      = statsData.habitStats,
                overallStats    = statsData.overallStats,
                moodHistory     = moodHistory,
                isLoading       = false
            )
        }
    }
}
