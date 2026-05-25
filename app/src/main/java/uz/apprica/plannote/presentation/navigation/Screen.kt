package uz.apprica.plannote.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {

    // ── Bottom nav ekranlari (4 ta) ───────────────────────────────────────────
    data object Home     : Screen("home")
    data object Tasks    : Screen("tasks")
    data object Notes    : Screen("notes")
    data object Settings : Screen("settings")

    // ── Home dan navigatsiya (bottom nav da emas) ─────────────────────────────
    data object Streak : Screen("streak")
    data object Stats  : Screen("stats")

    // ── Splash & OnBoarding ───────────────────────────────────────────────────
    data object Splash     : Screen("splash")
    data object OnBoarding : Screen("onboarding")

    companion object {
        /** Bottom nav da ko'rinadigan 4 ta tab */
        val bottomNavItems: List<BottomNavItem> = listOf(
            BottomNavItem(Home,     "Bosh",       Icons.Default.Home),
            BottomNavItem(Tasks,    "Vazifalar",  Icons.Default.CheckCircle),
            BottomNavItem(Notes,    "Eslatmalar", Icons.AutoMirrored.Filled.Notes),
            BottomNavItem(Settings, "Sozlamalar", Icons.Default.Settings)
        )
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)
