package uz.apprica.plannote.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import uz.apprica.plannote.data.datastore.PreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Ilova mavzusini (dark / light) boshqaruvchi ViewModel.
 * MainActivity ga inject qilinadi.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val prefsDataStore: PreferencesDataStore
) : ViewModel() {

    /** DataStore dan real-time dark-mode holati */
    val isDarkTheme: StateFlow<Boolean> = prefsDataStore.isDarkMode()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = true          // default: qorong'i mavzu
        )
}
