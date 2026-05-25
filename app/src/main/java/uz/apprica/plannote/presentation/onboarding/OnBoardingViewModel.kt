package uz.apprica.plannote.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import uz.apprica.plannote.data.datastore.PreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val prefsDataStore: PreferencesDataStore
) : ViewModel() {

    /** OnBoarding ko'rildi deb belgilaydi (keyingi ishga tushishda ko'rsatilmaydi) */
    fun markOnBoardingDone() {
        viewModelScope.launch {
            prefsDataStore.setFirstLaunchDone()
        }
    }
}
