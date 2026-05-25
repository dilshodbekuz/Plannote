package uz.apprica.plannote.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import uz.apprica.plannote.data.datastore.PreferencesDataStore
import uz.apprica.plannote.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int             = 8,
    val reminderMinute: Int           = 0,
    val isDarkMode: Boolean           = true,
    val currentStreak: Int            = 0,
    val bestStreak: Int               = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsDataStore: PreferencesDataStore,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = buildFlow()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    private fun buildFlow() = run {
        // 6 flow → ikkita combine
        val prefsFlow = combine(
            prefsDataStore.isNotificationsEnabled(),
            prefsDataStore.getReminderHour(),
            prefsDataStore.getReminderMinute()
        ) { notifEnabled, hour, min ->
            Triple(notifEnabled, hour, min)
        }
        val streakFlow = combine(
            prefsDataStore.isDarkMode(),
            prefsDataStore.getStreak(),
            prefsDataStore.getBestStreak()
        ) { dark, streak, best ->
            Triple(dark, streak, best)
        }
        combine(prefsFlow, streakFlow) { prefs, streak ->
            SettingsUiState(
                notificationsEnabled = prefs.first,
                reminderHour         = prefs.second,
                reminderMinute       = prefs.third,
                isDarkMode           = streak.first,
                currentStreak        = streak.second,
                bestStreak           = streak.third
            )
        }
    }

    // ── Notification toggle ───────────────────────────────────────────────────

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsDataStore.setNotificationsEnabled(enabled)
            if (enabled) alarmScheduler.scheduleDailyMotivation()
            else         alarmScheduler.cancelDailyMotivation()
        }
    }

    // ── Eslatish vaqti ────────────────────────────────────────────────────────

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            prefsDataStore.setReminderTime(hour, minute)
            alarmScheduler.scheduleDailyMotivationAt(hour, minute)
        }
    }

    // ── Dark mode ─────────────────────────────────────────────────────────────

    fun setDarkMode(dark: Boolean) {
        viewModelScope.launch { prefsDataStore.setDarkMode(dark) }
    }

    // ── Streak reset ──────────────────────────────────────────────────────────

    fun confirmResetStreak() {
        viewModelScope.launch { prefsDataStore.resetStreak() }
    }
}
