package uz.apprica.plannote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import uz.apprica.plannote.presentation.navigation.PlannoteNavGraph
import uz.apprica.plannote.presentation.theme.ThemeViewModel
import uz.apprica.plannote.ui.theme.PlannoteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint(ComponentActivity::class)
class MainActivity : Hilt_MainActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()   // darhol yopiladi, custom splash ko'rinadi
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val language    by themeViewModel.language.collectAsStateWithLifecycle()

            PlannoteTheme(darkTheme = isDarkTheme, language = language) {
                PlannoteNavGraph()
            }
        }
    }
}
