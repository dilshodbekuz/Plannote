package uz.apprica.plannote.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import uz.apprica.plannote.data.datastore.PreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val currentStreak: Int  = 0,
    val bestStreak: Int     = 0,
    val language: String    = "uz"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsDataStore: PreferencesDataStore
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        prefsDataStore.isDarkMode(),
        prefsDataStore.getStreak(),
        prefsDataStore.getBestStreak(),
        prefsDataStore.getLanguage()
    ) { dark, streak, best, lang ->
        SettingsUiState(
            isDarkMode    = dark,
            currentStreak = streak,
            bestStreak    = best,
            language      = lang
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    // ── Dark mode ─────────────────────────────────────────────────────────────

    fun setDarkMode(dark: Boolean) {
        viewModelScope.launch { prefsDataStore.setDarkMode(dark) }
    }

    // ── Language ──────────────────────────────────────────────────────────────

    fun setLanguage(lang: String) {
        viewModelScope.launch { prefsDataStore.setLanguage(lang) }
    }

    // ── Streak reset ──────────────────────────────────────────────────────────

    fun confirmResetStreak() {
        viewModelScope.launch { prefsDataStore.resetStreak() }
    }
}
