package uz.apprica.plannote.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import uz.apprica.plannote.presentation.home.HomeScreen
import uz.apprica.plannote.presentation.language.LanguageScreen
import uz.apprica.plannote.presentation.notes.NoteScreen
import uz.apprica.plannote.presentation.onboarding.OnBoardingScreen
import uz.apprica.plannote.presentation.settings.SettingsScreen
import uz.apprica.plannote.presentation.splash.SplashScreen
import uz.apprica.plannote.presentation.stats.StatsScreen
import uz.apprica.plannote.presentation.streak.StreakScreen
import uz.apprica.plannote.presentation.tasks.TaskScreen
import uz.apprica.plannote.ui.theme.appColors

private val slideInRight: EnterTransition =
    slideInHorizontally(tween(220)) { it } + fadeIn(tween(220))

private val slideOutLeft: ExitTransition =
    slideOutHorizontally(tween(220)) { -it } + fadeOut(tween(220))

private val pushIn: EnterTransition =
    slideInHorizontally(tween(220)) { it } + fadeIn(tween(220))

private val pushOut: ExitTransition =
    slideOutHorizontally(tween(220)) { it } + fadeOut(tween(220))

private val popIn: EnterTransition =
    slideInHorizontally(tween(220)) { -it } + fadeIn(tween(220))

private val popOut: ExitTransition =
    slideOutHorizontally(tween(220)) { it } + fadeOut(tween(220))

private val noNavBarRoutes = setOf(
    Screen.Splash.route,
    Screen.Language.route,
    Screen.OnBoarding.route,
    Screen.Streak.route,
    Screen.Stats.route
)

@Composable
fun PlannoteNavGraph() {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route
    val c             = MaterialTheme.appColors

    Scaffold(
        containerColor = c.background,
        bottomBar = {
            if (currentRoute !in noNavBarRoutes) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Splash.route,
            modifier         = Modifier.padding(innerPadding),
            enterTransition  = { EnterTransition.None },
            exitTransition   = { ExitTransition.None },
            popEnterTransition  = { EnterTransition.None },
            popExitTransition   = { ExitTransition.None }
        ) {
            // ── Splash ────────────────────────────────────────────────────────
            composable(
                route           = Screen.Splash.route,
                enterTransition = { fadeIn(tween(400)) },
                exitTransition  = { fadeOut(tween(400)) }
            ) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLanguage = {
                        navController.navigate(Screen.Language.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Language ──────────────────────────────────────────────────────
            composable(
                route           = Screen.Language.route,
                enterTransition = { slideInRight },
                exitTransition  = { slideOutLeft }
            ) {
                LanguageScreen(
                    onContinue = {
                        navController.navigate(Screen.OnBoarding.route) {
                            popUpTo(Screen.Language.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── OnBoarding ────────────────────────────────────────────────────
            composable(
                route           = Screen.OnBoarding.route,
                enterTransition = { slideInRight },
                exitTransition  = { slideOutLeft }
            ) {
                OnBoardingScreen(
                    onFinish = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.OnBoarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Home ──────────────────────────────────────────────────────────
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToTasks  = { navController.navigate(Screen.Tasks.route) },
                    onNavigateToNotes  = { navController.navigate(Screen.Notes.route) },
                    onNavigateToStreak = { navController.navigate(Screen.Streak.route) }
                )
            }

            // ── Tasks ─────────────────────────────────────────────────────────
            composable(Screen.Tasks.route) { TaskScreen() }

            // ── Notes ─────────────────────────────────────────────────────────
            composable(Screen.Notes.route) { NoteScreen() }

            // ── Streak ────────────────────────────────────────────────────────
            composable(
                route              = Screen.Streak.route,
                enterTransition    = { pushIn },
                exitTransition     = { ExitTransition.None },
                popEnterTransition = { popIn },
                popExitTransition  = { popOut }
            ) {
                StreakScreen(onNavigateBack = { navController.popBackStack() })
            }

            // ── Stats ─────────────────────────────────────────────────────────
            composable(Screen.Stats.route) { StatsScreen() }

            // ── Settings ──────────────────────────────────────────────────────
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
