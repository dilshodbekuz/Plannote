package uz.apprica.plannote.presentation.splash

import androidx.lifecycle.ViewModel
import uz.apprica.plannote.data.datastore.PreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val prefsDataStore: PreferencesDataStore
) : ViewModel() {

    /** null = hali ma'lum emas, true = birinchi marta, false = takroriy */
    val isFirstLaunch: StateFlow<Boolean?> = prefsDataStore
        .isFirstLaunch()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.Eagerly,
            initialValue = null
        )
}
