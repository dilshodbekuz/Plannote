package uz.apprica.plannote.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import uz.apprica.plannote.ui.theme.PrimaryTeal
import uz.apprica.plannote.ui.theme.appColors
import uz.apprica.plannote.ui.theme.strings

/** Bottom nav: faqat asosiy ekranlarda ko'rinadi */
private val NAV_ROUTES = Screen.bottomNavItems.map { it.screen.route }.toSet()

@Composable
fun BottomNavBar(navController: NavController) {
    val c = MaterialTheme.appColors
    val s = MaterialTheme.strings
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = c.surface,
        tonalElevation = androidx.compose.ui.unit.Dp.Unspecified
    ) {
        Screen.bottomNavItems.forEach { item ->
            val label = when (item.screen) {
                Screen.Home     -> s.navHome
                Screen.Tasks    -> s.navTasks
                Screen.Notes    -> s.navNotes
                Screen.Settings -> s.navSettings
                else            -> item.label
            }
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon  = { Icon(item.icon, contentDescription = label) },
                label = { Text(label, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = PrimaryTeal,
                    selectedTextColor   = PrimaryTeal,
                    indicatorColor      = PrimaryTeal.copy(alpha = 0.15f),
                    unselectedIconColor = c.textSecondary,
                    unselectedTextColor = c.textSecondary
                )
            )
        }
    }
}
