package uz.apprica.plannote.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.apprica.plannote.data.datastore.PreferencesDataStore
import uz.apprica.plannote.data.streak.StreakManager
import uz.apprica.plannote.domain.repository.QuoteRepository
import uz.apprica.plannote.domain.usecase.note.GetAllNotesUseCase
import uz.apprica.plannote.domain.usecase.task.GetTodayTasksUseCase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val greeting: String = "",
    val greetingEmoji: String = "☀️",
    val todayDate: String = "",
    val quote: String = "",
    val todayTasksTotal: Int = 0,
    val todayTasksDone: Int = 0,
    val notesCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val weeklyActivity: List<Boolean> = List(7) { false },
    val selectedMood: Int? = null,   // 1..5
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayTasksUseCase: GetTodayTasksUseCase,
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val streakManager: StreakManager,
    private val prefsDataStore: PreferencesDataStore,
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val (greeting, emoji) = buildGreeting()
        _uiState.update {
            it.copy(
                greeting = greeting,
                greetingEmoji = emoji,
                todayDate = buildTodayDate(),
                quote = quoteRepository.getTodayQuote()
            )
        }
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            // ── Tasks + Notes ────────────────────────────────────────────────
            combine(
                getTodayTasksUseCase(),
                getAllNotesUseCase()
            ) { tasks, notes -> tasks to notes }
                .collect { (tasks, notes) ->
                    _uiState.update { state ->
                        state.copy(
                            todayTasksTotal = tasks.size,
                            todayTasksDone = tasks.count { it.isCompleted },
                            notesCount = notes.size,
                            isLoading = false
                        )
                    }
                }
        }

        // ── Streak & Weekly Activity ──────────────────────────────────────────
        viewModelScope.launch {
            combine(
                streakManager.getTodayStreak(),
                streakManager.getBestStreak(),
                streakManager.getWeeklyActivity()
            ) { streak, best, activity ->
                Triple(streak, best, activity)
            }.collect { (streak, best, activity) ->
                _uiState.update { state ->
                    state.copy(
                        currentStreak = streak,
                        bestStreak = best,
                        weeklyActivity = activity
                    )
                }
            }
        }

        // ── Mood (DataStore dan tiklanish) ────────────────────────────────────
        viewModelScope.launch {
            prefsDataStore.getTodayMood().collect { mood ->
                _uiState.update { it.copy(selectedMood = mood) }
            }
        }
    }

    /** Kayfiyat tanlanganida DataStore ga saqlanadi */
    fun onMoodSelected(mood: Int) {
        _uiState.update { it.copy(selectedMood = mood) }
        viewModelScope.launch {
            prefsDataStore.saveMood(mood)
        }
    }

    fun refreshQuote() {
        _uiState.update { it.copy(quote = quoteRepository.getRandomQuote()) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildGreeting(): Pair<String, String> {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 6 -> "Yaxshi tun" to "🌙"
            hour < 12 -> "Xayrli tong" to "🌅"
            hour < 17 -> "Xayrli kun" to "☀️"
            hour < 21 -> "Xayrli kech" to "🌆"
            else -> "Yaxshi tun" to "🌙"
        }
    }

    private fun buildTodayDate(): String =
        SimpleDateFormat("d MMMM, yyyy • EEEE", Locale("uz"))
            .format(Date())

}
